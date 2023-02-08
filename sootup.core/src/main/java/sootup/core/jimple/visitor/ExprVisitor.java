package sootup.core.jimple.visitor;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Etienne Gagnon, Linghui Luo, Christian Brüggemann and others
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

import sootup.core.jimple.common.expr.*;

public interface ExprVisitor extends Visitor {

  void caseAddExpr(JAddExpr expr);

  void caseAndExpr(JAndExpr expr);

  void caseCmpExpr(JCmpExpr expr);

  void caseCmpgExpr(JCmpgExpr expr);

  void caseCmplExpr(JCmplExpr expr);

  void caseDivExpr(JDivExpr expr);

  void caseEqExpr(JEqExpr expr);

  void caseNeExpr(JNeExpr expr);

  void caseGeExpr(JGeExpr expr);

  void caseGtExpr(JGtExpr expr);

  void caseLeExpr(JLeExpr expr);

  void caseLtExpr(JLtExpr expr);

  void caseMulExpr(JMulExpr expr);

  void caseOrExpr(JOrExpr expr);

  void caseRemExpr(JRemExpr expr);

  void caseShlExpr(JShlExpr expr);

  void caseShrExpr(JShrExpr expr);

  void caseUshrExpr(JUshrExpr expr);

  void caseSubExpr(JSubExpr expr);

  void caseXorExpr(JXorExpr expr);

  void caseSpecialInvokeExpr(JSpecialInvokeExpr expr);

  void caseVirtualInvokeExpr(JVirtualInvokeExpr expr);

  void caseInterfaceInvokeExpr(JInterfaceInvokeExpr expr);

  void caseStaticInvokeExpr(JStaticInvokeExpr expr);

  void caseDynamicInvokeExpr(JDynamicInvokeExpr expr);

  void caseCastExpr(JCastExpr expr);

  void caseInstanceOfExpr(JInstanceOfExpr expr);

  void caseNewArrayExpr(JNewArrayExpr expr);

  void caseNewMultiArrayExpr(JNewMultiArrayExpr expr);

  void caseNewExpr(JNewExpr expr);

  void caseLengthExpr(JLengthExpr expr);

  void caseNegExpr(JNegExpr expr);

  void casePhiExpr(JPhiExpr v);

  void defaultCaseExpr(Expr expr);
}
