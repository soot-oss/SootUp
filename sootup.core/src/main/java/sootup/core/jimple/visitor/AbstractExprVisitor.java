package sootup.core.jimple.visitor;

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

import org.jspecify.annotations.NonNull;
import sootup.core.jimple.common.expr.*;

public abstract class AbstractExprVisitor implements ExprVisitor, Visitor {

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
  public void caseStaticInvokeExpr(@NonNull JStaticInvokeExpr expr) {
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
  public void casePhiExpr(@NonNull JPhiExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void defaultCaseExpr(@NonNull Expr expr) {}
}
