package de.upb.soot.jimple.basic;

import de.upb.soot.jimple.common.stmt.IStmt;

/** This class is for internal use only. It will be removed in the future. */
@Deprecated
public class $StmtBoxAccessor {
  // This class deliberately starts with a $-sign to discourage usage
  // of this Soot implementation detail. Some IDEs such as IntelliJ
  // don't suggest these classes in autocomplete.

  /** Violates immutability. Only use this for legacy code. */
  @Deprecated
  public static void setStmt(IStmtBox box, IStmt stmt) {
    box.setStmt(stmt);
  }

  private $StmtBoxAccessor() {}
}
