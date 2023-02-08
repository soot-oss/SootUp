package sootup.java.bytecode.interceptors;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Marcus Nachtigall and others
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

import javax.annotation.Nullable;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.expr.*;

/**
 * Evaluates, whether a value is constant and computes its constant value, if possible.
 *
 * @author Marcus Nachtigall
 */
public class Evaluator {

  // TODO: [ms] please rewrite that huge elseif construct to a ExprVisitor

  /**
   * Checks whether the value of op is constant
   *
   * @param op The value to be evaluated
   * @return True, if op is constant. Otherwise, false.
   */
  public static boolean isConstantValue(Value op) {
    if (op instanceof Constant) {
      return true;
    }
    if (op instanceof AbstractUnopExpr) {
      Value innerOp = ((AbstractUnopExpr) op).getOp();
      if (innerOp == NullConstant.getInstance()) {
        return false;
      }
      return isConstantValue(innerOp);
    }
    if (op instanceof AbstractBinopExpr) {
      final AbstractBinopExpr binExpr = (AbstractBinopExpr) op;
      final Value op1 = binExpr.getOp1();
      final Value op2 = binExpr.getOp2();

      // Only evaluate these checks once and then use the result multiple times
      final boolean isOp1Constant = isConstantValue(op1);
      final boolean isOp2Constant = isConstantValue(op2);

      // Handle weird cases
      if (op instanceof JDivExpr || op instanceof JRemExpr) {
        if (!isOp1Constant || !isOp2Constant) {
          return false;
        }

        // check fpr a 0 value. If so, punt
        Value c2 = getConstantValueOf(op2);
        if (c2 instanceof IntConstant && ((IntConstant) c2).getValue() == 0) {
          return false;
        } else if (c2 instanceof LongConstant && ((LongConstant) c2).getValue() == 0) {
          return false;
        }
      }
      return isOp1Constant && isOp2Constant;
    }
    return false;
  }

  /**
   * Returns the constant value of op, if it is easy to find the constant value; else returns null.
   *
   * @param op The value to be evaluated
   * @return The resulting constant or null
   */
  @Nullable
  public static Constant getConstantValueOf(Value op) {
    if (!isConstantValue(op)) {
      return null;
    }

    if (op instanceof Constant) {
      return (Constant) op;
    } else if (op instanceof AbstractUnopExpr) {
      if (op instanceof JNegExpr) {
        Value constant = getConstantValueOf(((JNegExpr) op).getOp());
        return ((NumericConstant) constant).negate();
      }
    } else if (op instanceof AbstractBinopExpr) {
      final AbstractBinopExpr binopExpr = (AbstractBinopExpr) op;
      final Value op1 = binopExpr.getOp1();
      final Value op2 = binopExpr.getOp2();

      final Value c1 = getConstantValueOf(op1);
      final Value c2 = getConstantValueOf(op2);

      if (op instanceof JAddExpr) {
        return ((NumericConstant) c1).add((NumericConstant) c2);
      } else if (op instanceof JSubExpr) {
        return ((NumericConstant) c1).subtract((NumericConstant) c2);
      } else if (op instanceof JMulExpr) {
        return ((NumericConstant) c1).multiply((NumericConstant) c2);
      } else if (op instanceof JDivExpr) {
        return ((NumericConstant) c1).divide((NumericConstant) c2);
      } else if (op instanceof JRemExpr) {
        return ((NumericConstant) c1).remainder((NumericConstant) c2);
      } else if (op instanceof JEqExpr || op instanceof JNeExpr) {
        if (c1 instanceof NumericConstant) {
          if (!(c2 instanceof NumericConstant)) {
            return IntConstant.getInstance(0);
          } else if (op instanceof JEqExpr) {
            return ((NumericConstant) c1).equalEqual((NumericConstant) c2);
          } else // op instanceof JNeExpr
          return ((NumericConstant) c1).notEqual((NumericConstant) c2);
        } else if (c1 instanceof StringConstant
            || c1 instanceof NullConstant
            || c1 instanceof ClassConstant) {
          boolean equality = c1.equals(c2);
          boolean truth = (op instanceof JEqExpr) == equality;
          return IntConstant.getInstance(truth ? 1 : 0);
        }
        return null;
        // throw new RuntimeException("Constant neither numeric nor string");
      } else if (op instanceof JGtExpr) {
        return ((NumericConstant) c1).greaterThan((NumericConstant) c2);
      } else if (op instanceof JGeExpr) {
        return ((NumericConstant) c1).greaterThanOrEqual((NumericConstant) c2);
      } else if (op instanceof JLtExpr) {
        return ((NumericConstant) c1).lessThan((NumericConstant) c2);
      } else if (op instanceof JLeExpr) {
        return ((NumericConstant) c1).lessThanOrEqual((NumericConstant) c2);
      } else if (op instanceof JAndExpr) {
        return ((LogicalConstant) c1).and((LogicalConstant) c2);
      } else if (op instanceof JOrExpr) {
        return ((LogicalConstant) c1).or((LogicalConstant) c2);
      } else if (op instanceof JXorExpr) {
        return ((LogicalConstant) c1).xor((LogicalConstant) c2);
      } else if (op instanceof JShlExpr) {
        return ((ShiftableConstant) c1).shiftLeft((IntConstant) c2);
      } else if (op instanceof JShrExpr) {
        return ((ShiftableConstant) c1).shiftRight((IntConstant) c2);
      } else if (op instanceof JUshrExpr) {
        return ((ShiftableConstant) c1).unsignedShiftRight((IntConstant) c2);
      } else if (op instanceof JCmpExpr) {
        if ((c1 instanceof LongConstant) && (c2 instanceof LongConstant)) {
          return ((LongConstant) c1).cmp((LongConstant) c2);
        } else {
          throw new IllegalArgumentException("CmpExpr: LongConstant(s) expected");
        }
      } else if ((op instanceof JCmpgExpr) || (op instanceof JCmplExpr)) {
        if ((c1 instanceof RealConstant) && (c2 instanceof RealConstant)) {

          if (op instanceof JCmpgExpr) {
            return ((RealConstant) c1).cmpg((RealConstant) c2);
          } else // op instanceof JCmplExpr
          return ((RealConstant) c1).cmpl((RealConstant) c2);
        } else {
          throw new IllegalArgumentException("CmpExpr: RealConstant(s) expected");
        }
      } else {
        // throw new RuntimeException("Unknown binary operator: " + op);
        return null;
      }
    }

    // throw new RuntimeException("couldn't getConstantValueOf of: " + op);
    return null;
  }
}
