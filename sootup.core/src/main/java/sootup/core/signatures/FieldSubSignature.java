package sootup.core.signatures;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Jan Martin Persch, Markus Schmidt
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

import com.google.common.base.Suppliers;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import sootup.core.jimple.Jimple;
import sootup.core.types.Type;
import sootup.core.util.printer.StmtPrinter;

/**
 * Defines a sub-signature of a field, containing the field name and the type signature.
 *
 * @author Jan Martin Persch
 */
public class FieldSubSignature extends SootClassMemberSubSignature
    implements Comparable<FieldSubSignature> {

  /**
   * Creates a new instance of the {@link FieldSubSignature} class.
   *
   * @param name The method name.
   * @param type The type signature.
   */
  public FieldSubSignature(@Nonnull String name, @Nonnull Type type) {
    super(name, type);
  }

  @Override
  public int compareTo(@Nonnull FieldSubSignature o) {
    return super.compareTo(o);
  }

  private final Supplier<String> _cachedToString =
      Suppliers.memoize(() -> String.format("%s %s", getType(), getName()));

  @Override
  @Nonnull
  public String toString() {
    return _cachedToString.get();
  }

  @Override
  public void toString(StmtPrinter printer) {
    printer.typeSignature(getType());
    printer.literal(" ");
    printer.literal(Jimple.escape(getName()));
  }
}
