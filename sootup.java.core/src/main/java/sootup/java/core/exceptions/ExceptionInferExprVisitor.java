package sootup.java.core.exceptions;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2025 Zun Wang
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
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.visitor.AbstractExprVisitor;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.*;
import sootup.core.util.ImmutableUtils;

public class ExceptionInferExprVisitor extends AbstractExprVisitor {
  private ExceptionInferResult result;
  private final TypeHierarchy hierarchy;

  public ExceptionInferExprVisitor(TypeHierarchy hierarchy) {
    this.hierarchy = hierarchy;
  }

  @Override
  public void caseAddExpr(@NonNull JAddExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseAndExpr(@NonNull JAndExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseCmpExpr(@NonNull JCmpExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseCmpgExpr(@NonNull JCmpgExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseCmplExpr(@NonNull JCmplExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseDivExpr(@NonNull JDivExpr expr) {
    Value divisor = expr.getOp2();
    Type divisorType = divisor.getType();
    ExceptionInferResult arithmeticException =
        new ExceptionInferResult(ExceptionInferResult.ExceptionType.ARITHMETIC_EXCEPTION);
    if (divisorType instanceof UnknownType) {
      result = arithmeticException;
    } else if (divisorType instanceof PrimitiveType) {
      if (divisor instanceof Local) {
        result = arithmeticException;
      } else if (isZero((Constant) divisor)) {
        result = arithmeticException;
      } else {
        defaultCaseExpr(expr);
      }
    } else {
      defaultCaseExpr(expr);
    }
  }

  @Override
  public void caseEqExpr(@NonNull JEqExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseNeExpr(@NonNull JNeExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseGeExpr(@NonNull JGeExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseGtExpr(@NonNull JGtExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseLeExpr(@NonNull JLeExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseLtExpr(@NonNull JLtExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseMulExpr(@NonNull JMulExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseOrExpr(@NonNull JOrExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseRemExpr(@NonNull JRemExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseShlExpr(@NonNull JShlExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseShrExpr(@NonNull JShrExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseUshrExpr(@NonNull JUshrExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseSubExpr(@NonNull JSubExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseXorExpr(@NonNull JXorExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseStaticInvokeExpr(@NonNull JStaticInvokeExpr expr) {
    result = new ExceptionInferResult(ExceptionInferResult.ErrorType.INITIALIZATION_ERROR);
  }

  @Override
  public void caseSpecialInvokeExpr(@NonNull JSpecialInvokeExpr expr) {
    result =
        new ExceptionInferResult(
            ImmutableUtils.immutableSet(
                ExceptionInferResult.ErrorType.ABSTRACT_METHOD_ERROR,
                ExceptionInferResult.ErrorType.NO_SUCH_METHOD_ERROR,
                ExceptionInferResult.ErrorType.UNSATISFIED_LINK_ERROR,
                ExceptionInferResult.ExceptionType.NUll_POINTER_EXCEPTION));
  }

  @Override
  public void caseVirtualInvokeExpr(@NonNull JVirtualInvokeExpr expr) {
    result =
        new ExceptionInferResult(
            ImmutableUtils.immutableSet(
                ExceptionInferResult.ErrorType.ABSTRACT_METHOD_ERROR,
                ExceptionInferResult.ErrorType.NO_SUCH_METHOD_ERROR,
                ExceptionInferResult.ErrorType.UNSATISFIED_LINK_ERROR,
                ExceptionInferResult.ExceptionType.NUll_POINTER_EXCEPTION));
  }

  @Override
  public void caseInterfaceInvokeExpr(@NonNull JInterfaceInvokeExpr expr) {
    result =
        new ExceptionInferResult(
            ImmutableUtils.immutableSet(
                ExceptionInferResult.ErrorType.ABSTRACT_METHOD_ERROR,
                ExceptionInferResult.ErrorType.NO_SUCH_METHOD_ERROR,
                ExceptionInferResult.ErrorType.UNSATISFIED_LINK_ERROR,
                ExceptionInferResult.ExceptionType.NUll_POINTER_EXCEPTION));
  }

  @Override
  public void caseDynamicInvokeExpr(@NonNull JDynamicInvokeExpr expr) {
    result = new ExceptionInferResult(ExceptionInferResult.ExceptionType.THROWABLE);
  }

  @Override
  public void caseCastExpr(@NonNull JCastExpr expr) {
    result = new ExceptionInferResult(ExceptionInferResult.ErrorType.RESOLVE_CLASS_ERROR);
    Type fromType = expr.getOp().getType();
    Type toType = expr.getType();
    if (toType instanceof ReferenceType) {
      if (fromType == null
          || fromType instanceof UnknownType
          || ((!(fromType instanceof NullType))
              && (!((hierarchy.isSubtype(toType, fromType)) || toType.equals(fromType))))) {
        result =
            result.addException(ExceptionInferResult.ExceptionType.CLASS_CAST_EXCEPTION, hierarchy);
      }
    }
  }

  @Override
  public void caseInstanceOfExpr(@NonNull JInstanceOfExpr expr) {
    result =
        new ExceptionInferResult(
            ImmutableUtils.immutableSet(ExceptionInferResult.ErrorType.RESOLVE_CLASS_ERROR));
  }

  @Override
  public void caseNewArrayExpr(@NonNull JNewArrayExpr expr) {
    if (expr.getBaseType() instanceof ReferenceType) {
      result = new ExceptionInferResult(ExceptionInferResult.ErrorType.RESOLVE_CLASS_ERROR);
    }
    Value count = expr.getSize();
    if (count instanceof Local) {
      result =
          result.addException(
              ExceptionInferResult.ExceptionType.NEGATIVE_ARRAY_SIZE_EXCEPTION, hierarchy);
    } else if (count instanceof IntConstant) {
      BooleanConstant isLessThan = ((IntConstant) count).lessThan(IntConstant.getInstance(0));
      if (isLessThan.equals(BooleanConstant.getInstance(true))) {
        result =
            result.addException(
                ExceptionInferResult.ExceptionType.NEGATIVE_ARRAY_SIZE_EXCEPTION, hierarchy);
      }
    }
  }

  @Override
  public void caseNewMultiArrayExpr(@NonNull JNewMultiArrayExpr expr) {
    result = new ExceptionInferResult(ExceptionInferResult.ErrorType.RESOLVE_CLASS_ERROR);
    for (int i = 0; i < expr.getSizeCount(); i++) {
      Value count = expr.getSize(i);
      if (count instanceof Local) {
        result =
            result.addException(
                ExceptionInferResult.ExceptionType.NEGATIVE_ARRAY_SIZE_EXCEPTION, hierarchy);
        break;
      } else if (count instanceof IntConstant) {
        BooleanConstant isLessThan = ((IntConstant) count).lessThan(IntConstant.getInstance(0));
        if (isLessThan.equals(BooleanConstant.getInstance(true))) {
          result =
              result.addException(
                  ExceptionInferResult.ExceptionType.NEGATIVE_ARRAY_SIZE_EXCEPTION, hierarchy);
          break;
        }
      }
    }
  }

  @Override
  public void caseNewExpr(@NonNull JNewExpr expr) {
    result = new ExceptionInferResult(ExceptionInferResult.ErrorType.INITIALIZATION_ERROR);
  }

  @Override
  public void caseLengthExpr(@NonNull JLengthExpr expr) {
    result = new ExceptionInferResult(ExceptionInferResult.ExceptionType.NUll_POINTER_EXCEPTION);
  }

  @Override
  public void caseNegExpr(@NonNull JNegExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void casePhiExpr(@NonNull JPhiExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void defaultCaseExpr(@NonNull Expr expr) {
    result = ExceptionInferResult.createEmptyException();
  }

  public ExceptionInferResult getResult() {
    return this.result;
  }

  private boolean isZero(Constant constant) {
    if (constant instanceof NumericConstant) {
      return (constant instanceof IntConstant && constant.equals(IntConstant.getInstance(0)))
          || (constant instanceof LongConstant && constant.equals(LongConstant.getInstance(0)))
          || (constant instanceof FloatConstant && constant.equals(FloatConstant.getInstance(0.0f)))
          || (constant instanceof DoubleConstant
              && constant.equals(DoubleConstant.getInstance(0.0)));
    }
    return false;
  }
}
