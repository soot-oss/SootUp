package sootup.core.signatures;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Jan Martin Persch and others
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

import com.google.common.base.Objects;
import com.google.common.base.Suppliers;
import java.util.function.Supplier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.jimple.Jimple;
import sootup.core.types.Type;
import sootup.core.util.printer.StmtPrinter;

/**
 * Defines the base class for class member sub signatures.
 *
 * @see FieldSubSignature
 * @see MethodSubSignature
 * @author Jan Martin Persch
 */
public abstract class SootClassMemberSubSignature {

  @NonNull private final String name;
  @NonNull private final Type type;

  /** Creates a new instance of the {@link SootClassMemberSubSignature} class. */
  protected SootClassMemberSubSignature(@NonNull String name, @NonNull Type type) {
    this.name = name;
    this.type = type;
  }

  /**
   * Gets the name.
   *
   * @return The value to get.
   */
  @NonNull
  public String getName() {
    return name;
  }

  /**
   * Gets the type.
   *
   * @return The value to get.
   */
  @NonNull
  public Type getType() {
    return type;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SootClassMemberSubSignature that = (SootClassMemberSubSignature) o;

    return Objects.equal(getName(), that.getName()) && Objects.equal(getType(), that.getType());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getName(), getType());
  }

  protected int compareTo(@NonNull SootClassMemberSubSignature o) {
    int r = this.getName().compareTo(o.getName());

    if (r != 0) {
      return r;
    }

    return this.getType().toString().compareTo(o.getType().toString());
  }

  private final Supplier<String> _cachedToString =
      Suppliers.memoize(
          () ->
              String.format(
                  "%s %s", Jimple.escape(getType().toString()), Jimple.escape(getName())));

  @Override
  @NonNull
  public String toString() {
    return _cachedToString.get();
  }

  public abstract void toString(StmtPrinter printer);
}
