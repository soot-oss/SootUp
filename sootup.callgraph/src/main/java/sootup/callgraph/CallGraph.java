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

import java.util.Set;
import javax.annotation.Nonnull;
import sootup.core.signatures.MethodSignature;

/** The interface of all implemented call graph data structures */
public interface CallGraph {

  /**
   * This method returns method signatures in the call graph. A method signature is a node in the
   * call graph.
   *
   * @return a set containing all method signatures in the call graph.
   */
  @Nonnull
  Set<MethodSignature> getMethodSignatures();

  /**
   * This method returns all method signatures that are called by a given method signature. It
   * returns the targets of outgoing edges of the given node (method signature) in the call graph
   *
   * @param sourceMethod the method signature of the requested node in the call graph
   * @return a set of method signatures that are reached by a direct outgoing edge in the call graph
   */
  @Nonnull
  Set<MethodSignature> callsFrom(@Nonnull MethodSignature sourceMethod);

  /**
   * This method returns all method signatures that call a given method signature. It returns the
   * sources of incoming edges of the given node (method signature) in the call graph
   *
   * @param targetMethod the method signature of the requested node in the call graph
   * @return a set of method signatures that reach the targetMethod by a direct edge in the call
   *     graph
   */
  @Nonnull
  Set<MethodSignature> callsTo(@Nonnull MethodSignature targetMethod);

  /**
   * This method checks if a given method signature is a node in the call graph.
   *
   * @param method the method signature of the requested node
   * @return it returns true if the node described by the method signature is included in the call
   *     graph, otherwise it will return false.
   */
  boolean containsMethod(@Nonnull MethodSignature method);

  /**
   * This method checks if an edge is contained in the call graph. The edge is defined by a source
   * and target method signature which can be nodes in the call graph
   *
   * @param sourceMethod it defines the source node in the call graph
   * @param targetMethod it defines the target node in the call graph
   * @return true if the edge is contained in the call graph, otherwise it will be false.
   */
  boolean containsCall(
      @Nonnull MethodSignature sourceMethod, @Nonnull MethodSignature targetMethod);

  /**
   * This method counts every edge in the call graph.
   *
   * @return it returns the number of all edges in the call graph.
   */
  int callCount();

  /** This method converts the call graph object into dot format and write it to a string file. */
  String exportAsDot();

  /**
   * This method copies a call graph.
   *
   * @return it returns a copied call graph.
   */
  @Nonnull
  MutableCallGraph copy();
}
