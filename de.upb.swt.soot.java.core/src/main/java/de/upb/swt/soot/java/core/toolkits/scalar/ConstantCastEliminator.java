package de.upb.swt.soot.java.core.toolkits.scalar;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2015 Steven Arzt
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


import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Transformer for removing unnecessary casts on primitive values. An assignment a = (float) 42 will for instance be
 * transformed to a = 42f;
 *
 * @author Steven Arzt
 */
public class ConstantCastEliminator implements BodyInterceptor {

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder bodyBuilder) {
    // Check for all assignments that perform casts on primitive constants
    for (Unit u : bodyBuilder.getUnits()) {
      if (u instanceof AssignStmt) {
        AssignStmt assign = (AssignStmt) u;
        Value rightOp = assign.getRightOp();
        if (rightOp instanceof CastExpr) {
          CastExpr ce = (CastExpr) rightOp;
          Value castOp = ce.getOp();
          if (castOp instanceof IntConstant) {
            Type castType = ce.getType();
            if (castType instanceof FloatType) {
              // a = (float) 42
              assign.setRightOp(FloatConstant.v(((IntConstant) castOp).value));
            } else if (castType instanceof DoubleType) {
              // a = (double) 42
              assign.setRightOp(DoubleConstant.v(((IntConstant) castOp).value));
            }
          }
        }
      }
    }
  }
}
