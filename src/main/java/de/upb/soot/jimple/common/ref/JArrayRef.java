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

package de.upb.soot.jimple.common.ref;

import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.JimpleComparator;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.common.type.ArrayType;
import de.upb.soot.jimple.common.type.NullType;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.jimple.common.type.UnknownType;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.util.printer.IStmtPrinter;

import java.util.ArrayList;
import java.util.List;

public class JArrayRef implements ConcreteRef {
  /**
   * 
   */
  private static final long serialVersionUID = 7705080573810511044L;
  protected ValueBox baseBox;
  protected ValueBox indexBox;

  public JArrayRef(Value base, Value index) {
    this(Jimple.newLocalBox(base), Jimple.newImmediateBox(index));
  }

  protected JArrayRef(ValueBox baseBox, ValueBox indexBox) {
    this.baseBox = baseBox;
    this.indexBox = indexBox;
  }

  @Override
  public Object clone() {
    return new JArrayRef(Jimple.cloneIfNecessary(getBase()), Jimple.cloneIfNecessary(getIndex()));
  }

  @Override
  public boolean equivTo(Object o) {
    return JimpleComparator.getInstance().caseArrayRef(this, o);
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseArrayRef(this, o);
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return getBase().equivHashCode() * 101 + getIndex().equivHashCode() + 17;
  }

  @Override
  public String toString() {
    return baseBox.getValue().toString() + "[" + indexBox.getValue().toString() + "]";
  }

  @Override
  public void toString(IStmtPrinter up) {
    baseBox.toString(up);
    up.literal("[");
    indexBox.toString(up);
    up.literal("]");
  }

  public Value getBase() {
    return baseBox.getValue();
  }

  public void setBase(Local base) {
    baseBox.setValue(base);
  }

  public ValueBox getBaseBox() {
    return baseBox;
  }

  public Value getIndex() {
    return indexBox.getValue();
  }

  public void setIndex(Value index) {
    indexBox.setValue(index);
  }

  public ValueBox getIndexBox() {
    return indexBox;
  }

  @Override
  public List<ValueBox> getUseBoxes() {

    List<ValueBox> useBoxes = new ArrayList<>(baseBox.getValue().getUseBoxes());
    useBoxes.add(baseBox);

    useBoxes.addAll(indexBox.getValue().getUseBoxes());
    useBoxes.add(indexBox);

    return useBoxes;
  }

  @Override
  public Type getType() {
    Value base = baseBox.getValue();
    Type type = base.getType();

    if (type.equals(UnknownType.INSTANCE)) {
      return UnknownType.INSTANCE;
    } else if (type.equals(NullType.INSTANCE)) {
      return NullType.INSTANCE;
    } else {
      // use makeArrayType on non-array type references when they propagate to this point.
      // kludge, most likely not correct.
      // may stop spark from complaining when it gets passed phantoms.
      // ideally I'd want to find out just how they manage to get this far.
      ArrayType arrayType;
      if (type instanceof ArrayType) {
        arrayType = (ArrayType) type;
      } else {
        arrayType = type.makeArrayType();
      }

      if (arrayType.numDimensions == 1) {
        return arrayType.baseType;
      } else {
        return ArrayType.getInstance(arrayType.baseType, arrayType.numDimensions - 1);
      }
    }
  }

  @Override
  public void accept(IVisitor sw) {
    // TODO
  }

}
