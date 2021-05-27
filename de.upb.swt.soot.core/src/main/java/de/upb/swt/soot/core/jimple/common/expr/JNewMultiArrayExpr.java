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

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.visitor.ExprVisitor;
import de.upb.swt.soot.core.types.ArrayType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/** Like {@link JNewArrayExpr}, but for multi-dimensional arrays. */
public final class JNewMultiArrayExpr implements Expr, Copyable {

  private final ArrayType baseType;
  private final List<Value> sizes;

  /**
   * Initiates a JNewMultiArrayExpr.
   *
   * @param type the type of the array
   * @param sizes the sizes
   */
  public JNewMultiArrayExpr(@Nonnull ArrayType type, @Nonnull List<Value> sizes) {
    this.baseType = type;
    this.sizes = sizes;
  }

  @Override
  public boolean equivTo(Object o, @Nonnull JimpleComparator comparator) {
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
  public void toString(@Nonnull StmtPrinter up) {
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

  public Value getSize(@Nonnull int index) {
    return sizes.get(index);
  }

  public int getSizeCount() {
    return sizes.size();
  }

  /** Returns a list of Values. */
  public List<Value> getSizes() {
    return sizes;
  }

  @Override
  @Nonnull
  public final List<Value> getUses() {
    List<Value> list = new ArrayList<>();
    list.addAll(sizes);
    for (Value size : sizes) {
      list.addAll(size.getUses());
    }
    return list;
  }

  @Nonnull
  @Override
  public Type getType() {
    return baseType;
  }

  @Override
  public void accept(@Nonnull ExprVisitor v) {
    v.caseNewMultiArrayExpr(this);
  }

  @Nonnull
  public JNewMultiArrayExpr withBaseType(@Nonnull ArrayType baseType) {
    return new JNewMultiArrayExpr(baseType, getSizes());
  }

  @Nonnull
  public JNewMultiArrayExpr withSizes(@Nonnull List<Value> sizes) {
    return new JNewMultiArrayExpr(baseType, sizes);
  }
}
