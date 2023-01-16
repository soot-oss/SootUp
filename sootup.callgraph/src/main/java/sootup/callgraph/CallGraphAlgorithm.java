package sootup.callgraph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Linghui Luo, Christian Brüggemann, Ben Hermann, Markus Schmidt
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
import javax.annotation.Nonnull;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.types.JavaClassType;

/** The interface of a implemented call graph algorithms */
public interface CallGraphAlgorithm {

  /**
   * This method initializes and starts the call graph algorithm without given entry points. Before
   * the algorithm is started, all main methods are searched and set as entry points.
   *
   * @return a generated call graph with every main class as starting point.
   */
  @Nonnull
  CallGraph initialize();

  /**
   * This method initializes and starts the call graph algorithm with given entry points. The entry
   * points define the start methods in the call graph algorithm.
   *
   * @param entryPoints a list of entry points for the call graph algorithm. The algorithm starts at
   *     these methods and inspects all reachable methods.
   * @return a generated call graph with every entry point as starting point.
   */
  @Nonnull
  CallGraph initialize(@Nonnull List<MethodSignature> entryPoints);

  /**
   * Adds a class to the call graph. All methods will be set as entry points in the call graph
   * algorithm. Starts the call graph algorithm. The found edges will be added to the call graph.
   *
   * @param oldCallGraph the call graph which will be modified.
   * @param classType the type of the calls.
   * @return the modified call graph containing all methods of the given class
   */
  @Nonnull
  CallGraph addClass(@Nonnull CallGraph oldCallGraph, @Nonnull JavaClassType classType);
}
