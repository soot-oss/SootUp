package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MutableBlockStmtGraph implements MutableStmtGraph {

  @Nonnull private MutableBasicBlock startingBlock = new MutableBasicBlock();
  @Nonnull private final Map<Stmt, MutableBasicBlock> stmtsToBlock = new HashMap<>();
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
  public Collection<BasicBlock> getBlocks() {
    return new LinkedHashSet<>(stmtsToBlock.values());
  }

  @Override
  public void addNode(@Nonnull Stmt node) {
    addNodeInternal(node);
  }

  protected MutableBasicBlock addNodeInternal(@Nonnull Stmt stmt) {
    MutableBasicBlock block = new MutableBasicBlock();
    stmtsToBlock.put(stmt, block);
    return block;
  }

  public void removeNode(@Nonnull Stmt stmt) {
    MutableBasicBlock blockOfRemovedStmt = stmtsToBlock.remove(stmt);
    blockOfRemovedStmt.removeStmt(stmt);
    removeBorderEdgesInternal(stmt, blockOfRemovedStmt);
  }

  @Override
  public void replaceNode(@Nonnull Stmt oldStmt, @Nonnull Stmt newStmt) {
    // TODO: [ms] implement it smarter
    removeNode(oldStmt);
    addNode(newStmt);
  }

  @Override
  public void putEdge(@Nonnull Stmt stmtA, @Nonnull Stmt stmtB) {

    MutableBasicBlock blockA = stmtsToBlock.get(stmtA);
    MutableBasicBlock blockB = stmtsToBlock.get(stmtB);

    if (blockA == null) {
      // stmtA<->blockA is is not in the graph -> create
      blockA = addNodeInternal(stmtA);
    } else if (blockA.getTail() != stmtA) {
      // StmtA is not at the end of its current BasicBlock -> needs split
      MutableBasicBlock newBlock = blockA.splitBlockLinked(stmtA, false);
      newBlock.getStmts().forEach(stmt -> stmtsToBlock.put(stmt, newBlock));
      blockA = newBlock;
    }

    if (stmtA.branches()) {
      // branching A indicates the end of BlockA and connects to another BlockB: reuse or create new
      // one
      if (blockB == null) {
        blockB = new MutableBasicBlock();
      }

      if (blockB.getHead() == stmtB) {
        blockB.addPredecessorBlock(blockA);
        blockA.addSuccessorBlock(blockB);
        stmtsToBlock.put(stmtB, blockB);
      } else {
        // stmtB is not at the beginning -> split Block so that stmtA is head of second Block
        MutableBasicBlock newBlock = blockB.splitBlockLinked(stmtB, true);
        newBlock.getStmts().forEach(stmt -> stmtsToBlock.put(stmt, newBlock));
        blockA.addSuccessorBlock(newBlock);
        newBlock.addPredecessorBlock(newBlock);
      }
    } else {
      // nonbranchingstmt can live in the same block
      if (blockB == null) {
        blockB = addNodeInternal(stmtB);
      }

      blockA.addStmt(stmtB);
    }

    System.out.println("added");
  }

  @Override
  public void removeEdge(@Nonnull Stmt from, @Nonnull Stmt to) {
    MutableBasicBlock blockOfFrom = stmtsToBlock.get(from);
    removeEdgeInternal(from, to, blockOfFrom);
  }

  protected void removeEdgeInternal(
      @Nonnull Stmt from, @Nonnull Stmt to, @Nonnull MutableBasicBlock blockOfFrom) {
    removeBorderEdgesInternal(from, blockOfFrom);

    // not tail or head
    if (!(from == blockOfFrom.getTail() || from == blockOfFrom.getHead())) {
      // divide block and dont link them
      MutableBasicBlock blockOfTo = stmtsToBlock.get(to);
      if (blockOfFrom != blockOfTo) {
        throw new IllegalStateException();
      }

      int fromIdx = blockOfFrom.getStmts().indexOf(from);
      if (blockOfFrom.getStmts().get(fromIdx + 1) == to) {
        MutableBasicBlock newBlock = blockOfFrom.splitBlockUnlinked(from, to);
        newBlock.getStmts().forEach(s -> stmtsToBlock.put(s, newBlock));
      }
    }
  }

  protected void removeBorderEdgesInternal(
      @Nonnull Stmt from, @Nonnull MutableBasicBlock blockOfFrom) {
    // TODO: is it intuitive to remove connections to the BasicBlock? (if we cant merge the blocks)
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
                        stmtsToBlock.put(k, blockOfFrom);
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
                        stmtsToBlock.put(k, blockOfFrom);
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
    // TODO [ms] implement smart i.e. not remove(old), add(other)
    MutableBasicBlock fromBlock = stmtsToBlock.get(from);
    if (fromBlock == null) {
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
    MutableBasicBlock startingBlock = stmtsToBlock.get(startingStmt);
    if (startingBlock != null) {
      this.startingBlock = startingBlock;
    } else {
      this.startingBlock = addNodeInternal(startingStmt);
      ;
    }
  }

  @Nonnull
  @Override
  public Set<Stmt> nodes() {
    return stmtsToBlock.keySet();
  }

  @Override
  public boolean containsNode(@Nonnull Stmt node) {
    return stmtsToBlock.containsKey(node);
  }

  @Nonnull
  @Override
  public List<Stmt> predecessors(@Nonnull Stmt node) {
    MutableBasicBlock block = stmtsToBlock.get(node);

    if (block == null) {
      throw new IllegalArgumentException("Stmt is not contained in the BlockStmtGraph: " + node);
    }

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
    MutableBasicBlock block = stmtsToBlock.get(node);

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

  @Override
  public int inDegree(@Nonnull Stmt node) {
    MutableBasicBlock block = stmtsToBlock.get(node);

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
    MutableBasicBlock block = stmtsToBlock.get(node);

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
    MutableBasicBlock blockA = stmtsToBlock.get(source);
    if (blockA == null) {
      throw new IllegalArgumentException(
          "source Stmt is not contained in the BlockStmtGraph: " + source);
    }

    if (source == blockA.getTail()) {
      MutableBasicBlock blockB = stmtsToBlock.get(source);
      if (blockB == null) {
        throw new IllegalArgumentException(
            "target Stmt is not contained in the BlockStmtGraph: " + source);
      }
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
    this.traps = traps;
    // FIXME: implement splitting into basicblocks

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
