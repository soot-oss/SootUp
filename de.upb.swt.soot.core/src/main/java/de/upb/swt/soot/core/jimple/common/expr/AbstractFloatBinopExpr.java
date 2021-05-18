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

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.basic.ValueBox;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.types.UnknownType;
import javax.annotation.Nonnull;

public abstract class AbstractFloatBinopExpr extends AbstractBinopExpr {

  AbstractFloatBinopExpr(Value op1, Value op2) {
    this(Jimple.newArgBox(op1), Jimple.newArgBox(op2));
  }

  private AbstractFloatBinopExpr(ValueBox op1Box, ValueBox op2Box) {
    super(op1Box, op2Box);
  }

  @Nonnull
  @Override
  public Type getType() {
    Value op1 = getOp1();
    Value op2 = getOp2();
    Type op1t = op1.getType();
    Type op2t = op2.getType();

    if (PrimitiveType.isIntLikeType(op1t) && PrimitiveType.isIntLikeType(op2t)) {
      return PrimitiveType.getInt();
    } else if (op1t.equals(PrimitiveType.getLong()) || op2t.equals(PrimitiveType.getLong())) {
      return PrimitiveType.getLong();
    } else if (op1t.equals(PrimitiveType.getDouble()) || op2t.equals(PrimitiveType.getDouble())) {
      return PrimitiveType.getDouble();
    } else if (op1t.equals(PrimitiveType.getFloat()) || op2t.equals(PrimitiveType.getFloat())) {
      return PrimitiveType.getFloat();
    } else {
      return UnknownType.getInstance();
    }
  }
}
