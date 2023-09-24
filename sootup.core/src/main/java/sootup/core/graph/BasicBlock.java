package sootup.core.graph;

import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.ClassType;

public interface BasicBlock<V extends BasicBlock<V>> {
  @Nonnull
  List<V> getPredecessors();

  @Nonnull
  List<V> getSuccessors();

  Map<ClassType, V> getExceptionalPredecessors();

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

  default boolean equivTo(@Nonnull Object o, @Nonnull JimpleComparator comparator) {
    return comparator.caseBlock(this, o);
  }
}
