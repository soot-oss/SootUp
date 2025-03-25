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
import java.util.Set;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.signatures.MethodSignature;

/** The interface of all implemented call graph data structures */
public interface CallGraph {

  class Call {
    @NonNull private final MethodSignature sourceMethodSignature;
    @NonNull private final MethodSignature targetMethodSignature;
    @NonNull private final InvokableStmt invokableStmt;

    public Call(
        @NonNull MethodSignature sourceMethodSignature,
        @NonNull MethodSignature targetMethodSignature,
        @NonNull InvokableStmt invokableStmt) {
      this.sourceMethodSignature = sourceMethodSignature;
      this.invokableStmt = invokableStmt;
      this.targetMethodSignature = targetMethodSignature;
    }

    @NonNull
    public MethodSignature getSourceMethodSignature() {
      return sourceMethodSignature;
    }

    @NonNull
    public MethodSignature getTargetMethodSignature() {
      return targetMethodSignature;
    }

    @NonNull
    public InvokableStmt getInvokableStmt() {
      return invokableStmt;
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
    public int hashCode() {
      int result = sourceMethodSignature.hashCode();
      result = 31 * result + targetMethodSignature.hashCode();
      result = 31 * result + invokableStmt.hashCode();
      return result;
    }

    @Override
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

  /** This method converts the call graph object into dot format and write it to a string file. */
  String exportAsDot();

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
