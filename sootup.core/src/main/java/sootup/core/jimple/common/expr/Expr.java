package sootup.core.jimple.common.expr;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallee-Rai, Linghui Luo and others
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

import java.util.Optional;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.visitor.Acceptor;
import sootup.core.jimple.visitor.ExprVisitor;

/*   An Expression is a Language construct that returns a Value  */
public interface Expr extends Value, Acceptor<ExprVisitor> {
  boolean isJAddExpr();

  boolean isJAndExpr();

  boolean isJCastExpr();

  boolean isJCmpExpr();

  boolean isJCmpgExpr();

  boolean isJCmplExpr();

  boolean isJDivExpr();

  boolean isJDynamicInvokeExpr();

  boolean isJEqExpr();

  boolean isJGeExpr();

  boolean isJGtExpr();

  boolean isJInstanceOfExpr();

  boolean isJInterfaceInvokeExpr();

  boolean isJLeExpr();

  boolean isJLengthExpr();

  boolean isJLtExpr();

  boolean isJMulExpr();

  boolean isJNeExpr();

  boolean isJNegExpr();

  boolean isJNewArrayExpr();

  boolean isJNewExpr();

  boolean isJNewMultiArrayExpr();

  boolean isJOrExpr();

  boolean isJPhiExpr();

  boolean isJRemExpr();

  boolean isJShlExpr();

  boolean isJShrExpr();

  boolean isJSpecialInvokeExpr();

  boolean isJStaticInvokeExpr();

  boolean isJSubExpr();

  boolean isJUshrExpr();

  boolean isJVirtualInvokeExpr();

  boolean isJXorExpr();

  JAddExpr asJAddExpr();

  JAndExpr asJAndExpr();

  JCastExpr asJCastExpr();

  JCmpExpr asJCmpExpr();

  JCmpgExpr asJCmpgExpr();

  JCmplExpr asJCmplExpr();

  JDivExpr asJDivExpr();

  JDynamicInvokeExpr asJDynamicInvokeExpr();

  JEqExpr asJEqExpr();

  JGeExpr asJGeExpr();

  JGtExpr asJGtExpr();

  JInstanceOfExpr asJInstanceOfExpr();

  JInterfaceInvokeExpr asJInterfaceInvokeExpr();

  JLeExpr asJLeExpr();

  JLengthExpr asJLengthExpr();

  JLtExpr asJLtExpr();

  JMulExpr asJMulExpr();

  JNeExpr asJNeExpr();

  JNegExpr asJNegExpr();

  JNewArrayExpr asJNewArrayExpr();

  JNewExpr asJNewExpr();

  JNewMultiArrayExpr asJNewMultiArrayExpr();

  JOrExpr asJOrExpr();

  JPhiExpr asJPhiExpr();

  JRemExpr asJRemExpr();

  JShlExpr asJShlExpr();

  JShrExpr asJShrExpr();

  JSpecialInvokeExpr asJSpecialInvokeExpr();

  JStaticInvokeExpr asJStaticInvokeExpr();

  JSubExpr asJSubExpr();

  JUshrExpr asJUshrExpr();

  JVirtualInvokeExpr asJVirtualInvokeExpr();

  JXorExpr asJXorExpr();

  Optional<JAddExpr> toJAddExpr();

  Optional<JAndExpr> toJAndExpr();

  Optional<JCastExpr> toJCastExpr();

  Optional<JCmpExpr> toJCmpExpr();

  Optional<JCmpgExpr> toJCmpgExpr();

  Optional<JCmplExpr> toJCmplExpr();

  Optional<JDivExpr> toJDivExpr();

  Optional<JDynamicInvokeExpr> toJDynamicInvokeExpr();

  Optional<JEqExpr> toJEqExpr();

  Optional<JGeExpr> toJGeExpr();

  Optional<JGtExpr> toJGtExpr();

  Optional<JInstanceOfExpr> toJInstanceOfExpr();

  Optional<JInterfaceInvokeExpr> toJInterfaceInvokeExpr();

  Optional<JLeExpr> toJLeExpr();

  Optional<JLengthExpr> toJLengthExpr();

  Optional<JLtExpr> toJLtExpr();

  Optional<JMulExpr> toJMulExpr();

  Optional<JNeExpr> toJNeExpr();

  Optional<JNegExpr> toJNegExpr();

  Optional<JNewArrayExpr> toJNewArrayExpr();

  Optional<JNewExpr> toJNewExpr();

  Optional<JNewMultiArrayExpr> toJNewMultiArrayExpr();

  Optional<JOrExpr> toJOrExpr();

  Optional<JPhiExpr> toJPhiExpr();

  Optional<JRemExpr> toJRemExpr();

  Optional<JShlExpr> toJShlExpr();

  Optional<JShrExpr> toJShrExpr();

  Optional<JSpecialInvokeExpr> toJSpecialInvokeExpr();

  Optional<JStaticInvokeExpr> toJStaticInvokeExpr();

  Optional<JSubExpr> toJSubExpr();

  Optional<JUshrExpr> toJUshrExpr();

  Optional<JVirtualInvokeExpr> toJVirtualInvokeExpr();

  Optional<JXorExpr> toJXorExpr();
}
