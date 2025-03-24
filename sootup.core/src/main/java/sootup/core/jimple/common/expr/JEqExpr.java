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

import org.jspecify.annotations.NonNull;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.visitor.ExprVisitor;

/** An expression that checks whether two value are equal. */
public final class JEqExpr extends AbstractConditionExpr {

  public JEqExpr(@NonNull Immediate op1, @NonNull Immediate op2) {
    super(op1, op2);
  }

  @NonNull
  @Override
  public String getSymbol() {
    return " == ";
  }

  @Override
  public <V extends ExprVisitor> V accept(@NonNull V v) {
    v.caseEqExpr(this);
    return v;
  }

  @NonNull
  public JEqExpr withOp1(@NonNull Immediate op1) {
    return new JEqExpr(op1, getOp2());
  }

  @NonNull
  public JEqExpr withOp2(@NonNull Immediate op2) {
    return new JEqExpr(getOp1(), op2);
  }
}
