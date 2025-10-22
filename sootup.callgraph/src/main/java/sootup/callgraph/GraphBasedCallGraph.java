package sootup.callgraph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Christian Brüggemann, Markus Schmidt
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

import java.util.*;
import java.util.stream.Collectors;
import org.jgrapht.graph.DirectedPseudograph;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.SootClassMemberSignature;

/** This class implements a mutable call graph as a graph. */
public class GraphBasedCallGraph implements MutableCallGraph {

  /**
   * This internal class is used to describe a vertex in the graph. The vertex is defined by a
   * method signature that describes the method.
   */
  protected static class Vertex {
    @NonNull final MethodSignature methodSignature;

    protected Vertex(@NonNull MethodSignature methodSignature) {
      this.methodSignature = methodSignature;
    }

    @NonNull
    protected MethodSignature getMethodSignature() {
      return methodSignature;
    }
  }

  @NonNull private final DirectedPseudograph<Vertex, Call> graph;
  @NonNull private final Map<MethodSignature, Vertex> signatureToVertex;
  @NonNull private final List<MethodSignature> entryMethods;

  /** The constructor of the graph based call graph. it initializes the call graph object. */
  public GraphBasedCallGraph(List<MethodSignature> entryMethods) {
    this(new DirectedPseudograph<>(null, null, false), new HashMap<>(), entryMethods);
  }

  protected GraphBasedCallGraph(
      @NonNull DirectedPseudograph<Vertex, Call> graph,
      @NonNull Map<MethodSignature, Vertex> signatureToVertex,
      @NonNull List<MethodSignature> entryMethods) {
    this.graph = graph;
    this.signatureToVertex = signatureToVertex;
    this.entryMethods = entryMethods;
  }

  @Override
  public void addMethod(@NonNull MethodSignature calledMethod) {
    Vertex v = new Vertex(calledMethod);
    addMethod(calledMethod, v);
  }

  protected void addMethod(@NonNull MethodSignature calledMethod, Vertex vertex) {
    if (containsMethod(calledMethod)) {
      return;
    }
    graph.addVertex(vertex);
    signatureToVertex.put(calledMethod, vertex);
  }

  @Override
  public void addCall(
      @NonNull MethodSignature sourceMethod,
      @NonNull MethodSignature targetMethod,
      @NonNull InvokableStmt invokableStmt) {
    addCall(new Call(sourceMethod, targetMethod, invokableStmt));
  }

  @Override
  public void addCall(@NonNull Call call) {
    if (!containsMethod(call.sourceMethodSignature())) {
      addMethod(call.sourceMethodSignature());
    }
    Vertex source = vertexOf(call.sourceMethodSignature());
    if (!containsMethod(call.targetMethodSignature())) {
      addMethod(call.targetMethodSignature());
    }
    Vertex target = vertexOf(call.targetMethodSignature());
    graph.addEdge(source, target, call);
  }

  @NonNull
  @Override
  public Set<MethodSignature> getMethodSignatures() {
    return signatureToVertex.keySet();
  }

  @NonNull
  @Override
  public Set<Call> getCalls() {
    return graph.edgeSet();
  }

  @NonNull
  @Override
  public Set<MethodSignature> callTargetsFrom(@NonNull MethodSignature sourceMethod) {
    return callsFrom(sourceMethod).stream()
        .map(Call::targetMethodSignature)
        .collect(Collectors.toSet());
  }

  @NonNull
  @Override
  public Set<MethodSignature> callSourcesTo(@NonNull MethodSignature targetMethod) {
    return callsTo(targetMethod).stream()
        .map(Call::sourceMethodSignature)
        .collect(Collectors.toSet());
  }

  @NonNull
  @Override
  public Set<Call> callsFrom(@NonNull MethodSignature sourceMethod) {
    return graph.outgoingEdgesOf(vertexOf(sourceMethod));
  }

  @NonNull
  @Override
  public Set<Call> sortedCallsFrom(@NonNull MethodSignature sourceMethod) {
    Set<Call> edges = callsFrom(sourceMethod);
    if (edges.isEmpty()) return edges;

    List<Call> sorted = new ArrayList<>(edges);
    sorted.sort(new CallSequenceComparator());
    return new LinkedHashSet<>(sorted);
  }

  @NonNull
  @Override
  public Set<Call> callsTo(@NonNull MethodSignature targetMethod) {
    return graph.incomingEdgesOf(vertexOf(targetMethod));
  }

  @Override
  public boolean containsMethod(@NonNull MethodSignature method) {
    return signatureToVertex.containsKey(method);
  }

  @Override
  public boolean containsCall(
      @NonNull MethodSignature sourceMethod,
      @NonNull MethodSignature targetMethod,
      @NonNull InvokableStmt invokableStmt) {
    if (!containsMethod(sourceMethod) || !containsMethod(targetMethod)) {
      return false;
    }
    return containsCall(new Call(sourceMethod, targetMethod, invokableStmt));
  }

  @Override
  public boolean containsCall(@NonNull Call call) {
    return graph.containsEdge(call);
  }

  @Override
  public int callCount() {
    return graph.edgeSet().size();
  }

  @SuppressWarnings("unchecked") // (graph.clone() preserves generic properties)
  @NonNull
  @Override
  public MutableCallGraph copy() {
    return new GraphBasedCallGraph(
        (DirectedPseudograph<Vertex, Call>) graph.clone(),
        new HashMap<>(signatureToVertex),
        new ArrayList<>(entryMethods));
  }

  @NonNull
  @Override
  public CallGraphDifference diff(@NonNull CallGraph callGraph) {
    return new CallGraphDifference(this, callGraph);
  }

  /**
   * it returns the vertex of the graph that describes the given method signature in the call graph.
   * It will throw an exception if the vertex is not found
   *
   * @param method the method signature searched in the call graph
   * @return the vertex of the requested method signature in optional otherwise an empty optional.
   * @throws IllegalArgumentException if there is no vertex for the requested method signature
   */
  @NonNull
  protected Vertex vertexOf(@NonNull MethodSignature method) {
    Vertex methodVertex = signatureToVertex.get(method);
    if (methodVertex == null) {
      throw new IllegalArgumentException("Vertex of Method signature " + method + " not found");
    }
    return methodVertex;
  }

  /**
   * it returns the edge of the graph that is described by the given source, target, stmt in the
   * call graph. It will throw an exception if the source or target is not contained in the call
   * graph or if the edge could not be found.
   *
   * @param source the signature of the source method
   * @param target the signature of the target method
   * @param invokableStmt the stmt causing the call
   * @return the found edge in an optional or otherwise an empty optional
   */
  protected CallGraph.@NonNull Call edgeOf(
      @NonNull MethodSignature source,
      @NonNull MethodSignature target,
      @NonNull InvokableStmt invokableStmt) {
    Vertex sourceVertexOpt = vertexOf(source);
    Vertex targetVertexOpt = vertexOf(target);
    // returns empty optional if the target vertex or the call is not found

    return graph.getAllEdges(sourceVertexOpt, targetVertexOpt).stream()
        .filter(call -> call.invokableStmt() == invokableStmt)
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Edge of source:"
                        + source
                        + " target:"
                        + target
                        + " stmt:"
                        + invokableStmt
                        + " not found"));
  }

  /**
   * This method exports the call graph in a human-readable string. The String lists all nodes in
   * the call graph. For each node it also lists the outgoing and incoming edges. An outgoing edge
   * is marked by a "To" and an incoming edge by a "From" The nodes, incoming edges, and outgoing
   * edges are sorted in order by the classname, method name, parameter list
   *
   * @return a string containing all nodes and edges of the call graph.
   */
  @Override
  public String toString() {
    StringBuilder stringBuilder =
        new StringBuilder(this.getClass().getSimpleName() + "(" + callCount() + ")");
    Set<MethodSignature> signatures = getMethodSignatures();
    if (signatures.isEmpty()) {
      stringBuilder.append(" is empty");
    } else {
      stringBuilder.append(":\n");
      signatures.stream()
          .sorted(
              Comparator.comparing((MethodSignature o) -> o.getDeclClassType().toString())
                  .thenComparing(SootClassMemberSignature::getName)
                  .thenComparing(o -> o.getParameterTypes().toString()))
          .forEach(
              method -> {
                stringBuilder.append(method).append(":\n");
                callsFrom(method).stream()
                    .sorted(
                        Comparator.comparing(
                                (Call call) ->
                                    call.targetMethodSignature().getDeclClassType().toString())
                            .thenComparing(call -> call.targetMethodSignature().getName())
                            .thenComparing(
                                call ->
                                    call.targetMethodSignature().getParameterTypes().toString()))
                    .forEach(
                        c ->
                            stringBuilder
                                .append("\tto ")
                                .append(printCalledMethods(c))
                                .append("\n"));
                callsTo(method).stream()
                    .sorted(
                        Comparator.comparing(
                                (Call call) ->
                                    call.sourceMethodSignature().getDeclClassType().toString())
                            .thenComparing(call -> call.sourceMethodSignature().getName())
                            .thenComparing(
                                call ->
                                    call.sourceMethodSignature().getParameterTypes().toString()))
                    .forEach(
                        call ->
                            stringBuilder
                                .append("\tfrom ")
                                .append(printCallingMethods(call))
                                .append("\n"));
                stringBuilder.append("\n");
              });
    }
    return stringBuilder.toString();
  }

  /**
   * This returns the string that is used in the toString Method to define the methods that call a
   * specific method
   *
   * @param call The data of the call
   * @return The returned String will be used in the toString method to define the methods that call
   *     a specific method
   */
  protected String printCallingMethods(CallGraph.Call call) {
    return call.sourceMethodSignature().toString();
  }

  /**
   * This returns the string that is used in the toString Method to define the called methods
   *
   * @param call The data of the call
   * @return The returned String will be used in the toString method to define the called methods
   */
  protected String printCalledMethods(CallGraph.Call call) {
    return call.targetMethodSignature().toString();
  }

  @Override
  @NonNull
  public List<MethodSignature> getEntryMethods() {
    return entryMethods;
  }
}
