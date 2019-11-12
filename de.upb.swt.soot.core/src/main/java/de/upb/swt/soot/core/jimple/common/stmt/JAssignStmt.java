/*
 * @author Linghui Luo
 * @version 1.0
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

package de.upb.swt.soot.core.jimple.common.stmt;

import de.upb.swt.soot.core.jimple.basic.Immediate;
import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.RValueBox;
import de.upb.swt.soot.core.jimple.basic.StmtBox;
import de.upb.swt.soot.core.jimple.basic.StmtBoxOwner;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.basic.ValueBox;
import de.upb.swt.soot.core.jimple.basic.VariableBox;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.common.ref.JArrayRef;
import de.upb.swt.soot.core.jimple.common.ref.JFieldRef;
import de.upb.swt.soot.core.jimple.visitor.StmtVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.List;
import javax.annotation.Nonnull;

/** Represents the assignment of one value to another */
public final class JAssignStmt extends AbstractDefinitionStmt implements Copyable {

  /** The Class LinkedVariableBox. */
  private static class LinkedVariableBox extends VariableBox {
    /** The other box. */
    ValueBox otherBox = null;

    /**
     * Instantiates a new linked variable box.
     *
     * @param v the v
     */
    private LinkedVariableBox(Value v) {
      super(v);
    }

    /**
     * Sets the other box.
     *
     * @param otherBox the new other box
     */
    public void setOtherBox(ValueBox otherBox) {
      this.otherBox = otherBox;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.upb.soot.jimple.VariableBox#canContainValue(de.upb.soot.jimple.Value)
     */
    @Override
    public boolean canContainValue(Value v) {
      if (super.canContainValue(v)) {
        if (otherBox == null) {
          return true;
        }

        Value o = otherBox.getValue();
        return (v instanceof Immediate) || (o instanceof Immediate);
      }
      return false;
    }
  }

  /** The Class LinkedRValueBox. */
  private static class LinkedRValueBox extends RValueBox {

    /** The other box. */
    ValueBox otherBox = null;

    /**
     * Instantiates a new linked R value box.
     *
     * @param v the v
     */
    private LinkedRValueBox(Value v) {
      super(v);
    }

    /**
     * Sets the other box.
     *
     * @param otherBox the new other box
     */
    public void setOtherBox(ValueBox otherBox) {
      this.otherBox = otherBox;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.upb.soot.jimple.RValueBox#canContainValue(de.upb.soot.jimple.Value)
     */
    @Override
    public boolean canContainValue(Value v) {
      if (super.canContainValue(v)) {
        if (otherBox == null) {
          return true;
        }

        Value o = otherBox.getValue();
        return (v instanceof Immediate) || (o instanceof Immediate);
      }
      return false;
    }
  }

  /**
   * Instantiates a new JAssignStmt.
   *
   * @param variable the variable on the left side of the assign statement.
   * @param rValue the value on the right side of the assign statement.
   */
  public JAssignStmt(Value variable, Value rValue, StmtPositionInfo positionInfo) {
    this(new LinkedVariableBox(variable), new LinkedRValueBox(rValue), positionInfo);

    ((LinkedVariableBox) getLeftBox()).setOtherBox(getRightBox());
    ((LinkedRValueBox) getRightBox()).setOtherBox(getLeftBox());

    if (!getLeftBox().canContainValue(variable) || !getRightBox().canContainValue(rValue)) {
      throw new RuntimeException(
          "Illegal assignment statement.  Make sure that either left side or right hand side has a local or constant.");
    }
  }

  /**
   * Instantiates a new JAssignStmt.
   *
   * @param variableBox the variable box on the left side of the assign statement.
   * @param rvalueBox the rvalue box on the right side of the assign statement.
   */
  protected JAssignStmt(ValueBox variableBox, ValueBox rvalueBox, StmtPositionInfo positionInfo) {
    super(variableBox, rvalueBox, positionInfo);
  }

  /*
   * (non-Javadoc)
   *
   * @see de.upb.soot.jimple.common.stmt.AbstractStmt#containsInvokeExpr()
   */
  @Override
  public boolean containsInvokeExpr() {
    return getRightOp() instanceof AbstractInvokeExpr;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.upb.soot.jimple.common.stmt.AbstractStmt#getInvokeExpr()
   */
  @Override
  public AbstractInvokeExpr getInvokeExpr() {
    if (!containsInvokeExpr()) {
      throw new RuntimeException("getInvokeExpr() called with no invokeExpr present!");
    }

    return (AbstractInvokeExpr) getRightBox().getValue();
  }

  /*
   * (non-Javadoc)
   *
   * @see de.upb.soot.jimple.common.stmt.AbstractStmt#getInvokeExprBox()
   */
  @Override
  public ValueBox getInvokeExprBox() {
    if (!containsInvokeExpr()) {
      throw new RuntimeException("getInvokeExpr() called with no invokeExpr present!");
    }

    return getRightBox();
  }

  /*
   * (non-Javadoc)
   *
   * @see de.upb.soot.jimple.common.stmt.AbstractStmt#containsArrayRef()
   */
  /* added by Feng */
  @Override
  public boolean containsArrayRef() {
    return ((getLeftOp() instanceof JArrayRef) || (getRightOp() instanceof JArrayRef));
  }

  /*
   * (non-Javadoc)
   *
   * @see de.upb.soot.jimple.common.stmt.AbstractStmt#getArrayRef()
   */
  @Override
  public JArrayRef getArrayRef() {
    if (!containsArrayRef()) {
      throw new RuntimeException("getArrayRef() called with no ArrayRef present!");
    }

    if (getLeftBox().getValue() instanceof JArrayRef) {
      return (JArrayRef) getLeftBox().getValue();
    } else {
      return (JArrayRef) getRightBox().getValue();
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see de.upb.soot.jimple.common.stmt.AbstractStmt#getArrayRefBox()
   */
  @Override
  public ValueBox getArrayRefBox() {
    if (!containsArrayRef()) {
      throw new RuntimeException("getArrayRefBox() called with no ArrayRef present!");
    }

    if (getLeftBox().getValue() instanceof JArrayRef) {
      return getLeftBox();
    } else {
      return getRightBox();
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see de.upb.soot.jimple.common.stmt.AbstractStmt#containsFieldRef()
   */
  @Override
  public boolean containsFieldRef() {
    return ((getLeftOp() instanceof JFieldRef) || (getRightOp() instanceof JFieldRef));
  }

  /*
   * (non-Javadoc)
   *
   * @see de.upb.soot.jimple.common.stmt.AbstractStmt#getFieldRef()
   */
  @Override
  public JFieldRef getFieldRef() {
    if (!containsFieldRef()) {
      throw new RuntimeException("getFieldRef() called with no JFieldRef present!");
    }

    if (getLeftBox().getValue() instanceof JFieldRef) {
      return (JFieldRef) getLeftBox().getValue();
    } else {
      return (JFieldRef) getRightBox().getValue();
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see de.upb.soot.jimple.common.stmt.AbstractStmt#getFieldRefBox()
   */
  @Override
  public ValueBox getFieldRefBox() {
    if (!containsFieldRef()) {
      throw new RuntimeException("getFieldRefBox() called with no JFieldRef present!");
    }

    if (getLeftBox().getValue() instanceof JFieldRef) {
      return getLeftBox();
    } else {
      return getRightBox();
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see de.upb.soot.jimple.common.stmt.AbstractStmt#getUnitBoxes()
   */
  @Override
  public List<StmtBox> getStmtBoxes() {
    // handle possible PhiExpr's
    Value rvalue = getRightBox().getValue();
    if (rvalue instanceof StmtBoxOwner) {
      return ((StmtBoxOwner) rvalue).getStmtBoxes();
    }

    return super.getStmtBoxes();
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getLeftBox().getValue().toString() + " = " + getRightBox().getValue().toString();
  }

  /*
   * (non-Javadoc)
   *
   * @see de.upb.soot.jimple.common.stmt.Stmt#toString(de.upb.soot.StmtPrinter)
   */
  @Override
  public void toString(StmtPrinter up) {
    getLeftBox().toString(up);
    up.literal(" = ");
    getRightBox().toString(up);
  }

  /*
   * (non-Javadoc)
   *
   * @see de.upb.soot.jimple.common.stmt.AbstractStmt#accept(de.upb.soot.jimple.visitor.Visitor)
   */
  @Override
  public void accept(Visitor sw) {
    ((StmtVisitor) sw).caseAssignStmt(this);
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseAssignStmt(this, o);
  }

  @Override
  public int equivHashCode() {
    return getLeftBox().getValue().equivHashCode() + 31 * getRightBox().getValue().equivHashCode();
  }

  @Nonnull
  public JAssignStmt withVariable(Value variable) {
    return new JAssignStmt(variable, getRightOp(), getPositionInfo());
  }

  @Nonnull
  public JAssignStmt withRValue(Value rValue) {
    return new JAssignStmt(getLeftOp(), rValue, getPositionInfo());
  }

  @Nonnull
  public JAssignStmt withPositionInfo(StmtPositionInfo positionInfo) {
    return new JAssignStmt(getLeftOp(), getRightOp(), positionInfo);
  }
}
