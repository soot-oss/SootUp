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
  @Nonnull private final Map<Stmt, MutableBasicBlock> stmtToBlock = new HashMap<>();

  @Nonnull private final Set<MutableBasicBlock> blocks = new HashSet<>();

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

      // possibly bad performance.. O(n * m)... sort it once: use some kind of -> O(1)*n with 2*m
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
    final MutableBasicBlock block = stmtToBlock.get(stmt);
    if (block == null) {
      return;
    }

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
  public void removeExceptionalEdge(@Nonnull Stmt node, @Nonnull ClassType exceptionType) {
    final MutableBasicBlock block = stmtToBlock.get(node);
    if (block == null) {
      return;
    }
    block.removeExceptionalSuccessorBlock(exceptionType);

    // FIXME: possibly merge this block if possible
    throw new IllegalArgumentException("cant handle trap removal yet for numberOfSuccessors");
  }

  @Override
  public void clearExceptionalEdges(@Nonnull Stmt node) {
    final MutableBasicBlock block = stmtToBlock.get(node);
    if (block == null) {
      return;
    }
    block.clearExceptionalSuccessorBlocks();

    // FIXME: possibly merge this block if possible
    throw new IllegalArgumentException("cant handle trap removal yet for numberOfSuccessors");
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
    final Iterator<Stmt> iterator = stmts.iterator();
    if (!iterator.hasNext()) {
      return;
    }
    final Stmt node = iterator.next();
    MutableBasicBlock block = createStmtsBlock(node);
    iterator.forEachRemaining(stmt -> addNodeToBlock(block, stmt));
  }

  @Override
  public void addNode(@Nonnull Stmt stmt, @Nonnull Map<ClassType, Stmt> exceptions) {
    MutableBasicBlock block = stmtToBlock.get(stmt);
    if (block == null) {
      // Stmt does not exist in the graph -> create
      block = createStmtsBlock(stmt);
    }

    // if no exceptions are associated with the (now) existing block: just add them
    if (block.getExceptionalSuccessors().size() == 0) {
      if (exceptions.size() > 0) {
        if (block.getStmtCount() != 1) {
          final MutableBasicBlock secondBlock = block.splitBlockLinked(stmt, true);
          int secondBlockIdx = blocks.size();
          blocks.add(secondBlock);
          if (secondBlock.getStmtCount() > 1) {
            final MutableBasicBlock thirdBlock = secondBlock.splitBlockLinked(stmt, false);
            blocks.add(thirdBlock);
            thirdBlock.getStmts().forEach(node -> stmtToBlock.put(node, thirdBlock));
          }
          secondBlock.getStmts().forEach(node -> stmtToBlock.put(node, secondBlock));
        }

        for (Map.Entry<ClassType, Stmt> exceptionMap : exceptions.entrySet()) {
          MutableBasicBlock targetBlock = stmtToBlock.get(exceptionMap.getValue());
          if (targetBlock == null) {
            targetBlock = createStmtsBlock(exceptionMap.getValue());
          }
          targetBlock.addExceptionalSuccessorBlock(exceptionMap.getKey(), targetBlock);
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
              "Cant intuitively handle adding/merging exceptions to an already existing stmt with already assigned exceptions into the graph.");
        }
      }
    }
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
    return stmtToBlock.putIfAbsent(stmt, block);
  }

  public void removeNode(@Nonnull Stmt stmt) {

    // do edges from or to this node exist? remove them
    // TODO: [ms] implement more performant solution based on datastructure implications
    predecessors(stmt).forEach(p -> removeEdge(p, stmt));
    successors(stmt).forEach(s -> removeEdge(stmt, s));

    MutableBasicBlock blockOfRemovedStmt = stmtToBlock.remove(stmt);
    if (blockOfRemovedStmt == null) {
      return;
    }

    blockOfRemovedStmt.removeStmt(stmt);

    if (blockOfRemovedStmt.getStmts().size() <= 0) {
      blocks.remove(blockOfRemovedStmt);
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

    MutableBasicBlock blockA = stmtToBlock.get(stmtA);
    MutableBasicBlock blockB = stmtToBlock.get(stmtB);

    if (blockA == null) {
      // stmtA is is not in the graph (i.e. no reference to BlockA) -> create
      blockA = createStmtsBlock(stmtA);
    } else {
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
      if (blockB == null) {
        blockB = new MutableBasicBlock();
        blocks.add(blockB);
        blockB.addStmt(stmtB);
        stmtToBlock.put(stmtB, blockB);
        blockB.addPredecessorBlock(blockA);
        blockA.addSuccessorBlock(blockB);

      } else {
        if (blockB.getHead() == stmtB) {
          // stmtB is at the beginning of the second Block -> connect blockA and blockB
          blockB.addPredecessorBlock(blockA);
          blockA.addSuccessorBlock(blockB);
        } else {

          MutableBasicBlock newBlock = blockB.splitBlockLinked(stmtB, true);
          blocks.add(newBlock);
          newBlock.getStmts().forEach(stmt -> stmtToBlock.put(stmt, newBlock));

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
      if (blockB == null) {
        // stmtB is new in the graph -> just add it to the same block
        addNodeToBlock(blockA, stmtB);
      } else {
        if (blockB.getHead() == stmtB) {
          // stmtB is at the beginning of the second Block -> connect blockA and blockB?
          // is stmtB already a branch target?
          // FIXME: do their blocks have the same traps?
          if (blockB.getPredecessors().isEmpty()) {
            // merge and remove now obsolete Block B
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
    removeEdgeInternal(from, to);
  }

  protected boolean removeEdgeInternal(@Nonnull Stmt from, @Nonnull Stmt to) {
    MutableBasicBlock blockOfFrom = stmtToBlock.get(from);
    MutableBasicBlock blockOfTo = stmtToBlock.get(to);

    boolean ret = removeBlockBorderEdgesInternal(from, blockOfFrom);

    // divide block if from and to are from the same block
    if (blockOfFrom == blockOfTo) {
      // divide block and don't link them
      final List<Stmt> stmtsOfBlock = blockOfFrom.getStmts();
      int fromIdx = stmtsOfBlock.indexOf(from);
      if (stmtsOfBlock.get(fromIdx + 1) == to) {
        MutableBasicBlock newBlock = blockOfFrom.splitBlockUnlinked(from, to);
        Integer newBlockIdx = blocks.size();
        blocks.add(newBlock);
        newBlock.getStmts().forEach(s -> stmtToBlock.put(s, newBlock));
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
  public void setEdges(@Nonnull Stmt from, @Nonnull List<Stmt> to) {
    MutableBasicBlock fromBlock = stmtToBlock.get(from);
    if (fromBlock != null) {
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
      return Collections.singletonList(stmts.get(stmts.indexOf(node) - 1));
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

  /** hint: little expensive getter - its more of a build/create */
  @Override
  public List<Trap> getTraps() {
    // [ms] try to incorporate it into the serialisation of jimple printing so the other half of
    // iteration information is not wasted..
    BlockGraphIteratorAndTrapAggregator it = new BlockGraphIteratorAndTrapAggregator();
    // it.getTraps() is valid/completely build when the iterator is done.
    while (it.hasNext()) {
      it.next();
    }
    return it.getTraps();
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
            if (trapHandlerBlock != currentBlocksExceptions.get(type)) {
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
                  final Stmt overriddenStmt = trapStarts.put(type, block.getHead());
                  if (overriddenStmt != null) {
                    throw new IllegalStateException("weird state!");
                  }
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
            // FIXME: what happens if the trapEnd is AFTER the last stmt i.e. covers until the end
            // -> trapENd is exclusive..?!
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
        final MutableBasicBlock startingBlock = getStartingStmtBlock();
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
          boolean andAgain;
          do {
            andAgain = false;
            final List<MutableBasicBlock> itPreds = leaderOfFallsthroughBlocks.getPredecessors();
            for (MutableBasicBlock pred : itPreds) {
              if (pred.getTail().fallsThrough()
                  && pred.getSuccessors().get(0) == leaderOfFallsthroughBlocks) {
                leaderOfFallsthroughBlocks = pred;
                andAgain = true;
                break;
              }
            }
          } while (andAgain);

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
