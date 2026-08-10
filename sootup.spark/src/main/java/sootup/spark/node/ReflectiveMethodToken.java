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
 * Models the {@code java.lang.reflect.Method} object handed back by {@code Class#getMethod(String,
 * Class[])} / {@code Class#getDeclaredMethod(String, Class[])} on a {@link ReflectiveClassToken}
 * receiver, once the looked-up method name is a compile-time string literal at the call site.
 *
 * <p>The {@code Class[]} parameter-type array isn't tracked (mirrors {@link ReflectiveClassToken}
 * not tracking {@code getConstructor(Class[])}'s array): {@link #methodName} alone decides what
 * {@code Solver}'s {@code Method#invoke(Object, Object[])} handling dispatches to, by scanning the
 * invoke call's runtime receiver's declared methods for a name match. This is exact whenever the
 * declaring class has at most one method by that name -- true for every reflective call site this
 * analysis has needed to resolve so far -- and an over-approximation (picks one arbitrary overload)
 * otherwise, same tradeoff {@code Solver}'s constructor resolution already makes.
 *
 * <p>Equality is keyed on {@link #declaringClass} + {@link #methodName} plus the allocation site,
 * mirroring {@link ReflectiveClassToken}: two different method names must never collapse into one
 * node even under {@code isTypesForSites()}.
 */
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@SuperBuilder
@Getter
public class ReflectiveMethodToken extends AllocationNode {

  @NonNull ClassType declaringClass;
  @NonNull String methodName;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ReflectiveMethodToken other)) return false;
    if (getAllocationSite() == null && other.getAllocationSite() == null) {
      return Objects.equals(declaringClass, other.declaringClass)
          && Objects.equals(methodName, other.methodName);
    }
    return Objects.equals(getAllocationSite(), other.getAllocationSite())
        && Objects.equals(declaringClass, other.declaringClass)
        && Objects.equals(methodName, other.methodName);
  }

  @Override
  public int hashCode() {
    return getAllocationSite() == null
        ? Objects.hash(declaringClass, methodName)
        : Objects.hash(getAllocationSite(), declaringClass, methodName);
  }

  @Override
  public String toString() {
    if (getAllocationSite() == null) {
      return String.format(
          "\"%s{reflective method:%s::%s}\"",
          getContainingMethodSig().getName(), declaringClass, methodName);
    }
    return String.format(
        "\"%s{%s:reflective method %s::%s}\"",
        getContainingMethodSig().getName(), getAllocationSite(), declaringClass, methodName);
  }
}
