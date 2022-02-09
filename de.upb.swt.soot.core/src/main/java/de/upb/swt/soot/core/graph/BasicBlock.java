package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.types.ClassType;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

public interface BasicBlock<V extends BasicBlock<V>> {
  @Nonnull
  List<V> getPredecessors();

  @Nonnull
  List<V> getSuccessors();

  List<V> getExceptionalPredecessors();

  @Nonnull
  Map<? extends ClassType, V> getExceptionalSuccessors();

  @Nonnull
  List<Stmt> getStmts();

  int getStmtCount();

  default boolean isEmpty() {
    return getStmtCount() <= 0;
  }

  @Nonnull
  Stmt getHead();

  @Nonnull
  Stmt getTail();
}
