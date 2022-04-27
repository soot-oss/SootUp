package de.upb.swt.soot.core.graph;

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
  @Nonnull private final Map<Stmt, Integer> stmtToBlock = new HashMap<>();

  @Nonnull
  private final ArrayList<MutableBasicBlock> blocks =
      new ArrayList<>(); // remove mapping: integerIdx->block and use a set here

  public MutableBlockStmtGraph() {}

  /** copies a StmtGraph into this Mutable instance */
  public MutableBlockStmtGraph(@Nonnull StmtGraph<? extends BasicBlock<?>> graph) {
    setStartingStmt(graph.getStartingStmt());
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
    for (int i = 0, stmtsSize = stmts.size(); i < stmtsSize; i++) {
      Stmt stmt = stmts.get(i);

      // TODO: bad performance.. O(n * m)... sort it once: use some kind of -> O(1)*n with 2*m
      // additional memory
      boolean trapsChanged = false;
      for (Trap trap : traps) {
        if (stmt == trap.getBeginStmt()) {
          currentTrapMap.put(trap.getExceptionType(), trap.getHandlerStmt());
          trapsChanged = true;
        }

        // endStmt is exclusive!
        if (stmt == trap.getEndStmt()) {
          currentTrapMap.remove(trap.getExceptionType(), trap.getHandlerStmt());
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
                  + "') which has no follower - so it can't fall through.");
        }
        putEdge(stmt, stmts.get(i + 1));
      }

      if (stmt instanceof BranchingStmt) {
        // => end of Block
        final List<Stmt> targets = branchingMap.get(stmt);
        int expectedBranchEntries = stmt.getSuccessorCount() - (stmt.fallsThrough() ? 1 : 0);
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
    addExceptionalEdge(stmt, 0, exceptionType, traphandlerStmt);
  }

  /**
   * @param numberOfSuccessors the exceptional edge is applied to the specified stmt and the number
   *     of fallsthrough successors
   */
  private void addExceptionalEdge(
      @Nonnull Stmt stmt,
      int numberOfSuccessors,
      @Nonnull ClassType exceptionType,
      @Nonnull Stmt traphandlerStmt) {
    Integer blockIdx = stmtToBlock.get(stmt);
    if (blockIdx == null) {
      return;
    }
    final MutableBasicBlock block = blocks.get(blockIdx);

    final Map<ClassType, MutableBasicBlock> exceptionalSuccessors =
        block.getExceptionalSuccessors();
    final MutableBasicBlock trapBlock = exceptionalSuccessors.get(exceptionType);
    if (trapBlock != null && trapBlock.getHead() == traphandlerStmt) {
      // edge already exists
      return;
    }

    final HashMap<ClassType, MutableBasicBlock> newExceptionalSuccessors =
        new HashMap<>(exceptionalSuccessors);

    if (block.getHead() == stmt) {
      // check if one of the previous blocks has the same traps and has no BranchingStmt as the Tail
      final List<MutableBasicBlock> predecessors = block.getPredecessors();
      final Optional<MutableBasicBlock> any =
          predecessors.stream()
              .filter(
                  p ->
                      !p.getTail().branches()
                          && p.getExceptionalSuccessors().equals(newExceptionalSuccessors))
              .findAny();
      if (any.isPresent()) {
        // TODO: merge it into this block.
        return;
      }

      // TODO: split from successor stmts of the block..

    } else if (block.getTail() == stmt) {
      // try to merge into following block if the traps are the same and it has exactly 1
      // predecessor (i.e. block)
      final List<MutableBasicBlock> successors = block.getSuccessors();
      if (!stmt.branches() && successors.size() == 1) {
        final MutableBasicBlock successor = successors.get(0);
        if (successor.getPredecessors().size() == 1) {
          if (successor.getExceptionalSuccessors().equals(newExceptionalSuccessors)) {
            // TODO: merge it into successor
            return;
          }
        }
      }
      // TODO: split tail from rest of the block
    } else {
      // TODO: stmt in the middle of a block: just split

    }

    // TODO: do sth with it
    block.addExceptionalSuccessorBlock(exceptionType, getBlockOf(traphandlerStmt));

    if (numberOfSuccessors != 0) {
      // TODO: do it for the numberOfSuccessors as well
      throw new IllegalArgumentException("cant handle trap removal yet for numberOfSuccessors");
    }
  }

  @Override
  public void removeExceptionalEdge(@Nonnull Stmt stmt, @Nonnull ClassType exceptionType) {
    Integer blockIdx = stmtToBlock.get(stmt);
    if (blockIdx == null) {
      return;
    }

    final MutableBasicBlock block = blocks.get(blockIdx);
    block.removeExceptionalSuccessorBlock(exceptionType);

    // FIXME: possibly merge this block if possible
    throw new IllegalArgumentException("cant handle trap removal yet for numberOfSuccessors");
  }

  @Override
  public void clearExceptionalEdges(@Nonnull Stmt node) {
    Integer blockIdx = stmtToBlock.get(node);
    if (blockIdx == null) {
      return;
    }

    final MutableBasicBlock block = blocks.get(blockIdx);
    block.clearExceptionalSuccessorBlocks();

    // FIXME: possibly merge this block if possible
    throw new IllegalArgumentException("cant handle trap removal yet for numberOfSuccessors");
  }

  @Override
  @Nonnull
  public List<MutableBasicBlock> getBlocks() {
    return blocks.stream().filter(Objects::nonNull).collect(Collectors.toList());
  }

  /**
   * The list of Stmts must contain only fallsthrough Stmts; A flow manipulating Stmt
   * (BranchingStmt, return, throw) is only allowed at the Tail. (like the conditions of a Block)
   */
  @Override
  public void addBlock(@Nonnull List<Stmt> stmts, @Nonnull Map<ClassType, Stmt> trapMap) {
    final Iterator<Stmt> iterator = stmts.iterator();
    if (!iterator.hasNext()) {
      return;
    }
    final Stmt node = iterator.next();
    int blockIdx = createStmtsBlock(node);
    if (blockIdx < 0) {
      throw new IllegalArgumentException("Stmt is already in the Graph!");
    }
    iterator.forEachRemaining(stmt -> addNodeToBlock(blocks.get(blockIdx), blockIdx, stmt));
  }

  @Override
  public void addNode(@Nonnull Stmt node, @Nonnull Map<ClassType, Stmt> exceptions) {
    Integer blockIdx = stmtToBlock.get(node);
    if (blockIdx == null) {
      // Stmt does not exist in the graph -> create
      blockIdx = createStmtsBlock(node);
    }

    final MutableBasicBlock block = blocks.get(blockIdx);

    // if no exceptions are associated with the (now) existing block: just add them
    if (block.getExceptionalSuccessors().size() == 0) {
      if (exceptions.size() > 0) {
        if (block.getStmtCount() != 1) {
          final MutableBasicBlock secondBlock = block.splitBlockLinked(node, true);
          int secondBlockIdx = blocks.size();
          blocks.add(secondBlock);
          if (secondBlock.getStmtCount() > 1) {
            final MutableBasicBlock thirdBlock = secondBlock.splitBlockLinked(node, false);
            int thirdBlockIdx = blocks.size();
            blocks.add(thirdBlock);
            thirdBlock.getStmts().forEach(stmt -> stmtToBlock.put(stmt, thirdBlockIdx));
          }
          secondBlock.getStmts().forEach(stmt -> stmtToBlock.put(stmt, secondBlockIdx));
        }

        for (Map.Entry<ClassType, Stmt> exceptionMap : exceptions.entrySet()) {
          Integer targetBlockIdx = stmtToBlock.get(exceptionMap.getValue());
          if (targetBlockIdx == null) {
            targetBlockIdx = createStmtsBlock(exceptionMap.getValue());
          }
          blocks
              .get(blockIdx)
              .addExceptionalSuccessorBlock(exceptionMap.getKey(), blocks.get(targetBlockIdx));
        }
      }
    } else {
      // check if parameter exceptions are equal to those in the Block of the existing Stmt:
      // otherwise fail.
      if (exceptions.size() > 0) {
        boolean different = false;
        if (block.getExceptionalSuccessors().size() == exceptions.size()) {
          for (Map.Entry<ClassType, MutableBasicBlock> entry :
              block.getExceptionalSuccessors().entrySet()) {
            final Stmt targetStmt = exceptions.get(entry.getKey());
            if (targetStmt == null) {
              different = true;
              break;
            } else if (targetStmt != entry.getValue().getHead()) {
              different = true;
              break;
            }
          }
        } else {
          different = true;
        }

        if (different) {
          throw new IllegalArgumentException(
              "Cant intuitively handle adding/merging exceptions to an already existing node with already assigned exceptions into the graph.");
        }
      }
    }
  }

  /**
   * creates a Block and inserts the given Stmt.
   *
   * @return -1 if Stmt is already in the graph!
   */
  protected int createStmtsBlock(@Nonnull Stmt stmt) {
    // add Block to graph, add+register Stmt to Block
    MutableBasicBlock block = new MutableBasicBlock();
    final int idx = blocks.size();
    if (addNodeToBlock(block, idx, stmt) != null) {
      throw new IllegalArgumentException("Stmt is already in the graph!");
      // return -1;
    }
    blocks.add(block);
    return idx;
  }

  /** Adds a Stmt to the end of a block i.e. stmt will become the new tail. */
  protected Integer addNodeToBlock(
      @Nonnull MutableBasicBlock block, int blockIdx, @Nonnull Stmt stmt) {
    block.addStmt(stmt);
    return stmtToBlock.putIfAbsent(stmt, blockIdx);
  }

  public void removeNode(@Nonnull Stmt stmt) {

    // do edges from or to this node exist? remove them
    // TODO: [ms] implement more performant solution based on datastructure implications
    predecessors(stmt).forEach(p -> removeEdge(p, stmt));
    successors(stmt).forEach(s -> removeEdge(stmt, s));

    Integer blockIdx = stmtToBlock.remove(stmt);
    if (blockIdx == null) {
      return;
    }

    MutableBasicBlock blockOfRemovedStmt = blocks.get(blockIdx);
    blockOfRemovedStmt.removeStmt(stmt);

    if (blockOfRemovedStmt.getStmts().size() <= 0) {
      // for GC: clear entry in blocks if block is empty -> not referenced anymore -> not reachable
      // anymore
      blocks.set(blockIdx, null);
      // blocks.remove( blockIdx.intValue() ); would need an expensive update of all higher blockIdx
      // that are then changed
    }
  }

  @Override
  public void replaceNode(@Nonnull Stmt oldStmt, @Nonnull Stmt newStmt) {
    // TODO: [ms] implement it smarter i.e. more performant based on implications of the
    // datastructure
    removeNode(oldStmt);
    addNode(newStmt);
  }

  @Override
  public void putEdge(@Nonnull Stmt stmtA, @Nonnull Stmt stmtB) {

    Integer blockAIdx = stmtToBlock.get(stmtA);
    Integer blockBIdx = stmtToBlock.get(stmtB);

    MutableBasicBlock blockA;
    MutableBasicBlock blockB;

    if (blockAIdx == null) {
      // stmtA is is not in the graph (i.e. no reference to BlockA) -> create
      blockAIdx = createStmtsBlock(stmtA);
      blockA = blocks.get(blockAIdx);
    } else {
      blockA = blocks.get(blockAIdx);
      if (blockA.getTail() != stmtA) {
        // if StmtA is not at the end of the block -> it needs to branch to reach StmtB
        // if StmtA branches -> it must at the end of a block
        throw new IllegalArgumentException(
            "StmtA is neither a BranchingStmt nor at the end of a block but it must be to reach StmtB.");
      }
    }

    if (blockA.getSuccessors().size() >= stmtA.getSuccessorCount()) {
      throw new IllegalArgumentException(
          "Can't add another flow - there are already enough flows i.e. "
              + stmtA.getSuccessorCount()
              + " outgoing from StmtA "
              + stmtA);
    }

    if (stmtA.branches()) {
      // branching A indicates the end of BlockA and connects to another BlockB: reuse or create new
      // one
      if (blockBIdx == null) {
        blockB = new MutableBasicBlock();
        blockBIdx = blocks.size();
        blocks.add(blockB);
        blockB.addStmt(stmtB);
        stmtToBlock.put(stmtB, blockBIdx);
        blockB.addPredecessorBlock(blockA);
        blockA.addSuccessorBlock(blockB);

      } else {
        blockB = blocks.get(blockBIdx);

        if (blockB.getHead() == stmtB) {
          // stmtB is at the beginning of the second Block -> connect blockA and blockB
          blockB.addPredecessorBlock(blockA);
          blockA.addSuccessorBlock(blockB);
        } else {

          MutableBasicBlock newBlock = blockB.splitBlockLinked(stmtB, true);
          int newBlockIdx = blocks.size();
          blocks.add(newBlock);
          newBlock.getStmts().forEach(stmt -> stmtToBlock.put(stmt, newBlockIdx));

          if (blockA == blockB) {
            // successor of block is the origin: end of block flows to beginning of new splitted
            // block (i.e.
            // the same block)
            newBlock.addSuccessorBlock(newBlock);
            newBlock.addPredecessorBlock(newBlock);
          } else {
            blockA.addSuccessorBlock(newBlock);
            newBlock.addPredecessorBlock(blockA);
          }
        }
      }

    } else {
      // stmtA does not branch
      if (blockBIdx == null) {
        // stmtB is new in the graph -> just add it to the same block
        addNodeToBlock(blockA, blockAIdx, stmtB);
      } else {
        blockB = blocks.get(blockBIdx);
        if (blockB.getHead() == stmtB) {
          // stmtB is at the beginning of the second Block -> connect blockA and blockB?
          // is stmtB already a branch target?
          // FIXME: do their blocks have the same traps?
          if (blockB.getPredecessors().isEmpty()) {
            // merge and remove now obsolete Block B
            Integer finalBlockAIdx = blockAIdx;
            blockB
                .getStmts()
                .forEach(
                    stmt -> {
                      blockA.addStmt(stmt);
                      stmtToBlock.put(stmt, finalBlockAIdx);
                    });
            // dont remove the block slot otherwise we need to update all higher indices to reflect
            // the new index positions
            blocks.set(blockBIdx, null);

            // TODO: hint: [ms] for serialisation we need to validate that n-1 predecessors are
            // branching stmts.
          } else {
            // stmtA does not branch but stmtB is already a branch target => link blocks
            blockA.addSuccessorBlock(blockB);
            blockB.addPredecessorBlock(blockA);
          }
        } else {
          throw new IllegalArgumentException(
              "Stmt is already in the Graph: a) remove StmtB or b) StmtA must be a branching Stmt that branches to StmtB ");
        }
      }
    }
  }

  @Override
  public void removeEdge(@Nonnull Stmt from, @Nonnull Stmt to) {
    Integer blockOfFromIdx = stmtToBlock.get(from);
    removeEdgeInternal(from, to, blockOfFromIdx);
  }

  protected boolean removeEdgeInternal(
      @Nonnull Stmt from, @Nonnull Stmt to, @Nonnull Integer blockIdxOfFrom) {
    MutableBasicBlock blockOfFrom = blocks.get(blockIdxOfFrom);
    MutableBasicBlock blockOfTo = blocks.get(stmtToBlock.get(to));

    boolean ret = removeBlockBorderEdgesInternal(from, blockOfFrom, blockIdxOfFrom);

    // divide block if from and to are from the same block
    if (blockOfFrom == blockOfTo) {
      // divide block and don't link them
      final List<Stmt> stmtsOfBlock = blockOfFrom.getStmts();
      int fromIdx = stmtsOfBlock.indexOf(from);
      if (stmtsOfBlock.get(fromIdx + 1) == to) {
        MutableBasicBlock newBlock = blockOfFrom.splitBlockUnlinked(from, to);
        Integer newBlockIdx = blocks.size();
        blocks.add(newBlock);
        newBlock.getStmts().forEach(s -> stmtToBlock.put(s, newBlockIdx));
        return true;
      } else {
        return false;
        // throw new IllegalArgumentException("Can't seperate the flow from '"+from+"' to '"+to+"'.
        // The Stmts are not connected in this graph!");
      }
    }

    return ret;
  }

  protected boolean removeBlockBorderEdgesInternal(
      @Nonnull Stmt from, @Nonnull MutableBasicBlock blockOfFrom, @Nonnull Integer blockIdxOfFrom) {
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
                      stmtToBlock.put(k, blockIdxOfFrom);
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
                        stmtToBlock.put(k, blockIdxOfFrom);
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
  public void setEdges(@Nonnull Stmt from, @Nonnull List<Stmt> to) {
    Integer fromBlockIdx = stmtToBlock.get(from);
    if (fromBlockIdx != null) {
      // cleanup existing edges
      successors(from).forEach(succ -> removeEdge(from, succ));
    }
    to.forEach(target -> putEdge(from, target));
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
  public MutableBasicBlock getStartingStmtBlock() {
    return getBlockOf(startingStmt);
  }

  @Override
  public MutableBasicBlock getBlockOf(@Nonnull Stmt stmt) {
    final Integer blockIdx = stmtToBlock.get(stmt);
    if (blockIdx == null) {
      return null;
    }
    return blocks.get(blockIdx);
  }

  @Nonnull
  @Override
  public StmtGraph<?> unmodifiableStmtGraph() {
    return new ForwardingStmtGraph<>(this);
  }

  public void setStartingStmt(@Nonnull Stmt startingStmt) {
    if (stmtToBlock.get(startingStmt) == null) {
      Integer blockIdx = stmtToBlock.get(startingStmt);
      if (blockIdx == null) {
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
    Integer blockIdx = stmtToBlock.get(node);
    if (blockIdx == null) {
      throw new IllegalArgumentException("Stmt is not contained in the BlockStmtGraph: " + node);
    }
    MutableBasicBlock block = blocks.get(blockIdx);

    if (node == block.getHead()) {
      List<MutableBasicBlock> predecessorBlocks = block.getPredecessors();
      // TODO: [ms] maybe dont copy
      List<Stmt> preds = new ArrayList<>(predecessorBlocks.size());
      predecessorBlocks.forEach(p -> preds.add(p.getTail()));
      return preds;
    } else {
      // argh indexOf.. possibly expensive..
      List<Stmt> stmts = block.getStmts();
      return Collections.singletonList(stmts.get(stmts.indexOf(node) - 1));
    }
  }

  @Nonnull
  @Override
  public List<Stmt> exceptionalPredecessors(@Nonnull Stmt node) {

    final Integer blockIdx = stmtToBlock.get(node);
    if (blockIdx == null) {
      throw new IllegalArgumentException("Stmt is not in the StmtGraph.");
    }
    BasicBlock currentBlock = blocks.get(blockIdx);

    if (currentBlock.getHead() != node
        || !(node instanceof JIdentityStmt
            && ((JIdentityStmt<?>) node).getRightOp() instanceof JCaughtExceptionRef)) {
      // only an exception handler stmt can have exceptional predecessors
      return Collections.emptyList();
    }

    List<Stmt> exceptionalPred = new ArrayList<>();
    for (BasicBlock block : getBlocks()) {
      if (block.getExceptionalSuccessors().containsValue(currentBlock)) {
        exceptionalPred.addAll(block.getStmts());
      }
    }
    return exceptionalPred;
  }

  @Nonnull
  @Override
  public List<Stmt> successors(@Nonnull Stmt node) {
    Integer blockIdx = stmtToBlock.get(node);
    if (blockIdx == null) {
      throw new IllegalArgumentException("Stmt is not contained in the BlockStmtGraph: " + node);
    }
    MutableBasicBlock block = blocks.get(blockIdx);

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
    Integer blockIdx = stmtToBlock.get(node);
    if (blockIdx == null) {
      throw new IllegalArgumentException("Stmt is not contained in the BlockStmtGraph: " + node);
    }
    MutableBasicBlock block = blocks.get(blockIdx);
    Map<ClassType, Stmt> map = new HashMap<>();
    for (Map.Entry<ClassType, MutableBasicBlock> b : block.getExceptionalSuccessors().entrySet()) {
      map.put(b.getKey(), b.getValue().getHead());
    }
    return map;
  }

  @Override
  public int inDegree(@Nonnull Stmt node) {
    Integer blockIdx = stmtToBlock.get(node);
    if (blockIdx == null) {
      throw new IllegalArgumentException("Stmt is not contained in the BlockStmtGraph: " + node);
    }
    MutableBasicBlock block = blocks.get(blockIdx);

    if (node == block.getHead()) {
      return block.getPredecessors().size();
    } else {
      return 1;
    }
  }

  @Override
  public int outDegree(@Nonnull Stmt node) {
    Integer blockIdx = stmtToBlock.get(node);
    if (blockIdx == null) {
      throw new IllegalArgumentException("Stmt is not contained in the BlockStmtGraph: " + node);
    }
    MutableBasicBlock block = blocks.get(blockIdx);

    if (node == block.getTail()) {
      return block.getSuccessors().size();
    } else {
      return 1;
    }
  }

  @Override
  public boolean hasEdgeConnecting(@Nonnull Stmt source, @Nonnull Stmt target) {
    Integer blockAIdx = stmtToBlock.get(source);
    if (blockAIdx == null) {
      throw new IllegalArgumentException(
          "source Stmt is not contained in the BlockStmtGraph: " + source);
    }

    MutableBasicBlock blockA = blocks.get(blockAIdx);

    if (source == blockA.getTail()) {
      Integer blockBIdx = stmtToBlock.get(source);
      if (blockBIdx == null) {
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

  @Override
  public List<Trap> getTraps() {
    List<Trap> traps = new ArrayList<>();

    Map<ClassType, List<MutableBasicBlock>> traphandler = new HashMap<>();
    Map<MutableBasicBlock, List<MutableBasicBlock>> trappedBlocks = new HashMap<>();

    // collect all traps from the blocks
    for (MutableBasicBlock block : blocks) {
      block
          .getExceptionalSuccessors()
          .forEach(
              (type, trapBlock) -> {
                // FIXME: Tail is not exclusive ! trapendRange is exclusive!
                traps.add(new Trap(type, block.getHead(), block.getTail(), trapBlock.getHead()));
                // trappedBlocks.put(type, trapBlock);
              });
    }

    // TODO: merge traps where possible

    return traps;
  }

  @Nonnull
  public Iterator<Stmt> iterator() {
    return new BlockStmtGraphIterator<>(this);
  }

  /** Iterates the Stmts according to the jimple output order. */
  private static class BlockStmtGraphIterator<
          MutableBasicBlock extends BasicBlock<MutableBasicBlock>>
      implements Iterator<Stmt> {

    private final BlockGraphIterator<MutableBasicBlock> blockIt;
    @Nonnull private Iterator<Stmt> currentBlockIt = Collections.emptyIterator();

    public BlockStmtGraphIterator(@Nonnull StmtGraph<MutableBasicBlock> graph) {
      blockIt = new BlockGraphIterator<>(graph);
    }

    @Override
    public boolean hasNext() {
      // hint: a BasicBlock has at least 1 Stmt!
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
  public static class BlockGraphIteratorAndTrapCollector<
          MutableBasicBlock extends BasicBlock<MutableBasicBlock>>
      extends BlockGraphIterator<MutableBasicBlock> {

    @Nonnull private final List<Trap> collectedTraps = new ArrayList<>();

    Map<ClassType, Stmt> trapStarts = new HashMap<>();
    MutableBasicBlock lastBlock = null;

    public BlockGraphIteratorAndTrapCollector(@Nonnull StmtGraph<MutableBasicBlock> graph) {
      super(graph);
    }

    @Nonnull
    @Override
    public MutableBasicBlock next() {
      final MutableBasicBlock block = super.next();

      final Map<? extends ClassType, MutableBasicBlock> currentBlocksExceptions =
          block.getExceptionalSuccessors();
      // former trap info is not in the block blocks info? -> add it to the trap collection
      final Map<? extends ClassType, MutableBasicBlock> lastBlocksExceptionalSuccessors;
      if (lastBlock != null) {
        lastBlocksExceptionalSuccessors = lastBlock.getExceptionalSuccessors();

        lastBlocksExceptionalSuccessors.forEach(
            (type, trapHandlerBlock) -> {
              final MutableBasicBlock mutableBasicBlock = currentBlocksExceptions.get(type);
              if (mutableBasicBlock == null) {
                final Stmt remove = trapStarts.remove(type);
                if (remove == null) {
                  throw new IllegalStateException(
                      "Trap start for '" + type + "' is not in the Map!");
                }
                collectedTraps.add(
                    new Trap(type, remove, lastBlock.getTail(), trapHandlerBlock.getHead()));
              }
            });
      } else {
        lastBlocksExceptionalSuccessors = Collections.emptyMap();
      }

      // theres a new trap in this block? add it to currentTraps
      block
          .getExceptionalSuccessors()
          .forEach(
              (type, trapHandlerBlock) -> {
                if (!lastBlocksExceptionalSuccessors.containsKey(type)) {
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
            final MutableBasicBlock mutableBasicBlock =
                lastBlock.getExceptionalSuccessors().get(type);
            if (mutableBasicBlock == null) {
              final Stmt remove = trapStarts.remove(type);
              if (remove == null) {
                throw new IllegalStateException(
                    "No matching Trap info found for '"
                        + type
                        + "' in ExceptionalSucessors() of the last iterated Block!");
              }
              collectedTraps.add(new Trap(type, remove, lastBlock.getTail(), trapStart));
            }
          });
      return collectedTraps;
    }
  }

  /** Iterates over the blocks */
  private static class BlockGraphIterator<MutableBasicBlock extends BasicBlock<MutableBasicBlock>>
      implements Iterator<MutableBasicBlock> {

    @Nonnull private final StmtGraph<MutableBasicBlock> graph;

    @Nonnull private final ArrayDeque<MutableBasicBlock> traps = new ArrayDeque<>();

    @Nonnull private final ArrayDeque<MutableBasicBlock> nestedBlocks = new ArrayDeque<>();
    @Nonnull private final ArrayDeque<MutableBasicBlock> otherBlocks = new ArrayDeque<>();

    // caching the next Stmt to implement a simple hasNext() and skipping already returned Stmts
    @Nonnull private final Set<MutableBasicBlock> iteratedBlocks;

    public BlockGraphIterator(@Nonnull StmtGraph<MutableBasicBlock> graph) {
      this.graph = graph;
      final List<MutableBasicBlock> blocks = graph.getBlocks();
      iteratedBlocks = new HashSet<>(blocks.size(), 1);
      Stmt startingStmt = graph.getStartingStmt();
      if (startingStmt != null) {
        final MutableBasicBlock startingBlock = graph.getStartingStmtBlock();
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
        } else if (!traps.isEmpty()) {
          nextBlock = traps.pollFirst();
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
        traps.addLast(trapHandlerBlock);
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
          // predecessor is not a fallsthrough stmt anymore and then the iterator will iterate down
          // from there.
          final MutableBasicBlock successorBlock = successors.get(i);
          MutableBasicBlock leaderOfUnbranchedBlocks = successorBlock;
          boolean anotherCheck;
          do {
            anotherCheck = false;
            final List<MutableBasicBlock> itPreds = leaderOfUnbranchedBlocks.getPredecessors();
            for (MutableBasicBlock pred : itPreds) {
              if (pred.getTail().fallsThrough()
                  && pred.getSuccessors().get(0) == leaderOfUnbranchedBlocks) {
                leaderOfUnbranchedBlocks = pred;
                anotherCheck = true;
                break;
              }
            }
          } while (anotherCheck);

          // find a return Stmt inside the current Block
          Stmt succTailStmt = successorBlock.getTail();
          boolean isReturnBlock =
              succTailStmt instanceof JReturnVoidStmt || succTailStmt instanceof JReturnStmt;

          // remember branching successors
          if (tailStmt instanceof JGotoStmt) {
            if (isReturnBlock) {
              nestedBlocks.removeFirstOccurrence(currentBlock.getHead());
              otherBlocks.addLast(leaderOfUnbranchedBlocks);
            } else {
              otherBlocks.addFirst(leaderOfUnbranchedBlocks);
            }
          } else if (!nestedBlocks.contains(leaderOfUnbranchedBlocks)) {
            // JSwitchStmt, JIfStmt
            if (isReturnBlock) {
              nestedBlocks.addLast(leaderOfUnbranchedBlocks);
            } else {
              nestedBlocks.addFirst(leaderOfUnbranchedBlocks);
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
        final List<MutableBasicBlock> blocks = graph.getBlocks();
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
                  + " Blocks (and their containing Stmts) that are not iterated! The StmtGraph is not connected from its startingStmt!"
                  + info
                  + GraphVizExporter.createUrlToWebeditor(graph));
        }
      }
      return hasIteratorMoreElements;
    }
  }
}
