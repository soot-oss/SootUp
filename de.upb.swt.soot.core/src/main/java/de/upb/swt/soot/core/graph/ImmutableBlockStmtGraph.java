package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// FIXME: implement
public class ImmutableBlockStmtGraph implements StmtGraph {
  public ImmutableBlockStmtGraph() {
    throw new IllegalStateException("implement it!");
  }

  @Nullable
  @Override
  public Stmt getStartingStmt() {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Nonnull
  @Override
  public Set<Stmt> nodes() {
    throw new IllegalStateException("Not implemented yet!");
  }

  @Nonnull
  @Override
  public Collection<BasicBlock> getBlocks() {
    throw new IllegalStateException("Not implemented yet!");
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
}
