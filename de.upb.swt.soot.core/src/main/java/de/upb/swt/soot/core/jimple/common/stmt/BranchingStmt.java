package de.upb.swt.soot.core.jimple.common.stmt;

import de.upb.swt.soot.core.model.Body;
import java.util.List;

/**
 * Interface for Stmts at which the execution does not necessarily continue with the following Stmt
 * in the List
 */
public interface BranchingStmt {
  List<Stmt> getTargetStmts(Body body);
}
