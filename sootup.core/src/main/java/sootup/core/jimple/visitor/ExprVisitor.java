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

/** Visitor interface for expression nodes in Jimple IR. */
public interface ExprVisitor extends Visitor {

  /** Visits an addition expression. */
  void caseAddExpr(JAddExpr expr);

  /** Visits a bitwise AND expression. */
  void caseAndExpr(JAndExpr expr);

  /** Visits a long/double comparison expression. */
  void caseCmpExpr(JCmpExpr expr);

  /** Visits a floating-point comparison (greater) expression. */
  void caseCmpgExpr(JCmpgExpr expr);

  /** Visits a floating-point comparison (less) expression. */
  void caseCmplExpr(JCmplExpr expr);

  /** Visits a division expression. */
  void caseDivExpr(JDivExpr expr);

  /** Visits an equality comparison expression. */
  void caseEqExpr(JEqExpr expr);

  /** Visits an inequality comparison expression. */
  void caseNeExpr(JNeExpr expr);

  /** Visits a greater-than-or-equal comparison expression. */
  void caseGeExpr(JGeExpr expr);

  /** Visits a greater-than comparison expression. */
  void caseGtExpr(JGtExpr expr);

  /** Visits a less-than-or-equal comparison expression. */
  void caseLeExpr(JLeExpr expr);

  /** Visits a less-than comparison expression. */
  void caseLtExpr(JLtExpr expr);

  /** Visits a multiplication expression. */
  void caseMulExpr(JMulExpr expr);

  /** Visits a bitwise OR expression. */
  void caseOrExpr(JOrExpr expr);

  /** Visits a remainder expression. */
  void caseRemExpr(JRemExpr expr);

  /** Visits a shift-left expression. */
  void caseShlExpr(JShlExpr expr);

  /** Visits a signed shift-right expression. */
  void caseShrExpr(JShrExpr expr);

  /** Visits an unsigned shift-right expression. */
  void caseUshrExpr(JUshrExpr expr);

  /** Visits a subtraction expression. */
  void caseSubExpr(JSubExpr expr);

  /** Visits a bitwise XOR expression. */
  void caseXorExpr(JXorExpr expr);

  /** Visits a special (private/super) invoke expression. */
  void caseSpecialInvokeExpr(JSpecialInvokeExpr expr);

  /** Visits a virtual invoke expression. */
  void caseVirtualInvokeExpr(JVirtualInvokeExpr expr);

  /** Visits an interface invoke expression. */
  void caseInterfaceInvokeExpr(JInterfaceInvokeExpr expr);

  /** Visits a static invoke expression. */
  void caseStaticInvokeExpr(JStaticInvokeExpr expr);

  /** Visits a dynamic invoke (invokedynamic) expression. */
  void caseDynamicInvokeExpr(JDynamicInvokeExpr expr);

  /** Visits a cast expression. */
  void caseCastExpr(JCastExpr expr);

  /** Visits an instanceof check expression. */
  void caseInstanceOfExpr(JInstanceOfExpr expr);

  /** Visits a new array expression. */
  void caseNewArrayExpr(JNewArrayExpr expr);

  /** Visits a new multi-dimensional array expression. */
  void caseNewMultiArrayExpr(JNewMultiArrayExpr expr);

  /** Visits a new object expression. */
  void caseNewExpr(JNewExpr expr);

  /** Visits an array length expression. */
  void caseLengthExpr(JLengthExpr expr);

  /** Visits a negation expression. */
  void caseNegExpr(JNegExpr expr);

  /** Visits a phi expression (used in SSA form). */
  void casePhiExpr(JPhiExpr v);

  /** Called for any expression not handled by a more specific case method. */
  void defaultCaseExpr(Expr expr);
}
