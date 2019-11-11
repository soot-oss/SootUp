/* Soot - a J*va Optimization Framework
 * Copyright (C) 1999 Patrick Lam
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

package de.upb.swt.soot.core.jimple.common.expr;

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.visitor.ExprVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.types.UnknownType;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import javax.annotation.Nonnull;

/** An expression that negates its operand (-). */
public final class JNegExpr extends AbstractUnopExpr implements Copyable {

  public JNegExpr(Value op) {
    super(Jimple.newImmediateBox(op));
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseNegExpr(this, o);
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return getOp().equivHashCode();
  }

  @Override
  public String toString() {
    return Jimple.NEG + " " + getOp().toString();
  }

  @Override
  public void toString(StmtPrinter up) {
    up.literal(Jimple.NEG);
    up.literal(" ");
    getOpBox().toString(up);
  }

  @Override
  public Type getType() {
    Value op = getOp();

    if (op.getType().equals(PrimitiveType.getInt())
        || op.getType().equals(PrimitiveType.getByte())
        || op.getType().equals(PrimitiveType.getShort())
        || op.getType().equals(PrimitiveType.getBoolean())
        || op.getType().equals(PrimitiveType.getChar())) {
      return PrimitiveType.getInt();
    } else if (op.getType().equals(PrimitiveType.getLong())) {
      return PrimitiveType.getLong();
    } else if (op.getType().equals(PrimitiveType.getDouble())) {
      return PrimitiveType.getDouble();
    } else if (op.getType().equals(PrimitiveType.getFloat())) {
      return PrimitiveType.getFloat();
    } else {
      return UnknownType.getInstance();
    }
  }

  @Override
  public void accept(Visitor sw) {
    ((ExprVisitor) sw).caseNegExpr(this);
  }

  @Nonnull
  public JNegExpr withOp(Value op) {
    return new JNegExpr(op);
  }
}
