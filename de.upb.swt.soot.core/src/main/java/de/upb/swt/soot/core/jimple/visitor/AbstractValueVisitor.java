package de.upb.swt.soot.core.jimple.visitor;

import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.*;
import de.upb.swt.soot.core.jimple.common.expr.*;
import de.upb.swt.soot.core.jimple.common.ref.*;

/** @author Markus Schmidt */
public abstract class AbstractValueVisitor<V> extends AbstractVisitor<V> implements ValueVisitor {

  @Override
  public void caseBooleanConstant(BooleanConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseDoubleConstant(DoubleConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseFloatConstant(FloatConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseIntConstant(IntConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseLongConstant(LongConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseNullConstant(NullConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseStringConstant(StringConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseClassConstant(ClassConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseMethodHandle(MethodHandle v) {
    defaultCaseConstant(v);
  }

  @Override
  public void caseMethodType(MethodType v) {
    defaultCaseConstant(v);
  }

  @Override
  public void defaultCaseConstant(Constant v) {
    defaultCaseValue(v);
  }

  @Override
  public void caseAddExpr(JAddExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseAndExpr(JAndExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseCmpExpr(JCmpExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseCmpgExpr(JCmpgExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseCmplExpr(JCmplExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseDivExpr(JDivExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseEqExpr(JEqExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseNeExpr(JNeExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseGeExpr(JGeExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseGtExpr(JGtExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseLeExpr(JLeExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseLtExpr(JLtExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseMulExpr(JMulExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseOrExpr(JOrExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseRemExpr(JRemExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseShlExpr(JShlExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseShrExpr(JShrExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseUshrExpr(JUshrExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseSubExpr(JSubExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseXorExpr(JXorExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseSpecialInvokeExpr(JSpecialInvokeExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseVirtualInvokeExpr(JVirtualInvokeExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseInterfaceInvokeExpr(JInterfaceInvokeExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseStaticInvokeExpr(JStaticInvokeExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseDynamicInvokeExpr(JDynamicInvokeExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseCastExpr(JCastExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseInstanceOfExpr(JInstanceOfExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseNewArrayExpr(JNewArrayExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseNewMultiArrayExpr(JNewMultiArrayExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseNewExpr(JNewExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseLengthExpr(JLengthExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseNegExpr(JNegExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void defaultCaseExpr(Expr expr) {
    defaultCaseValue(expr);
  }

  @Override
  public void caseStaticFieldRef(JStaticFieldRef ref) {
    defaultCaseRef(ref);
  }

  @Override
  public void caseInstanceFieldRef(JInstanceFieldRef ref) {
    defaultCaseRef(ref);
  }

  @Override
  public void caseArrayRef(JArrayRef ref) {
    defaultCaseRef(ref);
  }

  @Override
  public void caseParameterRef(JParameterRef ref) {
    defaultCaseRef(ref);
  }

  @Override
  public void caseCaughtExceptionRef(JCaughtExceptionRef ref) {
    defaultCaseRef(ref);
  }

  @Override
  public void caseThisRef(JThisRef ref) {
    defaultCaseRef(ref);
  }

  @Override
  public void defaultCaseRef(Ref ref) {
    defaultCaseValue(ref);
  }

  @Override
  public void caseLocal(Local local) {
    defaultCaseValue(local);
  }

  public void defaultCaseValue(Value v) {}
}
