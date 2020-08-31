package de.upb.swt.soot.core.jimple.common.expr;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999-2020 Patrick Lam, Linghui Luo, Markus Schmidt and others
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

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.basic.ValueBox;
import de.upb.swt.soot.core.jimple.visitor.ExprVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/** An expression that checks whether a value is of a certain type. */
public final class JInstanceOfExpr implements Expr, Copyable {

  private final ValueBox opBox;
  private final Type checkType;

  // new attribute: later if ValueBox is deleted, then add "final" to it.
  private Value op;

  public JInstanceOfExpr(Value op, Type checkType) {
    this.opBox = Jimple.newImmediateBox(op);
    this.checkType = checkType;
    // new attribute: later if ValueBox is deleted, then fit the constructor.
    this.op = op;
  }

  @Override
  public String toString() {
    return opBox.getValue().toString() + " " + Jimple.INSTANCEOF + " " + checkType.toString();
  }

  @Override
  public void toString(StmtPrinter up) {
    opBox.toString(up);
    up.literal(" ");
    up.literal(Jimple.INSTANCEOF);
    up.literal(" ");
    up.literal(Jimple.escape(checkType.toString()));
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseInstanceOfExpr(this, o);
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return opBox.getValue().equivHashCode() * 101 + checkType.hashCode() * 17;
  }

  public Value getOp() {
    return opBox.getValue();
  }

  public ValueBox getOpBox() {
    return opBox;
  }

  @Override
  public final List<Value> getUses() {
    List<Value> list = new ArrayList<>(op.getUses());
    list.add(op);
    return list;
  }

  @Override
  public Type getType() {
    return PrimitiveType.getBoolean();
  }

  public Type getCheckType() {
    return checkType;
  }

  @Override
  public void accept(Visitor sw) {
    ((ExprVisitor) sw).caseInstanceOfExpr(this);
  }

  @Nonnull
  public JInstanceOfExpr withOp(Value op) {
    return new JInstanceOfExpr(op, checkType);
  }

  @Nonnull
  public JInstanceOfExpr withCheckType(Type checkType) {
    return new JInstanceOfExpr(getOp(), checkType);
  }
}
