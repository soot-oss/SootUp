package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.List;
import javax.annotation.Nonnull;

public interface BasicBlock {
  @Nonnull
  List<? extends BasicBlock> getPredecessors();

  @Nonnull
  List<? extends BasicBlock> getSuccessors();

  // hint: theres no getExceptionalPredecessorBlocks() as it makes no sense in that direction -> use
  // getPredecessors() ;-)
  @Nonnull
  List<? extends BasicBlock> getExceptionalSuccessors();

  @Nonnull
  List<Stmt> getStmts();

  int getStmtCount();

  default boolean isEmpty() {
    return getStmtCount() > 0;
  }

  @Nonnull
  Stmt getHead();

  @Nonnull
  Stmt getTail();

  @Nonnull
  List<? extends Trap> getTraps();
}
