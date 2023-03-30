package sootup.core.jimple.visitor;

import javax.annotation.Nonnull;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.ref.*;

/** @author Markus Schmidt */
public abstract class AbstractValueVisitor<V> extends AbstractVisitor<V> implements ValueVisitor {

  @Override
  public void caseBooleanConstant(@Nonnull BooleanConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseDoubleConstant(@Nonnull DoubleConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseFloatConstant(@Nonnull FloatConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseIntConstant(@Nonnull IntConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseLongConstant(@Nonnull LongConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseNullConstant(@Nonnull NullConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseStringConstant(@Nonnull StringConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseEnumConstant(@Nonnull EnumConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseClassConstant(@Nonnull ClassConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseMethodHandle(@Nonnull MethodHandle v) {
    defaultCaseConstant(v);
  }

  @Override
  public void caseMethodType(@Nonnull MethodType v) {
    defaultCaseConstant(v);
  }

  @Override
  public void defaultCaseConstant(@Nonnull Constant v) {
    defaultCaseValue(v);
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
    defaultCaseExpr(expr);
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
  public void caseSpecialInvokeExpr(@Nonnull JSpecialInvokeExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseVirtualInvokeExpr(@Nonnull JVirtualInvokeExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseInterfaceInvokeExpr(@Nonnull JInterfaceInvokeExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseStaticInvokeExpr(@Nonnull JStaticInvokeExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseDynamicInvokeExpr(@Nonnull JDynamicInvokeExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseCastExpr(@Nonnull JCastExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseInstanceOfExpr(@Nonnull JInstanceOfExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseNewArrayExpr(@Nonnull JNewArrayExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseNewMultiArrayExpr(@Nonnull JNewMultiArrayExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseNewExpr(@Nonnull JNewExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseLengthExpr(@Nonnull JLengthExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void caseNegExpr(@Nonnull JNegExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void defaultCaseExpr(@Nonnull Expr expr) {
    defaultCaseValue(expr);
  }

  @Override
  public void caseStaticFieldRef(@Nonnull JStaticFieldRef ref) {
    defaultCaseRef(ref);
  }

  @Override
  public void caseInstanceFieldRef(@Nonnull JInstanceFieldRef ref) {
    defaultCaseRef(ref);
  }

  @Override
  public void caseArrayRef(@Nonnull JArrayRef ref) {
    defaultCaseRef(ref);
  }

  @Override
  public void caseParameterRef(@Nonnull JParameterRef ref) {
    defaultCaseRef(ref);
  }

  @Override
  public void caseCaughtExceptionRef(@Nonnull JCaughtExceptionRef ref) {
    defaultCaseRef(ref);
  }

  @Override
  public void caseThisRef(@Nonnull JThisRef ref) {
    defaultCaseRef(ref);
  }

  @Override
  public void defaultCaseRef(@Nonnull Ref ref) {
    defaultCaseValue(ref);
  }

  @Override
  public void caseLocal(@Nonnull Local local) {
    defaultCaseValue(local);
  }

  @Override
  public void casePhiExpr(JPhiExpr expr) {
    defaultCaseValue(expr);
  }

  @Override
  public void defaultCaseValue(@Nonnull Value v) {}
}
