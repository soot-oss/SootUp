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
    defaultCase(constant);
  }

  @Override
  public void caseDoubleConstant(DoubleConstant constant) {
    defaultCase(constant);
  }

  @Override
  public void caseFloatConstant(FloatConstant constant) {
    defaultCase(constant);
  }

  @Override
  public void caseIntConstant(IntConstant constant) {
    defaultCase(constant);
  }

  @Override
  public void caseLongConstant(LongConstant constant) {
    defaultCase(constant);
  }

  @Override
  public void caseNullConstant(NullConstant constant) {
    defaultCase(constant);
  }

  @Override
  public void caseStringConstant(StringConstant constant) {
    defaultCase(constant);
  }

  @Override
  public void caseClassConstant(ClassConstant constant) {
    defaultCase(constant);
  }

  @Override
  public void caseMethodHandle(MethodHandle v) {
    defaultCase(v);
  }

  @Override
  public void caseMethodType(MethodType v) {
    defaultCase(v);
  }

  @Override
  public void defaultCase(Constant v) {
    defaultValueCase(v);
  }

  @Override
  public void caseAddExpr(JAddExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseAndExpr(JAndExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseCmpExpr(JCmpExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseCmpgExpr(JCmpgExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseCmplExpr(JCmplExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseDivExpr(JDivExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseEqExpr(JEqExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseNeExpr(JNeExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseGeExpr(JGeExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseGtExpr(JGtExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseLeExpr(JLeExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseLtExpr(JLtExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseMulExpr(JMulExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseOrExpr(JOrExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseRemExpr(JRemExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseShlExpr(JShlExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseShrExpr(JShrExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseUshrExpr(JUshrExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseSubExpr(JSubExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseXorExpr(JXorExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseSpecialInvokeExpr(JSpecialInvokeExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseVirtualInvokeExpr(JVirtualInvokeExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseInterfaceInvokeExpr(JInterfaceInvokeExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseStaticInvokeExpr(JStaticInvokeExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseDynamicInvokeExpr(JDynamicInvokeExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseCastExpr(JCastExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseInstanceOfExpr(JInstanceOfExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseNewArrayExpr(JNewArrayExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseNewMultiArrayExpr(JNewMultiArrayExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseNewExpr(JNewExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseLengthExpr(JLengthExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void caseNegExpr(JNegExpr expr) {
    defaultCase(expr);
  }

  @Override
  public void defaultCase(Expr expr) {
    defaultValueCase(expr);
  }

  @Override
  public void caseStaticFieldRef(JStaticFieldRef ref) {
    defaultCase(ref);
  }

  @Override
  public void caseInstanceFieldRef(JInstanceFieldRef ref) {
    defaultCase(ref);
  }

  @Override
  public void caseArrayRef(JArrayRef ref) {
    defaultCase(ref);
  }

  @Override
  public void caseParameterRef(JParameterRef ref) {
    defaultCase(ref);
  }

  @Override
  public void caseCaughtExceptionRef(JCaughtExceptionRef ref) {
    defaultCase(ref);
  }

  @Override
  public void caseThisRef(JThisRef ref) {
    defaultCase(ref);
  }

  @Override
  public void defaultCase(Ref ref) {
    defaultValueCase(ref);
  }

  @Override
  public void caseLocal(Local local) {
    defaultValueCase(local);
  }

  public void defaultValueCase(Value v) {}
}
