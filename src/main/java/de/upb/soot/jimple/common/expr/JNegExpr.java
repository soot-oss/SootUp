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
import de.upb.soot.signatures.PrimitiveTypeSignature;
import de.upb.soot.signatures.TypeSignature;
import de.upb.soot.signatures.UnknownTypeSignature;
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
  public TypeSignature getSignature() {
    Value op = opBox.getValue();

    if (op.getSignature().equals(PrimitiveTypeSignature.getIntSignature())
        || op.getSignature().equals(PrimitiveTypeSignature.getByteSignature())
        || op.getSignature().equals(PrimitiveTypeSignature.getShortSignature())
        || op.getSignature().equals(PrimitiveTypeSignature.getBooleanSignature())
        || op.getSignature().equals(PrimitiveTypeSignature.getCharSignature())) {
      return PrimitiveTypeSignature.getIntSignature();
    } else if (op.getSignature().equals(PrimitiveTypeSignature.getLongSignature())) {
      return PrimitiveTypeSignature.getLongSignature();
    } else if (op.getSignature().equals(PrimitiveTypeSignature.getDoubleSignature())) {
      return PrimitiveTypeSignature.getDoubleSignature();
    } else if (op.getSignature().equals(PrimitiveTypeSignature.getFloatSignature())) {
      return PrimitiveTypeSignature.getFloatSignature();
    } else {
      return UnknownTypeSignature.getInstance();
    }
  }

  @Override
  public void accept(IVisitor sw) {
    ((IExprVisitor) sw).caseNegExpr(this);
  }
}
