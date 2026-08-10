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

/**
 * Models the {@code java.lang.Class} object a class-literal constant (a Jimple {@code
 * ClassConstant}, e.g. {@code SomeType.class} or the implicit constant a bytecode {@code ldc
 * <Class>} pushes) evaluates to, carrying the represented class's descriptor ({@link #value}, e.g.
 * {@code "Ljavax/xml/transform/TransformerFactory;"}) alongside it as it flows through the PAG.
 *
 * <p>{@link Node#getType()} is always {@code java.lang.Class} (see {@code
 * JavaJimple.newClassConstant}, which fixes every {@code ClassConstant}'s type this way) -- that
 * alone is what makes virtual dispatch on this node work at all: before this node type existed,
 * {@code ClassConstant}s were dropped entirely ({@code ignore()}), so a receiver holding one (e.g.
 * a {@code Class} argument passed as a class literal, then called with {@code .cast(...)} or {@code
 * .isInstance(...)}) had an empty points-to set and could never dispatch to anything. {@link
 * #value} additionally lets a caller recover *which* class-literal reached a given point, mirroring
 * {@link StringConstantNode} and {@link ReflectiveClassToken}.
 *
 * <p>Equality is keyed on {@link #value} (not {@link Node#getType()}, which is always the same for
 * every instance) plus the allocation site, mirroring {@link StringConstantNode}: two nodes for the
 * same class-literal at the same call site are the same node, but two different class literals must
 * never collapse into one node even under {@code isTypesForSites()}.
 */
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@SuperBuilder
@Getter
public class ClassConstantNode extends AllocationNode {

  @NonNull String value;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ClassConstantNode other)) return false;
    if (getAllocationSite() == null && other.getAllocationSite() == null) {
      return Objects.equals(value, other.value);
    }
    return Objects.equals(getAllocationSite(), other.getAllocationSite())
        && Objects.equals(value, other.value);
  }

  @Override
  public int hashCode() {
    return getAllocationSite() == null
        ? Objects.hash(value)
        : Objects.hash(getAllocationSite(), value);
  }

  @Override
  public String toString() {
    if (getAllocationSite() == null) {
      return String.format("\"%s{class:%s}\"", getContainingMethodSig().getName(), value);
    }
    return String.format(
        "\"%s{%s:class %s}\"", getContainingMethodSig().getName(), getAllocationSite(), value);
  }
}
