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
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.types.UnknownType;
import de.upb.swt.soot.core.util.Copyable;
import javax.annotation.Nonnull;

/** An expression that shifts its operand to the left (<<). */
public final class JShlExpr extends AbstractIntLongBinopExpr implements Copyable {

  public JShlExpr(Value op1, Value op2) {
    super(op1, op2);
  }

  @Override
  public String getSymbol() {
    return " << ";
  }

  @Override
  public void accept(@Nonnull ExprVisitor sw) {
    sw.caseShlExpr(this);
  }

  @Nonnull
  @Override
  public Type getType() {
    Value op1 = getOp1();
    Value op2 = getOp2();

    if (!PrimitiveType.isIntLikeType(op2.getType())) {
      return UnknownType.getInstance();
    }

    if (PrimitiveType.isIntLikeType(op1.getType())) {
      return PrimitiveType.getInt();
    }
    if (op1.getType().equals(PrimitiveType.getLong())) {
      return PrimitiveType.getLong();
    }

    return UnknownType.getInstance();
  }

  @Nonnull
  public JShlExpr withOp1(Value op1) {
    return new JShlExpr(op1, getOp2());
  }

  @Nonnull
  public JShlExpr withOp2(Value op2) {
    return new JShlExpr(getOp1(), op2);
  }
}
