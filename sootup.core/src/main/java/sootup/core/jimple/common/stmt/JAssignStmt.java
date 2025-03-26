package sootup.core.jimple.common.stmt;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 1997 - 2024 Raja Vallée-Rai and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

/*-
 * #%Value
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Etienne Gagnon, Linghui Luo, Markus Schmidt and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #Value%
 */

import java.util.Optional;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.basic.*;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.Expr;
import sootup.core.jimple.common.expr.JNewArrayExpr;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.expr.JNewMultiArrayExpr;
import sootup.core.jimple.common.expr.JStaticInvokeExpr;
import sootup.core.jimple.common.ref.ConcreteRef;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.visitor.StmtVisitor;
import sootup.core.util.printer.StmtPrinter;

/** Represents the assignment of one value to another */
public final class JAssignStmt extends AbstractDefinitionStmt
    implements FallsThroughStmt, InvokableStmt {

  @NonNull final LValue leftOp;
  @NonNull final Value rightOp;

  /**
   * Instantiates a new JAssignStmt.
   *
   * @param variable the variable on the left side of the assign statement.
   * @param rValue the value on the right side of the assign statement.
   */
  public JAssignStmt(
      @NonNull LValue variable, @NonNull Value rValue, @NonNull StmtPositionInfo positionInfo) {
    super(positionInfo);
    leftOp = variable;
    rightOp = rValue;

    if (!validateValue(rValue)) {
      throw new RuntimeException(
          "Illegal Assignment statement. Make sure that right hand side ("
              + rValue
              + ") is a valid operand.");
    }
  }

  /**
   * returns true if rValue can be on the right side of the assign statement
   *
   * @param rValue the value on the right side of the assign statement.
   */
  private boolean validateValue(@NonNull Value rValue) {
    // constant | local     |  *FieldRef | ArrayRef     | Expr ----> i.e. not IdentityRef
    return rValue instanceof Immediate || rValue instanceof ConcreteRef || rValue instanceof Expr;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.upb.sootup.jimple.common.stmt.AbstractStmt#containsInvokeExpr()
   */
  @Override
  public boolean containsInvokeExpr() {
    return getRightOp() instanceof AbstractInvokeExpr;
  }

  @Override
  public boolean invokesStaticInitializer() {
    if (getInvokeExpr().isPresent() && getInvokeExpr().get() instanceof JStaticInvokeExpr) {
      return true;
    }
    Value rightOp = getRightOp();
    if (rightOp instanceof JStaticFieldRef || getLeftOp() instanceof JStaticFieldRef) {
      return true;
    }
    if (rightOp instanceof JNewExpr) {
      return true;
    }
    if (rightOp instanceof JNewMultiArrayExpr) {
      return !((JNewMultiArrayExpr) rightOp).isArrayOfPrimitives();
    }
    if (rightOp instanceof JNewArrayExpr) {
      return !((JNewArrayExpr) rightOp).isArrayOfPrimitives();
    }
    return false;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.upb.sootup.jimple.common.stmt.AbstractStmt#getInvokeExpr()
   */
  @Override
  public Optional<AbstractInvokeExpr> getInvokeExpr() {
    if (!containsInvokeExpr()) {
      return Optional.empty();
    }
    return Optional.of((AbstractInvokeExpr) getRightOp());
  }

  /*
   * (non-Javadoc)
   *
   * @see de.upb.sootup.jimple.common.stmt.AbstractStmt#containsArrayRef()
   */
  /* added by Feng */
  @Override
  public boolean containsArrayRef() {
    return ((getLeftOp() instanceof JArrayRef) || (getRightOp() instanceof JArrayRef));
  }

  /*
   * (non-Javadoc)
   *
   * @see de.upb.sootup.jimple.common.stmt.AbstractStmt#getArrayRef()
   */
  // TODO [ms]: what is with assignments like: arr[0] = arr[6]? is that possible? if not ->
  // validator
  public JArrayRef getArrayRef() {
    if (getLeftOp() instanceof JArrayRef) {
      return (JArrayRef) getLeftOp();
    } else if (getRightOp() instanceof JArrayRef) {
      return (JArrayRef) getRightOp();
    } else {
      throw new RuntimeException("getArrayRef() called with no ArrayRef present!");
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see de.upb.sootup.jimple.common.stmt.AbstractStmt#containsFieldRef()
   */
  @Override
  public boolean containsFieldRef() {
    return ((getLeftOp() instanceof JFieldRef) || (getRightOp() instanceof JFieldRef));
  }

  /*
   * (non-Javadoc)
   *
   * @see de.upb.sootup.jimple.common.stmt.AbstractStmt#getFieldRef()
   */
  public JFieldRef getFieldRef() {
    // TODO: [MS] what if both Op's are a FieldRef? verify it in a verifier that this does not
    // happen or is it always handled via an intermediate Local?
    if (getLeftOp() instanceof JFieldRef) {
      return (JFieldRef) getLeftOp();
    } else if (getRightOp() instanceof JFieldRef) {
      return (JFieldRef) getRightOp();
    } else {
      throw new RuntimeException("getFieldRef() called with no JFieldRef present!");
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getLeftOp() + " = " + getRightOp();
  }

  /*
   * (non-Javadoc)
   *
   * @see de.upb.sootup.jimple.common.stmt.Stmt#toString(de.upb.sootup.StmtPrinter)
   */
  @Override
  public void toString(@NonNull StmtPrinter up) {
    getLeftOp().toString(up);
    up.literal(" = ");
    getRightOp().toString(up);
  }

  /*
   * (non-Javadoc)
   *
   * @see de.upb.sootup.jimple.common.stmt.AbstractStmt#accept(de.upb.sootup.jimple.visitor.Visitor)
   */
  @Override
  public <V extends StmtVisitor> V accept(@NonNull V v) {
    v.caseAssignStmt(this);
    return v;
  }

  @Override
  public boolean equivTo(Object o, @NonNull JimpleComparator comparator) {
    return comparator.caseAssignStmt(this, o);
  }

  @Override
  public int equivHashCode() {
    return getLeftOp().equivHashCode() + 31 * getRightOp().equivHashCode();
  }

  @NonNull
  @Override
  public LValue getLeftOp() {
    return leftOp;
  }

  @NonNull
  @Override
  public Value getRightOp() {
    return rightOp;
  }

  @Override
  public boolean fallsThrough() {
    return true;
  }

  @Override
  public boolean branches() {
    return false;
  }

  @NonNull
  @Override
  public JAssignStmt withNewDef(@NonNull Local newLocal) {
    // "ReplaceDefVisitor"
    final Value leftOp = getLeftOp();
    LValue newVal;
    if (leftOp instanceof ConcreteRef) {
      if (leftOp instanceof JArrayRef) {
        newVal = ((JArrayRef) leftOp).withBase(newLocal);
      } else if (leftOp instanceof JInstanceFieldRef) {
        newVal = ((JInstanceFieldRef) leftOp).withBase(newLocal);
      } else {
        // JStaticFieldRef -> do nothing
        return this;
      }
    } else {
      // it's a Local
      newVal = newLocal;
    }
    return withVariable(newVal);
  }

  @NonNull
  public JAssignStmt withVariable(@NonNull LValue variable) {
    return new JAssignStmt(variable, getRightOp(), getPositionInfo());
  }

  @NonNull
  public JAssignStmt withRValue(@NonNull Value rValue) {
    return new JAssignStmt(getLeftOp(), rValue, getPositionInfo());
  }

  @NonNull
  public JAssignStmt withPositionInfo(@NonNull StmtPositionInfo positionInfo) {
    return new JAssignStmt(getLeftOp(), getRightOp(), positionInfo);
  }
}
