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

import de.upb.soot.jimple.basic.JimpleComparator;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.util.printer.IStmtPrinter;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBinopExpr implements Expr {
  /** */
  private static final long serialVersionUID = -4967924464687577438L;

  protected ValueBox op1Box;
  protected ValueBox op2Box;

  public Value getOp1() {
    return op1Box.getValue();
  }

  public Value getOp2() {
    return op2Box.getValue();
  }

  public ValueBox getOp1Box() {
    return op1Box;
  }

  public ValueBox getOp2Box() {
    return op2Box;
  }

  public void setOp1(Value op1) {
    op1Box.setValue(op1);
  }

  public void setOp2(Value op2) {
    op2Box.setValue(op2);
  }

  @Override
  public final List<ValueBox> getUseBoxes() {

    List<ValueBox> list = new ArrayList<>(op1Box.getValue().getUseBoxes());
    list.add(op1Box);
    list.addAll(op2Box.getValue().getUseBoxes());
    list.add(op2Box);

    return list;
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseAbstractBinopExpr(this, o);
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return op1Box.getValue().equivHashCode() * 101 + op2Box.getValue().equivHashCode() + 17
        ^ getSymbol().hashCode();
  }

  /** Returns the unique symbol for an operator. */
  public abstract String getSymbol();

  @Override
  public abstract Object clone();

  @Override
  public String toString() {
    Value op1 = op1Box.getValue();
    Value op2 = op2Box.getValue();
    String leftOp = op1.toString();
    String rightOp = op2.toString();
    return leftOp + getSymbol() + rightOp;
  }

  @Override
  public void toString(IStmtPrinter up) {
    op1Box.toString(up);
    up.literal(getSymbol());
    op2Box.toString(up);
  }
}
