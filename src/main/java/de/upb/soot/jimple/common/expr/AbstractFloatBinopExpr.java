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
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.signatures.PrimitiveTypeSignature;
import de.upb.soot.signatures.TypeSignature;
import de.upb.soot.signatures.UnknownTypeSignature;

@SuppressWarnings("serial")
public abstract class AbstractFloatBinopExpr extends AbstractBinopExpr {

  AbstractFloatBinopExpr(Value op1, Value op2) {
    this(Jimple.newArgBox(op1), Jimple.newArgBox(op2));
  }

  protected AbstractFloatBinopExpr(ValueBox op1Box, ValueBox op2Box) {
    this.op1Box = op1Box;
    this.op2Box = op2Box;
  }

  @Override
  public TypeSignature getSignature() {
    Value op1 = op1Box.getValue();
    Value op2 = op2Box.getValue();
    TypeSignature op1t = op1.getSignature();
    TypeSignature op2t = op2.getSignature();
    if ((op1t.equals(PrimitiveTypeSignature.getIntSignature()) || op1t.equals(PrimitiveTypeSignature.getByteSignature()) || op1t.equals(PrimitiveTypeSignature.getShortSignature())
        || op1t.equals(PrimitiveTypeSignature.getCharSignature()) || op1t.equals(PrimitiveTypeSignature.getBooleanSignature()))
        && (op2t.equals(PrimitiveTypeSignature.getIntSignature()) || op2t.equals(PrimitiveTypeSignature.getByteSignature()) || op2t.equals(PrimitiveTypeSignature.getShortSignature())
            || op2t.equals(PrimitiveTypeSignature.getCharSignature()) || op2t.equals(PrimitiveTypeSignature.getBooleanSignature()))) {
      return PrimitiveTypeSignature.getIntSignature();
    } else if (op1t.equals(PrimitiveTypeSignature.getLongSignature()) || op2t.equals(PrimitiveTypeSignature.getLongSignature())) {
      return PrimitiveTypeSignature.getLongSignature();
    } else if (op1t.equals(PrimitiveTypeSignature.getDoubleSignature()) || op2t.equals(PrimitiveTypeSignature.getDoubleSignature())) {
      return PrimitiveTypeSignature.getDoubleSignature();
    } else if (op1t.equals(PrimitiveTypeSignature.getFloatSignature()) || op2t.equals(PrimitiveTypeSignature.getFloatSignature())) {
      return PrimitiveTypeSignature.getFloatSignature();
    } else {
      return UnknownTypeSignature.getInstance();
    }
  }
}
