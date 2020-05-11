package de.upb.swt.soot.core.jimple.common.stmt;

import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.common.ref.JArrayRef;
import de.upb.swt.soot.core.jimple.common.ref.JFieldRef;
import de.upb.swt.soot.core.jimple.visitor.Acceptor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public abstract class Stmt implements EquivTo, Acceptor, Copyable {

  protected final StmtPositionInfo positionInfo;

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
   * GotoStmt will return false and e.g. IfStmt will return true).
   */
  public abstract boolean fallsThrough();

  /**
   * Returns true if execution after this statement does not necessarily continue at the following
   * statement. The {@link BranchingStmt}'s GotoStmt, JSwitchStmt and IfStmt will return true.
   */
  public abstract boolean branches();

  public abstract void toString(StmtPrinter up);

  /** Used to implement the Switchable construct via OOP */
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

  public boolean isBranchTarget(Body body) {
    return body.isStmtBranchTarget(this);
  }
}
