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

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.signatures.MethodSignature;

/** The interface of all implemented call graph data structures */
public interface CallGraph {

  record Call(
      @NonNull MethodSignature sourceMethodSignature,
      @NonNull MethodSignature targetMethodSignature,
      @NonNull InvokableStmt invokableStmt) {

    /**
     * The line number of the stmt causing the call
     *
     * @return the line number of the stmt. If the position is unknown, it will return -1
     */
    public int getLineNumber() {
      return invokableStmt.getPositionInfo().getStmtPosition().getFirstLine();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      Call call = (Call) o;
      return sourceMethodSignature.equals(call.sourceMethodSignature)
          && targetMethodSignature.equals(call.targetMethodSignature)
          && invokableStmt.equals(call.invokableStmt);
    }

    @Override
    @NonNull
    public String toString() {
      return "Call:"
          + sourceMethodSignature
          + " -> "
          + targetMethodSignature
          + " via "
          + invokableStmt
          + ";";
    }
  }

  /**
   * This method returns method signatures in the call graph. A method signature is a node in the
   * call graph.
   *
   * @return a set containing all method signatures in the call graph.
   */
  @NonNull Set<MethodSignature> getMethodSignatures();

  /**
   * This method returns all calls in the call graph. Calls are a edges in the call graph. They
   * contain the source, target and calling stmt.
   *
   * @return a set containing all calls in the call graph.
   */
  @NonNull Set<Call> getCalls();

  /**
   * This method returns all method signatures that are called by a given method signature. It
   * returns the targets of outgoing edges of the given node (method signature) in the call graph
   *
   * @param sourceMethod the method signature of the requested node in the call graph
   * @return a set of method signatures that are reached by a direct outgoing edge in the call graph
   */
  @NonNull Set<MethodSignature> callTargetsFrom(@NonNull MethodSignature sourceMethod);

  /**
   * This method returns all method signatures that call a given method signature. It returns the
   * sources of incoming edges of the given node (method signature) in the call graph
   *
   * @param targetMethod the method signature of the requested node in the call graph
   * @return a set of method signatures that reach the targetMethod by a direct edge in the call
   *     graph
   */
  @NonNull Set<MethodSignature> callSourcesTo(@NonNull MethodSignature targetMethod);

  /**
   * This method returns all method signatures that are called by a given method signature. It
   * returns the targets of outgoing edges of the given node (method signature) in the call graph
   *
   * @param sourceMethod the method signature of the requested node in the call graph
   * @return a set of method signatures that are reached by a direct outgoing edge in the call graph
   */
  @NonNull Set<Call> callsFrom(@NonNull MethodSignature sourceMethod);

  /**
   * This method returns all method signatures that are called by a given method signature. It
   * returns the targets of outgoing edges of the given node (method signature) in the call graph
   * sorted by the call sequence.
   *
   * @param sourceMethod the method signature of the requested node in the call graph
   * @return a sorted set of method signatures that are reached by a direct outgoing edge in the
   *     call graph
   */
  @NonNull Set<Call> sortedCallsFrom(@NonNull MethodSignature sourceMethod);

  /**
   * This method returns all method signatures that call a given method signature. It returns the
   * sources of incoming edges of the given node (method signature) in the call graph
   *
   * @param targetMethod the method signature of the requested node in the call graph
   * @return a set of method signatures that reach the targetMethod by a direct edge in the call
   *     graph
   */
  @NonNull Set<Call> callsTo(@NonNull MethodSignature targetMethod);

  /**
   * This method checks if a given method signature is a node in the call graph.
   *
   * @param method the method signature of the requested node
   * @return it returns true if the node described by the method signature is included in the call
   *     graph, otherwise it will return false.
   */
  boolean containsMethod(@NonNull MethodSignature method);

  /**
   * This method checks if an edge is contained in the call graph. The edge is defined by a source
   * and target method signature which can be nodes in the call graph
   *
   * @param sourceMethod it defines the source node in the call graph
   * @param targetMethod it defines the target node in the call graph
   * @param invokableStmt it defines the invoke stmt of the call
   * @return true if the edge is contained in the call graph, otherwise it will be false.
   */
  boolean containsCall(
      @NonNull MethodSignature sourceMethod,
      @NonNull MethodSignature targetMethod,
      InvokableStmt invokableStmt);

  /**
   * This method checks if an edge is contained in the call graph. The edge is defined by a source
   * and target method signature which can be nodes in the call graph
   *
   * @param call it defines the requested call in the call graph
   * @return true if the edge is contained in the call graph, otherwise it will be false.
   */
  boolean containsCall(@NonNull Call call);

  /**
   * This method counts every edge in the call graph.
   *
   * @return it returns the number of all edges in the call graph.
   */
  int callCount();

  /**
   * exports a call of the call graph to an edge in a dot file
   *
   * @param call the data of the call
   * @return an edge defining the call in the dot file
   */
  default StringBuilder toDotEdge(Call call) {
    return new StringBuilder("\"")
        .append(call.sourceMethodSignature())
        .append("\"->\"")
        .append(call.targetMethodSignature())
        .append("\"[label=\"")
        .append(call.getLineNumber())
        .append("\"]");
  }

  /**
   * This method converts the call graph object into dot format and write it to a string file. The
   * first entry contains the graph info and the last entry closes the graph.
   *
   * @return a stream containing all edges as Strings in the dot format
   */
  default Stream<String> exportAsDot() {
    return Stream.concat(
        Stream.concat(
            Stream.of("strict digraph ObjectGraph {"),
            getCalls().stream().map(call -> toDotEdge(call).toString())),
        Stream.of("}"));
  }

  /**
   * This method converts the call graph object into dot format and writes it to a string file. The
   * calls are sorted by the given Comparator for Call Objects. The first entry opens the graph and
   * the last entry closes the graph.
   *
   * @param callComparator the comparator responsible for sorting the calls
   * @return a stream containing all edges as Strings in the dot format sorted by the given
   *     Comparator
   */
  default Stream<String> exportAsDot(Comparator<Call> callComparator) {
    return Stream.concat(
        Stream.concat(
            Stream.of("strict digraph ObjectGraph {"),
            getCalls().stream().sorted(callComparator).map(call -> toDotEdge(call).toString())),
        Stream.of("}"));
  }

  /**
   * This method copies a call graph.
   *
   * @return it returns a copied call graph.
   */
  @NonNull MutableCallGraph copy();

  /**
   * This method returns all entry methods of the call graph
   *
   * @return a list of method signatures of entry points of the call graph
   */
  List<MethodSignature> getEntryMethods();

  /**
   * This method compares the difference between the current call graph and call graph passed into
   * the argument.
   */
  @NonNull CallGraphDifference diff(@NonNull CallGraph callGraph);
}
