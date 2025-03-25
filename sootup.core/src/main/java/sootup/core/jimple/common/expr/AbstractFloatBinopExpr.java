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

import org.jspecify.annotations.NonNull;
import sootup.core.jimple.basic.Immediate;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.types.UnknownType;

public abstract class AbstractFloatBinopExpr extends AbstractBinopExpr {

  protected AbstractFloatBinopExpr(@NonNull Immediate op1, @NonNull Immediate op2) {
    super(op1, op2);
  }

  @NonNull
  @Override
  public Type getType() {
    Immediate op1 = getOp1();
    Immediate op2 = getOp2();
    Type op1t = op1.getType();
    Type op2t = op2.getType();

    if (Type.isIntLikeType(op1t) && Type.isIntLikeType(op2t)) {
      return PrimitiveType.getInt();
    }
    final PrimitiveType.LongType longType = PrimitiveType.getLong();
    if (op1t == longType || op2t == longType) {
      return longType;
    }
    final PrimitiveType.DoubleType doubleType = PrimitiveType.getDouble();
    if (op1t == doubleType || op2t == doubleType) {
      return doubleType;
    }
    final PrimitiveType.FloatType floatType = PrimitiveType.getFloat();
    if (op1t == floatType || op2t == floatType) {
      return floatType;
    }
    return UnknownType.getInstance();
  }
}
