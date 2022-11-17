package sootup.core.jimple.common.ref;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999-2020 Patrick Lam, Ondrej Lhotak, Linghui Luo
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

/*
 * @author Linghui Luo
 * @version 1.0
 */

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.visitor.RefVisitor;
import sootup.core.signatures.FieldSignature;
import sootup.core.util.Copyable;
import sootup.core.util.printer.StmtPrinter;

public final class JInstanceFieldRef extends JFieldRef implements Copyable {

  private final Local base;

  /**
   * Create a reference to a class' instance field.
   *
   * @param base the base value of the field
   * @param fieldSig the field sig
   */
  public JInstanceFieldRef(@Nonnull Local base, @Nonnull FieldSignature fieldSig) {
    super(fieldSig);
    this.base = base;
  }

  @Override
  public String toString() {
    return base.toString() + "." + getFieldSignature().toString();
  }

  @Override
  public void toString(@Nonnull StmtPrinter up) {
    base.toString(up);
    up.literal(".");
    up.fieldSignature(getFieldSignature());
  }

  public Local getBase() {
    return base;
  }

  @Override
  @Nonnull
  public final List<Value> getUses() {
    List<Value> list = new ArrayList<>(base.getUses());
    list.add(base);
    return list;
  }

  @Override
  public void accept(@Nonnull RefVisitor v) {
    v.caseInstanceFieldRef(this);
  }

  @Override
  public boolean equivTo(Object o, @Nonnull JimpleComparator comparator) {
    return comparator.caseInstanceFieldRef(this, o);
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return getFieldSignature().hashCode() * 101 + base.hashCode() + 17;
  }

  @Nonnull
  public JInstanceFieldRef withBase(@Nonnull Local base) {
    return new JInstanceFieldRef(base, getFieldSignature());
  }

  @Nonnull
  public JInstanceFieldRef withFieldSignature(@Nonnull FieldSignature fieldSignature) {
    return new JInstanceFieldRef(getBase(), fieldSignature);
  }
}
