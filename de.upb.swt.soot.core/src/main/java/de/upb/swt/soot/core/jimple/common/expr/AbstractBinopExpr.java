package de.upb.swt.soot.core.jimple.common.expr;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999-2020 Patrick Lam, Christian Brüggemann, Linghui Luo and others
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

import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.basic.ValueBox;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBinopExpr implements Expr {

  private final ValueBox op1Box;
  private final ValueBox op2Box;

  // new attributes : later if ValueBox is deleted, then add "final" to it.
  private Value op1;
  private Value op2;

  AbstractBinopExpr(ValueBox op1Box, ValueBox op2Box) {
    this.op1Box = op1Box;
    this.op2Box = op2Box;

    // new attributes: later if ValueBox is deleted, then fit the constructor.
    this.op1 = op1Box.getValue();
    this.op2 = op2Box.getValue();
  }

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
    return op1Box.getValue().equivHashCode() * 101 + op2Box.getValue().equivHashCode() + 17
        ^ getSymbol().hashCode();
  }

  /** Returns the unique symbol for an operator. */
  public abstract String getSymbol();

  @Override
  public String toString() {
    Value op1 = op1Box.getValue();
    Value op2 = op2Box.getValue();
    String leftOp = op1.toString();
    String rightOp = op2.toString();
    return leftOp + getSymbol() + rightOp;
  }

  @Override
  public void toString(StmtPrinter up) {
    op1Box.toString(up);
    up.literal(getSymbol());
    op2Box.toString(up);
  }
}
