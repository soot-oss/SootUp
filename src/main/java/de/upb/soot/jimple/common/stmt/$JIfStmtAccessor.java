package de.upb.soot.jimple.common.stmt;

/** This class is for internal use only. It will be removed in the future. */
@Deprecated
public class $JIfStmtAccessor {
  // This class deliberately starts with a $-sign to discourage usage
  // of this Soot implementation detail. Some IDEs such as IntelliJ
  // don't suggest these classes in autocomplete.

  /** Violates immutability. Only use this for legacy code. */
  @Deprecated
  public static void setTarget(JIfStmt stmt, IStmt target) {
    stmt.setTarget(target);
  }

  private $JIfStmtAccessor() {}
}
