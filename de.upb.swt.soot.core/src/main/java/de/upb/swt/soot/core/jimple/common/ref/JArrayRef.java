package de.upb.swt.soot.core.jimple.common.ref;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999-2020 Patrick Lam, Linghui Luo, Markus Schmidt and others
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
import de.upb.swt.soot.core.jimple.basic.Immediate;
import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.visitor.RefVisitor;
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

  private final Local base;
  private final Immediate index;
  private final IdentifierFactory identifierFactory;

  public JArrayRef(
      @Nonnull Local base, @Nonnull Immediate index, @Nonnull IdentifierFactory identifierFactory) {
    this.base = base;
    this.index = index;
    this.identifierFactory = identifierFactory;
  }

  private Type determineType(@Nonnull IdentifierFactory identifierFactory) {
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
  public boolean equivTo(Object o, @Nonnull JimpleComparator comparator) {
    return comparator.caseArrayRef(this, o);
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return getBase().equivHashCode() * 101 + getIndex().equivHashCode() + 17;
  }

  @Override
  public String toString() {
    return base.toString() + "[" + index.toString() + "]";
  }

  @Override
  public void toString(@Nonnull StmtPrinter up) {
    base.toString(up);
    up.literal("[");
    index.toString(up);
    up.literal("]");
  }

  @Nonnull
  public Local getBase() {
    return base;
  }

  @Nonnull
  public Immediate getIndex() {
    return index;
  }

  @Override
  @Nonnull
  public List<Value> getUses() {
    List<Value> list = new ArrayList<>(base.getUses());
    list.add(base);
    list.addAll(index.getUses());
    list.add(index);
    return list;
  }

  @Override
  @Nonnull
  public Type getType() {
    return determineType(identifierFactory);
  }

  @Override
  public void accept(@Nonnull RefVisitor v) {
    v.caseArrayRef(this);
  }

  @Nonnull
  public JArrayRef withBase(@Nonnull Local base) {
    return new JArrayRef(base, getIndex(), identifierFactory);
  }

  @Nonnull
  public JArrayRef withIndex(@Nonnull Immediate index) {
    return new JArrayRef(getBase(), index, identifierFactory);
  }
}
