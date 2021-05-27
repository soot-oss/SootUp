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

import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.visitor.ExprVisitor;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/** An expression that casts a value to a certain type. */
public final class JCastExpr implements Expr, Copyable {

  private final Value op;
  private final Type type;

  public JCastExpr(@Nonnull Value op, @Nonnull Type type) {
    this.op = op;
    this.type = type;
  }

  @Override
  public boolean equivTo(@Nonnull Object o, @Nonnull JimpleComparator comparator) {
    return comparator.caseCastExpr(this, o);
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return op.equivHashCode() * 101 + type.hashCode() + 17;
  }

  @Override
  public String toString() {
    return "(" + type.toString() + ") " + op.toString();
  }

  @Override
  public void toString(@Nonnull StmtPrinter up) {
    up.literal("(");
    up.typeSignature(type);
    up.literal(") ");
    op.toString(up);
  }

  public Value getOp() {
    return op;
  }

  @Override
  @Nonnull
  public final List<Value> getUses() {
    List<Value> list = new ArrayList<>(op.getUses());
    list.add(op);

    return list;
  }

  @Nonnull
  @Override
  public Type getType() {
    return type;
  }

  @Override
  public void accept(@Nonnull ExprVisitor v) {
    v.caseCastExpr(this);
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
