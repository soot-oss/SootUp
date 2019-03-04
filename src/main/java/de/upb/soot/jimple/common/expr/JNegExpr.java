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
import de.upb.soot.jimple.common.type.BooleanType;
import de.upb.soot.jimple.common.type.ByteType;
import de.upb.soot.jimple.common.type.CharType;
import de.upb.soot.jimple.common.type.DoubleType;
import de.upb.soot.jimple.common.type.FloatType;
import de.upb.soot.jimple.common.type.IntType;
import de.upb.soot.jimple.common.type.LongType;
import de.upb.soot.jimple.common.type.ShortType;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.jimple.common.type.UnknownType;
import de.upb.soot.jimple.visitor.IExprVisitor;
import de.upb.soot.jimple.visitor.IVisitor;
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

  /** Compares the specified object with this one for structural equality. */
  @Override
  public boolean equivTo(Object o) {
    return JimpleComparator.getInstance().caseNegExpr(this, o);
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

    if (op.getType().equals(IntType.INSTANCE)
        || op.getType().equals(ByteType.INSTANCE)
        || op.getType().equals(ShortType.INSTANCE)
        || op.getType().equals(BooleanType.INSTANCE)
        || op.getType().equals(CharType.INSTANCE)) {
      return IntType.INSTANCE;
    } else if (op.getType().equals(LongType.INSTANCE)) {
      return LongType.INSTANCE;
    } else if (op.getType().equals(DoubleType.INSTANCE)) {
      return DoubleType.INSTANCE;
    } else if (op.getType().equals(FloatType.INSTANCE)) {
      return FloatType.INSTANCE;
    } else {
      return UnknownType.INSTANCE;
    }
  }

  @Override
  public void accept(IVisitor sw) {
    ((IExprVisitor) sw).caseNegExpr(this);
  }
}
