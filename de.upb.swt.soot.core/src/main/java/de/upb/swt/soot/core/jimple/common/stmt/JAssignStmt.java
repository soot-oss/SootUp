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

import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.common.expr.Expr;
import de.upb.swt.soot.core.jimple.common.ref.ConcreteRef;
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

  /** The Class LinkedVariable. */
  private static class LinkedVariable {

    Value value;

    /** The other Value. */
    Value otherValue = null;

    /**
     * Instantiates a new linked variable.
     *
     * @param value the value
     */
    private LinkedVariable(Value value) {
      if (value == null) {
        throw new IllegalArgumentException("value may not be null");
      }
      if (canContainValue(value)) {
        this.value = value;
      } else {
        throw new RuntimeException(
            "LinkedVariable "
                + this
                + " cannot contain value: "
                + value
                + " ("
                + value.getClass()
                + ")");
      }
    }

    /**
     * Sets the other value.
     *
     * @param otherValue the new other Value
     */
    public void setOtherValue(Value otherValue) {
      this.otherValue = otherValue;
    }

    public boolean canContainValue(Value v) {
      if (v instanceof Local || v instanceof ConcreteRef) {
        if (otherValue == null) {
          return true;
        }
        Value o = otherValue;
        return (v instanceof Immediate) || (o instanceof Immediate);
      }
      return false;
    }
  }

  /** The Class LinkedRValue. */
  private static class LinkedRValue {

    Value value;

    /** The other value. */
    Value otherValue = null;

    /**
     * Instantiates a new linked R value.
     *
     * @param value the value
     */
    private LinkedRValue(Value value) {

      if (value == null) {
        throw new IllegalArgumentException("value may not be null");
      }
      if (canContainValue(value)) {
        this.value = value;
      } else {
        throw new RuntimeException(
            "LinkedRValue "
                + this
                + " cannot contain value: "
                + value
                + " ("
                + value.getClass()
                + ")");
      }
    }

    /**
     * Sets the other value.
     *
     * @param otherValue the new other value
     */
    public void setOtherValue(Value otherValue) {
      this.otherValue = otherValue;
    }

    public boolean canContainValue(Value v) {
      if (v instanceof Immediate || v instanceof ConcreteRef || v instanceof Expr) {
        if (otherValue == null) {
          return true;
        }

        Value o = otherValue;
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
    super(variable, rValue, positionInfo);

    LinkedVariable linkedVariable = new LinkedVariable(variable);
    LinkedRValue linkedRValue = new LinkedRValue(rValue);

    linkedVariable.setOtherValue(getRightOp());
    linkedRValue.setOtherValue(getLeftOp());

    if (!linkedVariable.canContainValue(variable) || linkedRValue.canContainValue(rValue)) {
      throw new RuntimeException(
          "Illegal assignment statement.  Make sure that either left side or right hand side has a local or constant.");
    }
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

    return (AbstractInvokeExpr) getRightOp();
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

    if (getLeftOp() instanceof JArrayRef) {
      return (JArrayRef) getLeftOp();
    } else {
      return (JArrayRef) getRightOp();
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

    if (getLeftOp() instanceof JFieldRef) {
      return (JFieldRef) getLeftOp();
    } else {
      return (JFieldRef) getRightOp();
    }
  }

  /*
   * (non-Javadoc)
   * How can rvalue be an instance of StmtOwner(StmtBoxOwner), or StmtOwner implements Value????
   * @see de.upb.soot.jimple.common.stmt.AbstractStmt#getUnitBoxes()
   */
  @Override
  public List<Stmt> getStmts() {
    // handle possible PhiExpr's
    Value rvalue = getRightOp();
    if (rvalue instanceof StmtOwner) {
      return ((StmtOwner) rvalue).getStmts();
    }

    return super.getStmts();
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getLeftOp().toString() + " = " + getRightOp().toString();
  }

  /*
   * (non-Javadoc)
   *
   * @see de.upb.soot.jimple.common.stmt.Stmt#toString(de.upb.soot.StmtPrinter)
   */
  @Override
  public void toString(StmtPrinter up) {
    getLeftOp().toString(up);
    up.literal(" = ");
    getRightOp().toString(up);
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
    return getLeftOp().equivHashCode() + 31 * getRightOp().equivHashCode();
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
