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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.jgrapht.graph.DirectedPseudograph;
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
    @Nonnull final MethodSignature methodSignature;

    protected Vertex(@Nonnull MethodSignature methodSignature) {
      this.methodSignature = methodSignature;
    }

    @Nonnull
    protected MethodSignature getMethodSignature() {
      return methodSignature;
    }
  }

  @Nonnull private final DirectedPseudograph<Vertex, Call> graph;
  @Nonnull private final Map<MethodSignature, Vertex> signatureToVertex;
  @Nonnull private final List<MethodSignature> entryMethods;

  /** The constructor of the graph based call graph. it initializes the call graph object. */
  public GraphBasedCallGraph(List<MethodSignature> entryMethods) {
    this(new DirectedPseudograph<>(null, null, false), new HashMap<>(), entryMethods);
  }

  protected GraphBasedCallGraph(
      @Nonnull DirectedPseudograph<Vertex, Call> graph,
      @Nonnull Map<MethodSignature, Vertex> signatureToVertex,
      @Nonnull List<MethodSignature> entryMethods) {
    this.graph = graph;
    this.signatureToVertex = signatureToVertex;
    this.entryMethods = entryMethods;
  }

  @Override
  public void addMethod(@Nonnull MethodSignature calledMethod) {
    Vertex v = new Vertex(calledMethod);
    addMethod(calledMethod, v);
  }

  protected void addMethod(@Nonnull MethodSignature calledMethod, Vertex vertex) {
    if (containsMethod(calledMethod)) {
      return;
    }
    graph.addVertex(vertex);
    signatureToVertex.put(calledMethod, vertex);
  }

  @Override
  public void addCall(
      @Nonnull MethodSignature sourceMethod,
      @Nonnull MethodSignature targetMethod,
      @Nonnull InvokableStmt invokableStmt) {
    addCall(new Call(sourceMethod, targetMethod, invokableStmt));
  }

  @Override
  public void addCall(@Nonnull Call call) {
    if (!containsMethod(call.getSourceMethodSignature())) {
      addMethod(call.getSourceMethodSignature());
    }
    Vertex source = vertexOf(call.getSourceMethodSignature());
    if (!containsMethod(call.getTargetMethodSignature())) {
      addMethod(call.getTargetMethodSignature());
    }
    Vertex target = vertexOf(call.getTargetMethodSignature());
    graph.addEdge(source, target, call);
  }

  @Nonnull
  @Override
  public Set<MethodSignature> getMethodSignatures() {
    return signatureToVertex.keySet();
  }

  @Nonnull
  @Override
  public Set<MethodSignature> callTargetsFrom(@Nonnull MethodSignature sourceMethod) {
    return callsFrom(sourceMethod).stream()
        .map(Call::getTargetMethodSignature)
        .collect(Collectors.toSet());
  }

  @Nonnull
  @Override
  public Set<MethodSignature> callSourcesTo(@Nonnull MethodSignature targetMethod) {
    return callsTo(targetMethod).stream()
        .map(Call::getSourceMethodSignature)
        .collect(Collectors.toSet());
  }

  @Nonnull
  @Override
  public Set<Call> callsFrom(@Nonnull MethodSignature sourceMethod) {
    return graph.outgoingEdgesOf(vertexOf(sourceMethod));
  }

  @Nonnull
  @Override
  public Set<Call> callsTo(@Nonnull MethodSignature targetMethod) {
    return graph.incomingEdgesOf(vertexOf(targetMethod));
  }

  @Override
  public boolean containsMethod(@Nonnull MethodSignature method) {
    return signatureToVertex.containsKey(method);
  }

  @Override
  public boolean containsCall(
      @Nonnull MethodSignature sourceMethod,
      @Nonnull MethodSignature targetMethod,
      @Nonnull InvokableStmt invokableStmt) {
    if (!containsMethod(sourceMethod) || !containsMethod(targetMethod)) {
      return false;
    }
    return containsCall(new Call(sourceMethod, targetMethod, invokableStmt));
  }

  @Override
  public boolean containsCall(@Nonnull Call call) {
    return graph.containsEdge(call);
  }

  @Override
  public int callCount() {
    return graph.edgeSet().size();
  }

  @Override
  public String exportAsDot() {
    StringBuilder dotFormatBuilder = new StringBuilder();
    // The edgeSet is first sorted with the sourceMethod first and then targetMethod. It is sorted
    // by className, then the method name
    // and then the parameters.
    graph.edgeSet().stream()
        .sorted(
            Comparator.comparing(
                    (Call call) -> {
                      Vertex edgeSource = graph.getEdgeSource(call);
                      return edgeSource.methodSignature.getDeclClassType().getFullyQualifiedName();
                    })
                .thenComparing(
                    (Call call) -> {
                      Vertex edgeSource = graph.getEdgeSource(call);
                      return edgeSource.methodSignature.getName();
                    })
                .thenComparing(
                    (Call call) -> {
                      Vertex edgeSource = graph.getEdgeSource(call);
                      return edgeSource.methodSignature.getParameterTypes().toString();
                    })
                .thenComparing(
                    (Call call) -> {
                      Vertex edgeTarget = graph.getEdgeTarget(call);
                      return edgeTarget.methodSignature.getDeclClassType().getClassName();
                    })
                .thenComparing(
                    (Call call) -> {
                      Vertex edgeTarget = graph.getEdgeTarget(call);
                      return edgeTarget.methodSignature.getName();
                    })
                .thenComparing(
                    (Call call) -> {
                      Vertex edgeTarget = graph.getEdgeTarget(call);
                      return edgeTarget.methodSignature.getParameterTypes().toString();
                    }))
        .forEach(edge -> dotFormatBuilder.append("\t").append(toDotEdge(edge)).append("\n"));

    return "strict digraph ObjectGraph {\n" + dotFormatBuilder + "}";
  }

  /**
   * exports a call of the call graph to an edge in a dot file
   *
   * @param call the data of the call
   * @return an edge defining the call in the dot file
   */
  protected String toDotEdge(CallGraph.Call call) {
    Vertex sourceVertex = graph.getEdgeSource(call);
    Vertex targetVertex = graph.getEdgeTarget(call);
    return "\"" + sourceVertex.methodSignature + "\" -> \"" + targetVertex.methodSignature + "\";";
  }

  @SuppressWarnings("unchecked") // (graph.clone() preserves generic properties)
  @Nonnull
  @Override
  public MutableCallGraph copy() {
    return new GraphBasedCallGraph(
        (DirectedPseudograph<Vertex, Call>) graph.clone(),
        new HashMap<>(signatureToVertex),
        new ArrayList<>(entryMethods));
  }

  @Nonnull
  @Override
  public CallGraphDifference diff(@Nonnull CallGraph callGraph) {
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
  @Nonnull
  protected Vertex vertexOf(@Nonnull MethodSignature method) {
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
  @Nonnull
  protected CallGraph.Call edgeOf(
      @Nonnull MethodSignature source,
      @Nonnull MethodSignature target,
      @Nonnull InvokableStmt invokableStmt) {
    Vertex sourceVertexOpt = vertexOf(source);
    Vertex targetVertexOpt = vertexOf(target);
    // returns empty optional if the target vertex or the call is not found

    return graph.getAllEdges(sourceVertexOpt, targetVertexOpt).stream()
        .filter(call -> call.getInvokableStmt() == invokableStmt)
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
                                    call.getTargetMethodSignature().getDeclClassType().toString())
                            .thenComparing(call -> call.getTargetMethodSignature().getName())
                            .thenComparing(
                                call ->
                                    call.getTargetMethodSignature().getParameterTypes().toString()))
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
                                    call.getSourceMethodSignature().getDeclClassType().toString())
                            .thenComparing(call -> call.getSourceMethodSignature().getName())
                            .thenComparing(
                                call ->
                                    call.getSourceMethodSignature().getParameterTypes().toString()))
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
    return call.getSourceMethodSignature().toString();
  }

  /**
   * This returns the string that is used in the toString Method to define the called methods
   *
   * @param call The data of the call
   * @return The returned String will be used in the toString method to define the called methods
   */
  protected String printCalledMethods(CallGraph.Call call) {
    return call.getTargetMethodSignature().toString();
  }

  @Override
  @Nonnull
  public List<MethodSignature> getEntryMethods() {
    return entryMethods;
  }
}
