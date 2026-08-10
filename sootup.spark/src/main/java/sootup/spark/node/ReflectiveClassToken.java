package sootup.spark.node;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2002-2026 Ondrej Lhotak, Kadiray Karakaya and others
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

import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import sootup.core.types.ClassType;

/**
 * Models the object a reflective class-lookup API hands back at a {@code Class.forName(String)} /
 * {@code Class#getConstructor()} / {@code Class#getDeclaredConstructor()} call site, once the
 * looked-up class name is a compile-time string literal.
 *
 * <p>{@link Node#getType()} stays the static return type of the producing call ({@code
 * java.lang.Class} or {@code java.lang.reflect.Constructor}), since that's what ordinary virtual
 * dispatch would (harmlessly) see this node as if some other, unrecognized reflective method is
 * called on it. {@link #represented} is what {@code Solver}'s reflective-call handling actually
 * dispatches on: the class that {@code newInstance()} would instantiate.
 *
 * <p>Equality is keyed on {@link #represented} (not {@link Node#getType()}, which is always the
 * same for every token) plus the allocation site, mirroring {@link AllocationNode} -- two tokens
 * for the same literal class name at the same call site are the same node; two different literal
 * class names must never collapse into one node even under {@code isTypesForSites()}, since that
 * would silently pick one arbitrary target class for {@code newInstance()}.
 */
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@SuperBuilder
@Getter
public class ReflectiveClassToken extends AllocationNode {

  @NonNull ClassType represented;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ReflectiveClassToken other)) return false;
    if (getAllocationSite() == null && other.getAllocationSite() == null) {
      return Objects.equals(represented, other.represented);
    }
    return Objects.equals(getAllocationSite(), other.getAllocationSite())
        && Objects.equals(represented, other.represented);
  }

  @Override
  public int hashCode() {
    return getAllocationSite() == null
        ? Objects.hash(represented)
        : Objects.hash(getAllocationSite(), represented);
  }

  @Override
  public String toString() {
    if (getAllocationSite() == null) {
      return String.format(
          "\"%s{reflective:%s}\"", getContainingMethodSig().getName(), represented);
    }
    return String.format(
        "\"%s{%s:reflective %s}\"",
        getContainingMethodSig().getName(), getAllocationSite(), represented);
  }
}
