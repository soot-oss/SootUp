package sootup.core.jimple.common.expr;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999-2020 Patrick Lam, Linghui Luo, Christian Brüggemann
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
import sootup.core.jimple.visitor.ExprVisitor;
import sootup.core.types.PrimitiveType;
import sootup.core.util.printer.StmtPrinter;

/** An expression that returns the length of an array. */
public final class JLengthExpr extends AbstractUnopExpr {

  public JLengthExpr(@NonNull Immediate op) {
    super(op);
  }

  @Override
  public boolean equivTo(Object o, @NonNull JimpleComparator comparator) {
    return comparator.caseLengthExpr(this, o);
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return getOp().equivHashCode();
  }

  @Override
  public String toString() {
    return Jimple.LENGTHOF + " " + getOp();
  }

  @Override
  public void toString(@NonNull StmtPrinter up) {
    up.literal(Jimple.LENGTHOF);
    up.literal(" ");
    getOp().toString(up);
  }

  @NonNull
  @Override
  public PrimitiveType getType() {
    return PrimitiveType.getInt();
  }

  @Override
  public <V extends ExprVisitor> V accept(@NonNull V v) {
    v.caseLengthExpr(this);
    return v;
  }

  @NonNull
  public JLengthExpr withOp(@NonNull Immediate op) {
    return new JLengthExpr(op);
  }
}
