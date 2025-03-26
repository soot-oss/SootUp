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

import java.util.List;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.visitor.ExprVisitor;
import sootup.core.types.ArrayType;
import sootup.core.types.Type;
import sootup.core.util.printer.StmtPrinter;

/** Like {@link JNewArrayExpr}, but for multi-dimensional arrays. */
public final class JNewMultiArrayExpr implements Expr {

  private final ArrayType baseType;
  private final List<Immediate> sizes;

  /**
   * Initiates a JNewMultiArrayExpr.
   *
   * @param type the type of the array
   * @param sizes the sizes
   */
  public JNewMultiArrayExpr(@NonNull ArrayType type, @NonNull List<Immediate> sizes) {
    this.baseType = type;
    this.sizes = sizes;
  }

  @Override
  public boolean equivTo(Object o, @NonNull JimpleComparator comparator) {
    return comparator.caseNewMultiArrayExpr(this, o);
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return baseType.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    Type t = baseType.getBaseType();
    builder.append(Jimple.NEWMULTIARRAY + " (").append(t.toString()).append(")");

    for (Value element : sizes) {
      builder.append("[").append(element.toString()).append("]");
    }

    for (int i = 0; i < baseType.getDimension() - sizes.size(); i++) {
      builder.append("[]");
    }

    return builder.toString();
  }

  @Override
  public void toString(@NonNull StmtPrinter up) {
    Type t = baseType.getBaseType();

    up.literal(Jimple.NEWMULTIARRAY);
    up.literal(" (");
    up.typeSignature(t);
    up.literal(")");

    for (Value element : sizes) {
      up.literal("[");
      element.toString(up);
      up.literal("]");
    }

    for (int i = 0; i < baseType.getDimension() - sizes.size(); i++) {
      up.literal("[]");
    }
  }

  public ArrayType getBaseType() {
    return baseType;
  }

  public Immediate getSize(int index) {
    return sizes.get(index);
  }

  public int getSizeCount() {
    return sizes.size();
  }

  /** Returns a list of Values. */
  public List<Immediate> getSizes() {
    return sizes;
  }

  @Override
  @NonNull
  public Stream<Value> getUses() {
    return Stream.concat(sizes.stream(), sizes.stream().flatMap(Value::getUses));
  }

  @NonNull
  @Override
  public Type getType() {
    return baseType;
  }

  @Override
  public <V extends ExprVisitor> V accept(@NonNull V v) {
    v.caseNewMultiArrayExpr(this);
    return v;
  }

  @NonNull
  public JNewMultiArrayExpr withBaseType(@NonNull ArrayType baseType) {
    return new JNewMultiArrayExpr(baseType, getSizes());
  }

  @NonNull
  public JNewMultiArrayExpr withSizes(@NonNull List<Immediate> sizes) {
    return new JNewMultiArrayExpr(baseType, sizes);
  }

  public boolean isArrayOfPrimitives() {
    return baseType.isArrayTypeOfPrimitives();
  }
}
