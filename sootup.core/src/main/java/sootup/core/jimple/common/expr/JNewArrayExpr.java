package sootup.core.jimple.common.expr;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999-2020 Patrick Lam, Christian Brüggemann, Linghui Luo and others
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
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import sootup.core.IdentifierFactory;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.visitor.ExprVisitor;
import sootup.core.types.ArrayType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.util.printer.StmtPrinter;

/** An expression that creates a new array of a certain type and a certain size. */
public final class JNewArrayExpr implements Expr {

  @NonNull private final Type baseType;
  @NonNull private final Immediate size;
  @NonNull private final IdentifierFactory identifierFactory;

  public JNewArrayExpr(
      @NonNull Type baseType,
      @NonNull Immediate size,
      @NonNull IdentifierFactory identifierFactory) {
    this.baseType = baseType;
    this.size = size;
    this.identifierFactory = identifierFactory;
  }

  // TODO: [ms] wrong layer of responsibility; maybe move that in an own transformer
  private static Type simplify(Type baseType, IdentifierFactory identifierFactory) {
    if (baseType instanceof ArrayType) {
      return identifierFactory.getArrayType(
          ((ArrayType) baseType).getBaseType(), ((ArrayType) baseType).getDimension() + 1);
    } else {
      return identifierFactory.getArrayType(baseType, 1);
    }
  }

  @Override
  public boolean equivTo(Object o, @NonNull JimpleComparator comparator) {
    return comparator.caseNewArrayExpr(this, o);
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return size.equivHashCode() * 101 + baseType.hashCode() * 17;
  }

  @Override
  public String toString() {
    return (Jimple.NEWARRAY + " (") + baseType + ")" + "[" + size + "]";
  }

  /** Converts a parameter of type StmtPrinter to a string literal. */
  @Override
  public void toString(@NonNull StmtPrinter up) {
    up.literal(Jimple.NEWARRAY);
    up.literal(" ");
    up.literal("(");
    up.typeSignature(baseType);
    up.literal(")");
    up.literal("[");
    size.toString(up);
    up.literal("]");
  }

  @NonNull
  public Type getBaseType() {
    return baseType;
  }

  @NonNull
  public Immediate getSize() {
    return size;
  }

  /**
   * Returns a list of type Value, contains a list of values with size
   *
   * @return
   */
  @Override
  @NonNull
  public Stream<Value> getUses() {
    return Stream.concat(size.getUses(), Stream.of(size));
  }

  /** Returns an instance of ArrayType(). */
  @NonNull
  @Override
  public Type getType() {
    return simplify(baseType, identifierFactory);
  }

  @Override
  public <V extends ExprVisitor> V accept(@NonNull V v) {
    v.caseNewArrayExpr(this);
    return v;
  }

  @NonNull
  public JNewArrayExpr withBaseType(@NonNull Type baseType) {
    return new JNewArrayExpr(baseType, getSize(), identifierFactory);
  }

  @NonNull
  public JNewArrayExpr withSize(@NonNull Immediate size) {
    return new JNewArrayExpr(baseType, size, identifierFactory);
  }

  public boolean isArrayOfPrimitives() {
    if (baseType instanceof PrimitiveType) {
      return true;
    }
    if (baseType instanceof ArrayType) {
      return ((ArrayType) baseType).isArrayTypeOfPrimitives();
    }
    return false;
  }

  @Override
  public boolean isJAddExpr() {
    return false;
  }

  @Override
  public boolean isJAndExpr() {
    return false;
  }

  @Override
  public boolean isJCastExpr() {
    return false;
  }

  @Override
  public boolean isJCmpExpr() {
    return false;
  }

  @Override
  public boolean isJCmpgExpr() {
    return false;
  }

  @Override
  public boolean isJCmplExpr() {
    return false;
  }

  @Override
  public boolean isJDivExpr() {
    return false;
  }

  @Override
  public boolean isJDynamicInvokeExpr() {
    return false;
  }

  @Override
  public boolean isJEqExpr() {
    return false;
  }

  @Override
  public boolean isJGeExpr() {
    return false;
  }

  @Override
  public boolean isJGtExpr() {
    return false;
  }

  @Override
  public boolean isJInstanceOfExpr() {
    return false;
  }

  @Override
  public boolean isJInterfaceInvokeExpr() {
    return false;
  }

  @Override
  public boolean isJLeExpr() {
    return false;
  }

  @Override
  public boolean isJLengthExpr() {
    return false;
  }

  @Override
  public boolean isJLtExpr() {
    return false;
  }

  @Override
  public boolean isJMulExpr() {
    return false;
  }

  @Override
  public boolean isJNeExpr() {
    return false;
  }

  @Override
  public boolean isJNegExpr() {
    return false;
  }

  @Override
  public boolean isJNewArrayExpr() {
    return true;
  }

  @Override
  public boolean isJNewExpr() {
    return false;
  }

  @Override
  public boolean isJNewMultiArrayExpr() {
    return false;
  }

  @Override
  public boolean isJOrExpr() {
    return false;
  }

  @Override
  public boolean isJPhiExpr() {
    return false;
  }

  @Override
  public boolean isJRemExpr() {
    return false;
  }

  @Override
  public boolean isJShlExpr() {
    return false;
  }

  @Override
  public boolean isJShrExpr() {
    return false;
  }

  @Override
  public boolean isJSpecialInvokeExpr() {
    return false;
  }

  @Override
  public boolean isJStaticInvokeExpr() {
    return false;
  }

  @Override
  public boolean isJSubExpr() {
    return false;
  }

  @Override
  public boolean isJUshrExpr() {
    return false;
  }

  @Override
  public boolean isJVirtualInvokeExpr() {
    return false;
  }

  @Override
  public boolean isJXorExpr() {
    return false;
  }

  @Override
  public JAddExpr asJAddExpr() {
    return null;
  }

  @Override
  public JAndExpr asJAndExpr() {
    return null;
  }

  @Override
  public JCastExpr asJCastExpr() {
    return null;
  }

  @Override
  public JCmpExpr asJCmpExpr() {
    return null;
  }

  @Override
  public JCmpgExpr asJCmpgExpr() {
    return null;
  }

  @Override
  public JCmplExpr asJCmplExpr() {
    return null;
  }

  @Override
  public JDivExpr asJDivExpr() {
    return null;
  }

  @Override
  public JDynamicInvokeExpr asJDynamicInvokeExpr() {
    return null;
  }

  @Override
  public JEqExpr asJEqExpr() {
    return null;
  }

  @Override
  public JGeExpr asJGeExpr() {
    return null;
  }

  @Override
  public JGtExpr asJGtExpr() {
    return null;
  }

  @Override
  public JInstanceOfExpr asJInstanceOfExpr() {
    return null;
  }

  @Override
  public JInterfaceInvokeExpr asJInterfaceInvokeExpr() {
    return null;
  }

  @Override
  public JLeExpr asJLeExpr() {
    return null;
  }

  @Override
  public JLengthExpr asJLengthExpr() {
    return null;
  }

  @Override
  public JLtExpr asJLtExpr() {
    return null;
  }

  @Override
  public JMulExpr asJMulExpr() {
    return null;
  }

  @Override
  public JNeExpr asJNeExpr() {
    return null;
  }

  @Override
  public JNegExpr asJNegExpr() {
    return null;
  }

  @Override
  public JNewArrayExpr asJNewArrayExpr() {
    return this;
  }

  @Override
  public JNewExpr asJNewExpr() {
    return null;
  }

  @Override
  public JNewMultiArrayExpr asJNewMultiArrayExpr() {
    return null;
  }

  @Override
  public JOrExpr asJOrExpr() {
    return null;
  }

  @Override
  public JPhiExpr asJPhiExpr() {
    return null;
  }

  @Override
  public JRemExpr asJRemExpr() {
    return null;
  }

  @Override
  public JShlExpr asJShlExpr() {
    return null;
  }

  @Override
  public JShrExpr asJShrExpr() {
    return null;
  }

  @Override
  public JSpecialInvokeExpr asJSpecialInvokeExpr() {
    return null;
  }

  @Override
  public JStaticInvokeExpr asJStaticInvokeExpr() {
    return null;
  }

  @Override
  public JSubExpr asJSubExpr() {
    return null;
  }

  @Override
  public JUshrExpr asJUshrExpr() {
    return null;
  }

  @Override
  public JVirtualInvokeExpr asJVirtualInvokeExpr() {
    return null;
  }

  @Override
  public JXorExpr asJXorExpr() {
    return null;
  }

  @Override
  public Optional<JAddExpr> toJAddExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JAndExpr> toJAndExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JCastExpr> toJCastExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JCmpExpr> toJCmpExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JCmpgExpr> toJCmpgExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JCmplExpr> toJCmplExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JDivExpr> toJDivExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JDynamicInvokeExpr> toJDynamicInvokeExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JEqExpr> toJEqExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JGeExpr> toJGeExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JGtExpr> toJGtExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JInstanceOfExpr> toJInstanceOfExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JInterfaceInvokeExpr> toJInterfaceInvokeExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JLeExpr> toJLeExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JLengthExpr> toJLengthExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JLtExpr> toJLtExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JMulExpr> toJMulExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JNeExpr> toJNeExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JNegExpr> toJNegExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JNewArrayExpr> toJNewArrayExpr() {
    return Optional.of(this);
  }

  @Override
  public Optional<JNewExpr> toJNewExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JNewMultiArrayExpr> toJNewMultiArrayExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JOrExpr> toJOrExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JPhiExpr> toJPhiExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JRemExpr> toJRemExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JShlExpr> toJShlExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JShrExpr> toJShrExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JSpecialInvokeExpr> toJSpecialInvokeExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JStaticInvokeExpr> toJStaticInvokeExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JSubExpr> toJSubExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JUshrExpr> toJUshrExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JVirtualInvokeExpr> toJVirtualInvokeExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JXorExpr> toJXorExpr() {
    return Optional.empty();
  }
}
