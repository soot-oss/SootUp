package de.upb.swt.soot.core.graph;

import com.google.common.collect.Lists;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// FIXME: implement
public class ImmutableBlockStmtGraph extends StmtGraph {

  private final List<Stmt> stmts;
  private final List<ImmutableBasicBlock> blocks;

  public ImmutableBlockStmtGraph(@Nonnull StmtGraph graph) {
    stmts = Lists.newArrayListWithExpectedSize(graph.nodes().size());
    // linearize..
    // TODO: add blocks in matching order!
    for (Stmt stmt : graph) {
      stmts.add(stmt);
    }

    // TODO: traps

    throw new IllegalStateException("implement it!");
  }

  @Nullable
  @Override
  public Stmt getStartingStmt() {
    return stmts.isEmpty() ? null : stmts.get(0);
  }

  @Nonnull
  @Override
  public List<Stmt> nodes() {
    return stmts;
  }

  @Nonnull
  @Override
  public List<ImmutableBasicBlock> getBlocks() {
    return blocks;
  }

  @Override
  public boolean containsNode(@Nonnull Stmt node) {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Nonnull
  @Override
  public List<Stmt> predecessors(@Nonnull Stmt node) {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Nonnull
  @Override
  public List<Stmt> successors(@Nonnull Stmt node) {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Nonnull
  @Override
  public List<Stmt> exceptionalSuccessors(@Nonnull Stmt node) {
    // FIXME! implement
    throw new RuntimeException("not implemented");
  }

  @Override
  public int inDegree(@Nonnull Stmt node) {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public int outDegree(@Nonnull Stmt node) {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Override
  public boolean hasEdgeConnecting(@Nonnull Stmt source, @Nonnull Stmt target) {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Nonnull
  @Override
  public List<Trap> getTraps() {
    throw new IllegalStateException("Not implemented yet!");
  }

  private class ImmutableBasicBlock implements BasicBlock {
    private final ImmutableStmtGraph graph;
    private final int startIdx;
    private final int endIdx;
    private final List<ImmutableBasicBlock> successors;
    private final List<ImmutableBasicBlock> predecessors;

    private ImmutableBasicBlock(
        ImmutableStmtGraph graph,
        int startIdx,
        int endIdx,
        List<ImmutableBasicBlock> successors,
        List<ImmutableBasicBlock> predecessors) {
      this.graph = graph;
      this.startIdx = startIdx;
      this.endIdx = endIdx;
      this.successors = successors;
      this.predecessors = predecessors;
    }

    @Nonnull
    @Override
    public List<? extends BasicBlock> getPredecessors() {
      return predecessors;
    }

    @Nonnull
    @Override
    public List<? extends BasicBlock> getSuccessors() {
      return successors;
    }

    @Nonnull
    @Override
    public List<? extends BasicBlock> getExceptionalSuccessors() {
      throw new UnsupportedOperationException("not implemented yet");
    }

    @Nonnull
    @Override
    public List<Stmt> getStmts() {
      throw new UnsupportedOperationException();
    }

    @Override
    public int getStmtCount() {
      return endIdx - startIdx + 1;
    }

    @Nonnull
    @Override
    public Stmt getHead() {
      throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public Stmt getTail() {
      throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public List<? extends Trap> getTraps() {
      throw new UnsupportedOperationException();
    }
  }
}
