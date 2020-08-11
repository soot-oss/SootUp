package de.upb.swt.soot.core.jimple.common.expr;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999 Patrick Lam
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

/*
 * Modified by the Sable Research Group and others 1997-1999.
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.visitor.ExprVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import javax.annotation.Nonnull;

/** An expression that returns the length of an array. */
public final class JLengthExpr extends AbstractUnopExpr implements Copyable {

  public JLengthExpr(Value op) {
    super(Jimple.newImmediateBox(op));
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
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
  public void toString(StmtPrinter up) {
    up.literal(Jimple.LENGTHOF);
    up.literal(" ");
    getOpBox().toString(up);
  }

  @Override
  public PrimitiveType getType() {
    return PrimitiveType.getInt();
  }

  @Override
  public void accept(Visitor sw) {
    ((ExprVisitor) sw).caseLengthExpr(this);
  }

  @Nonnull
  public JLengthExpr withOp(Value op) {
    return new JLengthExpr(op);
  }
}
