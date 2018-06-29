/* Soot - a J*va Optimization Framework
 * Copyright (C) 1999 Patrick Lam
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.  
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

package de.upb.soot.jimple.common.stmt;

import de.upb.soot.StmtPrinter;
import de.upb.soot.jimple.Immediate;
import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.RValueBox;
import de.upb.soot.jimple.StmtBox;
import de.upb.soot.jimple.StmtBoxOwner;
import de.upb.soot.jimple.Value;
import de.upb.soot.jimple.ValueBox;
import de.upb.soot.jimple.VariableBox;
import de.upb.soot.jimple.common.expr.AbstractInvokeExpr;
import de.upb.soot.jimple.common.ref.ArrayRef;
import de.upb.soot.jimple.common.ref.FieldRef;
import de.upb.soot.jimple.visitor.IStmtVisitor;
import de.upb.soot.jimple.visitor.IVisitor;

import java.util.List;

public class JAssignStmt extends AbstractDefinitionStmt {
  private static class LinkedVariableBox extends VariableBox {
    ValueBox otherBox = null;

    private LinkedVariableBox(Value v) {
      super(v);
    }

    public void setOtherBox(ValueBox otherBox) {
      this.otherBox = otherBox;
    }

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

  private static class LinkedRValueBox extends RValueBox {
    ValueBox otherBox = null;

    private LinkedRValueBox(Value v) {
      super(v);
    }

    public void setOtherBox(ValueBox otherBox) {
      this.otherBox = otherBox;
    }

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

  public JAssignStmt(Value variable, Value rvalue) {
    this(new LinkedVariableBox(variable), new LinkedRValueBox(rvalue));

    ((LinkedVariableBox) leftBox).setOtherBox(rightBox);
    ((LinkedRValueBox) rightBox).setOtherBox(leftBox);

    if (!leftBox.canContainValue(variable) || !rightBox.canContainValue(rvalue)) {
      throw new RuntimeException(
          "Illegal assignment statement.  Make sure that either left side or right hand side has a local or constant.");
    }

  }

  protected JAssignStmt(ValueBox variableBox, ValueBox rvalueBox) {
    super(variableBox, rvalueBox);
  }

  @Override
  public boolean containsInvokeExpr() {
    return getRightOp() instanceof AbstractInvokeExpr;
  }

  @Override
  public AbstractInvokeExpr getInvokeExpr() {
    if (!containsInvokeExpr()) {
      throw new RuntimeException("getInvokeExpr() called with no invokeExpr present!");
    }

    return (AbstractInvokeExpr) rightBox.getValue();
  }

  @Override
  public ValueBox getInvokeExprBox() {
    if (!containsInvokeExpr()) {
      throw new RuntimeException("getInvokeExpr() called with no invokeExpr present!");
    }

    return rightBox;
  }

  /* added by Feng */
  @Override
  public boolean containsArrayRef() {
    return ((getLeftOp() instanceof ArrayRef) || (getRightOp() instanceof ArrayRef));
  }

  @Override
  public ArrayRef getArrayRef() {
    if (!containsArrayRef()) {
      throw new RuntimeException("getArrayRef() called with no ArrayRef present!");
    }

    if (leftBox.getValue() instanceof ArrayRef) {
      return (ArrayRef) leftBox.getValue();
    } else {
      return (ArrayRef) rightBox.getValue();
    }
  }

  @Override
  public ValueBox getArrayRefBox() {
    if (!containsArrayRef()) {
      throw new RuntimeException("getArrayRefBox() called with no ArrayRef present!");
    }

    if (leftBox.getValue() instanceof ArrayRef) {
      return leftBox;
    } else {
      return rightBox;
    }
  }

  @Override
  public boolean containsFieldRef() {
    return ((getLeftOp() instanceof FieldRef) || (getRightOp() instanceof FieldRef));
  }

  @Override
  public FieldRef getFieldRef() {
    if (!containsFieldRef()) {
      throw new RuntimeException("getFieldRef() called with no FieldRef present!");
    }

    if (leftBox.getValue() instanceof FieldRef) {
      return (FieldRef) leftBox.getValue();
    } else {
      return (FieldRef) rightBox.getValue();
    }
  }

  @Override
  public ValueBox getFieldRefBox() {
    if (!containsFieldRef()) {
      throw new RuntimeException("getFieldRefBox() called with no FieldRef present!");
    }

    if (leftBox.getValue() instanceof FieldRef) {
      return leftBox;
    } else {
      return rightBox;
    }
  }

  @Override
  public List<StmtBox> getUnitBoxes() {
    // handle possible PhiExpr's
    Value rvalue = rightBox.getValue();
    if (rvalue instanceof StmtBoxOwner) {
      return ((StmtBoxOwner) rvalue).getStmtBoxes();
    }

    return super.getUnitBoxes();
  }

  @Override
  public String toString() {
    return leftBox.getValue().toString() + " = " + rightBox.getValue().toString();
  }

  @Override
  public void toString(StmtPrinter up) {
    leftBox.toString(up);
    up.literal(" = ");
    rightBox.toString(up);
  }

  @Override
  public Object clone() {
    return new JAssignStmt(Jimple.cloneIfNecessary(getLeftOp()), Jimple.cloneIfNecessary(getRightOp()));
  }

  public void setLeftOp(Value variable) {
    getLeftOpBox().setValue(variable);
  }

  public void setRightOp(Value rvalue) {
    getRightOpBox().setValue(rvalue);
  }

  @Override
  public void accept(IVisitor sw) {
    ((IStmtVisitor) sw).caseAssignStmt(this);
  }

}
