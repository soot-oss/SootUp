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
import javax.annotation.Nonnull;
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
  public void caseAddExpr(@Nonnull JAddExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseAndExpr(@Nonnull JAndExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseCmpExpr(@Nonnull JCmpExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseCmpgExpr(@Nonnull JCmpgExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseCmplExpr(@Nonnull JCmplExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseDivExpr(@Nonnull JDivExpr expr) {
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
  public void caseEqExpr(@Nonnull JEqExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseNeExpr(@Nonnull JNeExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseGeExpr(@Nonnull JGeExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseGtExpr(@Nonnull JGtExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseLeExpr(@Nonnull JLeExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseLtExpr(@Nonnull JLtExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseMulExpr(@Nonnull JMulExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseOrExpr(@Nonnull JOrExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseRemExpr(@Nonnull JRemExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseShlExpr(@Nonnull JShlExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseShrExpr(@Nonnull JShrExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseUshrExpr(@Nonnull JUshrExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseSubExpr(@Nonnull JSubExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseXorExpr(@Nonnull JXorExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseStaticInvokeExpr(@Nonnull JStaticInvokeExpr expr) {
    result = new ExceptionInferResult(ExceptionInferResult.ErrorType.INITIALIZATION_ERROR);
  }

  @Override
  public void caseSpecialInvokeExpr(@Nonnull JSpecialInvokeExpr expr) {
    result =
        new ExceptionInferResult(
            ImmutableUtils.immutableSet(
                ExceptionInferResult.ErrorType.ABSTRACT_METHOD_ERROR,
                ExceptionInferResult.ErrorType.NO_SUCH_METHOD_ERROR,
                ExceptionInferResult.ErrorType.UNSATISFIED_LINK_ERROR,
                ExceptionInferResult.ExceptionType.NUll_POINTER_EXCEPTION));
  }

  @Override
  public void caseVirtualInvokeExpr(@Nonnull JVirtualInvokeExpr expr) {
    result =
        new ExceptionInferResult(
            ImmutableUtils.immutableSet(
                ExceptionInferResult.ErrorType.ABSTRACT_METHOD_ERROR,
                ExceptionInferResult.ErrorType.NO_SUCH_METHOD_ERROR,
                ExceptionInferResult.ErrorType.UNSATISFIED_LINK_ERROR,
                ExceptionInferResult.ExceptionType.NUll_POINTER_EXCEPTION));
  }

  @Override
  public void caseInterfaceInvokeExpr(@Nonnull JInterfaceInvokeExpr expr) {
    result =
        new ExceptionInferResult(
            ImmutableUtils.immutableSet(
                ExceptionInferResult.ErrorType.ABSTRACT_METHOD_ERROR,
                ExceptionInferResult.ErrorType.NO_SUCH_METHOD_ERROR,
                ExceptionInferResult.ErrorType.UNSATISFIED_LINK_ERROR,
                ExceptionInferResult.ExceptionType.NUll_POINTER_EXCEPTION));
  }

  @Override
  public void caseDynamicInvokeExpr(@Nonnull JDynamicInvokeExpr expr) {
    result = new ExceptionInferResult(ExceptionInferResult.ExceptionType.THROWABLE);
  }

  @Override
  public void caseCastExpr(@Nonnull JCastExpr expr) {
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
  public void caseInstanceOfExpr(@Nonnull JInstanceOfExpr expr) {
    result =
        new ExceptionInferResult(
            ImmutableUtils.immutableSet(ExceptionInferResult.ErrorType.RESOLVE_CLASS_ERROR));
  }

  @Override
  public void caseNewArrayExpr(@Nonnull JNewArrayExpr expr) {
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
  public void caseNewMultiArrayExpr(@Nonnull JNewMultiArrayExpr expr) {
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
  public void caseNewExpr(@Nonnull JNewExpr expr) {
    result = new ExceptionInferResult(ExceptionInferResult.ErrorType.INITIALIZATION_ERROR);
  }

  @Override
  public void caseLengthExpr(@Nonnull JLengthExpr expr) {
    result = new ExceptionInferResult(ExceptionInferResult.ExceptionType.NUll_POINTER_EXCEPTION);
  }

  @Override
  public void caseNegExpr(@Nonnull JNegExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void casePhiExpr(@Nonnull JPhiExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void defaultCaseExpr(@Nonnull Expr expr) {
    result = ExceptionInferResult.createEmptyException();
  }

  public ExceptionInferResult getResult() {
    return this.result;
  }

  private boolean isZero(Constant constant) {
    if (constant instanceof NumericConstant) {
      if ((constant instanceof IntConstant && constant.equals(IntConstant.getInstance(0)))
          || (constant instanceof LongConstant && constant.equals(LongConstant.getInstance(0)))
          || (constant instanceof FloatConstant && constant.equals(FloatConstant.getInstance(0.0f)))
          || (constant instanceof DoubleConstant
              && constant.equals(DoubleConstant.getInstance(0.0)))) {
        return true;
      }
    }
    return false;
  }
}
