package de.upb.swt.soot.core.jimple.common.stmt;

import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.model.Body;
import javax.annotation.Nonnull;

/**
 * Interface for Stmts at which the execution does not necessarily continue with the following Stmt
 * in the List
 */
public abstract class BranchingStmt extends Stmt {
  public BranchingStmt(@Nonnull StmtPositionInfo positionInfo) {
    super(positionInfo);
  }

  public abstract Iterable<Stmt> getTargetStmts(Body body);
}
