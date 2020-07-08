package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.common.stmt.*;
import java.util.*;
import javax.annotation.Nonnull;

class Block {
  @Nonnull private List<Stmt> blockStmts;

  Block(@Nonnull List<Stmt> stmts) {
    assert (stmts.size() > 0);
    blockStmts = stmts;
  }

  public List<Stmt> getBlockStmts() {
    final List<Stmt> stmts = Collections.unmodifiableList(blockStmts);
    blockStmts = stmts;
    return stmts;
  }

  @Nonnull
  public Stmt getLeadingStmt() {
    return blockStmts.get(0);
  }

  @Nonnull
  public Stmt getTailStmt() {
    return blockStmts.get(blockStmts.size() - 1);
  }

  /** a Block is identified by its Leading Stmt. */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Stmt otherLeadingStmt;
    if (o instanceof Stmt) {
      otherLeadingStmt = (Stmt) o;
    } else {
      Block block = (Block) o;
      otherLeadingStmt = block.getLeadingStmt();
    }
    return Objects.equals(getLeadingStmt(), otherLeadingStmt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getLeadingStmt());
  }

  @Override
  public String toString() {
    return "{" + blockStmts + '}';
  }
}

/** @author Markus Schmidt */
public class BlockGraph {
  @Nonnull private final Map<Block, Integer> blockToIdx;
  @Nonnull private final ArrayList<List<Block>> predecessors;
  @Nonnull private final ArrayList<List<Block>> successors;
  @Nonnull private final Block startingBlock;

  public BlockGraph(StmtGraph stmtGraph) {

    if (stmtGraph.nodes().isEmpty()) {
      throw new RuntimeException("The Graph is emtpy");
    }

    final Stmt startingStmt = stmtGraph.getStartingStmt();
    Set<Stmt> queuedStmts = new HashSet<>(stmtGraph.nodes().size());
    ArrayDeque<Stmt> q = new ArrayDeque<>();
    stmtGraph.getTraps().forEach(t -> q.addFirst(t.getHandlerStmt()));
    q.addFirst(startingStmt);

    ArrayDeque<Stmt> cutQ = new ArrayDeque<>();
    // mark cut positions in blocks
    int maxBlockSize = 0;
    int currBlockSize = 0;

    while (!q.isEmpty()) {
      final Stmt stmt = q.pollFirst();

      // add successor(s) to working q
      final List<Stmt> successorList = stmtGraph.successors(stmt);
      for (Stmt succ : successorList) {
        if (!queuedStmts.contains(succ)) {
          q.addFirst(succ);
          queuedStmts.add(succ);
        }
      }
      currBlockSize++;

      // remember cut position: add the tail stmt of a block as marker
      if (stmtGraph.predecessors(stmt).size() != 1
          || stmtGraph.predecessors(stmt).size() == 1
              && stmtGraph.predecessors(stmt).get(0) instanceof BranchingStmt) {
        // detect block entrypoint (startingStmt,trapHandler) or target of a branching stmt
        if (q.peekFirst() != null) {
          cutQ.addLast(q.peekFirst());
        }
        if (currBlockSize > maxBlockSize) {
          maxBlockSize = currBlockSize;
          currBlockSize = 0;
        }
      } else if (stmt instanceof BranchingStmt
          || stmtGraph.successors(stmt).size() == 0
          || stmt instanceof JInvokeStmt
          || stmt instanceof JAssignStmt
              && (((JAssignStmt) stmt).getRightOp() instanceof AbstractInvokeExpr
                  || ((JAssignStmt) stmt).getRightOp() instanceof AbstractInstanceInvokeExpr)) {
        // detect block tail
        // end stmt branches, is return|throw or is method call which possibly could throw an
        // exception
        cutQ.addLast(stmt);
        if (currBlockSize > maxBlockSize) {
          maxBlockSize = currBlockSize;
          currBlockSize = 0;
        }
      }
    }

    if (cutQ.isEmpty()) {
      throw new RuntimeException("There are no Blocks to cut.");
    }

    // initialize container sizes
    final int blockCount = cutQ.size();
    blockToIdx = new HashMap<>(blockCount);
    predecessors = new ArrayList<>(blockCount);
    successors = new ArrayList<>(blockCount);

    // block cutting - aka "we are cutting ice" ;)
    Block[] blockArray = new Block[blockCount];
    ArrayList<Stmt> currentBlock = new ArrayList<>(maxBlockSize);
    stmtGraph.getTraps().forEach(t -> q.addFirst(t.getHandlerStmt()));
    q.addFirst(cutQ.pollFirst()); // move startingStmt into workqueue
    while (!q.isEmpty()) {
      final Stmt stmt = q.pollFirst();

      final List<Stmt> successorList = stmtGraph.successors(stmt);
      for (Stmt succ : successorList) {
        if (!queuedStmts.contains(succ)) {
          q.addFirst(succ);
          queuedStmts.add(succ);
        }
      }
      queuedStmts.add(stmt);
      currentBlock.add(stmt);

      // is last stmt of a block -> cut
      if (cutQ.peekFirst() == stmt) {
        cutQ.pollFirst();
        final int newIdx = blockToIdx.size();
        final Block block = new Block(new ArrayList<>(currentBlock));
        blockToIdx.put(block, newIdx);
        blockArray[newIdx] = block;
        currentBlock.clear();
      }
    }

    // set predecessors/successors
    for (int i = 0, size = blockArray.length; i < size; i++) {
      Block b = blockArray[i];
      final List<Stmt> tailSuccessors = stmtGraph.successors(b.getTailStmt());
      List<Block> blockSuccessors = new ArrayList<>(tailSuccessors.size());
      for (Stmt succ : tailSuccessors) {
        //noinspection SuspiciousMethodCalls
        final Integer successorIdx = blockToIdx.get(succ);
        final Block successorBlock = blockArray[successorIdx];
        blockSuccessors.add(successorBlock);

        List<Block> blockPredecessors = predecessors.get(successorIdx);
        if (blockPredecessors == null) {
          blockPredecessors =
              new ArrayList<>(stmtGraph.predecessors(successorBlock.getLeadingStmt()).size());
          predecessors.set(successorIdx, blockPredecessors);
        }
        blockPredecessors.add(successorBlock);
      }
      successors.set(i, blockSuccessors);
    }

    startingBlock = blockArray[0];
  }

  Iterator<Stmt> stmtIterator() {
    BlockGraph graph = this;

    return new Iterator<Stmt>() {
      private Block currentBlock = getStartingBlock();
      int currentBlockIdx = 0;

      private final ArrayDeque<Block> blockQ = new ArrayDeque<>();

      @Override
      public boolean hasNext() {
        if (!blockQ.isEmpty()) {
          return true;
        }
        return false;
      }

      @Override
      public Stmt next() {
        return null;
      }
    };
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    for (Map.Entry<Block, Integer> b : blockToIdx.entrySet()) {
      sb.append(b.getValue()).append(':').append('\n').append(b.getKey()).append('\n');
      sb.append("=> ");
      successors.get(b.getValue()).forEach(succ -> sb.append(blockToIdx.get(succ)).append(", "));
      sb.append('\n');
    }

    return sb.toString();
  }

  @Nonnull
  Block getStartingBlock() {
    return startingBlock;
  }

  @Nonnull
  public List<Block> predecessors(Block b) {
    Integer idx = blockToIdx.get(b);
    if (idx == null) {
      throw new NoSuchElementException("The given Block does not exist in this BlockGraph.");
    }
    return predecessors.get(idx);
  }

  @Nonnull
  public List<Block> successors(Block b) {
    Integer idx = blockToIdx.get(b);
    if (idx == null) {
      throw new NoSuchElementException("The given Block does not exist in this BlockGraph.");
    }
    return successors.get(idx);
  }

  public int getBlockCount() {
    return blockToIdx.size();
  }
}
