package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.types.ClassType;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

public class ForwardingBasicBlock<V extends BasicBlock<V>> implements BasicBlock<V> {
  private final V backingBlock;

  ForwardingBasicBlock(@Nonnull V block) {
    backingBlock = block;
  }

  @Nonnull
  @Override
  public List<V> getPredecessors() {
    return backingBlock.getPredecessors();
  }

  @Nonnull
  @Override
  public List<V> getSuccessors() {
    return backingBlock.getSuccessors();
  }

  @Override
  public List<V> getExceptionalPredecessors() {
    return backingBlock.getExceptionalPredecessors();
  }

  @Nonnull
  @Override
  public Map<? extends ClassType, V> getExceptionalSuccessors() {
    return backingBlock.getExceptionalSuccessors();
  }

  @Nonnull
  @Override
  public List<Stmt> getStmts() {
    return backingBlock.getStmts();
  }

  @Override
  public int getStmtCount() {
    return backingBlock.getStmtCount();
  }

  @Nonnull
  @Override
  public Stmt getHead() {
    return backingBlock.getHead();
  }

  @Nonnull
  @Override
  public Stmt getTail() {
    return backingBlock.getTail();
  }
}
