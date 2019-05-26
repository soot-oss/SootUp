package de.upb.soot.jimple.common.stmt;

import de.upb.soot.jimple.basic.EquivTo;
import de.upb.soot.jimple.basic.IStmtBox;
import de.upb.soot.jimple.basic.PositionInfo;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.common.expr.AbstractInvokeExpr;
import de.upb.soot.jimple.common.ref.JArrayRef;
import de.upb.soot.jimple.common.ref.JFieldRef;
import de.upb.soot.jimple.visitor.IAcceptor;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.util.printer.IStmtPrinter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

public abstract class IStmt implements EquivTo, IAcceptor, Serializable {

  /** List of UnitBoxes pointing to this Unit. */
  @Nullable private List<IStmtBox> boxesPointingToThis = null;

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
  public List<IStmtBox> getStmtBoxes() {
    return Collections.emptyList();
  }

  /** Returns a list of Boxes pointing to this Unit. */
  public List<IStmtBox> getBoxesPointingToThis() {
    if (boxesPointingToThis == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(boxesPointingToThis);
  }

  public void addBoxPointingToThis(IStmtBox b) {
    if (boxesPointingToThis == null) {
      boxesPointingToThis = new ArrayList<>();
    }
    boxesPointingToThis.add(b);
  }

  public void removeBoxPointingToThis(IStmtBox b) {
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

  public abstract IStmt clone();

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

  public abstract void toString(IStmtPrinter up);

  /** Used to implement the Switchable construct. */
  @Override
  public void accept(IVisitor sw) {}

  public void redirectJumpsToThisTo(IStmt newLocation) {
    List<IStmtBox> boxesPointing = getBoxesPointingToThis();

    // important to have a static copy
    List<IStmtBox> boxesCopy = new ArrayList<>(boxesPointing);

    for (IStmtBox box : boxesCopy) {
      if (box.getStmt() != this) {
        throw new RuntimeException("Something weird's happening");
      }

      if (box.isBranchTarget()) {
        IStmtBox.$Accessor.setStmt(box, newLocation);
      }
    }
  }

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

  public abstract PositionInfo getPositionInfo();
}
