package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MutableBlockStmtGraph implements StmtGraph {

  @Nonnull private MutableBasicBlock startingBlock = new MutableBasicBlock();

  @Nonnull private final Map<Stmt, MutableBasicBlock> stmtsToBlock = new HashMap<>();

  public MutableBlockStmtGraph() {}

  @Nonnull
  Collection<? extends BasicBlock> getBlocks() {
    return stmtsToBlock.values();
  }

  public MutableBasicBlock addStmt(@Nonnull Stmt stmt) {
    MutableBasicBlock block = new MutableBasicBlock();
    addStmt(stmt, block);
    return block;
  }

  public void addStmt(@Nonnull Stmt stmt, @Nonnull MutableBasicBlock block) {
    stmtsToBlock.put(stmt, block);
  }

  public void addFlow(@Nonnull Stmt stmtA, @Nonnull Stmt stmtB) {

    MutableBasicBlock blockA = stmtsToBlock.get(stmtA);
    MutableBasicBlock blockB = stmtsToBlock.get(stmtB);

    if (blockA == null) {
      // stmtA<->blockA is is not in the graph -> create
      blockA = addStmt(stmtA);
    } else if (blockA.getTail() != stmtA) {
      // StmtA is not at the end of its current BasicBlock -> needs split
      MutableBasicBlock newBlock = blockA.splitBlock(stmtA, false);
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
        MutableBasicBlock newBlock = blockB.splitBlock(stmtB, true);
        newBlock.getStmts().forEach(stmt -> stmtsToBlock.put(stmt, newBlock));
        blockA.addSuccessorBlock(newBlock);
        newBlock.addPredecessorBlock(newBlock);
      }
    } else {
      // nonbranchingstmt can live in the same block
      if (blockB == null) {
        blockB = addStmt(stmtB);
      }

      blockA.addStmt(stmtB);
    }

    System.out.println("added");
  }

  public void removeStmt(@Nonnull Stmt stmt) {
    MutableBasicBlock blockOfRemovedStmt = stmtsToBlock.remove(stmt);
    blockOfRemovedStmt.removeStmt(stmt);

    // TODO: is it intuitive to remove connections to the BasicBlock?
    if (stmt == blockOfRemovedStmt.getHead()) {}

    // remove outgoing connections if stmts is the tail
    if (stmt == blockOfRemovedStmt.getTail()) {
      blockOfRemovedStmt.clearSuccessorBlocks();
    }
  }

  public void replaceStmt(@Nonnull Stmt stmtA, @Nonnull Stmt stmtB) {
    throw new UnsupportedOperationException("Not yet implemented!");
  }

  @Nullable
  @Override
  public Stmt getStartingStmt() {
    return startingBlock.getHead();
  }

  public void setStartingStmt(@Nonnull Stmt startingStmt) {
    MutableBasicBlock startingBlock = stmtsToBlock.get(startingStmt);
    if (startingBlock != null) {
      this.startingBlock = startingBlock;
    } else {
      this.startingBlock = addStmt(startingStmt);
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

  @Nonnull
  @Override
  public List<Trap> getTraps() {
    // FIXME: implement.. collect from BasicBlocks? or use own List?
    throw new UnsupportedOperationException("not yet implemented.");
  }
}
