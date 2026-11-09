package sootup.core.jimple.common.expr;

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

import java.util.List;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Value;
import sootup.core.util.printer.StmtPrinter;

/** Abstract base class for binary operator expressions. */
public abstract class AbstractBinopExpr implements Expr {

  @NonNull private final Immediate op1;
  @NonNull private final Immediate op2;

  AbstractBinopExpr(@NonNull Immediate op1, @NonNull Immediate op2) {
    this.op1 = op1;
    this.op2 = op2;
  }

  /** Returns the left operand of this binary expression. */
  @NonNull
  public Immediate getOp1() {
    return op1;
  }

  /** Returns the right operand of this binary expression. */
  @NonNull
  public Immediate getOp2() {
    return op2;
  }

  @Override
  public void collectUses(List<Value> collector) {
    op1.collectUses(collector);
    collector.add(op1);
    op2.collectUses(collector);
    collector.add(op2);
  }

  @Override
  public boolean equivTo(@NonNull Object o, @NonNull JimpleComparator comparator) {
    return comparator.caseAbstractBinopExpr(this, o);
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return op1.equivHashCode() * 101 + op2.equivHashCode() + 17 ^ getSymbol().hashCode();
  }

  /**
   * Returns the unique symbol for an operator.
   *
   * @return the operator symbol string (e.g., "+", "-")
   */
  @NonNull
  public abstract String getSymbol();

  @Override
  public String toString() {
    String leftOp = op1.toString();
    String rightOp = op2.toString();
    return leftOp + getSymbol() + rightOp;
  }

  @Override
  public void toString(@NonNull StmtPrinter up) {
    op1.toString(up);
    up.literal(getSymbol());
    op2.toString(up);
  }

  /** Returns a copy of this expression with the left operand replaced. */
  @NonNull
  public abstract AbstractBinopExpr withOp1(@NonNull Immediate value);

  /** Returns a copy of this expression with the right operand replaced. */
  @NonNull
  public abstract AbstractBinopExpr withOp2(@NonNull Immediate value);
}
