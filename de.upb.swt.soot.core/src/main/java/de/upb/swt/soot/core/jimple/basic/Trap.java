package de.upb.swt.soot.core.jimple.basic;

import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.types.ClassType;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * A trap is an exception catcher.
 *
 * @author Linghui Luo
 */
public interface Trap {
  @Nonnull
  List<Stmt> getStmts();

  @Nonnull
  ClassType getExceptionType();

  @Nonnull
  Stmt getBeginStmt();

  @Nonnull
  Stmt getEndStmt();

  @Nonnull
  Stmt getHandlerStmt();
}
