package de.upb.swt.soot.core.jimple.visitor;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Etienne Gagnon, Linghui Luo, Zun Wang
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

import de.upb.swt.soot.core.jimple.common.expr.*;

public abstract class AbstractExprVisitor implements ExprVisitor {
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
  public void caseStaticInvokeExpr(JStaticInvokeExpr v) {
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
  public void defaultCase(Object obj) {}

  public void setResult(Object result) {
    this.result = result;
  }

  public Object getResult() {
    return result;
  }
}
