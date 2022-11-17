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

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import sootup.core.IdentifierFactory;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.visitor.ExprVisitor;
import sootup.core.types.ArrayType;
import sootup.core.types.Type;
import sootup.core.util.Copyable;
import sootup.core.util.printer.StmtPrinter;

/** An expression that creates a new array of a certain type and a certain size. */
public final class JNewArrayExpr implements Expr, Copyable {

  @Nonnull private final Type baseType;
  @Nonnull private final Immediate size;
  @Nonnull private final IdentifierFactory identifierFactory;

  public JNewArrayExpr(
      @Nonnull Type baseType,
      @Nonnull Immediate size,
      @Nonnull IdentifierFactory identifierFactory) {
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
  public boolean equivTo(Object o, @Nonnull JimpleComparator comparator) {
    return comparator.caseNewArrayExpr(this, o);
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return size.equivHashCode() * 101 + baseType.hashCode() * 17;
  }

  @Override
  public String toString() {
    return (Jimple.NEWARRAY + " (") + baseType.toString() + ")" + "[" + size.toString() + "]";
  }

  /** Converts a parameter of type StmtPrinter to a string literal. */
  @Override
  public void toString(@Nonnull StmtPrinter up) {
    up.literal(Jimple.NEWARRAY);
    up.literal(" ");
    up.literal("(");
    up.typeSignature(baseType);
    up.literal(")");
    up.literal("[");
    size.toString(up);
    up.literal("]");
  }

  @Nonnull
  public Type getBaseType() {
    return baseType;
  }

  @Nonnull
  public Immediate getSize() {
    return size;
  }

  /**
   * Returns a list of type Value, contains a list of values with size
   *
   * @return
   */
  @Override
  @Nonnull
  public final List<Value> getUses() {
    List<Value> uses = new ArrayList<>(size.getUses());
    uses.add(size);
    return uses;
  }

  /** Returns an instance of ArrayType(). */
  @Nonnull
  @Override
  public Type getType() {
    return simplify(baseType, identifierFactory);
  }

  @Override
  public void accept(@Nonnull ExprVisitor v) {
    v.caseNewArrayExpr(this);
  }

  @Nonnull
  public JNewArrayExpr withBaseType(@Nonnull Type baseType) {
    return new JNewArrayExpr(baseType, getSize(), identifierFactory);
  }

  @Nonnull
  public JNewArrayExpr withSize(@Nonnull Immediate size) {
    return new JNewArrayExpr(baseType, size, identifierFactory);
  }
}
