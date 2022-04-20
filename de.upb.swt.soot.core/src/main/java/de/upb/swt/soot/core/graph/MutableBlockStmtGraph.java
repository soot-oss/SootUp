package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.ref.JCaughtExceptionRef;
import de.upb.swt.soot.core.jimple.common.stmt.BranchingStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JIdentityStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.types.ClassType;
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
  public MutableBlockStmtGraph(@Nonnull StmtGraph<?> graph) {
    setStartingStmt(graph.getStartingStmt());
    for (Stmt stmt : graph) {
      setEdges(stmt, successors(stmt));
    }

    // FIXME: add traps
    // FIXME: add

  }

  /**
   * Creates a Graph representation from the 'legacy' representation i.e. a List of Stmts and Traps.
   */
  public static MutableStmtGraph from(
      @Nonnull List<Stmt> stmts,
      @Nonnull Map<BranchingStmt, List<Stmt>> branchingMap,
      @Nonnull List<Trap> traps) {
    final MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    if (stmts.isEmpty()) {
      return graph;
    }

    graph.setStartingStmt(stmts.get(0));

    Map<ClassType, Stmt> currentTrapMap = new HashMap<>();
    for (int i = 0, stmtsSize = stmts.size(); i < stmtsSize; i++) {
      Stmt stmt = stmts.get(i);

      // FIXME: bad performance.. O(n * m)... sort it once: use some kind of -> O(1)*n with 2*m
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

      // if(trapsChanged) => new Block
      graph.addNode(stmt, currentTrapMap);

      if (stmt.fallsThrough()) {
        // bad performance if not stmts is not instanceof RandomAccess
        if (i >= stmts.size()) {
          throw new IllegalArgumentException(
              "Theres a fallsthrough Stmt ('"
                  + stmt
                  + "') which has no follower - so it can't fall through.");
        }
        graph.putEdge(stmt, stmts.get(i + 1));
      }

      if (stmt.branches()) {
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
          // a possible fallsthrough is not in branchingMap
          graph.putEdge(stmt, target);
        }
      }
    }

    return graph;
  }

  public static MutableStmtGraph from(@Nonnull Collection<MutableBasicBlock> blocks) {
    final MutableBlockStmtGraph graph = new MutableBlockStmtGraph();

    final HashSet<MutableBasicBlock> checkExists = new HashSet<>();
    for (MutableBasicBlock block : blocks) {
      block.getExceptionalSuccessors().values().forEach(exb -> checkExists.add(exb));
      block.getSuccessors().forEach(exb -> checkExists.add(exb));
    }

    for (BasicBlock block : blocks) {
      if (block.isEmpty()) {
        return null;
      }

      //

      if (true) {
        // FIXME
      }
    }

    graph.blocks.addAll(blocks);
    return graph;
  }

  @Override
  public void clearExceptionalEdges(@Nonnull Stmt stmt) {
    // FIXME implement
    throw new IllegalArgumentException("cant handle trap removal yet.");
  }

  @Override
  public void addExceptionalEdge(
      @Nonnull Stmt stmt, @Nonnull ClassType exceptionType, @Nonnull Stmt traphandlerStmt) {
    // FIXME implement
    throw new IllegalArgumentException("cant handle trap removal yet.");
  }

  @Override
  public void removeExceptionalEdge(@Nonnull Stmt stmt, @Nonnull ClassType exceptionType) {
    // FIXME implement
    throw new UnsupportedOperationException("cant handle trap removal yet.");
  }

  @Override
  @Nonnull
  public List<MutableBasicBlock> getBlocks() {
    return blocks.stream().filter(Objects::nonNull).collect(Collectors.toList());
  }

  /**
   * caution: handle direct manipulation of MutableBasicBlocks after calling addBlock(..) with care!
   * those changes are not necessarily reflected in the graphs datastructure.
   */
  @Override
  public void addBlock(@Nonnull MutableBasicBlock block) {
    // check if the block is already existing in blocks
    if (blocks.contains(block)) {
      return;
    }

    if (block.isEmpty()) {
      throw new IllegalArgumentException("You cannot add an empty Block.");
    }

    int newIdx = blocks.size();
    blocks.add(block);
    for (Stmt stmt : block.getStmts()) {
      final Integer exists = stmtToBlock.put(stmt, newIdx);
      if (exists != null) {
        // duplicate stmt in the same block?
        throw new IllegalArgumentException(
            "Stmt \"" + stmt + "\" of the given Block exists already in the graph!");
      }
    }

    // TODO: check predecessorBlocks/successorBlocks ?

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

      for (Map.Entry<ClassType, Stmt> exceptionMap : exceptions.entrySet()) {
        Integer targetBlockIdx = stmtToBlock.get(exceptionMap.getValue());
        if (targetBlockIdx == null) {
          targetBlockIdx = createStmtsBlock(exceptionMap.getValue());
        }
        blocks
            .get(blockIdx)
            .addExceptionalSuccessorBlock(exceptionMap.getKey(), blocks.get(targetBlockIdx));
      }

    } else {
      // check if parameter exceptions are equal to those in the Block of the existing Stmt:
      // otherwise fail.
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

  protected int createStmtsBlock(@Nonnull Stmt stmt) {
    // add Block to graph, add+register Stmt to Block
    MutableBasicBlock block = new MutableBasicBlock();
    final int idx = blocks.size();
    blocks.add(block);
    addNodeToBlock(block, idx, stmt);
    return idx;
  }

  protected void addNodeToBlock(
      @Nonnull MutableBasicBlock block, int blockIdx, @Nonnull Stmt stmt) {
    block.addStmt(stmt);
    stmtToBlock.put(stmt, blockIdx);
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

  protected void removeEdgeInternal(
      @Nonnull Stmt from, @Nonnull Stmt to, @Nonnull Integer blockIdxOfFrom) {
    MutableBasicBlock blockOfFrom = blocks.get(blockIdxOfFrom);
    MutableBasicBlock blockOfTo = blocks.get(stmtToBlock.get(to));
    removeBlockBorderEdgesInternal(from, blockOfFrom, blockIdxOfFrom);

    // divide block if from and to are from the same block
    if (blockOfFrom == blockOfTo) {
      // divide block and don't link them
      int fromIdx = blockOfFrom.getStmts().indexOf(from);
      if (blockOfFrom.getStmts().get(fromIdx + 1) == to) {
        MutableBasicBlock newBlock = blockOfFrom.splitBlockUnlinked(from, to);
        Integer newBlockIdx = blocks.size();
        blocks.add(newBlock);
        newBlock.getStmts().forEach(s -> stmtToBlock.put(s, newBlockIdx));
      } else {
        // TODO: check: do we need to do sth here? i.e. in the case that: ...
      }
    }
  }

  protected void removeBlockBorderEdgesInternal(
      @Nonnull Stmt from, @Nonnull MutableBasicBlock blockOfFrom, @Nonnull Integer blockIdxOfFrom) {
    // TODO: is it intuitive to remove connections to the BasicBlock in the case we cant merge the
    // blocks?

    // merge previous block if possible i.e. previous block has no branchingstmt as tail && same
    // traps
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
          }
        }
      }
    }

    // remove outgoing connections from block if from stmt is the tail
    if (from == blockOfFrom.getTail()) {

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
            }
          }
        }
      } else {
        blockOfFrom.clearSuccessorBlocks();
      }
    }
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
}
