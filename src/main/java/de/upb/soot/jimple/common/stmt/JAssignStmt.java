/*
 * @author Linghui Luo
 * @version 1.0
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.  
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

package de.upb.soot.jimple.common.stmt;

import java.util.List;

import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.IStmtBox;
import de.upb.soot.jimple.basic.Immediate;
import de.upb.soot.jimple.basic.PositionInfo;
import de.upb.soot.jimple.basic.JimpleComparator;
import de.upb.soot.jimple.basic.RValueBox;
import de.upb.soot.jimple.basic.StmtBoxOwner;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.basic.VariableBox;
import de.upb.soot.jimple.common.expr.AbstractInvokeExpr;
import de.upb.soot.jimple.common.ref.JArrayRef;
import de.upb.soot.jimple.common.ref.JFieldRef;
import de.upb.soot.jimple.visitor.IStmtVisitor;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.util.printer.IStmtPrinter;

/**
 * The Class JAssignStmt.
 */
public class JAssignStmt extends AbstractDefinitionStmt {

  /**
   * 
   */
  private static final long serialVersionUID = -4560269896679815285L;

  /**
   * The Class LinkedVariableBox.
   */
  private static class LinkedVariableBox extends VariableBox {

    /**
     * 
     */
    private static final long serialVersionUID = -249895672780916220L;
    /** The other box. */
    ValueBox otherBox = null;

    /**
     * Instantiates a new linked variable box.
     *
     * @param v
     *          the v
     */
    private LinkedVariableBox(Value v) {
      super(v);
    }

    /**
     * Sets the other box.
     *
     * @param otherBox
     *          the new other box
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

  /**
   * The Class LinkedRValueBox.
   */
  private static class LinkedRValueBox extends RValueBox {

    /** The other box. */
    ValueBox otherBox = null;

    /**
     * Instantiates a new linked R value box.
     *
     * @param v
     *          the v
     */
    private LinkedRValueBox(Value v) {
      super(v);
    }

    /**
     * Sets the other box.
     *
     * @param otherBox
     *          the new other box
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
   * @param variable
   *          the variable on the left side of the assign statement.
   * @param rvalue
   *          the value on the right side of the assign statement.
   */
  public JAssignStmt(Value variable, Value rvalue, PositionInfo positionInfo) {
    this(new LinkedVariableBox(variable), new LinkedRValueBox(rvalue),positionInfo);

    ((LinkedVariableBox) leftBox).setOtherBox(rightBox);
    ((LinkedRValueBox) rightBox).setOtherBox(leftBox);

    if (!leftBox.canContainValue(variable) || !rightBox.canContainValue(rvalue)) {
      throw new RuntimeException(
          "Illegal assignment statement.  Make sure that either left side or right hand side has a local or constant.");
    }

  }

  /**
   * Instantiates a new JAssignStmt.
   *
   * @param variableBox
   *          the variable box on the left side of the assign statement.
   * @param rvalueBox
   *          the rvalue box on the right side of the assign statement.
   */
  protected JAssignStmt(ValueBox variableBox, ValueBox rvalueBox, PositionInfo positionInfo) {
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

    return (AbstractInvokeExpr) rightBox.getValue();
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

    return rightBox;
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

    if (leftBox.getValue() instanceof JArrayRef) {
      return (JArrayRef) leftBox.getValue();
    } else {
      return (JArrayRef) rightBox.getValue();
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

    if (leftBox.getValue() instanceof JArrayRef) {
      return leftBox;
    } else {
      return rightBox;
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

    if (leftBox.getValue() instanceof JFieldRef) {
      return (JFieldRef) leftBox.getValue();
    } else {
      return (JFieldRef) rightBox.getValue();
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

    if (leftBox.getValue() instanceof JFieldRef) {
      return leftBox;
    } else {
      return rightBox;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.upb.soot.jimple.common.stmt.AbstractStmt#getUnitBoxes()
   */
  @Override
  public List<IStmtBox> getStmtBoxes() {
    // handle possible PhiExpr's
    Value rvalue = rightBox.getValue();
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
    return leftBox.getValue().toString() + " = " + rightBox.getValue().toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.upb.soot.jimple.common.stmt.Stmt#toString(de.upb.soot.StmtPrinter)
   */
  @Override
  public void toString(IStmtPrinter up) {
    leftBox.toString(up);
    up.literal(" = ");
    rightBox.toString(up);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.upb.soot.jimple.common.stmt.AbstractStmt#clone()
   */
  @Override
  public JAssignStmt clone() {
    return new JAssignStmt(Jimple.cloneIfNecessary(getLeftOp()), Jimple.cloneIfNecessary(getRightOp()), getPositionInfo().clone());
  }

  /**
   * Sets the left op.
   *
   * @param variable
   *          the new left op
   */
  public void setLeftOp(Value variable) {
    getLeftOpBox().setValue(variable);
  }

  /**
   * Sets the right op.
   *
   * @param rvalue
   *          the new right op
   */
  public void setRightOp(Value rvalue) {
    getRightOpBox().setValue(rvalue);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.upb.soot.jimple.common.stmt.AbstractStmt#accept(de.upb.soot.jimple.visitor.IVisitor)
   */
  @Override
  public void accept(IVisitor sw) {
    ((IStmtVisitor) sw).caseAssignStmt(this);
  }

  @Override
  public boolean equivTo(Object o) {
    return JimpleComparator.getInstance().caseAssignStmt(this, o);
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseAssignStmt(this, o);
  }

  @Override
  public int equivHashCode() {
    return leftBox.getValue().equivHashCode() + 31 * rightBox.getValue().equivHashCode();
  }

}
