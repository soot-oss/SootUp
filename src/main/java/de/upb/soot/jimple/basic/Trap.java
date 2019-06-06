package de.upb.soot.jimple.basic;

import de.upb.soot.jimple.common.stmt.Stmt;
import de.upb.soot.types.JavaClassType;

/**
 * A trap is an exception catcher.
 *
 * @author Linghui Luo
 */
public interface Trap extends StmtBoxOwner {

  JavaClassType getException();

  Stmt getBeginStmt();

  Stmt getEndStmt();

  Stmt getHandlerStmt();
}
