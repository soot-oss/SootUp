package de.upb.swt.soot.core.jimple.common.stmt;

/*-
 * #%L
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
 * #L%
 */

import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.common.expr.Expr;
import de.upb.swt.soot.core.jimple.common.ref.ConcreteRef;
import de.upb.swt.soot.core.jimple.common.ref.JArrayRef;
import de.upb.swt.soot.core.jimple.common.ref.JFieldRef;
import de.upb.swt.soot.core.jimple.visitor.StmtVisitor;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import javax.annotation.Nonnull;

/** Represents the assignment of one value to another */
public final class JAssignStmt extends AbstractDefinitionStmt implements Copyable {

  /**
   * Instantiates a new JAssignStmt.
   *
   * @param variable the variable on the left side of the assign statement.
   * @param rValue the value on the right side of the assign statement.
   */
  public JAssignStmt(
      @Nonnull Value variable, @Nonnull Value rValue, @Nonnull StmtPositionInfo positionInfo) {
    super(variable, rValue, positionInfo);
    if (!checkVariable(variable)) {
      throw new RuntimeException(
          "Illegal Assignment statement. Make sure that left hand side has a valid operand.");
    }
    if (!checkValue(rValue)) {
      throw new RuntimeException(
          "Illegal Assignment statement. Make sure that right hand side has a valid operand.");
    }
  }

  /**
   * returns true if variable can be on the left side of the assign statement
   *
   * @param variable the variable on the left side of the assign statement.
   */
  private boolean checkVariable(@Nonnull Value variable) {
    return variable instanceof Local || variable instanceof ConcreteRef;
  }

  /**
   * returns true if rValue can be on the right side of the assign statement
   *
   * @param rValue the value on the right side of the assign statement.
   */
  private boolean checkValue(@Nonnull Value rValue) {
    return rValue instanceof Immediate || rValue instanceof ConcreteRef || rValue instanceof Expr;
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
    return getLeftOp().toString() + " = " + getRightOp().toString();
  }

  /*
   * (non-Javadoc)
   *
   * @see de.upb.soot.jimple.common.stmt.Stmt#toString(de.upb.soot.StmtPrinter)
   */
  @Override
  public void toString(@Nonnull StmtPrinter up) {
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
  public void accept(@Nonnull StmtVisitor sw) {
    sw.caseAssignStmt(this);
  }

  @Override
  public boolean equivTo(Object o, @Nonnull JimpleComparator comparator) {
    return comparator.caseAssignStmt(this, o);
  }

  @Override
  public int equivHashCode() {
    return getLeftOp().equivHashCode() + 31 * getRightOp().equivHashCode();
  }

  @Nonnull
  public JAssignStmt withVariable(@Nonnull Value variable) {
    return new JAssignStmt(variable, getRightOp(), getPositionInfo());
  }

  @Nonnull
  public JAssignStmt withRValue(@Nonnull Value rValue) {
    return new JAssignStmt(getLeftOp(), rValue, getPositionInfo());
  }

  @Nonnull
  public JAssignStmt withPositionInfo(@Nonnull StmtPositionInfo positionInfo) {
    return new JAssignStmt(getLeftOp(), getRightOp(), positionInfo);
  }
}
