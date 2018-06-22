package de.upb.soot.jimple.visitor;

import de.upb.soot.jimple.common.expr.AbstractCastExpr;
import de.upb.soot.jimple.common.expr.AbstractInstanceOfExpr;
import de.upb.soot.jimple.common.expr.AbstractInterfaceInvokeExpr;
import de.upb.soot.jimple.common.expr.AbstractLengthExpr;
import de.upb.soot.jimple.common.expr.AbstractNegExpr;
import de.upb.soot.jimple.common.expr.AbstractNewArrayExpr;
import de.upb.soot.jimple.common.expr.AbstractNewExpr;
import de.upb.soot.jimple.common.expr.AbstractNewMultiArrayExpr;
import de.upb.soot.jimple.common.expr.AbstractSpecialInvokeExpr;
import de.upb.soot.jimple.common.expr.AbstractStaticInvokeExpr;
import de.upb.soot.jimple.common.expr.AbstractVirtualInvokeExpr;
import de.upb.soot.jimple.common.expr.JAddExpr;
import de.upb.soot.jimple.common.expr.JAndExpr;
import de.upb.soot.jimple.common.expr.JCmpExpr;
import de.upb.soot.jimple.common.expr.JCmpgExpr;
import de.upb.soot.jimple.common.expr.JCmplExpr;
import de.upb.soot.jimple.common.expr.JDivExpr;
import de.upb.soot.jimple.common.expr.JDynamicInvokeExpr;
import de.upb.soot.jimple.common.expr.JEqExpr;
import de.upb.soot.jimple.common.expr.JGeExpr;
import de.upb.soot.jimple.common.expr.JGtExpr;
import de.upb.soot.jimple.common.expr.JLeExpr;
import de.upb.soot.jimple.common.expr.JLtExpr;
import de.upb.soot.jimple.common.expr.JMulExpr;
import de.upb.soot.jimple.common.expr.JNeExpr;
import de.upb.soot.jimple.common.expr.JOrExpr;
import de.upb.soot.jimple.common.expr.JRemExpr;
import de.upb.soot.jimple.common.expr.JShlExpr;
import de.upb.soot.jimple.common.expr.JShrExpr;
import de.upb.soot.jimple.common.expr.JSubExpr;
import de.upb.soot.jimple.common.expr.JUshrExpr;
import de.upb.soot.jimple.common.expr.JXorExpr;

public abstract class AbstractExprVisitor implements IExprVisitor {
  Object result;

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
  public void caseInterfaceInvokeExpr(AbstractInterfaceInvokeExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseSpecialInvokeExpr(AbstractSpecialInvokeExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseStaticInvokeExpr(AbstractStaticInvokeExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseVirtualInvokeExpr(AbstractVirtualInvokeExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseDynamicInvokeExpr(JDynamicInvokeExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseCastExpr(AbstractCastExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseInstanceOfExpr(AbstractInstanceOfExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseNewArrayExpr(AbstractNewArrayExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseNewMultiArrayExpr(AbstractNewMultiArrayExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseNewExpr(AbstractNewExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseLengthExpr(AbstractLengthExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseNegExpr(AbstractNegExpr v) {
    defaultCase(v);
  }

  @Override
  public void defaultCase(Object obj) {
  }

  public void setResult(Object result) {
    this.result = result;
  }

  public Object getResult() {
    return result;
  }
}
