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
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/** An expression that casts a value to a certain type. */
public final class JCastExpr implements Expr, Copyable {

  private final ValueBox opBox;
  private final Type type;
  // new attribute: later if ValueBox is deleted, then add "final" to it.
  private final Value op;

  public JCastExpr(Value op, Type type) {
    this.opBox = Jimple.newImmediateBox(op);
    this.type = type;
    // new attribute: later if ValueBox is deleted, then fit the constructor.
    this.op = op;
  }

  @Override
  public boolean equivTo(@Nonnull Object o, @Nonnull JimpleComparator comparator) {
    return comparator.caseCastExpr(this, o);
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return opBox.getValue().equivHashCode() * 101 + type.hashCode() + 17;
  }

  @Override
  public String toString() {
    return "(" + type.toString() + ") " + opBox.getValue().toString();
  }

  @Override
  public void toString(@Nonnull StmtPrinter up) {
    up.literal("(");
    up.typeSignature(type);
    up.literal(") ");
    opBox.toString(up);
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
    return type;
  }

  @Override
  public void accept(@Nonnull Visitor sw) {
    ((ExprVisitor) sw).caseCastExpr(this);
  }

  @Nonnull
  public JCastExpr withOp(@Nonnull Value op) {
    return new JCastExpr(op, type);
  }

  @Nonnull
  public JCastExpr withType(@Nonnull Type type) {
    return new JCastExpr(getOp(), type);
  }
}
