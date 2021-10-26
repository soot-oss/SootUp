package de.upb.swt.soot.java.core.toolkits.scalar;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2018 Raja Vallée-Rai and others
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


import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.DoubleConstant;
import de.upb.swt.soot.core.jimple.common.constant.FloatConstant;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.constant.LongConstant;
import de.upb.swt.soot.core.jimple.common.expr.*;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.Type;

import javax.annotation.Nonnull;
import java.util.Iterator;

/**
 * Transformer that eliminates unnecessary logic operations such as
 * 
 * $z0 = a | 0
 * 
 * which can more easily be represented as
 * 
 * $z0 = a
 * 
 * @author Steven Arzt
 */
public class IdentityOperationEliminator implements BodyInterceptor {

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder bodyBuilder) {
    final StmtGraph stmts = bodyBuilder.getStmtGraph();
    for (Stmt stmt : stmts) {
      if (stmt instanceof JAssignStmt) {
        final JAssignStmt assignStmt = (JAssignStmt) stmt;
        final Value rightOp = assignStmt.getRightOp();
        if (rightOp instanceof JAddExpr) {
          // a = bodyBuilder + 0 --> a = bodyBuilder
          // a = 0 + bodyBuilder --> a = bodyBuilder
          AbstractBinopExpr aer = (AbstractBinopExpr) rightOp;
          if (isConstZero(aer.getOp1())) {
            assignStmt.withRightOp(aer.getOp2());
          } else if (isConstZero(aer.getOp2())) {
            assignStmt.withRightOp(aer.getOp1());
          }
        } else if (rightOp instanceof JSubExpr) {
          // a = bodyBuilder - 0 --> a = bodyBuilder
          AbstractBinopExpr aer = (AbstractBinopExpr) rightOp;
          if (isConstZero(aer.getOp2())) {
            assignStmt.withRightOp(aer.getOp1());
          }
        } else if (rightOp instanceof JMulExpr) {
          // a = bodyBuilder * 0 --> a = 0
          // a = 0 * bodyBuilder --> a = 0
          AbstractBinopExpr aer = (AbstractBinopExpr) rightOp;
          if (isConstZero(aer.getOp1())) {
            assignStmt.withRightOp(getZeroConst(assignStmt.getLeftOp().getType()));
          } else if (isConstZero(aer.getOp2())) {
            assignStmt.withRightOp(getZeroConst(assignStmt.getLeftOp().getType()));
          }
        } else if (rightOp instanceof JOrExpr) {
          // a = bodyBuilder | 0 --> a = bodyBuilder
          // a = 0 | bodyBuilder --> a = bodyBuilder
          JOrExpr orExpr = (JOrExpr) rightOp;
          if (isConstZero(orExpr.getOp1())) {
            assignStmt.withRightOp(orExpr.getOp2());
          } else if (isConstZero(orExpr.getOp2())) {
            assignStmt.withRightOp(orExpr.getOp1());
          }
        }
      }
    }

    // In a second step, we remove assingments such as <a = a>
    for (Iterator<Stmt> stmtIt = stmts.iterator(); stmtIt.hasNext();) {
      Stmt stmt = stmtIt.next();
      if (stmt instanceof JAssignStmt) {
        JAssignStmt assignStmt = (JAssignStmt) stmt;
        if (assignStmt.getLeftOp() == assignStmt.getRightOp()) {
          stmtIt.remove();
        }
      }
    }
  }

  /**
   * Gets the constant value 0 with the given type (integer, float, etc.)
   * 
   * @param type
   *          The type for which to get the constant zero value
   * @return The constant zero value of the given type
   */
  private static Value getZeroConst(Type type) {
    if (type instanceof PrimitiveType.IntType) {
      return IntConstant.getInstance(0);
    } else if (type instanceof PrimitiveType.LongType) {
      return LongConstant.getInstance(0);
    } else if (type instanceof PrimitiveType.FloatType) {
      return FloatConstant.getInstance(0);
    } else if (type instanceof PrimitiveType.DoubleType) {
      return DoubleConstant.getInstance(0);
    }
    throw new RuntimeException("Unsupported numeric type");
  }

  /**
   * Checks whether the given value is the constant integer 0
   * 
   * @param op
   *          The value to check
   * @return True if the given value is the constant integer 0, otherwise false
   */
  private static boolean isConstZero(Value op) {
    return (op instanceof IntConstant) && (((IntConstant) op).getValue() == 0);
  }

}
