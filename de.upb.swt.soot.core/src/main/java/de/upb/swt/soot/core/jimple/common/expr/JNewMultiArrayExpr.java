/* Soot - a J*va Optimization Framework
 * Copyright (C) 1999 Patrick Lam
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

package de.upb.swt.soot.core.jimple.common.expr;

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.basic.ValueBox;
import de.upb.swt.soot.core.jimple.visitor.ExprVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.types.ArrayType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

/** Like {@link JNewArrayExpr}, but for multi-dimensional arrays. */
public final class JNewMultiArrayExpr implements Expr, Copyable {

  private final ArrayType baseType;
  private final ValueBox[] sizeBoxes;

  /**
   * Initiates a JNewMultiArrayExpr.
   *
   * @param type the type of the array
   * @param sizes the sizes
   */
  public JNewMultiArrayExpr(ArrayType type, List<? extends Value> sizes) {
    this.baseType = type;
    this.sizeBoxes = new ValueBox[sizes.size()];
    for (int i = 0; i < sizes.size(); i++) {
      sizeBoxes[i] = Jimple.newImmediateBox(sizes.get(i));
    }
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
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

    for (ValueBox element : sizeBoxes) {
      builder.append("[").append(element.getValue().toString()).append("]");
    }

    for (int i = 0; i < baseType.getDimension() - sizeBoxes.length; i++) {
      builder.append("[]");
    }

    return builder.toString();
  }

  @Override
  public void toString(StmtPrinter up) {
    Type t = baseType.getBaseType();

    up.literal(Jimple.NEWMULTIARRAY);
    up.literal(" (");
    up.typeSignature(t);
    up.literal(")");

    for (ValueBox element : sizeBoxes) {
      up.literal("[");
      element.toString(up);
      up.literal("]");
    }

    for (int i = 0; i < baseType.getDimension() - sizeBoxes.length; i++) {
      up.literal("[]");
    }
  }

  public ArrayType getBaseType() {
    return baseType;
  }

  public ValueBox getSizeBox(int index) {
    return sizeBoxes[index];
  }

  public int getSizeCount() {
    return sizeBoxes.length;
  }

  public Value getSize(int index) {
    return sizeBoxes[index].getValue();
  }

  /** Returns a list of values of sizeBoxes. */
  public List<Value> getSizes() {
    List<Value> toReturn = new ArrayList<>();

    for (ValueBox element : sizeBoxes) {
      toReturn.add(element.getValue());
    }

    return toReturn;
  }

  @Override
  public final List<ValueBox> getUseBoxes() {
    List<ValueBox> list = new ArrayList<>();
    Collections.addAll(list, sizeBoxes);

    for (ValueBox element : sizeBoxes) {
      list.addAll(element.getValue().getUseBoxes());
    }

    return list;
  }

  @Override
  public Type getType() {
    return baseType;
  }

  @Override
  public void accept(Visitor sw) {
    ((ExprVisitor) sw).caseNewMultiArrayExpr(this);
  }

  @Nonnull
  public JNewMultiArrayExpr withBaseType(ArrayType baseType) {
    return new JNewMultiArrayExpr(baseType, getSizes());
  }

  @Nonnull
  public JNewMultiArrayExpr withSizes(List<Value> sizes) {
    return new JNewMultiArrayExpr(baseType, sizes);
  }
}
