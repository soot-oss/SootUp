package de.upb.swt.soot.core.jimple.visitor;

import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.*;
import de.upb.swt.soot.core.jimple.common.expr.*;
import de.upb.swt.soot.core.jimple.common.ref.*;

/** @author Markus Schmidt */
public abstract class AbstractValueVisitor<V> extends AbstractVisitor<V> implements ValueVisitor {

  @Override
  public void caseBooleanConstant(BooleanConstant v) {
    defaultCase(v);
  }

  @Override
  public void caseDoubleConstant(DoubleConstant v) {
    defaultCase(v);
  }

  @Override
  public void caseFloatConstant(FloatConstant v) {
    defaultCase(v);
  }

  @Override
  public void caseIntConstant(IntConstant v) {
    defaultCase(v);
  }

  @Override
  public void caseLongConstant(LongConstant v) {
    defaultCase(v);
  }

  @Override
  public void caseNullConstant(NullConstant v) {
    defaultCase(v);
  }

  @Override
  public void caseStringConstant(StringConstant v) {
    defaultCase(v);
  }

  @Override
  public void caseClassConstant(ClassConstant v) {
    defaultCase(v);
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
  public void caseAddExpr(JAddExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseAndExpr(JAndExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseCmpExpr(JCmpExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseCmpgExpr(JCmpgExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseCmplExpr(JCmplExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseDivExpr(JDivExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseEqExpr(JEqExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseNeExpr(JNeExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseGeExpr(JGeExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseGtExpr(JGtExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseLeExpr(JLeExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseLtExpr(JLtExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseMulExpr(JMulExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseOrExpr(JOrExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseRemExpr(JRemExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseShlExpr(JShlExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseShrExpr(JShrExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseUshrExpr(JUshrExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseSubExpr(JSubExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseXorExpr(JXorExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseSpecialInvokeExpr(JSpecialInvokeExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseVirtualInvokeExpr(JVirtualInvokeExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseInterfaceInvokeExpr(JInterfaceInvokeExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseStaticInvokeExpr(JStaticInvokeExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseDynamicInvokeExpr(JDynamicInvokeExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseCastExpr(JCastExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseInstanceOfExpr(JInstanceOfExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseNewArrayExpr(JNewArrayExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseNewMultiArrayExpr(JNewMultiArrayExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseNewExpr(JNewExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseLengthExpr(JLengthExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseNegExpr(JNegExpr v) {
    defaultCase(v);
  }

  @Override
  public void defaultCase(Expr v) {
    defaultValueCase(v);
  }

  @Override
  public void caseStaticFieldRef(JStaticFieldRef v) {
    defaultCase(v);
  }

  @Override
  public void caseInstanceFieldRef(JInstanceFieldRef v) {
    defaultCase(v);
  }

  @Override
  public void caseArrayRef(JArrayRef v) {
    defaultCase(v);
  }

  @Override
  public void caseParameterRef(JParameterRef v) {
    defaultCase(v);
  }

  @Override
  public void caseCaughtExceptionRef(JCaughtExceptionRef v) {
    defaultCase(v);
  }

  @Override
  public void caseThisRef(JThisRef v) {
    defaultCase(v);
  }

  @Override
  public void defaultCase(Ref v) {
    defaultValueCase(v);
  }

  @Override
  public void caseLocal(Local local) {
    defaultValueCase(local);
  }

  public void defaultValueCase(Value v) {}
}
