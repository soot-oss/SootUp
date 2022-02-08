package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.types.ClassType;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

// FIXME: [ms] IMPLEMENT!
public class ImmutableBasicBlock implements BasicBlock<ImmutableBasicBlock> {

  @Nonnull
  @Override
  public List<ImmutableBasicBlock> getPredecessors() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Nonnull
  @Override
  public List<ImmutableBasicBlock> getSuccessors() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public List<ImmutableBasicBlock> getExceptionalPredecessors() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Nonnull
  @Override
  public Map<? extends ClassType, ImmutableBasicBlock> getExceptionalSuccessors() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Nonnull
  @Override
  public List<Stmt> getStmts() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public int getStmtCount() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Nonnull
  @Override
  public Stmt getHead() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Nonnull
  @Override
  public Stmt getTail() {
    throw new UnsupportedOperationException("not implemented");
  }
}
