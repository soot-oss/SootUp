/* Soot - a J*va Optimization Framework
 * Copyright (C) 1999 Patrick Lam
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

package de.upb.soot.jimple.common.expr;

import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.types.PrimitiveType;
import de.upb.soot.types.Type;
import de.upb.soot.types.UnknownType;

@SuppressWarnings("serial")
public abstract class AbstractIntLongBinopExpr extends AbstractBinopExpr {

  AbstractIntLongBinopExpr(Value op1, Value op2) {
    super(Jimple.newArgBox(op1), Jimple.newArgBox(op2));
  }

  static boolean isIntLikeType(Type t) {
    return t.equals(PrimitiveType.getInt())
        || t.equals(PrimitiveType.getByte())
        || t.equals(PrimitiveType.getShort())
        || t.equals(PrimitiveType.getChar())
        || t.equals(PrimitiveType.getBoolean());
  }

  @Override
  public Type getType() {
    Value op1 = getOp1();
    Value op2 = getOp2();

    if (isIntLikeType(op1.getType()) && isIntLikeType(op2.getType())) {
      return PrimitiveType.getInt();
    } else if (op1.getType().equals(PrimitiveType.getLong())
        && op2.getType().equals(PrimitiveType.getLong())) {
      return PrimitiveType.getLong();
    } else {
      return UnknownType.getInstance();
    }
  }
}
