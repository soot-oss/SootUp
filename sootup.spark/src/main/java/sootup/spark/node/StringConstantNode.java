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
 * Models the {@code String} object a compile-time string literal (a Jimple {@code StringConstant})
 * evaluates to, carrying the literal's actual {@link #value} alongside it as it flows through the
 * PAG -- unlike a plain {@link AllocationNode}, which only records that *some* String was
 * allocated, with no record of which one.
 *
 * <p>This lets a literal be resolved against interprocedurally, at any point it reaches after being
 * passed as an argument, stored in a field, or otherwise assigned -- e.g. by {@code
 * MethodPAGStmtVisitor}'s {@code Class.forName} handling, which needs to know the actual string
 * value reaching a call site that may be several parameter-passing hops away from the literal's
 * origin, not just the site where the literal itself was written.
 *
 * <p>Equality is keyed on {@link #value} (not {@link Node#getType()}, which is always {@code
 * java.lang.String} for every instance) plus the allocation site, mirroring {@link
 * ReflectiveClassToken}: two nodes for the same literal at the same call site are the same node,
 * but two different literals must never collapse into one node even under {@code
 * isTypesForSites()}, since that would silently make one literal's value stand in for another's.
 */
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@SuperBuilder
@Getter
public class StringConstantNode extends AllocationNode {

  @NonNull String value;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof StringConstantNode other)) return false;
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
      return String.format("\"%s{string:%s}\"", getContainingMethodSig().getName(), value);
    }
    return String.format(
        "\"%s{%s:string %s}\"", getContainingMethodSig().getName(), getAllocationSite(), value);
  }
}
