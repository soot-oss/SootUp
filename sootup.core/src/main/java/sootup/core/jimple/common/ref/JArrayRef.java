package sootup.core.jimple.common.ref;

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

import java.util.stream.Stream;
import javax.annotation.Nonnull;
import sootup.core.jimple.basic.*;
import sootup.core.jimple.visitor.RefVisitor;
import sootup.core.types.ArrayType;
import sootup.core.types.NullType;
import sootup.core.types.Type;
import sootup.core.types.UnknownType;
import sootup.core.util.printer.StmtPrinter;

public final class JArrayRef implements ConcreteRef, LValue {

  private final Local base;
  private final Immediate index;

  public JArrayRef(@Nonnull Local base, @Nonnull Immediate index) {
    this.base = base;
    this.index = index;
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
  public Stream<Value> getUses() {
    return Stream.concat(
        Stream.concat(base.getUses(), Stream.of(base)),
        Stream.concat(index.getUses(), Stream.of(index)));
  }

  @Override
  @Nonnull
  public Type getType() {
    Type baseType = base.getType();
    if (baseType instanceof ArrayType) {
      return ((ArrayType) baseType).getElementType();
    } else if (baseType.equals(NullType.getInstance())) {
      return NullType.getInstance();
    } else {
      return UnknownType.getInstance();
    }
  }

  @Override
  public <V extends RefVisitor> V accept(@Nonnull V v) {

    v.caseArrayRef(this);
    return v;
  }

  @Nonnull
  public JArrayRef withBase(@Nonnull Local base) {
    return new JArrayRef(base, getIndex());
  }

  @Nonnull
  public JArrayRef withIndex(@Nonnull Immediate index) {
    return new JArrayRef(getBase(), index);
  }
}
