package de.upb.swt.soot.core.jimple.common.stmt;

import javax.annotation.Nullable;

public final class StmtHandler {
  @Nullable private Stmt stmt;

  public StmtHandler(@Nullable Stmt stmt) {
    this.stmt = stmt;
  }

  private void setStmt(@Nullable Stmt stmt) {
    // Remove this from set of back pointers.
    if (this.stmt != null) {
      Stmt.$Accessor.removeStmtPointingToThis(this.stmt, this.stmt);
    }

    // Perform link
    this.stmt = stmt;

    // Add this to back pointers
    if (this.stmt != null) {
      Stmt.$Accessor.addStmtPointingToThis(this.stmt, this.stmt);
    }
  }

  /** This class is for internal use only. It will be removed in the future. */
  @Deprecated
  public static class $Accessor {
    // This class deliberately starts with a $-sign to discourage usage
    // of this Soot implementation detail.

    /** Violates immutability. Only use this for legacy code. */
    @Deprecated
    public static void setStmt(StmtHandler stmtHandler, Stmt newStmt) {
      stmtHandler.setStmt(newStmt);
    }

    private $Accessor() {}
  }
}
