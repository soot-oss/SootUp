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

import de.upb.swt.soot.core.jimple.basic.Immediate;
import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.util.printer.StmtPrinter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBinopExpr implements Expr {

  private final Value op1;
  private final Value op2;

  AbstractBinopExpr(@Nonnull Immediate op1, @Nonnull Immediate op2) {
    this.op1 = (Value) op1;
    this.op2 = (Value) op2;

  }

  public Value getOp1() {
    return op1;
  }

  public Value getOp2() {
    return op2;
  }

  @Override
  public final List<Value> getUses() {
    List<Value> list = new ArrayList<>(op1.getUses());
    list.add(op1);
    list.addAll(op2.getUses());
    list.add(op2);
    return list;
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseAbstractBinopExpr(this, o);
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return op1.equivHashCode() * 101 + op2.equivHashCode() + 17 ^ getSymbol().hashCode();
  }

  /** Returns the unique symbol for an operator. */
  public abstract String getSymbol();

  @Override
  public String toString() {
    String leftOp = op1.toString();
    String rightOp = op2.toString();
    return leftOp + getSymbol() + rightOp;
  }

  @Override
  public void toString(StmtPrinter up) {
    op1.toString(up);
    up.literal(getSymbol());
    op2.toString(up);
  }
}
