package de.upb.swt.soot.core.jimple.common.expr;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999-2020 Patrick Lam, Christian Brüggemann, Linghui Luo
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

import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.visitor.ExprVisitor;
import de.upb.swt.soot.core.util.Copyable;
import javax.annotation.Nonnull;

/** The opposite of {@link JEqExpr} */
public final class JNeExpr extends AbstractConditionExpr implements Copyable {

  public JNeExpr(Value op1, Value op2) {
    super(op1, op2);
  }

  @Override
  public final String getSymbol() {
    return " != ";
  }

  @Override
  public void accept(@Nonnull ExprVisitor sw) {
    sw.caseNeExpr(this);
  }

  @Nonnull
  public JNeExpr withOp1(Value op1) {
    return new JNeExpr(op1, getOp2());
  }

  @Nonnull
  public JNeExpr withOp2(Value op2) {
    return new JNeExpr(getOp1(), op2);
  }
}
