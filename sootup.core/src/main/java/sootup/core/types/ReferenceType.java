package sootup.core.types;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2018-2020 Linghui Luo, Christian Brüggemann
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

import java.util.Optional;

/** Represents the signature of a Java type, e.g., a class, an array type, or null. */
public abstract class ReferenceType extends Type {
  protected boolean isClassType() {
    return false;
  }

  protected boolean isArrayType() {
    return false;
  }

  protected boolean isNullType() {
    return false;
  }

  protected ClassType asClassType() {
    return null;
  }

  protected ArrayType asArrayType() {
    return null;
  }

  protected NullType asNullType() {
    return null;
  }

  protected Optional<ClassType> toClassType() {
    return Optional.empty();
  }

  protected Optional<ArrayType> toArrayType() {
    return Optional.empty();
  }

  protected Optional<NullType> toNullType() {
    return Optional.empty();
  }
}
