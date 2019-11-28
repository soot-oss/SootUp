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
import de.upb.swt.soot.core.jimple.basic.ValueBox;
import de.upb.swt.soot.core.jimple.visitor.ExprVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/** An expression that checks whether a value is of a certain type. */
public final class JInstanceOfExpr implements Expr, Copyable {

  private final ValueBox opBox;
  private final Type checkType;

  public JInstanceOfExpr(Value op, Type checkType) {
    this.opBox = Jimple.newImmediateBox(op);
    this.checkType = checkType;
  }

  @Override
  public String toString() {
    return opBox.getValue().toString() + " " + Jimple.INSTANCEOF + " " + checkType.toString();
  }

  @Override
  public void toString(StmtPrinter up) {
    opBox.toString(up);
    up.literal(" ");
    up.literal(Jimple.INSTANCEOF);
    up.literal(" ");
    up.literal(checkType.toString());
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseInstanceOfExpr(this, o);
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return opBox.getValue().equivHashCode() * 101 + checkType.hashCode() * 17;
  }

  public Value getOp() {
    return opBox.getValue();
  }

  public ValueBox getOpBox() {
    return opBox;
  }

  @Override
  public final List<ValueBox> getUseBoxes() {

    List<ValueBox> list = new ArrayList<>(opBox.getValue().getUseBoxes());
    list.add(opBox);

    return list;
  }

  @Override
  public Type getType() {
    return PrimitiveType.getBoolean();
  }

  public Type getCheckType() {
    return checkType;
  }

  @Override
  public void accept(Visitor sw) {
    ((ExprVisitor) sw).caseInstanceOfExpr(this);
  }

  @Nonnull
  public JInstanceOfExpr withOp(Value op) {
    return new JInstanceOfExpr(op, checkType);
  }

  @Nonnull
  public JInstanceOfExpr withCheckType(Type checkType) {
    return new JInstanceOfExpr(getOp(), checkType);
  }
}
