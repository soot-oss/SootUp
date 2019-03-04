package de.upb.soot.jimple.basic;

import de.upb.soot.core.SootClass;
import de.upb.soot.jimple.common.stmt.IStmt;

/**
 * A trap is an exception catcher.
 *
 * @author Linghui Luo
 */
public interface Trap extends StmtBoxOwner {

  /** Performs a shallow clone of this trap. */
  Object clone();

  SootClass getException();

  IStmt getBeginStmt();

  IStmt getEndStmt();

  IStmt getHandlerStmt();
}
