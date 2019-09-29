package de.upb.soot.core.jimple.basic;

import de.upb.soot.core.jimple.common.stmt.Stmt;
import de.upb.soot.core.types.JavaClassType;

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
