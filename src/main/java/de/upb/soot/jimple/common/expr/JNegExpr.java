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

package de.upb.soot.jimple.common.expr;

import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.JimpleComparator;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.visitor.IExprVisitor;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.types.PrimitiveType;
import de.upb.soot.types.Type;
import de.upb.soot.types.UnknownType;
import de.upb.soot.util.printer.IStmtPrinter;

public class JNegExpr extends AbstractUnopExpr {
  /** */
  private static final long serialVersionUID = -5215362038683846098L;

  public JNegExpr(Value op) {
    super(Jimple.newImmediateBox(op));
  }

  @Override
  public Object clone() {
    return new JNegExpr(Jimple.cloneIfNecessary(getOp()));
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseNegExpr(this, o);
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return opBox.getValue().equivHashCode();
  }

  @Override
  public String toString() {
    return Jimple.NEG + " " + opBox.getValue().toString();
  }

  @Override
  public void toString(IStmtPrinter up) {
    up.literal(Jimple.NEG);
    up.literal(" ");
    opBox.toString(up);
  }

  @Override
  public Type getType() {
    Value op = opBox.getValue();

    if (op.getType().equals(PrimitiveType.getInt())
        || op.getType().equals(PrimitiveType.getByteSignature())
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
  public void accept(IVisitor sw) {
    ((IExprVisitor) sw).caseNegExpr(this);
  }
}
