package de.upb.swt.soot.core.graph;

import com.google.common.collect.ComparisonChain;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.ref.JCaughtExceptionRef;
import de.upb.swt.soot.core.jimple.common.stmt.*;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.util.GraphVizExporter;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MutableBlockStmtGraph extends MutableStmtGraph {
  @Nullable private Stmt startingStmt = null;
  @Nonnull private final Map<Stmt, MutableBasicBlock> stmtToBlock = new HashMap<>();

  @Nonnull private final Set<MutableBasicBlock> blocks = new HashSet<>();

  public MutableBlockStmtGraph() {}

  /** copies a StmtGraph into this Mutable instance */
  public MutableBlockStmtGraph(@Nonnull StmtGraph<? extends BasicBlock<?>> graph) {
    setStartingStmt(graph.getStartingStmt());
    // copy blocks into this graph
    graph
        .getBlocks()
        .forEach(
            b -> {
              final Map<? extends ClassType, ? extends BasicBlock<?>> exceptionalSuccessors =
                  b.getExceptionalSuccessors();
              final Map<ClassType, Stmt> exSuccs = new HashMap<>();
              exceptionalSuccessors.forEach((k, v) -> exSuccs.put(k, v.getHead()));
              addBlock(b.getStmts(), exSuccs);
            });

    // copy links between the blocks
    graph
        .getBlocks()
        .forEach(
            b -> {
              // getBlockOf is necessary to find the new existing/copied block which are refering to
              // the same a immutable Stmt
              final MutableBasicBlock blockOf = getBlockOf(b.getTail());
              b.getSuccessors().forEach(succ -> linkBlocks(blockOf, getBlockOf(succ.getHead())));
            });
  }

  /**
   * Creates a Graph representation from the 'legacy' representation i.e. a List of Stmts and Traps.
   */
  public void initializeWith(
      @Nonnull List<Stmt> stmts,
      @Nonnull Map<BranchingStmt, List<Stmt>> branchingMap,
      @Nonnull List<Trap> traps) {

    if (stmts.isEmpty()) {
      return;
    }

    setStartingStmt(stmts.get(0));
    Map<ClassType, Stmt> currentTrapMap = new HashMap<>();
    Map<ClassType, List<Stmt>> overlappingTraps = new HashMap<>();
    for (int i = 0, stmtsSize = stmts.size(); i < stmtsSize; i++) {
      Stmt stmt = stmts.get(i);

      // possibly bad performance.. O(n * m)... sort trap data once: use some kind of -> O(1)*n with
      // 2*m
      // additional memory
      boolean trapsChanged = false;
      for (Trap trap : traps) {
        // endStmt is exclusive! -> trap ends before this stmt -> remove exception info here
        if (stmt == trap.getEndStmt()) {
          final ClassType exceptionType = trap.getExceptionType();
          final boolean isRemoved = currentTrapMap.remove(exceptionType, trap.getHandlerStmt());
          final List<Stmt> overridenTrapHandlers = overlappingTraps.get(exceptionType);
          if (!isRemoved) {
            // check if theres an overlapping trap that has a less specific TrapRange which is
            // ending before it gets the active exception information again
            // not logical as a compiler output... but possible.
            if (overridenTrapHandlers != null && overridenTrapHandlers.size() > 0) {
              final boolean overlappingTrapRemoved =
                  overridenTrapHandlers.remove(trap.getHandlerStmt());
              if (!overlappingTrapRemoved) {
                throw new IllegalStateException(
                    "There is a Trap that should end here which was not applied before nor was not active due to a more specific traprange for that exeption! \n "
                        + trap);
              }
            }
          }

          if (overridenTrapHandlers != null && overridenTrapHandlers.size() > 0) {
            currentTrapMap.put(
                exceptionType, overridenTrapHandlers.remove(overridenTrapHandlers.size() - 1));
          }

          trapsChanged = true;
        }
      }

      for (Trap trap : traps) {
        if (stmt == trap.getBeginStmt()) {
          final Stmt overridenExFlow =
              currentTrapMap.put(trap.getExceptionType(), trap.getHandlerStmt());
          if (overridenExFlow != null) {
            final List<Stmt> overridenTrapHandlers =
                overlappingTraps.computeIfAbsent(trap.getExceptionType(), k -> new ArrayList<>());
            overridenTrapHandlers.add(overridenExFlow);
          }
          trapsChanged = true;
        }
      }

      // TODO: [ms] implement via more performant addBlock() as we already know where Block borders
      // are
      // if(trapsChanged) => we need a new Block -> beware: currentTrapMap contains here the
      // new/updated values!
      addNode(stmt, currentTrapMap);

      if (stmt.fallsThrough()) {
        // hint: possible bad performance if not stmts is not instanceof RandomAccess
        if (i >= stmts.size()) {
          throw new IllegalArgumentException(
              "Theres a fallsthrough Stmt ('"
                  + stmt
                  + "') which has no sucessor - which means it currently falls into the abyss i.e. can't fall through to another Stmt.");
        }
        putEdge(stmt, stmts.get(i + 1));
      }

      if (stmt instanceof BranchingStmt) {
        // => end of Block
        final List<Stmt> targets = branchingMap.get(stmt);
        int expectedBranchEntries =
            stmt.getExpectedSuccessorCount() - (stmt.fallsThrough() ? 1 : 0);
        if (targets == null || targets.size() != expectedBranchEntries) {
          throw new IllegalArgumentException(
              "The corresponding branchingMap entry for the BranchingStmt ('"
                  + stmt
                  + "') needs to have exactly the amount of targets as the BranchingStmt has successors.");
        }

        for (Stmt target : targets) {
          // a possible fallsthrough (i.e. from IfStmt) is not in branchingMap
          putEdge(stmt, target);
        }
      }
    }
  }

  @Override
  public void addExceptionalEdge(
      @Nonnull Stmt stmt, @Nonnull ClassType exceptionType, @Nonnull Stmt traphandlerStmt) {

    MutableBasicBlock block = stmtToBlock.get(stmt);
    if (block == null) {
      throw new IllegalArgumentException("Stmt is not in the StmtGraph!");
    }

    final Map<ClassType, MutableBasicBlock> exceptionalSuccessors =
        block.getExceptionalSuccessors();
    final MutableBasicBlock trapBlock = exceptionalSuccessors.get(exceptionType);
    if (trapBlock != null && trapBlock.getHead() == traphandlerStmt) {
      // edge already exists
      return;
    }

    MutableBasicBlock seperatedBlock = excludeStmtFromBlock(stmt, block);
    seperatedBlock.addExceptionalSuccessorBlock(exceptionType, getOrCreateBlock(traphandlerStmt));
    tryMergeIntoSurroundingBlocks(seperatedBlock);
  }

  @Override
  public void removeExceptionalEdge(@Nonnull Stmt node, @Nonnull ClassType exceptionType) {
    final MutableBasicBlock block = stmtToBlock.get(node);
    if (block == null) {
      throw new IllegalArgumentException("Stmt is not in the StmtGraph!");
    }
    block.removeExceptionalSuccessorBlock(exceptionType);
    tryMergeIntoSurroundingBlocks(block);
  }

  @Override
  public void clearExceptionalEdges(@Nonnull Stmt node) {
    final MutableBasicBlock block = stmtToBlock.get(node);
    if (block == null) {
      throw new IllegalArgumentException("Stmt is not in the StmtGraph!");
    }
    block.clearExceptionalSuccessorBlocks();
    tryMergeIntoSurroundingBlocks(block);
  }

  @Override
  @Nonnull
  public List<MutableBasicBlock> getBlocks() {
    return new ArrayList<>(blocks);
  }

  /**
   * The list of Stmts must contain only fallsthrough Stmts; A flow manipulating Stmt
   * (BranchingStmt, return, throw) is only allowed at the Tail. (like the conditions of a Block)
   */
  @Override
  public void addBlock(@Nonnull List<Stmt> stmts, @Nonnull Map<ClassType, Stmt> trapMap) {
    if (stmts.isEmpty()) {
      return;
    }
    addBlockInternal(stmts, trapMap);
  }

  /**
   * @param stmts List has to be non-empty!
   * @param trapMap
   */
  private MutableBasicBlock addBlockInternal(
      @Nonnull List<Stmt> stmts, Map<ClassType, Stmt> trapMap) {
    final Iterator<Stmt> iterator = stmts.iterator();
    final Stmt node = iterator.next();
    MutableBasicBlock block = getOrCreateBlock(node);
    if (block.getHead() != node || block.getSuccessors().size() > 0) {
      throw new IllegalArgumentException(
          "The first Stmt in the List is already in the StmtGraph and and is not the head of a Block where currently no successor are set, yet.");
    } else if (block.getStmtCount() > 1) {
      throw new IllegalArgumentException(
          "The first Stmt in the List is already in the StmtGraph and has at least one (fallsthrough) successor in its Block.");
    }

    while (iterator.hasNext()) {
      Stmt stmt = iterator.next();
      final MutableBasicBlock overwrittenBlock = addNodeToBlock(block, stmt);
      if (overwrittenBlock != null) {
        if (iterator.hasNext()) {
          throw new IllegalArgumentException(
              "the Stmt '"
                  + stmt
                  + "' you want to add as a Stmt of a whole Block is already in this StmtGraph.");
        } else {
          // existing is last element of stmtlist
          // TODO: hint: we can allow other n-th elements as well e.g. if a sequence of stmts exists
          // already and can/should be inside that added block as well.

          if (overwrittenBlock.getHead() == stmt) {
            // last stmt is head of another block

            // cleanup started add action
            stmtToBlock.put(stmt, overwrittenBlock);
            block.removeStmt(stmt);

            // try to merge
            if (!tryMergeBlocks(block, overwrittenBlock)) {
              // otherwise link them
              linkBlocks(block, overwrittenBlock);
            }
          } else {
            throw new IllegalArgumentException(
                "the Stmt '"
                    + stmt
                    + "' you want to add as a Stmt of a whole Block is already in this StmtGraph.");
          }
        }
      }
    }

    trapMap.forEach(
        (type, handlerStmt) ->
            block.addExceptionalSuccessorBlock(type, getOrCreateBlock(handlerStmt)));
    return block;
  }

  @Override
  public void addNode(@Nonnull Stmt stmt, @Nonnull Map<ClassType, Stmt> exceptions) {
    MutableBasicBlock block = stmtToBlock.get(stmt);
    if (block == null) {
      // Stmt does not exist in the graph -> create
      block = createStmtsBlock(stmt);
    }
    boolean isExceptionalFlowDifferent = false;
    if (block.getExceptionalSuccessors().size() == exceptions.size()) {
      for (Map.Entry<ClassType, MutableBasicBlock> entry :
          block.getExceptionalSuccessors().entrySet()) {
        final Stmt targetStmt = exceptions.get(entry.getKey());
        if (targetStmt == null) {
          isExceptionalFlowDifferent = true;
          break;
        } else if (targetStmt != entry.getValue().getHead()) {
          isExceptionalFlowDifferent = true;
          break;
        }
      }
    } else {
      isExceptionalFlowDifferent = true;
    }
    final MutableBasicBlock excludeFromOrigBlock;
    if (isExceptionalFlowDifferent) {
      excludeFromOrigBlock = excludeStmtFromBlock(stmt, block);
      // apply exceptional flow info to seperated block
      exceptions.forEach(
          (type, trapHandler) -> {
            MutableBasicBlock trapHandlerBlock = getOrCreateBlock(trapHandler);
            excludeFromOrigBlock.addExceptionalSuccessorBlock(type, trapHandlerBlock);
            trapHandlerBlock.addPredecessorBlock(excludeFromOrigBlock);
          });
      tryMergeIntoSurroundingBlocks(excludeFromOrigBlock);
    }
  }

  /**
   * splits a block depending on the situation in multiple (0-3) Blocks so that at the end splitStmt
   * is the only Stmt in its BasicBlock. The flow between the splitted BasicBlock(s) is still
   * maintained.
   */
  @Nonnull
  private MutableBasicBlock excludeStmtFromBlock(@Nonnull Stmt splitStmt, MutableBasicBlock block) {
    final MutableBasicBlock excludeFromOrigBlock;
    if (block.getStmtCount() > 1) {
      final List<Stmt> blockStmts = block.getStmts();
      int stmtIdx = blockStmts.indexOf(splitStmt);

      if (stmtIdx < 1) {
        // stmt is the head -> just a split is necessary
        excludeFromOrigBlock = block;
      } else {
        // i.e. stmt != block.getHead() -> there is a "middle" Block containing Stmt which needs
        // to be seperated
        excludeFromOrigBlock = new MutableBasicBlock();
        addNodeToBlock(excludeFromOrigBlock, splitStmt);
        blocks.add(excludeFromOrigBlock);
      }

      // copy last part - if there are more Stmts after stmt in Block block
      if (stmtIdx + 1 < blockStmts.size()) {
        final MutableBasicBlock restOfOrigBlock = new MutableBasicBlock();
        for (int i = stmtIdx + 1; i < blockStmts.size(); i++) {
          // stmtToBlock is already updated while inserting each Stmt into another Block
          addNodeToBlock(restOfOrigBlock, blockStmts.get(i));
        }

        // copy successors of block which are now the successors of the third block
        block
            .getSuccessors()
            .forEach(
                successor -> {
                  linkBlocks(restOfOrigBlock, successor);
                });
        block.clearSuccessorBlocks();

        // link with previous stmts from the block
        linkBlocks(excludeFromOrigBlock, restOfOrigBlock);
        block.clearSuccessorBlocks();

        // add blocks exceptional flows
        block
            .getExceptionalSuccessors()
            .forEach(
                (type, trapHandlerBlock) -> {
                  restOfOrigBlock.addExceptionalSuccessorBlock(type, trapHandlerBlock);
                  trapHandlerBlock.addPredecessorBlock(restOfOrigBlock);
                });

        blocks.add(restOfOrigBlock);
      } else {
        // no third block necessary
        if (stmtIdx < 1) {
          // i.e. stmt == block.getHead() -> copy successors to second block as its the last part
          // of the original block
          block
              .getSuccessors()
              .forEach(
                  s -> {
                    linkBlocks(excludeFromOrigBlock, s);
                  });
          block.clearSuccessorBlocks();
        }
      }

      // cleanup original block -> "beforeBlock" -> remove now copied Stmts
      for (int i = blockStmts.size() - 1; i >= stmtIdx; i--) {
        block.removeStmt(blockStmts.get(i));
      }

      // if stmt != block.getHead() i.e. excludeFromOrigBlock is seperated from block -> block !=
      // excludeFromOrigBlock -> link them
      if (stmtIdx > 0) {
        linkBlocks(block, excludeFromOrigBlock);
      }

      return excludeFromOrigBlock;

    } else {
      // just a single stmt in the block -> e.g. its the block we add the exception info
      return block;
    }
  }

  /** Merges block into Predecessor/Successor if possible. */
  private void tryMergeIntoSurroundingBlocks(@Nonnull MutableBasicBlock block) {
    // merge with predecessor if possible
    block = tryMergeWithPredecessorBlock(block);
    // and/or merge with successorBlock
    tryMergeWithSuccessorBlock(block);
  }

  @Nonnull
  private MutableBasicBlock tryMergeWithSuccessorBlock(@Nonnull MutableBasicBlock block) {
    final List<MutableBasicBlock> successors = block.getSuccessors();
    if (successors.size() == 1) {
      final MutableBasicBlock singleSuccessor = successors.get(0);
      if (tryMergeBlocks(block, singleSuccessor)) {
        return singleSuccessor;
      }
    }
    return block;
  }

  @Nonnull
  private MutableBasicBlock tryMergeWithPredecessorBlock(@Nonnull MutableBasicBlock block) {
    final List<MutableBasicBlock> predecessors = block.getPredecessors();
    if (predecessors.size() == 1) {
      final MutableBasicBlock singlePredecessor = predecessors.get(0);
      if (tryMergeBlocks(singlePredecessor, block)) {
        return singlePredecessor;
      }
    }
    return block;
  }

  @Nonnull
  private MutableBasicBlock getOrCreateBlock(@Nonnull Stmt stmt) {
    MutableBasicBlock trapHandlerBlock = getBlockOf(stmt);
    if (trapHandlerBlock == null) {
      // traphandlerStmt does not exist in the graph -> create
      trapHandlerBlock = createStmtsBlock(stmt);
    }
    return trapHandlerBlock;
  }

  protected boolean isMergeable(
      @Nonnull MutableBasicBlock firstBlock, @Nonnull MutableBasicBlock followingBlock) {
    if (firstBlock.getTail().branches()) {
      return false;
    }
    final List<MutableBasicBlock> fBlocksuccessors = firstBlock.getSuccessors();
    if (fBlocksuccessors.size() != 1 || fBlocksuccessors.get(0) != followingBlock) {
      return false;
    }
    // if we are here the datastructure should have managed that the next if is true..
    final List<MutableBasicBlock> sBlockPredecessors = followingBlock.getPredecessors();
    if (sBlockPredecessors.size() != 1 || sBlockPredecessors.get(0) != firstBlock) {
      return false;
    }
    // check if the same traps are applied to both blocks
    if (!firstBlock.getExceptionalSuccessors().equals(followingBlock.getExceptionalSuccessors())) {
      return false;
    }
    return true;
  }

  /** trys to merge the second block into the first one if possible */
  protected boolean tryMergeBlocks(
      @Nonnull MutableBasicBlock firstBlock, @Nonnull MutableBasicBlock followingBlock) {
    final boolean mergeable = isMergeable(firstBlock, followingBlock);
    if (mergeable) {
      for (Stmt stmt : followingBlock.getStmts()) {
        addNodeToBlock(firstBlock, stmt);
      }

      // i.e. can just be the single followingblock which we merge now
      firstBlock.clearSuccessorBlocks();

      // update linking info into firstBlock
      // done in clearPredecessorBlock      firstBlock.removeSuccessorBlock(followingBlock);
      followingBlock.getSuccessors().forEach(succ -> linkBlocks(firstBlock, succ));
      followingBlock.clearSuccessorBlocks();

      blocks.remove(followingBlock);

      // cleanup old block..
      followingBlock.clearPredecessorBlocks();
    }
    return mergeable;
  }

  /**
   * creates a Block and inserts the given Stmt.
   *
   * @return -1 if Stmt is already in the graph!
   */
  @Nonnull
  protected MutableBasicBlock createStmtsBlock(@Nonnull Stmt stmt) {
    // add Block to graph, add+register Stmt to Block
    MutableBasicBlock block = new MutableBasicBlock();
    if (addNodeToBlock(block, stmt) != null) {
      throw new IllegalArgumentException("Stmt is already in the graph!");
      // return -1;
    }
    blocks.add(block);
    return block;
  }

  /** Adds a Stmt to the end of a block i.e. stmt will become the new tail. */
  protected MutableBasicBlock addNodeToBlock(@Nonnull MutableBasicBlock block, @Nonnull Stmt stmt) {
    block.addStmt(stmt);
    return stmtToBlock.put(stmt, block);
  }

  public void removeNode(@Nonnull Stmt stmt) {

    MutableBasicBlock blockOfRemovedStmt = stmtToBlock.remove(stmt);
    if (blockOfRemovedStmt == null) {
      throw new IllegalArgumentException("Stmt is not in the StmtGraph!");
    }

    final boolean isHead = blockOfRemovedStmt.getHead() == stmt;
    final boolean isTail = blockOfRemovedStmt.getTail() == stmt;

    // do edges from or to this node exist -> remove them
    if (isHead && false) {
      // TODO: [ms] whats intuitive? removing the flows to the block too? or is deleting a stmt
      // keeping the flows to it
      // is the answer different if its the tail? consistency vs intuitivity..
      final MutableBasicBlock finalBlockOfRemovedStmt = blockOfRemovedStmt;
      blockOfRemovedStmt
          .getPredecessors()
          .forEach(
              b -> {
                b.removeSuccessorBlock(finalBlockOfRemovedStmt);
                finalBlockOfRemovedStmt.removePredecessorBlock(b);
              });
      blockOfRemovedStmt.clearPredecessorBlocks();
    }

    if (isTail) {
      // TODO: [ms] see question above.. i.e. shall we remove edges as well
      // switch, if, goto vs. usual stmt
      if (stmt.branches()) {
        final MutableBasicBlock finalBlockOfRemovedStmt = blockOfRemovedStmt;
        blockOfRemovedStmt
            .getSuccessors()
            .forEach(
                b -> {
                  b.removePredecessorBlock(finalBlockOfRemovedStmt);
                  finalBlockOfRemovedStmt.removeSuccessorBlock(b);
                });
        blockOfRemovedStmt.clearSuccessorBlocks();
      }
    }

    // cleanup or merge blocks if necesssary (stmt itself is not removed from the block yet)
    if (blockOfRemovedStmt.getStmtCount() > 1) {
      blockOfRemovedStmt.removeStmt(stmt);

      if (isHead) {
        blockOfRemovedStmt = tryMergeWithPredecessorBlock(blockOfRemovedStmt);
      }
      if (isTail) {
        tryMergeWithSuccessorBlock(blockOfRemovedStmt);
      }

    } else {
      // cleanup block as its not needed in the graph anymore if it only contains stmt - which is
      // now deleted
      blocks.remove(blockOfRemovedStmt);
      // not really necessary - but just if theres still somewhere a reference in use..don't!
      blockOfRemovedStmt.removeStmt(stmt);
    }
  }

  @Override
  public void replaceNode(@Nonnull Stmt oldStmt, @Nonnull Stmt newStmt) {

    final MutableBasicBlock blockOfOldStmt = getBlockOf(oldStmt);
    if (blockOfOldStmt == null) {
      throw new IllegalArgumentException("oldStmt does not exist in the StmtGraph!");
    }

    if (blockOfOldStmt.getTail() == oldStmt) {
      stmtToBlock.remove(oldStmt);
      blockOfOldStmt.removeStmt(oldStmt);
      blockOfOldStmt.addStmt(newStmt);
      stmtToBlock.put(newStmt, blockOfOldStmt);
      // tail could not be branching anymore -> try to merge
      tryMergeWithSuccessorBlock(blockOfOldStmt);
    } else if (blockOfOldStmt.getHead() == oldStmt) {
      stmtToBlock.remove(oldStmt);
      final MutableBasicBlock newBlock = createStmtsBlock(newStmt);
      final Iterator<Stmt> iterator = blockOfOldStmt.getStmts().stream().skip(1).iterator();
      iterator.forEachRemaining(
          stmt -> {
            newBlock.addStmt(stmt);
            stmtToBlock.put(stmt, newBlock);
          });

      blockOfOldStmt
          .getPredecessors()
          .forEach(
              predBlock -> {
                predBlock.removeSuccessorBlock(blockOfOldStmt);
                linkBlocks(predBlock, newBlock);
              });
      blockOfOldStmt.clearPredecessorBlocks();

      blockOfOldStmt
          .getSuccessors()
          .forEach(
              succBlock -> {
                succBlock.removePredecessorBlock(blockOfOldStmt);
                linkBlocks(newBlock, succBlock);
              });
      blockOfOldStmt.clearSuccessorBlocks();
      blocks.remove(blockOfOldStmt);

      newBlock.copyExceptionalFlowFrom(blockOfOldStmt);
      stmtToBlock.put(newStmt, newBlock);

    } else {
      // [ms] possibility to use performance implications of the datastructure to improve
      // unnecessary BasicBlock allocation/stmt copying/removal
      final MutableBasicBlock excludedBlock = excludeStmtFromBlock(oldStmt, blockOfOldStmt);
      stmtToBlock.remove(oldStmt);
      excludedBlock.removeStmt(oldStmt);
      excludedBlock.addStmt(newStmt);
      stmtToBlock.put(newStmt, excludedBlock);
      tryMergeIntoSurroundingBlocks(excludedBlock);
    }
  }

  public void validateBlocks() {
    for (MutableBasicBlock block : getBlocks()) {
      for (Stmt stmt : block.getStmts()) {
        if (stmtToBlock.get(stmt) != block) {
          throw new IllegalStateException("wrong stmt to block mapping");
        }
      }
    }
  }

  /**
   * @param stmts can only allow fallsthrough Stmts except for the last Stmt in the List there is a
   *     single BranchingStmt allowed!
   * @param beforeStmt the Stmt which succeeds the inserted Stmts (its not preceeding as this
   *     simplifies the handling of BranchingStmts)
   */
  public void insertBefore(
      @Nonnull Stmt beforeStmt,
      @Nonnull List<Stmt> stmts,
      @Nonnull Map<ClassType, Stmt> exceptionMap) {
    if (stmts.isEmpty()) {
      return;
    }
    final MutableBasicBlock block = getBlockOf(beforeStmt);
    if (block.getHead() == beforeStmt) {
      // insert before a Stmt that is at the beginning of a Block? -> new block, reconnect, try to
      // merge blocks - performance hint: if exceptionMap equals the current blocks exception and
      // the stmts have only fallsthrough Stmts there could be some allocation/deallocation be saved
      final MutableBasicBlock predecessorBlock = addBlockInternal(stmts, exceptionMap);
      for (MutableBasicBlock predecessor : block.getPredecessors()) {
        // cleanup old
        predecessor.removeSuccessorBlock(block);
        block.removePredecessorBlock(predecessor);
        // add new link
        linkBlocks(predecessor, predecessorBlock);
      }
      tryMergeBlocks(predecessorBlock, block);
    } else {
      final MutableBasicBlock successorBlock = block.splitBlockLinked(beforeStmt, true);
      exceptionMap.forEach(
          (type, handler) ->
              successorBlock.addExceptionalSuccessorBlock(type, getOrCreateBlock(handler)));
      stmts.forEach(stmt -> addNodeToBlock(block, stmt));
      tryMergeBlocks(block, successorBlock);
    }

    if (beforeStmt == getStartingStmt()) {
      setStartingStmt(stmts.get(0));
    }
  }

  @Override
  public void putEdge(@Nonnull Stmt stmtA, @Nonnull Stmt stmtB) {
    MutableBasicBlock blockA = stmtToBlock.get(stmtA);
    MutableBasicBlock blockB = stmtToBlock.get(stmtB);

    if (blockA == null) {
      // stmtA is is not in the graph (i.e. no reference to BlockA) -> create
      blockA = createStmtsBlock(stmtA);
    } else {
      if (blockA.getTail() != stmtA) {
        // if StmtA is not at the end of the block -> it needs to branch to reach StmtB or is
        // falling through to another Block
        throw new IllegalArgumentException(
            "StmtA '"
                + stmtA
                + "' is not at the end of a block but it must be to reach StmtB '"
                + stmtB
                + "'.");
      }
    }

    if (blockA.getSuccessors().size() >= stmtA.getExpectedSuccessorCount()) {
      throw new IllegalArgumentException(
          "Can't add another flow - there are already enough flows i.e. "
              + stmtA.getExpectedSuccessorCount()
              + " outgoing from StmtA '"
              + stmtA
              + "'");
    }

    if (stmtA.branches()) {
      // branching A indicates the end of BlockA and connects to another BlockB: reuse or create new
      // one
      if (blockB == null) {
        blockB = createStmtsBlock(stmtB);
        linkBlocks(blockA, blockB);
      } else {
        if (blockB.getHead() == stmtB) {
          // stmtB is at the beginning of the second Block -> connect blockA and blockB
          linkBlocks(blockA, blockB);
        } else {

          MutableBasicBlock newBlock = blockB.splitBlockLinked(stmtB, true);
          newBlock.copyExceptionalFlowFrom(blockB);
          blocks.add(newBlock);
          newBlock.getStmts().forEach(stmt -> stmtToBlock.put(stmt, newBlock));

          if (blockA == blockB) {
            // successor of block is the origin: end of block flows to beginning of new splitted
            // block (i.e.
            // the same block)
            linkBlocks(newBlock, newBlock);
          } else {
            linkBlocks(blockA, newBlock);
          }
        }
      }

    } else {
      // stmtA does not branch
      if (blockB == null) {
        // stmtB is new in the graph -> just add it to the same block
        addNodeToBlock(blockA, stmtB);
      } else {
        if (blockB.getHead() == stmtB) {
          // stmtB is at the beginning of the second Block -> try to connect blockA and blockB
          // is stmtB already a branch target and do their blocks have the same traps?
          if (blockB.getPredecessors().isEmpty()
              && blockA.getExceptionalSuccessors().equals(blockB.getExceptionalSuccessors())) {
            // merge blockB into blockA and remove now obsolete Block B
            MutableBasicBlock finalBlockA = blockA;
            blockB
                .getStmts()
                .forEach(
                    stmt -> {
                      finalBlockA.addStmt(stmt);
                      stmtToBlock.put(stmt, finalBlockA);
                    });

          } else {
            // stmtA does not branch but stmtB is already a branch target => link blocks
            linkBlocks(blockA, blockB);
          }
        } else {
          throw new IllegalArgumentException(
              "StmtB is already in the Graph and has a already a non-branching predecessor!");
        }
      }
    }
  }

  /**
   * makes blockA the predecessor of BlockB and BlockB the Successor of BlockA in a combined Method
   */
  private void linkBlocks(@Nonnull MutableBasicBlock blockA, @Nonnull MutableBasicBlock blockB) {
    blockA.addSuccessorBlock(blockB);
    blockB.addPredecessorBlock(blockA);
  }

  @Override
  public void removeEdge(@Nonnull Stmt from, @Nonnull Stmt to) {
    MutableBasicBlock blockOfFrom = stmtToBlock.get(from);
    MutableBasicBlock blockOfTo = stmtToBlock.get(to);

    removeBlockBorderEdgesInternal(from, blockOfFrom);

    // divide block if from and to are from the same block
    if (blockOfFrom == blockOfTo) {
      // divide block and don't link them
      final List<Stmt> stmtsOfBlock = blockOfFrom.getStmts();
      int fromIdx = stmtsOfBlock.indexOf(from);
      if (stmtsOfBlock.get(fromIdx + 1) == to) {
        MutableBasicBlock newBlock = blockOfFrom.splitBlockUnlinked(from, to);
        newBlock.copyExceptionalFlowFrom(blockOfFrom);
        blocks.add(newBlock);
        newBlock.getStmts().forEach(s -> stmtToBlock.put(s, newBlock));
      } else {
        // throw new IllegalArgumentException("Can't seperate the flow from '"+from+"' to '"+to+"'.
        // The Stmts are not connected in this graph!");
      }
    }
  }

  protected boolean removeBlockBorderEdgesInternal(
      @Nonnull Stmt from, @Nonnull MutableBasicBlock blockOfFrom) {
    // TODO: is it intuitive to remove connections to the BasicBlock in the case we cant merge the
    // blocks?

    // add BlockB to BlockA if blockA has no branchingstmt as tail && same traps
    if (blockOfFrom.getStmts().size() > 0 && from == blockOfFrom.getTail()) {
      if (blockOfFrom.getPredecessors().size() == 1) {
        MutableBasicBlock singlePreviousBlock = blockOfFrom.getPredecessors().get(0);
        if (!singlePreviousBlock.getTail().branches() && singlePreviousBlock != blockOfFrom) {
          if (singlePreviousBlock
              .getExceptionalSuccessors()
              .equals(blockOfFrom.getExceptionalSuccessors())) {
            blockOfFrom
                .getStmts()
                .forEach(
                    k -> {
                      singlePreviousBlock.addStmt(k);
                      stmtToBlock.put(k, blockOfFrom);
                    });
            return true;
          }
        }
      }

      // remove outgoing connections from blockA if from stmt is the tail
      if (!from.branches()) {
        if (blockOfFrom.getStmts().size() > 0 && blockOfFrom.getSuccessors().size() == 1) {
          // merge previous block if possible i.e. no branchingstmt as tail && same traps && no
          // other predesccorblocks
          MutableBasicBlock singleSuccessorBlock = blockOfFrom.getSuccessors().get(0);
          if (singleSuccessorBlock.getPredecessors().size() == 1
              && singleSuccessorBlock.getPredecessors().get(0) == blockOfFrom) {
            if (singleSuccessorBlock
                .getExceptionalSuccessors()
                .equals(blockOfFrom.getExceptionalSuccessors())) {
              singleSuccessorBlock
                  .getStmts()
                  .forEach(
                      k -> {
                        blockOfFrom.addStmt(k);
                        stmtToBlock.put(k, blockOfFrom);
                      });

              return true;
            }
          }
        }
      } else {
        blockOfFrom.clearSuccessorBlocks();
        return true;
      }
    }

    return false;
  }

  @Override
  public void setEdges(@Nonnull Stmt fromStmt, @Nonnull List<Stmt> targets) {
    if (fromStmt.getExpectedSuccessorCount() != targets.size()) {
      throw new IllegalArgumentException(
          "Size of Targets is not the amount of from's expected successors.");
    }
    MutableBasicBlock fromBlock = getOrCreateBlock(fromStmt);
    if (fromBlock.getTail() == fromStmt) {
      // cleanup existing edges
      fromBlock.clearSuccessorBlocks();
    }
    targets.forEach(target -> putEdge(fromStmt, target));
  }

  @Nullable
  @Override
  public Stmt getStartingStmt() {
    // is the stmt currently in a block associated with the graph?
    if (stmtToBlock.get(startingStmt) == null) {
      return null;
    }
    return startingStmt;
  }

  @Override
  @Nullable
  public MutableBasicBlock getStartingStmtBlock() {
    return getBlockOf(startingStmt);
  }

  @Override
  @Nullable
  public MutableBasicBlock getBlockOf(@Nonnull Stmt stmt) {
    return stmtToBlock.get(stmt);
  }

  @Nonnull
  @Override
  public StmtGraph<?> unmodifiableStmtGraph() {
    return new ForwardingStmtGraph<>(this);
  }

  public void setStartingStmt(@Nonnull Stmt startingStmt) {
    if (stmtToBlock.get(startingStmt) == null) {
      MutableBasicBlock block = stmtToBlock.get(startingStmt);
      if (block == null) {
        // Stmt does not exist in the graph
        createStmtsBlock(startingStmt);
      }
    }
    this.startingStmt = startingStmt;
  }

  @Nonnull
  @Override
  public Set<Stmt> nodes() {
    return stmtToBlock.keySet();
  }

  @Override
  public boolean containsNode(@Nonnull Stmt node) {
    return stmtToBlock.containsKey(node);
  }

  @Nonnull
  @Override
  public List<Stmt> predecessors(@Nonnull Stmt node) {
    MutableBasicBlock block = stmtToBlock.get(node);
    if (block == null) {
      throw new IllegalArgumentException("Stmt is not contained in the BlockStmtGraph: " + node);
    }

    if (node == block.getHead()) {
      List<MutableBasicBlock> predecessorBlocks = block.getPredecessors();
      List<Stmt> preds = new ArrayList<>(predecessorBlocks.size());
      predecessorBlocks.forEach(p -> preds.add(p.getTail()));
      return preds;
    } else {
      // argh indexOf.. possibly expensive..
      List<Stmt> stmts = block.getStmts();
      final int i = stmts.indexOf(node);
      assert (stmts.size() > 0) : "no stmts in " + block + " " + block.hashCode();
      ;
      assert (i > 0) : " stmt not found in " + block;
      return Collections.singletonList(stmts.get(i - 1));
    }
  }

  @Nonnull
  @Override
  public List<Stmt> exceptionalPredecessors(@Nonnull Stmt node) {

    final MutableBasicBlock currentBlock = stmtToBlock.get(node);
    if (currentBlock == null) {
      throw new IllegalArgumentException("Stmt is not in the StmtGraph.");
    }

    if (currentBlock.getHead() != node
        || !(node instanceof JIdentityStmt
            && ((JIdentityStmt<?>) node).getRightOp() instanceof JCaughtExceptionRef)) {
      // only an exception handler stmt can have exceptional predecessors
      return Collections.emptyList();
    }

    List<Stmt> exceptionalPred = new ArrayList<>();
    for (BasicBlock<?> block : getBlocks()) {
      if (block.getExceptionalSuccessors().containsValue(currentBlock)) {
        exceptionalPred.addAll(block.getStmts());
      }
    }
    return exceptionalPred;
  }

  @Nonnull
  @Override
  public List<Stmt> successors(@Nonnull Stmt node) {
    MutableBasicBlock block = stmtToBlock.get(node);
    if (block == null) {
      throw new IllegalArgumentException("Stmt is not contained in the BlockStmtGraph: " + node);
    }

    if (node == block.getTail()) {
      List<MutableBasicBlock> successorBlocks = block.getSuccessors();
      List<Stmt> succs = new ArrayList<>(successorBlocks.size());
      successorBlocks.forEach(p -> succs.add(p.getHead()));
      return succs;
    } else {
      // argh indexOf.. possibly expensive..
      List<Stmt> stmts = block.getStmts();
      return Collections.singletonList(stmts.get(stmts.indexOf(node) + 1));
    }
  }

  @Nonnull
  @Override
  public Map<ClassType, Stmt> exceptionalSuccessors(@Nonnull Stmt node) {
    MutableBasicBlock block = stmtToBlock.get(node);
    if (block == null) {
      throw new IllegalArgumentException("Stmt is not contained in the BlockStmtGraph: " + node);
    }
    Map<ClassType, Stmt> map = new HashMap<>();
    for (Map.Entry<ClassType, MutableBasicBlock> b : block.getExceptionalSuccessors().entrySet()) {
      map.put(b.getKey(), b.getValue().getHead());
    }
    return map;
  }

  @Override
  public int inDegree(@Nonnull Stmt node) {
    MutableBasicBlock block = stmtToBlock.get(node);
    if (block == null) {
      throw new IllegalArgumentException("Stmt is not contained in the BlockStmtGraph: " + node);
    }

    if (node == block.getHead()) {
      return block.getPredecessors().size();
    } else {
      return 1;
    }
  }

  @Override
  public int outDegree(@Nonnull Stmt node) {
    MutableBasicBlock block = stmtToBlock.get(node);
    if (block == null) {
      throw new IllegalArgumentException("Stmt is not contained in the BlockStmtGraph: " + node);
    }

    if (node == block.getTail()) {
      return block.getSuccessors().size();
    } else {
      return 1;
    }
  }

  @Override
  public boolean hasEdgeConnecting(@Nonnull Stmt source, @Nonnull Stmt target) {
    MutableBasicBlock blockA = stmtToBlock.get(source);
    if (blockA == null) {
      throw new IllegalArgumentException(
          "source Stmt is not contained in the BlockStmtGraph: " + source);
    }

    if (source == blockA.getTail()) {
      MutableBasicBlock blockB = stmtToBlock.get(source);
      if (blockB == null) {
        throw new IllegalArgumentException(
            "target Stmt is not contained in the BlockStmtGraph: " + source);
      }
      return blockA.getSuccessors().stream()
          .anyMatch(
              successorBlock -> /*successorBlock == blockB && */
                  successorBlock.getHead() == target);
    } else {
      List<Stmt> stmtsA = blockA.getStmts();
      return stmtsA.get(stmtsA.indexOf(source) + 1) == target;
    }
  }

  /** Comparator which sorts the trap output in getTraps() */
  public Comparator<Trap> getTrapComparator(@Nonnull HashMap<Stmt, Integer> stmtsBlockIdx) {
    return (a, b) ->
        ComparisonChain.start()
            .compare(stmtsBlockIdx.get(a.getBeginStmt()), stmtsBlockIdx.get(b.getBeginStmt()))
            .compare(stmtsBlockIdx.get(a.getEndStmt()), stmtsBlockIdx.get(b.getEndStmt()))
            // [ms] would be nice to have the traps ordered by exception hierarchy as well
            .compare(a.getExceptionType().toString(), b.getExceptionType().toString())
            .result();
  }

  /** hint: little expensive getter - its more of a build/create */
  @Override
  public List<Trap> getTraps() {
    // [ms] try to incorporate it into the serialisation of jimple printing so the other half of
    // iteration information is not wasted..
    BlockGraphIteratorAndTrapAggregator it = new BlockGraphIteratorAndTrapAggregator();
    // it.getTraps() is valid/completely build when the iterator is done.
    HashMap<Stmt, Integer> stmtsBlockIdx = new HashMap<>();
    int i = 0;
    while (it.hasNext()) {
      final MutableBasicBlock nextBlock = it.next();
      stmtsBlockIdx.put(nextBlock.getHead(), i);
      stmtsBlockIdx.put(nextBlock.getTail(), i);
      i++;
    }
    final List<Trap> traps = it.getTraps();

    traps.sort(getTrapComparator(stmtsBlockIdx));
    return traps;
  }

  @Nonnull
  public Iterator<Stmt> iterator() {
    return new BlockStmtGraphIterator();
  }

  /** Iterates the Stmts according to the jimple output order. */
  private class BlockStmtGraphIterator implements Iterator<Stmt> {

    private final BlockGraphIterator blockIt;
    @Nonnull private Iterator<Stmt> currentBlockIt = Collections.emptyIterator();

    public BlockStmtGraphIterator() {
      this(new BlockGraphIterator());
    }

    public BlockStmtGraphIterator(@Nonnull BlockGraphIterator blockIterator) {
      blockIt = blockIterator;
    }

    @Override
    public boolean hasNext() {
      // hint: a BasicBlock has at least 1 Stmt or should not be in a StmtGraph!
      return currentBlockIt.hasNext() || blockIt.hasNext();
    }

    @Override
    public Stmt next() {
      if (!currentBlockIt.hasNext()) {
        if (!blockIt.hasNext()) {
          throw new NoSuchElementException("Iterator has no more Stmts.");
        }
        MutableBasicBlock currentBlock = blockIt.next();
        currentBlockIt = currentBlock.getStmts().iterator();
      }
      return currentBlockIt.next();
    }
  }

  /** Iterates over the Blocks and collects/aggregates Trap information */
  public class BlockGraphIteratorAndTrapAggregator extends BlockGraphIterator {

    @Nonnull private final List<Trap> collectedTraps = new ArrayList<>();

    Map<ClassType, Stmt> trapStarts = new HashMap<>();
    MutableBasicBlock lastBlock =
        new MutableBasicBlock(); // dummy value to remove n-1 unnecessary null-checks

    @Nonnull
    @Override
    public MutableBasicBlock next() {
      final MutableBasicBlock block = super.next();

      final Map<? extends ClassType, MutableBasicBlock> currentBlocksExceptions =
          block.getExceptionalSuccessors();
      final Map<? extends ClassType, MutableBasicBlock> lastBlocksExceptions =
          lastBlock.getExceptionalSuccessors();

      // former trap info is not in the current blocks info -> add it to the trap collection
      lastBlocksExceptions.forEach(
          (type, trapHandlerBlock) -> {
            if (trapHandlerBlock != block.getExceptionalSuccessors().get(type)) {
              final Stmt trapBeginStmt = trapStarts.remove(type);
              if (trapBeginStmt == null) {
                throw new IllegalStateException("Trap start for '" + type + "' is not in the Map!");
              }
              // trapend is exclusive!
              collectedTraps.add(
                  new Trap(type, trapBeginStmt, block.getHead(), trapHandlerBlock.getHead()));
            }
          });

      // is there a new trap in the current block -> add it to currentTraps
      block
          .getExceptionalSuccessors()
          .forEach(
              (type, trapHandlerBlock) -> {
                if (trapHandlerBlock != lastBlocksExceptions.get(type)) {
                  trapStarts.put(type, block.getHead());
                }
              });

      lastBlock = block;
      return block;
    }

    /**
     * for jimple serialization -> this is the info for the end of the method contains only
     * valid/useful information when all stmts are iterated i.e. hasNext() == false!
     *
     * @return List of Traps
     */
    public List<Trap> getTraps() {
      // aggregate dangling trap data
      trapStarts.forEach(
          (type, trapStart) -> {
            final MutableBasicBlock trapHandler = lastBlock.getExceptionalSuccessors().get(type);
            if (trapHandler == null) {
              throw new IllegalStateException(
                  "No matching Trap info found for '"
                      + type
                      + "' in ExceptionalSucessors() of the last iterated Block!");
            }
            collectedTraps.add(
                new Trap(type, trapStart, lastBlock.getTail(), trapHandler.getTail()));
          });
      trapStarts.clear();
      return collectedTraps;
    }
  }

  /** Iterates over the blocks */
  public class BlockGraphIterator implements Iterator<MutableBasicBlock> {

    @Nonnull private final ArrayDeque<MutableBasicBlock> trapHandlerBlocks = new ArrayDeque<>();

    @Nonnull private final ArrayDeque<MutableBasicBlock> nestedBlocks = new ArrayDeque<>();
    @Nonnull private final ArrayDeque<MutableBasicBlock> otherBlocks = new ArrayDeque<>();
    @Nonnull private final Set<MutableBasicBlock> iteratedBlocks;

    public BlockGraphIterator() {
      final List<MutableBasicBlock> blocks = getBlocks();
      iteratedBlocks = new HashSet<>(blocks.size(), 1);
      Stmt startingStmt = getStartingStmt();
      if (startingStmt != null) {
        final MutableBasicBlock startingBlock = getBlockOf(startingStmt);
        updateFollowingBlocks(startingBlock);
        nestedBlocks.addFirst(startingBlock);
      }
    }

    @Nullable
    private MutableBasicBlock retrieveNextBlock() {
      MutableBasicBlock nextBlock;
      do {
        if (!nestedBlocks.isEmpty()) {
          nextBlock = nestedBlocks.pollFirst();
        } else if (!trapHandlerBlocks.isEmpty()) {
          nextBlock = trapHandlerBlocks.pollFirst();
        } else if (!otherBlocks.isEmpty()) {
          nextBlock = otherBlocks.pollFirst();
        } else {
          return null;
        }

        // skip retrieved nextBlock if its already returned
      } while (iteratedBlocks.contains(nextBlock));
      return nextBlock;
    }

    @Override
    @Nonnull
    public MutableBasicBlock next() {
      MutableBasicBlock currentBlock = retrieveNextBlock();
      if (currentBlock == null) {
        throw new NoSuchElementException("Iterator has no more Blocks.");
      }
      updateFollowingBlocks(currentBlock);
      iteratedBlocks.add(currentBlock);
      return currentBlock;
    }

    //
    private void updateFollowingBlocks(MutableBasicBlock currentBlock) {
      // collect traps
      final Stmt tailStmt = currentBlock.getTail();
      for (Map.Entry<? extends ClassType, MutableBasicBlock> entry :
          currentBlock.getExceptionalSuccessors().entrySet()) {
        MutableBasicBlock trapHandlerBlock = entry.getValue();
        trapHandlerBlocks.addLast(trapHandlerBlock);
        nestedBlocks.addFirst(trapHandlerBlock);
      }

      final List<MutableBasicBlock> successors = currentBlock.getSuccessors();

      for (int i = successors.size() - 1; i >= 0; i--) {
        if (i == 0 && tailStmt.fallsThrough()) {
          // non-branching successors i.e. not a BranchingStmt or is the first successor (i.e. its
          // false successor) of
          // JIfStmt
          nestedBlocks.addFirst(successors.get(0));
        } else {

          // create the most biggest fallsthrough sequence of basicblocks as possible -> go to the
          // top until
          // predecessor is not a fallsthrough stmt anymore and then the iterator will iterate
          // from there.
          final MutableBasicBlock successorBlock = successors.get(i);
          MutableBasicBlock leaderOfFallsthroughBlocks = successorBlock;
          while (true) {
            final List<MutableBasicBlock> itPreds = leaderOfFallsthroughBlocks.getPredecessors();
            if (itPreds.size() != 1) {
              break;
            }
            MutableBasicBlock predecessorBlock = itPreds.get(0);
            if (predecessorBlock.getTail().fallsThrough()
                && predecessorBlock.getSuccessors().get(0) == leaderOfFallsthroughBlocks) {
              leaderOfFallsthroughBlocks = predecessorBlock;
            } else {
              break;
            }
          }

          // find a return Stmt inside the current Block
          Stmt succTailStmt = successorBlock.getTail();
          boolean isReturnBlock =
              succTailStmt instanceof JReturnVoidStmt || succTailStmt instanceof JReturnStmt;

          // remember branching successors
          if (tailStmt instanceof JGotoStmt) {
            if (isReturnBlock) {
              nestedBlocks.removeFirstOccurrence(currentBlock.getHead());
              otherBlocks.addLast(leaderOfFallsthroughBlocks);
            } else {
              otherBlocks.addFirst(leaderOfFallsthroughBlocks);
            }
          } else if (!nestedBlocks.contains(leaderOfFallsthroughBlocks)) {
            // JSwitchStmt, JIfStmt
            if (isReturnBlock) {
              nestedBlocks.addLast(leaderOfFallsthroughBlocks);
            } else {
              nestedBlocks.addFirst(leaderOfFallsthroughBlocks);
            }
          }
        }
      }
    }

    @Override
    public boolean hasNext() {
      final boolean hasIteratorMoreElements;
      MutableBasicBlock b = retrieveNextBlock();
      if (b != null) {
        // reinsert at FIRST position -> not great for performance - but easier handling in next()
        nestedBlocks.addFirst(b);
        hasIteratorMoreElements = true;
      } else {
        hasIteratorMoreElements = false;
      }

      // "assertion" that all elements are iterated
      if (!hasIteratorMoreElements) {
        final int returnedSize = iteratedBlocks.size();
        final List<MutableBasicBlock> blocks = getBlocks();
        final int actualSize = blocks.size();
        if (returnedSize != actualSize) {
          String info =
              blocks.stream()
                  .filter(n -> !iteratedBlocks.contains(n))
                  .map(BasicBlock::getStmts)
                  .collect(Collectors.toList())
                  .toString();
          throw new IllegalStateException(
              "There are "
                  + (actualSize - returnedSize)
                  + " Blocks that are not iterated! i.e. the StmtGraph is not connected from its startingStmt!"
                  + info
                  + GraphVizExporter.createUrlToWebeditor(MutableBlockStmtGraph.this));
        }
      }
      return hasIteratorMoreElements;
    }
  }
}
