package de.upb.swt.soot.core.jimple.basic;

import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.types.ClassType;
import java.util.List;

/**
 * A trap is an exception catcher.
 *
 * @author Linghui Luo
 */
public interface Trap {

  List<StmtBox> getStmtBoxes();

  ClassType getExceptionType();

  Stmt getBeginStmt();

  Stmt getEndStmt();

  Stmt getHandlerStmt();
}
