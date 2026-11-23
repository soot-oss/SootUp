package sootup.spark;

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

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SparkOptions {

  public static SparkOptions defaultOptions() {
    return SparkOptions.builder().build();
  }

  /**
   * Options that emulate Variable Type Analysis (Sundaresan et al., 2000), like Soot's {@code vta}
   * option: field-based ({@code ignoreBaseObjects}), one abstract object per type ({@code
   * typesForSites}), and the points-to sets computed on a CHA call graph, which they then narrow
   * (no {@code onFlyCallGraph}). Unlike Soot, the strongly connected components of the pointer
   * assignment graph are not collapsed, so it takes longer than Soot's VTA.
   */
  public static SparkOptions vta() {
    return SparkOptions.builder()
        .ignoreBaseObjects(true)
        .typesForSites(true)
        .onFlyCallGraph(false)
        .build();
  }

  @Builder.Default boolean ignoreBaseObjects = false; // Field-based vs. field-sensitive
  @Builder.Default boolean parmsAsFields = false; // Represent params as field ref nodes
  @Builder.Default boolean returnsAsFields = false; // Represent returns as field ref nodes

  @Builder.Default boolean typesForSites = false; // Group allocations by run-time type
  @Builder.Default boolean mergeStringBuffer = true; // Group StringBuffer allocations

  @Builder.Default boolean simulateNatives = true; // Model standard library native methods
  @Builder.Default boolean simpleEdgesBidirectional = false; // For unification-based analysis
  @Builder.Default boolean onFlyCallGraph = false; // Build interprocedural edges dynamically
}
