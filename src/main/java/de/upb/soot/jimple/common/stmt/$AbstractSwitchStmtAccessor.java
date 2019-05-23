package de.upb.soot.jimple.common.stmt;

import java.util.List;

/** This class is for internal use only. It will be removed in the future. */
@Deprecated
public class $AbstractSwitchStmtAccessor {
  // This class deliberately starts with a $-sign to discourage usage
  // of this Soot implementation detail. Some IDEs such as IntelliJ
  // don't suggest these classes in autocomplete.

  /** Violates immutability. Only use this for legacy code. */
  @Deprecated
  public static void setTargets(AbstractSwitchStmt stmt, List<? extends IStmt> targets) {
    stmt.setTargets(targets);
  }

  /** Violates immutability. Only use this for legacy code. */
  @Deprecated
  public static void setDefaultTarget(AbstractSwitchStmt stmt, IStmt defaultTarget) {
    stmt.setDefaultTarget(defaultTarget);
  }

  private $AbstractSwitchStmtAccessor() {}
}
