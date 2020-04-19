package de.upb.swt.soot.core.jimple.common.stmt;

import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.common.ref.JArrayRef;
import de.upb.swt.soot.core.jimple.common.ref.JFieldRef;
import de.upb.swt.soot.core.jimple.visitor.Acceptor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public abstract class Stmt implements EquivTo, Acceptor, Copyable {

  protected final StmtPositionInfo positionInfo;
  /** List of Stmts pointing to this Stmt. */
  @Nonnull private List<Stmt> stmtsPointingToThis = new ArrayList<>();

  public Stmt(@Nonnull StmtPositionInfo positionInfo) {
    this.positionInfo = positionInfo;
  }

  /**
   * Returns a list of Values used in this Stmt. Note that they are returned in usual evaluation
   * order.
   */
  @Nonnull
  public List<Value> getUses() {
    return Collections.emptyList();
  }

  /** Returns a list of Values defined in this Stmt. */
  @Nonnull
  public List<Value> getDefs() {
    return Collections.emptyList();
  }

  /** Returns a list of Stmts defined in this Stmt; typically branch targets. */
  @Nonnull
  public List<Stmt> getStmts() {
    return Collections.emptyList();
  }

  /** Returns a list of Stmts pointing to this Stmt. */
  @Nonnull
  public List<Stmt> getStmtsPointingToThis() {
    return Collections.unmodifiableList(stmtsPointingToThis);
  }

  @Deprecated
  private void addStmtPointingToThis(@Nonnull Stmt fromStmt) {
    stmtsPointingToThis.add(fromStmt);
  }

  @Deprecated
  private void removeStmtPointingToThis(@Nonnull Stmt fromStmt) {
    stmtsPointingToThis.remove(fromStmt);
  }

  /** Returns a list of Values, either used or defined or both in this Stmt. */
  @Nonnull
  public List<Value> getUsesAndDefs() {
    List<Value> uses = getUses();
    List<Value> defs = getDefs();
    if (uses.isEmpty()) {
      return defs;
    } else if (defs.isEmpty()) {
      return uses;
    } else {
      List<Value> values = new ArrayList<>();
      values.addAll(defs);
      values.addAll(uses);
      return values;
    }
  }

  /**
   * Returns true if execution after this statement may continue at the following statement. (e.g.
   * GotoStmt will return false and IfStmt will return true).
   */
  public abstract boolean fallsThrough();

  /**
   * Returns true if execution after this statement does not necessarily continue at the following
   * statement. GotoStmt and IfStmt will both return true.
   */
  public abstract boolean branches();

  public abstract void toString(StmtPrinter up);

  /** Used to implement the Switchable construct. */
  public void accept(@Nonnull Visitor sw) {}

  public boolean containsInvokeExpr() {
    return false;
  }

  public AbstractInvokeExpr getInvokeExpr() {
    throw new RuntimeException("getInvokeExpr() called with no invokeExpr present!");
  }

  public boolean containsArrayRef() {
    return false;
  }

  public JArrayRef getArrayRef() {
    throw new RuntimeException("getArrayRef() called with no ArrayRef present!");
  }

  public boolean containsFieldRef() {
    return false;
  }

  public JFieldRef getFieldRef() {
    throw new RuntimeException("getFieldRef() called with no JFieldRef present!");
  }

  public StmtPositionInfo getPositionInfo() {
    return positionInfo;
  }

  public boolean isBranchTarget() {
    return !stmtsPointingToThis.isEmpty();
  }

  /** This class is for internal use only. It will be removed in the future. */
  @Deprecated
  public static class $Accessor {
    // This class deliberately starts with a $-sign to discourage usage
    // of this Soot implementation detail.

    /** Violates immutability. Only use this for legacy code. */
    @Deprecated
    public static void addStmtPointingToTarget(@Nonnull Stmt fromStmt, @Nonnull Stmt targetStmt) {
      targetStmt.addStmtPointingToThis(fromStmt);
    }

    /** Violates immutability. Only use this for legacy code. */
    @Deprecated
    public static void removeStmtPointingToTarget(
        @Nonnull Stmt fromStmt, @Nonnull Stmt targetStmt) {
      targetStmt.removeStmtPointingToThis(fromStmt);
    }

    private $Accessor() {}
  }
}
