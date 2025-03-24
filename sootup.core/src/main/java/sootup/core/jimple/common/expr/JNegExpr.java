package sootup.core.jimple.common.expr;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999-2020 Patrick Lam, Christian Brüggemann, Linghui Luo
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

import org.jspecify.annotations.NonNull;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.visitor.ExprVisitor;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.types.UnknownType;
import sootup.core.util.printer.StmtPrinter;

/** An expression that negates its operand (-). */
public final class JNegExpr extends AbstractUnopExpr {

  public JNegExpr(@NonNull Immediate op) {
    super(op);
  }

  @Override
  public boolean equivTo(Object o, @NonNull JimpleComparator comparator) {
    return comparator.caseNegExpr(this, o);
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return getOp().equivHashCode();
  }

  @Override
  public String toString() {
    return Jimple.NEG + " " + getOp();
  }

  @Override
  public void toString(@NonNull StmtPrinter up) {
    up.literal(Jimple.NEG);
    up.literal(" ");
    getOp().toString(up);
  }

  @NonNull
  @Override
  public Type getType() {
    Value op = getOp();

    // TODO ms: create a TypeVisitor for this -> O(1)
    if (op.getType() == PrimitiveType.getInt()
        || op.getType() == PrimitiveType.getByte()
        || op.getType() == PrimitiveType.getShort()
        || op.getType() == PrimitiveType.getBoolean()
        || op.getType() == PrimitiveType.getChar()) {
      return PrimitiveType.getInt();
    } else if (op.getType() == PrimitiveType.getLong()) {
      return PrimitiveType.getLong();
    } else if (op.getType() == PrimitiveType.getDouble()) {
      return PrimitiveType.getDouble();
    } else if (op.getType() == PrimitiveType.getFloat()) {
      return PrimitiveType.getFloat();
    } else {
      return UnknownType.getInstance();
    }
  }

  @Override
  public <V extends ExprVisitor> V accept(@NonNull V v) {
    v.caseNegExpr(this);
    return v;
  }

  @NonNull
  public JNegExpr withOp(@NonNull Immediate op) {
    return new JNegExpr(op);
  }
}
