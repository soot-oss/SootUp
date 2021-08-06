package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MutableBlockStmtGraph implements MutableStmtGraph {

  @Nullable private MutableBasicBlock startingBlock = null;
  @Nonnull private final Map<Stmt, Integer> stmtToBlock = new HashMap<>();
  @Nonnull private final ArrayList<MutableBasicBlock> blocks = new ArrayList<>();
  private List<Trap> traps = null;

  public MutableBlockStmtGraph() {}

  /** copies a StmtGraph into this Mutable instance */
  public MutableBlockStmtGraph(@Nonnull StmtGraph graph) {
    setStartingStmt(graph.getStartingStmt());
    for (Stmt stmt : graph) {
      setEdges(stmt, successors(stmt));
    }
    setTraps(graph.getTraps());
  }

  @Override
  @Nonnull
  // FIXME: return them in post-reverse-order
  public List<? extends BasicBlock> getBlocks() {
    return blocks;
  }

  @Override
  public void addNode(@Nonnull Stmt node) {
    addNodeInternal(node);
  }

  protected MutableBasicBlock addNodeInternal(@Nonnull Stmt stmt) {
    MutableBasicBlock block = new MutableBasicBlock();
    int idx = blocks.size();
    blocks.add(block);
    return addNodeInternal(block, idx, stmt);
  }

  protected MutableBasicBlock addNodeInternal(
      @Nonnull MutableBasicBlock block, int blockIdx, @Nonnull Stmt stmt) {
    block.addStmt(stmt);
    stmtToBlock.put(stmt, blockIdx);
    return block;
  }

  public void removeNode(@Nonnull Stmt stmt) {
    Integer blockIdx = stmtToBlock.remove(stmt);
    MutableBasicBlock blockOfRemovedStmt = blocks.set(blockIdx, null);

    blockOfRemovedStmt.removeStmt(stmt);
    removeBorderEdgesInternal(stmt, blockIdx);

    // for GC: clear entry in blocks if block is empty -> not referenced anymore -> not reachable
    // for the user
    if (blockOfRemovedStmt.getStmts().size() <= 0) {
      blocks.set(blockIdx, null);
    }
  }

  @Override
  public void replaceNode(@Nonnull Stmt oldStmt, @Nonnull Stmt newStmt) {
    // TODO: [ms] implement it smarter
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
      blockA = addNodeInternal(stmtA);
      blockAIdx = stmtToBlock.get(stmtA);
    } else {
      blockA = blocks.get(blockAIdx);
      if (blockA.getTail() != stmtA) {
        // StmtA is not at the end of its current Block -> needs split
        MutableBasicBlock newBlock = blockA.splitBlockLinked(stmtA, false);
        int newBlockIdx = blocks.size();
        blocks.add(newBlock);
        newBlock.getStmts().forEach(stmt -> stmtToBlock.put(stmt, newBlockIdx));
        blockA = newBlock;
        blockAIdx = newBlockIdx;
      }
    }

    if (stmtA.branches()) {
      // branching A indicates the end of BlockA and connects to another BlockB: reuse or create new
      // one
      if (blockBIdx == null) {
        blockB = new MutableBasicBlock();
        blockBIdx = blocks.size();
        blocks.add(blockB);
      } else {
        blockB = blocks.get(blockBIdx);
      }

      if (blockB.getHead() == stmtB) {
        // stmtB is at the beginning of the second Block ->
        blockB.addPredecessorBlock(blockA);
        blockA.addSuccessorBlock(blockB);
        stmtToBlock.put(stmtB, blockBIdx);
      } else {
        // stmtB is not at the beginning -> split Block: stmtB is head of newly created Block
        MutableBasicBlock newBlock = blockB.splitBlockLinked(stmtB, true);
        int newBlockIdx = blocks.size();
        blocks.add(newBlock);
        newBlock.getStmts().forEach(stmt -> stmtToBlock.put(stmt, newBlockIdx));
        blockA.addSuccessorBlock(newBlock);
        newBlock.addPredecessorBlock(newBlock);
      }
    } else {
      // nonbranchingstmt can live in the same block
      if (blockBIdx == null) {
        addNodeInternal(blockA, blockAIdx, stmtB);
      } else {
        // TODO: check if we can update an edge?
        throw new IllegalStateException(
            "Stmt is already in the Graph - nodes/Stmt's must be unique objects.");
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
    removeBorderEdgesInternal(from, blockIdxOfFrom);

    MutableBasicBlock blockOfFrom = blocks.get(blockIdxOfFrom);
    // not tail or head
    if (!(from == blockOfFrom.getTail() || from == blockOfFrom.getHead())) {
      // divide block and dont link them
      MutableBasicBlock blockOfTo = blocks.get(stmtToBlock.get(to));
      if (blockOfFrom != blockOfTo) {
        throw new IllegalStateException();
      }

      int fromIdx = blockOfFrom.getStmts().indexOf(from);
      if (blockOfFrom.getStmts().get(fromIdx + 1) == to) {
        MutableBasicBlock newBlock = blockOfFrom.splitBlockUnlinked(from, to);
        Integer newBlockIdx = blocks.size();
        blocks.add(newBlock);
        newBlock.getStmts().forEach(s -> stmtToBlock.put(s, newBlockIdx));
      }
    }
  }

  protected void removeBorderEdgesInternal(@Nonnull Stmt from, @Nonnull Integer blockIdxOfFrom) {
    MutableBasicBlock blockOfFrom = blocks.get(blockIdxOfFrom);
    // TODO: is it intuitive to remove connections to the BasicBlock in the case we cant merge the
    // blocks?
    if (from == blockOfFrom.getHead()) {

      if (blockOfFrom.getStmts().size() > 0) {
        // merge previous block if possible i.e. no branchingstmt as tail && same traps
        if (blockOfFrom.getPredecessors().size() == 1) {
          MutableBasicBlock singlePreviousBlock = blockOfFrom.getPredecessors().get(0);
          if (!singlePreviousBlock.getTail().branches()) {
            if (singlePreviousBlock.getTraps().equals(blockOfFrom.getTraps())) {
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
    }

    // remove outgoing connections if stmts is the tail
    if (from == blockOfFrom.getTail()) {

      if (!from.branches()) {
        if (blockOfFrom.getStmts().size() > 0 && blockOfFrom.getSuccessors().size() == 1) {
          // merge previous block if possible i.e. no branchingstmt as tail && same traps && no
          // other predesccorblocks
          MutableBasicBlock singleSuccessorBlock = blockOfFrom.getSuccessors().get(0);
          if (singleSuccessorBlock.getPredecessors().size() == 1
              && singleSuccessorBlock.getPredecessors().get(0) == blockOfFrom) {
            if (singleSuccessorBlock.getTraps().equals(blockOfFrom.getTraps())) {
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
  public void setEdges(@Nonnull Stmt from, @Nonnull List<Stmt> targets) {
    // TODO [ms] implement smart i.e. not remove(old), add(other) ?
    Integer fromBlockIdx = stmtToBlock.get(from);
    if (fromBlockIdx == null) {
      // 'from Stmt' does not exist yet -> create
      addNodeInternal(from);
    } else {
      successors(from).forEach(succ -> removeEdge(from, succ));
    }
    targets.forEach(to -> putEdge(from, to));
  }

  @Nullable
  @Override
  public Stmt getStartingStmt() {
    return startingBlock.getHead();
  }

  @Nonnull
  @Override
  public StmtGraph unmodifiableStmtGraph() {
    return new ForwardingStmtGraph(this);
  }

  public void setStartingStmt(@Nonnull Stmt startingStmt) {
    Integer startingBlockIdx = stmtToBlock.get(startingStmt);
    if (startingBlockIdx != null) {
      // TODO: make sure starting stmt is (and keeps beeing) at the beginning of the block!
      this.startingBlock = blocks.get(startingBlockIdx);
    } else {
      this.startingBlock = addNodeInternal(startingStmt);
    }
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
      MutableBasicBlock blockB = blocks.get(blockBIdx);
      return blockA.getSuccessors().stream()
          .anyMatch(
              successorBlock -> successorBlock == blockB && successorBlock.getHead() == target);
    } else {
      List<Stmt> stmtsA = blockA.getStmts();
      int stmtAIdx = stmtsA.indexOf(source);
      return stmtsA.get(stmtAIdx) == target;
    }
  }

  @Override
  public void setTraps(@Nonnull List<Trap> traps) {
    if (this.traps != null && this.traps.size() > 0) {
      // TODO: remove old trap information and possibly merge blocks if necessary

    }
    this.traps = traps;

    for (Trap trap : traps) {
      // FIXME: implement splitting into basicblocks
      // TODO: find startblock and possibly split
      // TODO: add trap to in between blocks
      // TODO: find endblock and possibly split
    }
  }

  @Nonnull
  @Override
  public List<Trap> getTraps() {
    if (traps == null) {
      Collections.emptyList();
    }
    return traps;
  }
}
