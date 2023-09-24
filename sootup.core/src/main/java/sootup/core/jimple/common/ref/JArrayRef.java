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

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.visitor.RefVisitor;
import sootup.core.types.Type;
import sootup.core.util.Copyable;
import sootup.core.util.printer.StmtPrinter;

public final class JArrayRef implements ConcreteRef, Copyable {

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
    return base.getType();
  }

  @Override
  public void accept(@Nonnull RefVisitor v) {
    v.caseArrayRef(this);
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
