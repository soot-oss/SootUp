package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.*;
import de.upb.swt.soot.core.jimple.common.expr.*;

/**
 * Evaluates, whether a value is constant and computes its constant value, if possible.
 *
 * @author Marcus Nachtigall
 */
public class Evaluator {

  /**
   * Checks whether the value of op is constant
   *
   * @param op The value to be evaluated
   * @return True, if op is constant. Otherwise, false.
   */
  public static boolean isValueConstantValue(Value op) {
    if (op instanceof Constant) {
      return true;
    } else if (op instanceof AbstractUnopExpr) {
      Value innerOp = ((AbstractUnopExpr) op).getOp();
      if (innerOp == NullConstant.getInstance()) {
        return false;
      }
      return isValueConstantValue(innerOp);
    } else if (op instanceof AbstractBinopExpr) {
      final AbstractBinopExpr binExpr = (AbstractBinopExpr) op;
      final Value op1 = binExpr.getOp1();
      final Value op2 = binExpr.getOp2();

      // Only evaluate these checks once and then use the result multiple times
      final boolean isOp1Constant = isValueConstantValue(op1);
      final boolean isOp2Constant = isValueConstantValue(op2);

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
  public static Value getConstantValueOf(Value op) {
    if (!isValueConstantValue(op)) {
      return null;
    }

    if (op instanceof Constant) {
      return op;
    } else if (op instanceof AbstractUnopExpr) {
      Value constant = getConstantValueOf(((AbstractUnopExpr) op).getOp());
      if (op instanceof JNegExpr) {
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
        throw new RuntimeException("Constant neither numeric nor string");
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
        throw new RuntimeException("Unknown binary operator: " + op);
      }
    }

    throw new RuntimeException("couldn't getConstantValueOf of: " + op);
  }
}
