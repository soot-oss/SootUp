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
import javax.annotation.Nonnull;

public abstract class AbstractBinopExpr implements Expr {

  @Nonnull private final ValueBox op1Box;
  @Nonnull private final ValueBox op2Box;

  AbstractBinopExpr(@Nonnull ValueBox op1Box, @Nonnull ValueBox op2Box) {
    this.op1Box = op1Box;
    this.op2Box = op2Box;
  }

  @Nonnull
  public Value getOp1() {
    return op1Box.getValue();
  }

  @Nonnull
  public Value getOp2() {
    return op2Box.getValue();
  }

  @Nonnull
  public ValueBox getOp1Box() {
    return op1Box;
  }

  @Nonnull
  public ValueBox getOp2Box() {
    return op2Box;
  }

  @Override
  @Nonnull
  public List<Value> getUses() {
    List<Value> list = new ArrayList<>(op1Box.getValue().getUses());
    list.add(op1Box.getValue());
    list.addAll(op2Box.getValue().getUses());
    list.add(op2Box.getValue());
    return list;
  }

  @Override
  public boolean equivTo(@Nonnull Object o, @Nonnull JimpleComparator comparator) {
    return comparator.caseAbstractBinopExpr(this, o);
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return op1Box.getValue().equivHashCode() * 101 + op2Box.getValue().equivHashCode() + 17
        ^ getSymbol().hashCode();
  }

  /** Returns the unique symbol for an operator. */
  @Nonnull
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
  public void toString(@Nonnull StmtPrinter up) {
    op1Box.toString(up);
    up.literal(getSymbol());
    op2Box.toString(up);
  }

  @Nonnull
  public abstract AbstractBinopExpr withOp1(Value value);

  @Nonnull
  public abstract AbstractBinopExpr withOp2(Value value);
}
