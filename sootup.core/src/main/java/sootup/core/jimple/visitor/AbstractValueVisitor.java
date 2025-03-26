package sootup.core.jimple.visitor;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 1997 - 2024 Raja Vallée-Rai and others
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
import sootup.core.jimple.common.ref.*;

/**
 * @author Markus Schmidt
 */
public abstract class AbstractValueVisitor implements ValueVisitor, Visitor {

  @Override
  public void caseBooleanConstant(@NonNull BooleanConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseDoubleConstant(@NonNull DoubleConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseFloatConstant(@NonNull FloatConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseIntConstant(@NonNull IntConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseLongConstant(@NonNull LongConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseNullConstant(@NonNull NullConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseStringConstant(@NonNull StringConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseEnumConstant(@NonNull EnumConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseClassConstant(@NonNull ClassConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseMethodHandle(@NonNull MethodHandle v) {
    defaultCaseConstant(v);
  }

  @Override
  public void caseMethodType(@NonNull MethodType v) {
    defaultCaseConstant(v);
  }

  @Override
  public void defaultCaseConstant(@NonNull Constant v) {
    defaultCaseValue(v);
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
    defaultCaseExpr(expr);
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
  public void caseSpecialInvokeExpr(@NonNull JSpecialInvokeExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseVirtualInvokeExpr(@NonNull JVirtualInvokeExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseInterfaceInvokeExpr(@NonNull JInterfaceInvokeExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseStaticInvokeExpr(@NonNull JStaticInvokeExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseDynamicInvokeExpr(@NonNull JDynamicInvokeExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseCastExpr(@NonNull JCastExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseInstanceOfExpr(@NonNull JInstanceOfExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseNewArrayExpr(@NonNull JNewArrayExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseNewMultiArrayExpr(@NonNull JNewMultiArrayExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseNewExpr(@NonNull JNewExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseLengthExpr(@NonNull JLengthExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseNegExpr(@NonNull JNegExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void defaultCaseExpr(@NonNull Expr expr) {
    defaultCaseValue(expr);
  }

  @Override
  public void caseStaticFieldRef(@NonNull JStaticFieldRef ref) {
    defaultCaseRef(ref);
  }

  @Override
  public void caseInstanceFieldRef(@NonNull JInstanceFieldRef ref) {
    defaultCaseRef(ref);
  }

  @Override
  public void caseArrayRef(@NonNull JArrayRef ref) {
    defaultCaseRef(ref);
  }

  @Override
  public void caseParameterRef(@NonNull JParameterRef ref) {
    defaultCaseRef(ref);
  }

  @Override
  public void caseCaughtExceptionRef(@NonNull JCaughtExceptionRef ref) {
    defaultCaseRef(ref);
  }

  @Override
  public void caseThisRef(@NonNull JThisRef ref) {
    defaultCaseRef(ref);
  }

  @Override
  public void defaultCaseRef(@NonNull Ref ref) {
    defaultCaseValue(ref);
  }

  @Override
  public void caseLocal(@NonNull Local local) {
    defaultCaseValue(local);
  }

  @Override
  public void casePhiExpr(JPhiExpr expr) {
    defaultCaseValue(expr);
  }

  @Override
  public void defaultCaseValue(@NonNull Value v) {}
}
