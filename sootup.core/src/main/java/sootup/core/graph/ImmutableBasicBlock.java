package sootup.core.graph;

import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.ClassType;

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
  @Nonnull
  public Map<ClassType, ImmutableBasicBlock> getExceptionalPredecessors() {
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
