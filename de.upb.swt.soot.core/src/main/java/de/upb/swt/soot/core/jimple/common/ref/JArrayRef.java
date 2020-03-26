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

package de.upb.swt.soot.core.jimple.common.ref;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.basic.ValueBox;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.types.ArrayType;
import de.upb.swt.soot.core.types.NullType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.types.UnknownType;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public final class JArrayRef implements ConcreteRef, Copyable {

  private final ValueBox baseBox;
  private final ValueBox indexBox;
  private final IdentifierFactory identifierFactory;

  public JArrayRef(Value base, Value index, IdentifierFactory identifierFactory) {
    this(Jimple.newLocalBox(base), Jimple.newImmediateBox(index), identifierFactory);
  }

  private JArrayRef(ValueBox baseBox, ValueBox indexBox, IdentifierFactory identifierFactory) {
    this.baseBox = baseBox;
    this.indexBox = indexBox;
    this.identifierFactory = identifierFactory;
  }

  private Type determineType(IdentifierFactory identifierFactory) {
    Value base = baseBox.getValue();
    Type type = base.getType();

    if (type.equals(UnknownType.getInstance())) {
      return UnknownType.getInstance();
    } else if (type.equals(NullType.getInstance())) {
      return NullType.getInstance();
    } else {
      // use makeArrayType on non-array type references when they propagate to this point.
      // kludge, most likely not correct.
      // may stop spark from complaining when it gets passed phantoms.
      // ideally I'd want to find out just how they manage to get this far.
      ArrayType arrayType;
      if (type instanceof ArrayType) {
        arrayType = (ArrayType) type;
      } else {
        arrayType = identifierFactory.getArrayType(type, 1);
      }

      // FIXME: [JMP] Should unwrapping not be done by the `ArrayType` itself?
      if (arrayType.getDimension() == 1) {
        return arrayType.getBaseType();
      } else {
        return identifierFactory.getArrayType(
            arrayType.getBaseType(), arrayType.getDimension() - 1);
      }
    }
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
  public void toString(StmtPrinter up) {
    baseBox.toString(up);
    up.literal("[");
    indexBox.toString(up);
    up.literal("]");
  }

  public Value getBase() {
    return baseBox.getValue();
  }

  public ValueBox getBaseBox() {
    return baseBox;
  }

  public Value getIndex() {
    return indexBox.getValue();
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
    return determineType(identifierFactory);
  }

  @Override
  public void accept(Visitor sw) {
    // TODO
  }

  @Nonnull
  public JArrayRef withBase(Value base) {
    return new JArrayRef(base, getIndex(), identifierFactory);
  }

  @Nonnull
  public JArrayRef withIndex(Value index) {
    return new JArrayRef(getBase(), index, identifierFactory);
  }
}
