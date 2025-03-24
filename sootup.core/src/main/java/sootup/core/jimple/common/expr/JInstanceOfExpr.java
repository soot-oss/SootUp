package sootup.core.jimple.common.expr;

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

import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.visitor.ExprVisitor;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.util.printer.StmtPrinter;

/** An expression that checks whether a value is of a certain type. */
public final class JInstanceOfExpr implements Expr {

  private final Immediate op;
  private final Type checkType;

  public JInstanceOfExpr(@NonNull Immediate op, @NonNull Type checkType) {

    this.op = op;
    this.checkType = checkType;
  }

  @Override
  public String toString() {
    return op + " " + Jimple.INSTANCEOF + " " + checkType;
  }

  @Override
  public void toString(@NonNull StmtPrinter up) {
    op.toString(up);
    up.literal(" ");
    up.literal(Jimple.INSTANCEOF);
    up.literal(" ");
    up.literal(Jimple.escape(checkType.toString()));
  }

  @Override
  public boolean equivTo(Object o, @NonNull JimpleComparator comparator) {
    return comparator.caseInstanceOfExpr(this, o);
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return op.equivHashCode() * 101 + checkType.hashCode() * 17;
  }

  public Immediate getOp() {
    return op;
  }

  @Override
  @NonNull
  public Stream<Value> getUses() {
    return Stream.concat(op.getUses(), Stream.of(op));
  }

  @NonNull
  @Override
  public Type getType() {
    return PrimitiveType.getBoolean();
  }

  public Type getCheckType() {
    return checkType;
  }

  @Override
  public <V extends ExprVisitor> V accept(@NonNull V v) {
    v.caseInstanceOfExpr(this);
    return v;
  }

  @NonNull
  public JInstanceOfExpr withOp(@NonNull Immediate op) {
    return new JInstanceOfExpr(op, getCheckType());
  }

  @NonNull
  public JInstanceOfExpr withCheckType(@NonNull Type checkType) {
    return new JInstanceOfExpr(getOp(), checkType);
  }
}
