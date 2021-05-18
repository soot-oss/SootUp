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

public abstract class AbstractExprVisitor<V> extends AbstractVisitor<V> implements ExprVisitor {

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
  public void caseStaticInvokeExpr(JStaticInvokeExpr expr) {
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
  public void defaultCase(Expr expr) {}
}
