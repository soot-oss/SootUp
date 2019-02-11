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

package de.upb.soot.jimple.common.expr;

import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.common.type.ArrayType;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.jimple.visitor.IExprVisitor;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.util.printer.IStmtPrinter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class JNewArrayExpr implements Expr {
  /**
   * 
   */
  private static final long serialVersionUID = 4481534412297120257L;
  private Type baseType;
  private final ValueBox sizeBox;

  public JNewArrayExpr(Type type, Value size) {
    this.baseType = type;
    this.sizeBox = Jimple.newImmediateBox(size);
  }

  @Override
  public Object clone() {
    return new JNewArrayExpr(getBaseType(), Jimple.cloneIfNecessary(getSize()));
  }

  /**
   * Returns a value of sizeBox if o is an instance of AbstractNewArrayExpr, else returns false.
   */
  @Override
  public boolean equivTo(Object o) {
    if (o instanceof JNewArrayExpr) {
      JNewArrayExpr ae = (JNewArrayExpr) o;
      return sizeBox.getValue().equivTo(ae.sizeBox.getValue()) && baseType.equals(ae.baseType);
    }
    return false;
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return sizeBox.getValue().equivHashCode() * 101 + baseType.hashCode() * 17;
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();

    buffer.append(Jimple.NEWARRAY + " (" + getBaseTypeString() + ")");
    buffer.append("[" + sizeBox.getValue().toString() + "]");

    return buffer.toString();
  }

  /**
   * Converts a parameter of type StmtPrinter to a string literal.
   */
  @Override
  public void toString(IStmtPrinter up) {
    up.literal(Jimple.NEWARRAY);
    up.literal(" ");
    up.literal("(");
    up.type(baseType);
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

  public void setBaseType(Type type) {
    baseType = type;
  }

  public ValueBox getSizeBox() {
    return sizeBox;
  }

  public Value getSize() {
    return sizeBox.getValue();
  }

  public void setSize(Value size) {
    sizeBox.setValue(size);
  }

  /**
   * Returns a list of type ValueBox, contains a list of values of sizeBox.
   */
  @Override
  public final List<ValueBox> getUseBoxes() {

    List<ValueBox> useBoxes = new ArrayList<>(sizeBox.getValue().getUseBoxes());
    useBoxes.add(sizeBox);

    return useBoxes;
  }

  /**
   * Returns an instance of ArrayType().
   */
  @Override
  public Type getType() {
    if (baseType instanceof ArrayType) {
      return ArrayType.getInstance(((ArrayType) baseType).baseType, ((ArrayType) baseType).numDimensions + 1);
    } else {
      return ArrayType.getInstance(baseType, 1);
    }
  }

  @Override
  public void accept(IVisitor sw) {
    ((IExprVisitor) sw).caseNewArrayExpr(this);
  }

  @Override
  public boolean equivTo(Object o, Comparator comparator) {
    return comparator.compare(this, o) == 0;
  }

}
