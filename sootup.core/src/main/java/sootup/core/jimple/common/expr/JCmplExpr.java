package sootup.core.jimple.common.expr;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999-2020 Patrick Lam, Linghui Luo, Christian Brüggemann and others
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

import javax.annotation.Nonnull;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.visitor.ExprVisitor;
import sootup.core.util.Copyable;

public final class JCmplExpr extends AbstractIntBinopExpr implements Copyable {

  public JCmplExpr(@Nonnull Immediate op1, @Nonnull Immediate op2) {
    super(op1, op2);
  }

  @Nonnull
  @Override
  public final String getSymbol() {
    return " " + Jimple.CMPL + " ";
  }

  @Override
  public void accept(@Nonnull ExprVisitor v) {
    v.caseCmplExpr(this);
  }

  @Nonnull
  public JCmplExpr withOp1(@Nonnull Immediate op1) {
    return new JCmplExpr(op1, getOp2());
  }

  @Nonnull
  public JCmplExpr withOp2(@Nonnull Immediate op2) {
    return new JCmplExpr(getOp1(), op2);
  }
}
