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
import sootup.core.jimple.common.Value;
import sootup.core.jimple.visitor.Acceptor;
import sootup.core.jimple.visitor.ExprVisitor;

/*   An Expression is a Language construct that returns a Value  */
public interface Expr extends Value, Acceptor<ExprVisitor> {

  default boolean isJAddExpr() {
    return false;
  }

  default boolean isJAndExpr() {
    return false;
  }

  default boolean isJCastExpr() {
    return false;
  }

  default boolean isJCmpExpr() {
    return false;
  }

  default boolean isJCmpgExpr() {
    return false;
  }

  default boolean isJCmplExpr() {
    return false;
  }

  default boolean isJDivExpr() {
    return false;
  }

  default boolean isJDynamicInvokeExpr() {
    return false;
  }

  default boolean isJEqExpr() {
    return false;
  }

  default boolean isJGeExpr() {
    return false;
  }

  default boolean isJGtExpr() {
    return false;
  }

  default boolean isJInstanceOfExpr() {
    return false;
  }

  default boolean isJInterfaceInvokeExpr() {
    return false;
  }

  default boolean isJLeExpr() {
    return false;
  }

  default boolean isJLengthExpr() {
    return false;
  }

  default boolean isJLtExpr() {
    return false;
  }

  default boolean isJMulExpr() {
    return false;
  }

  default boolean isJNeExpr() {
    return false;
  }

  default boolean isJNegExpr() {
    return false;
  }

  default boolean isJNewArrayExpr() {
    return false;
  }

  default boolean isJNewExpr() {
    return false;
  }

  default boolean isJNewMultiArrayExpr() {
    return false;
  }

  default boolean isJOrExpr() {
    return false;
  }

  default boolean isJPhiExpr() {
    return false;
  }

  default boolean isJRemExpr() {
    return false;
  }

  default boolean isJShlExpr() {
    return false;
  }

  default boolean isJShrExpr() {
    return false;
  }

  default boolean isJSpecialInvokeExpr() {
    return false;
  }

  default boolean isJStaticInvokeExpr() {
    return false;
  }

  default boolean isJSubExpr() {
    return false;
  }

  default boolean isJUshrExpr() {
    return false;
  }

  default boolean isJVirtualInvokeExpr() {
    return false;
  }

  default boolean isJXorExpr() {
    return false;
  }

  default JAddExpr asJAddExpr() {
    return null;
  }

  default JAndExpr asJAndExpr() {
    return null;
  }

  default JCastExpr asJCastExpr() {
    return null;
  }

  default JCmpExpr asJCmpExpr() {
    return null;
  }

  default JCmpgExpr asJCmpgExpr() {
    return null;
  }

  default JCmplExpr asJCmplExpr() {
    return null;
  }

  default JDivExpr asJDivExpr() {
    return null;
  }

  default JDynamicInvokeExpr asJDynamicInvokeExpr() {
    return null;
  }

  default JEqExpr asJEqExpr() {
    return null;
  }

  default JGeExpr asJGeExpr() {
    return null;
  }

  default JGtExpr asJGtExpr() {
    return null;
  }

  default JInstanceOfExpr asJInstanceOfExpr() {
    return null;
  }

  default JInterfaceInvokeExpr asJInterfaceInvokeExpr() {
    return null;
  }

  default JLeExpr asJLeExpr() {
    return null;
  }

  default JLengthExpr asJLengthExpr() {
    return null;
  }

  default JLtExpr asJLtExpr() {
    return null;
  }

  default JMulExpr asJMulExpr() {
    return null;
  }

  default JNeExpr asJNeExpr() {
    return null;
  }

  default JNegExpr asJNegExpr() {
    return null;
  }

  default JNewArrayExpr asJNewArrayExpr() {
    return null;
  }

  default JNewExpr asJNewExpr() {
    return null;
  }

  default JNewMultiArrayExpr asJNewMultiArrayExpr() {
    return null;
  }

  default JOrExpr asJOrExpr() {
    return null;
  }

  default JPhiExpr asJPhiExpr() {
    return null;
  }

  default JRemExpr asJRemExpr() {
    return null;
  }

  default JShlExpr asJShlExpr() {
    return null;
  }

  default JShrExpr asJShrExpr() {
    return null;
  }

  default JSpecialInvokeExpr asJSpecialInvokeExpr() {
    return null;
  }

  default JStaticInvokeExpr asJStaticInvokeExpr() {
    return null;
  }

  default JSubExpr asJSubExpr() {
    return null;
  }

  default JUshrExpr asJUshrExpr() {
    return null;
  }

  default JVirtualInvokeExpr asJVirtualInvokeExpr() {
    return null;
  }

  default JXorExpr asJXorExpr() {
    return null;
  }

  default Optional<JAddExpr> toJAddExpr() {
    return Optional.empty();
  }

  default Optional<JAndExpr> toJAndExpr() {
    return Optional.empty();
  }

  default Optional<JCastExpr> toJCastExpr() {
    return Optional.empty();
  }

  default Optional<JCmpExpr> toJCmpExpr() {
    return Optional.empty();
  }

  default Optional<JCmpgExpr> toJCmpgExpr() {
    return Optional.empty();
  }

  default Optional<JCmplExpr> toJCmplExpr() {
    return Optional.empty();
  }

  default Optional<JDivExpr> toJDivExpr() {
    return Optional.empty();
  }

  default Optional<JDynamicInvokeExpr> toJDynamicInvokeExpr() {
    return Optional.empty();
  }

  default Optional<JEqExpr> toJEqExpr() {
    return Optional.empty();
  }

  default Optional<JGeExpr> toJGeExpr() {
    return Optional.empty();
  }

  default Optional<JGtExpr> toJGtExpr() {
    return Optional.empty();
  }

  default Optional<JInstanceOfExpr> toJInstanceOfExpr() {
    return Optional.empty();
  }

  default Optional<JInterfaceInvokeExpr> toJInterfaceInvokeExpr() {
    return Optional.empty();
  }

  default Optional<JLeExpr> toJLeExpr() {
    return Optional.empty();
  }

  default Optional<JLengthExpr> toJLengthExpr() {
    return Optional.empty();
  }

  default Optional<JLtExpr> toJLtExpr() {
    return Optional.empty();
  }

  default Optional<JMulExpr> toJMulExpr() {
    return Optional.empty();
  }

  default Optional<JNeExpr> toJNeExpr() {
    return Optional.empty();
  }

  default Optional<JNegExpr> toJNegExpr() {
    return Optional.empty();
  }

  default Optional<JNewArrayExpr> toJNewArrayExpr() {
    return Optional.empty();
  }

  default Optional<JNewExpr> toJNewExpr() {
    return Optional.empty();
  }

  default Optional<JNewMultiArrayExpr> toJNewMultiArrayExpr() {
    return Optional.empty();
  }

  default Optional<JOrExpr> toJOrExpr() {
    return Optional.empty();
  }

  default Optional<JPhiExpr> toJPhiExpr() {
    return Optional.empty();
  }

  default Optional<JRemExpr> toJRemExpr() {
    return Optional.empty();
  }

  default Optional<JShlExpr> toJShlExpr() {
    return Optional.empty();
  }

  default Optional<JShrExpr> toJShrExpr() {
    return Optional.empty();
  }

  default Optional<JSpecialInvokeExpr> toJSpecialInvokeExpr() {
    return Optional.empty();
  }

  default Optional<JStaticInvokeExpr> toJStaticInvokeExpr() {
    return Optional.empty();
  }

  default Optional<JSubExpr> toJSubExpr() {
    return Optional.empty();
  }

  default Optional<JUshrExpr> toJUshrExpr() {
    return Optional.empty();
  }

  default Optional<JVirtualInvokeExpr> toJVirtualInvokeExpr() {
    return Optional.empty();
  }

  default Optional<JXorExpr> toJXorExpr() {
    return Optional.empty();
  }
}
