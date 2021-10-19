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


import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;

import javax.annotation.Nonnull;
import java.util.Map;

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
    final Chain<Unit> units = bodyBuilder.getUnits();
    for (Unit u : units) {
      if (u instanceof AssignStmt) {
        final AssignStmt assignStmt = (AssignStmt) u;
        final Value rightOp = assignStmt.getRightOp();
        if (rightOp instanceof AddExpr) {
          // a = bodyBuilder + 0 --> a = bodyBuilder
          // a = 0 + bodyBuilder --> a = bodyBuilder
          BinopExpr aer = (BinopExpr) rightOp;
          if (isConstZero(aer.getOp1())) {
            assignStmt.setRightOp(aer.getOp2());
          } else if (isConstZero(aer.getOp2())) {
            assignStmt.setRightOp(aer.getOp1());
          }
        } else if (rightOp instanceof SubExpr) {
          // a = bodyBuilder - 0 --> a = bodyBuilder
          BinopExpr aer = (BinopExpr) rightOp;
          if (isConstZero(aer.getOp2())) {
            assignStmt.setRightOp(aer.getOp1());
          }
        } else if (rightOp instanceof MulExpr) {
          // a = bodyBuilder * 0 --> a = 0
          // a = 0 * bodyBuilder --> a = 0
          BinopExpr aer = (BinopExpr) rightOp;
          if (isConstZero(aer.getOp1())) {
            assignStmt.setRightOp(getZeroConst(assignStmt.getLeftOp().getType()));
          } else if (isConstZero(aer.getOp2())) {
            assignStmt.setRightOp(getZeroConst(assignStmt.getLeftOp().getType()));
          }
        } else if (rightOp instanceof OrExpr) {
          // a = bodyBuilder | 0 --> a = bodyBuilder
          // a = 0 | bodyBuilder --> a = bodyBuilder
          OrExpr orExpr = (OrExpr) rightOp;
          if (isConstZero(orExpr.getOp1())) {
            assignStmt.setRightOp(orExpr.getOp2());
          } else if (isConstZero(orExpr.getOp2())) {
            assignStmt.setRightOp(orExpr.getOp1());
          }
        }
      }
    }

    // In a second step, we remove assingments such as <a = a>
    for (Iterator<Unit> unitIt = units.iterator(); unitIt.hasNext();) {
      Unit u = unitIt.next();
      if (u instanceof AssignStmt) {
        AssignStmt assignStmt = (AssignStmt) u;
        if (assignStmt.getLeftOp() == assignStmt.getRightOp()) {
          unitIt.remove();
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
    if (type instanceof IntType) {
      return IntConstant.v(0);
    } else if (type instanceof LongType) {
      return LongConstant.v(0);
    } else if (type instanceof FloatType) {
      return FloatConstant.v(0);
    } else if (type instanceof DoubleType) {
      return DoubleConstant.v(0);
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
    return (op instanceof IntConstant) && (((IntConstant) op).value == 0);
  }

}
