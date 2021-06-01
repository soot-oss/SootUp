package de.upb.swt.soot.core.jimple.common.expr;

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

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.basic.ValueBox;
import de.upb.swt.soot.core.jimple.visitor.ExprVisitor;
import de.upb.swt.soot.core.types.ArrayType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/** An expression that creates a new array of a certain type and a certain size. */
public final class JNewArrayExpr implements Expr, Copyable {

  private final Type baseType;
  private final ValueBox sizeBox;
  private final IdentifierFactory identifierFactory;
  // new attribute: later if ValueBox is deleted, then add "final" to it.
  private Value size;

  public JNewArrayExpr(Type baseType, Value size, IdentifierFactory identifierFactory) {
    this.baseType = baseType;
    this.sizeBox = Jimple.newImmediateBox(size);
    this.identifierFactory = identifierFactory;
    // new attribute: later if ValueBox is deleted, then fit the constructor.
    this.size = size;
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
    return sizeBox.getValue().equivHashCode() * 101 + baseType.hashCode() * 17;
  }

  @Override
  public String toString() {
    return (Jimple.NEWARRAY + " (")
        + getBaseTypeString()
        + ")"
        + "["
        + sizeBox.getValue().toString()
        + "]";
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
    sizeBox.toString(up);
    up.literal("]");
  }

  private String getBaseTypeString() {
    return baseType.toString();
  }

  public Type getBaseType() {
    return baseType;
  }

  public ValueBox getSizeBox() {
    return sizeBox;
  }

  public Value getSize() {
    return sizeBox.getValue();
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
  public JNewArrayExpr withBaseType(Type baseType) {
    return new JNewArrayExpr(baseType, getSize(), identifierFactory);
  }

  @Nonnull
  public JNewArrayExpr withSize(Value size) {
    return new JNewArrayExpr(baseType, size, identifierFactory);
  }
}
