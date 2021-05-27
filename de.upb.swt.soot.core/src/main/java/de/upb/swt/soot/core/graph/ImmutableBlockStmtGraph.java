package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// FIXME: implement
public class ImmutableBlockStmtGraph implements StmtGraph {
  public ImmutableBlockStmtGraph() {
    throw new UnsupportedOperationException("implement it!");
  }

  @Nullable
  @Override
  public Stmt getStartingStmt() {
    return null;
  }

  @Nonnull
  @Override
  public Set<Stmt> nodes() {
    return null;
  }

  @Override
  public boolean containsNode(@Nonnull Stmt node) {
    return false;
  }

  @Nonnull
  @Override
  public List<Stmt> predecessors(@Nonnull Stmt node) {
    return null;
  }

  @Nonnull
  @Override
  public List<Stmt> successors(@Nonnull Stmt node) {
    return null;
  }

  @Override
  public int inDegree(@Nonnull Stmt node) {
    return 0;
  }

  @Override
  public int outDegree(@Nonnull Stmt node) {
    return 0;
  }

  @Override
  public boolean hasEdgeConnecting(@Nonnull Stmt source, @Nonnull Stmt target) {
    return false;
  }

  @Nonnull
  @Override
  public List<Trap> getTraps() {
    return null;
  }
}
