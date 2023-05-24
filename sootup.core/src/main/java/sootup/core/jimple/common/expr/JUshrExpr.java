package sootup.core.jimple.common.expr;

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

import javax.annotation.Nonnull;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.visitor.ExprVisitor;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.types.UnknownType;
import sootup.core.util.Copyable;

/** Similar to {@link JShrExpr}, but shifts zero into the leftmost position. */
public final class JUshrExpr extends AbstractIntLongBinopExpr implements Copyable {
  public JUshrExpr(@Nonnull Immediate op1, @Nonnull Immediate op2) {
    super(op1, op2);
  }

  @Nonnull
  @Override
  public final String getSymbol() {
    return " >>> ";
  }

  @Override
  public void accept(@Nonnull ExprVisitor v) {
    v.caseUshrExpr(this);
  }

  @Nonnull
  @Override
  public Type getType() {
    Value op1 = getOp1();
    Value op2 = getOp2();

    if (Type.isIntLikeType(op2.getType())) {
      return UnknownType.getInstance();
    }

    if (Type.isIntLikeType(op1.getType())) {
      return PrimitiveType.getInt();
    }
    if (op1.getType().equals(PrimitiveType.getLong())) {
      return PrimitiveType.getLong();
    }

    return UnknownType.getInstance();
  }

  @Nonnull
  public JUshrExpr withOp1(@Nonnull Immediate op1) {
    return new JUshrExpr(op1, getOp2());
  }

  @Nonnull
  public JUshrExpr withOp2(@Nonnull Immediate op2) {
    return new JUshrExpr(getOp1(), op2);
  }
}
