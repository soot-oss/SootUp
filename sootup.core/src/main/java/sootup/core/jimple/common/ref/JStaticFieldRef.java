package sootup.core.jimple.common.ref;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallee-Rai, Linghui Luo, Christian Brüggemann and others
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

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.visitor.RefVisitor;
import sootup.core.signatures.FieldSignature;
import sootup.core.util.Copyable;
import sootup.core.util.printer.StmtPrinter;

public final class JStaticFieldRef extends JFieldRef implements Copyable {

  public JStaticFieldRef(@Nonnull FieldSignature fieldSig) {
    super(fieldSig);
  }

  @Override
  public String toString() {
    return getFieldSignature().toString();
  }

  @Override
  public void toString(@Nonnull StmtPrinter up) {
    up.fieldSignature(getFieldSignature());
  }

  @Override
  @Nonnull
  public List<Value> getUses() {
    return Collections.emptyList();
  }

  @Override
  public boolean equivTo(Object o, @Nonnull JimpleComparator comparator) {
    return comparator.caseStaticFieldRef(this, o);
  }

  @Override
  public int equivHashCode() {
    return getFieldSignature().hashCode() * 23;
  }

  @Override
  public void accept(@Nonnull RefVisitor v) {
    v.caseStaticFieldRef(this);
  }

  @Nonnull
  public JStaticFieldRef withFieldSignature(@Nonnull FieldSignature fieldSig) {
    return new JStaticFieldRef(fieldSig);
  }
}
