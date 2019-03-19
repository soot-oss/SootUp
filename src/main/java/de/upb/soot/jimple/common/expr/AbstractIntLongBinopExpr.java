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
import de.upb.soot.signatures.PrimitiveTypeSignature;
import de.upb.soot.signatures.TypeSignature;
import de.upb.soot.signatures.UnknownTypeSignature;

@SuppressWarnings("serial")
public abstract class AbstractIntLongBinopExpr extends AbstractBinopExpr {

  protected AbstractIntLongBinopExpr(Value op1, Value op2) {
    this.op1Box = Jimple.newArgBox(op1);
    this.op2Box = Jimple.newArgBox(op2);
  }

  public static boolean isIntLikeType(TypeSignature t) {
    return t.equals(PrimitiveTypeSignature.getIntSignature()) || t.equals(PrimitiveTypeSignature.getByteSignature()) || t.equals(PrimitiveTypeSignature.getShortSignature())
        || t.equals(PrimitiveTypeSignature.getCharSignature()) || t.equals(PrimitiveTypeSignature.getBooleanSignature());
  }

  @Override
  public TypeSignature getSignature() {
    Value op1 = op1Box.getValue();
    Value op2 = op2Box.getValue();

    if (isIntLikeType(op1.getSignature()) && isIntLikeType(op2.getSignature())) {
      return PrimitiveTypeSignature.getIntSignature();
    } else if (op1.getSignature().equals(PrimitiveTypeSignature.getLongSignature()) && op2.getSignature().equals(PrimitiveTypeSignature.getLongSignature())) {
      return PrimitiveTypeSignature.getLongSignature();
    } else {
      return UnknownTypeSignature.getInstance();
    }
  }
}
