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


import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.DoubleConstant;
import de.upb.swt.soot.core.jimple.common.constant.FloatConstant;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.expr.JCastExpr;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.Type;

import javax.annotation.Nonnull;

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
    for (Stmt stmt : bodyBuilder.getStmts()) {
      if (stmt instanceof JAssignStmt) {
        JAssignStmt assign = (JAssignStmt) stmt;
        Value rightOp = assign.getRightOp();
        if (rightOp instanceof JCastExpr) {
          JCastExpr ce = (JCastExpr) rightOp;
          Value castOp = ce.getOp();
          if (castOp instanceof IntConstant) {
            Type castType = ce.getType();
            if (castType instanceof PrimitiveType.FloatType) {
              // a = (float) 42
              assign.withRightOp(FloatConstant.getInstance(((IntConstant) castOp).getValue()));
            } else if (castType instanceof PrimitiveType.DoubleType) {
              // a = (double) 42
              assign.withRightOp(DoubleConstant.getInstance(((IntConstant) castOp).getValue()));
            }
          }
        }
      }
    }
  }
}
