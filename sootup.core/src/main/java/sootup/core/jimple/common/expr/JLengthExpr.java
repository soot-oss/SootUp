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

import javax.annotation.Nonnull;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.visitor.ExprVisitor;
import sootup.core.types.PrimitiveType;
import sootup.core.util.Copyable;
import sootup.core.util.printer.StmtPrinter;

/** An expression that returns the length of an array. */
public final class JLengthExpr extends AbstractUnopExpr implements Copyable {

  public JLengthExpr(@Nonnull Immediate op) {
    super(op);
  }

  @Override
  public boolean equivTo(Object o, @Nonnull JimpleComparator comparator) {
    return comparator.caseLengthExpr(this, o);
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return getOp().equivHashCode();
  }

  @Override
  public String toString() {
    return Jimple.LENGTHOF + " " + getOp().toString();
  }

  @Override
  public void toString(@Nonnull StmtPrinter up) {
    up.literal(Jimple.LENGTHOF);
    up.literal(" ");
    getOp().toString(up);
  }

  @Nonnull
  @Override
  public PrimitiveType getType() {
    return PrimitiveType.getInt();
  }

  @Override
  public void accept(@Nonnull ExprVisitor v) {
    v.caseLengthExpr(this);
  }

  @Nonnull
  public JLengthExpr withOp(@Nonnull Immediate op) {
    return new JLengthExpr(op);
  }
}
