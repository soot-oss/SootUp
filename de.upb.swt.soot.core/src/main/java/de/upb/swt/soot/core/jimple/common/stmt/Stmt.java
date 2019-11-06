package de.upb.swt.soot.core.jimple.common.stmt;

import de.upb.swt.soot.core.jimple.basic.EquivTo;
import de.upb.swt.soot.core.jimple.basic.StmtBox;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.ValueBox;
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
import javax.annotation.Nullable;

public abstract class Stmt implements EquivTo, Acceptor, Copyable {

  /** List of UnitBoxes pointing to this Unit. */
  @Nullable private List<StmtBox> boxesPointingToThis = null;

  /**
   * Returns a list of Boxes containing Values used in this Unit. The list of boxes is dynamically
   * updated as the structure changes. Note that they are returned in usual evaluation order. (this
   * is important for aggregation)
   */
  public List<ValueBox> getUseBoxes() {
    return Collections.emptyList();
  }

  /**
   * Returns a list of Boxes containing Values defined in this Unit. The list of boxes is
   * dynamically updated as the structure changes.
   */
  public List<ValueBox> getDefBoxes() {
    return Collections.emptyList();
  }

  /**
   * Returns a list of Boxes containing Units defined in this Unit; typically branch targets. The
   * list of boxes is dynamically updated as the structure changes.
   */
  public List<StmtBox> getStmtBoxes() {
    return Collections.emptyList();
  }

  /** Returns a list of Boxes pointing to this Unit. */
  public List<StmtBox> getBoxesPointingToThis() {
    if (boxesPointingToThis == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(boxesPointingToThis);
  }

  @Deprecated
  private void addBoxPointingToThis(StmtBox b) {
    if (boxesPointingToThis == null) {
      boxesPointingToThis = new ArrayList<>();
    }
    boxesPointingToThis.add(b);
  }

  @Deprecated
  private void removeBoxPointingToThis(StmtBox b) {
    if (boxesPointingToThis != null) {
      boxesPointingToThis.remove(b);
    }
  }

  /** Returns a list of ValueBoxes, either used or defined in this Unit. */
  public List<ValueBox> getUseAndDefBoxes() {
    List<ValueBox> useBoxes = getUseBoxes();
    List<ValueBox> defBoxes = getDefBoxes();
    if (useBoxes.isEmpty()) {
      return defBoxes;
    } else {
      if (defBoxes.isEmpty()) {
        return useBoxes;
      } else {
        List<ValueBox> valueBoxes = new ArrayList<>();
        valueBoxes.addAll(defBoxes);
        valueBoxes.addAll(useBoxes);
        return valueBoxes;
      }
    }
  }

  /**
   * Returns true if execution after this statement may continue at the following statement.
   * GotoStmt will return false but IfStmt will return true.
   */
  public abstract boolean fallsThrough();

  /**
   * Returns true if execution after this statement does not necessarily continue at the following
   * statement. GotoStmt and IfStmt will both return true.
   */
  public abstract boolean branches();

  public abstract void toString(StmtPrinter up);

  /** Used to implement the Switchable construct. */
  public void accept(Visitor sw) {}

  public boolean containsInvokeExpr() {
    return false;
  }

  public AbstractInvokeExpr getInvokeExpr() {
    throw new RuntimeException("getInvokeExpr() called with no invokeExpr present!");
  }

  public ValueBox getInvokeExprBox() {
    throw new RuntimeException("getInvokeExprBox() called with no invokeExpr present!");
  }

  public boolean containsArrayRef() {
    return false;
  }

  public JArrayRef getArrayRef() {
    throw new RuntimeException("getArrayRef() called with no ArrayRef present!");
  }

  public ValueBox getArrayRefBox() {
    throw new RuntimeException("getArrayRefBox() called with no ArrayRef present!");
  }

  public boolean containsFieldRef() {
    return false;
  }

  public JFieldRef getFieldRef() {
    throw new RuntimeException("getFieldRef() called with no JFieldRef present!");
  }

  public ValueBox getFieldRefBox() {
    throw new RuntimeException("getFieldRefBox() called with no JFieldRef present!");
  }

  public abstract StmtPositionInfo getPositionInfo();

  /** This class is for internal use only. It will be removed in the future. */
  @Deprecated
  public static class $Accessor {
    // This class deliberately starts with a $-sign to discourage usage
    // of this Soot implementation detail.

    /** Violates immutability. Only use this for legacy code. */
    @Deprecated
    public static void addBoxPointingToThis(Stmt stmt, StmtBox box) {
      stmt.addBoxPointingToThis(box);
    }

    /** Violates immutability. Only use this for legacy code. */
    @Deprecated
    public static void removeBoxPointingToThis(Stmt stmt, StmtBox box) {
      stmt.removeBoxPointingToThis(box);
    }

    private $Accessor() {}
  }
}
