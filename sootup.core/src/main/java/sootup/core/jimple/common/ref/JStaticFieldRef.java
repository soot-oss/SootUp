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

import java.util.List;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.common.LValue;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.visitor.RefVisitor;
import sootup.core.signatures.FieldSignature;
import sootup.core.util.printer.StmtPrinter;

public final class JStaticFieldRef extends JFieldRef implements LValue {

  public JStaticFieldRef(@NonNull FieldSignature fieldSig) {
    super(fieldSig);
  }

  @Override
  public String toString() {
    return getFieldSignature().toString();
  }

  @Override
  public void toString(@NonNull StmtPrinter up) {
    up.fieldSignature(getFieldSignature());
  }

  @Override
  public void collectUses(List<Value> collector) {}

  @Override
  public boolean equivTo(Object o, @NonNull JimpleComparator comparator) {
    return comparator.caseStaticFieldRef(this, o);
  }

  @Override
  public int equivHashCode() {
    return getFieldSignature().hashCode() * 23;
  }

  @Override
  public <V extends RefVisitor> V accept(@NonNull V v) {

    v.caseStaticFieldRef(this);
    return v;
  }

  @NonNull
  public JStaticFieldRef withFieldSignature(@NonNull FieldSignature fieldSig) {
    return new JStaticFieldRef(fieldSig);
  }
}
