package sootup.core.jimple.common.ref;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999-2020 Patrick Lam, linghui Luo, Markus Schmidt
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
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.visitor.RefVisitor;
import sootup.core.types.Type;
import sootup.core.util.printer.StmtPrinter;

public final class JCaughtExceptionRef implements IdentityRef {

  private final Type type;

  public JCaughtExceptionRef(@NonNull Type type) {
    this.type = type;
  }

  @Override
  public boolean equivTo(Object o, @NonNull JimpleComparator comparator) {
    return comparator.caseCaughtException(this, o);
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return 1729;
  }

  @Override
  public String toString() {
    return "@caughtexception";
  }

  @Override
  public void toString(@NonNull StmtPrinter up) {
    up.identityRef(this);
  }

  @Override
  @NonNull
  public Stream<Value> getUses() {
    return Stream.empty();
  }

  @NonNull
  @Override
  public Type getType() {
    return type;
  }

  @Override
  public <V extends RefVisitor> V accept(@NonNull V v) {

    v.caseCaughtExceptionRef(this);
    return v;
  }
}
