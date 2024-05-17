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

import com.google.common.base.Preconditions;
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
    this(new DirectedPseudograph<>(null, null, false),new HashMap<>(), entryMethods);
  }

  public GraphBasedCallGraph(
      @Nonnull DirectedPseudograph<Vertex, Call> graph,
      @Nonnull Map<MethodSignature, Vertex> signatureToVertex, List<MethodSignature> entryMethods) {
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
    if (containsMethod(calledMethod)){
      return;
    }
    graph.addVertex(vertex);
    signatureToVertex.put(calledMethod, vertex);
  }

  @Override
  public void addCall(
      @Nonnull MethodSignature sourceMethod, @Nonnull MethodSignature targetMethod, @Nonnull InvokableStmt invokableStmt) {
    addCall(sourceMethod, targetMethod, new Call(sourceMethod,targetMethod,invokableStmt));
  }

  protected void addCall(
      @Nonnull MethodSignature sourceMethod, @Nonnull MethodSignature targetMethod, Call call) {
    if (!call.getSourceMethodSignature().equals(sourceMethod) || !call.getTargetMethodSignature().equals(targetMethod) || containsCall(call)){
      return;
    }
    Vertex source=vertexOf(sourceMethod);
    Vertex target=vertexOf(targetMethod);
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
    return graph.outgoingEdgesOf(vertexOf(sourceMethod)).stream()
        .map(graph::getEdgeTarget)
        .map(targetVertex -> targetVertex.methodSignature)
        .collect(Collectors.toSet());
  }

  @Nonnull
  @Override
  public Set<MethodSignature> callSourcesTo(@Nonnull MethodSignature targetMethod) {
    return graph.incomingEdgesOf(vertexOf(targetMethod)).stream()
        .map(graph::getEdgeSource)
        .map(targetVertex -> targetVertex.methodSignature)
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
      @Nonnull MethodSignature sourceMethod, @Nonnull MethodSignature targetMethod, @Nonnull InvokableStmt invokableStmt) {
    if (!containsMethod(sourceMethod) || !containsMethod(targetMethod)) {
      return false;
    }
    return containsCall(new Call(sourceMethod,targetMethod,invokableStmt));
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
        .forEach(
            edge -> {
              Vertex sourceVertex = graph.getEdgeSource(edge);
              Vertex targetVertex = graph.getEdgeTarget(edge);
              dotFormatBuilder
                  .append("\t")
                  .append("\"" + sourceVertex.methodSignature + "\"")
                  .append(" -> ")
                  .append("\"" + targetVertex.methodSignature + "\"")
                  .append(";\n");
            });

    return "strict digraph ObjectGraph {\n" + dotFormatBuilder + "}";
  }

  @SuppressWarnings("unchecked") // (graph.clone() preserves generic properties)
  @Nonnull
  @Override
  public MutableCallGraph copy() {
    return new GraphBasedCallGraph(
        (DirectedPseudograph<Vertex, Call>) graph.clone(), new HashMap<>(signatureToVertex),new ArrayList<>(
        entryMethods));
  }

  /**
   * it returns the vertex of the graph that describes the given method signature in the call graph.
   *
   * @param method the method signature searched in the call graph
   * @return the vertex of the requested method signature.
   */
  @Nonnull
  protected Vertex vertexOf(@Nonnull MethodSignature method) {
    Vertex methodVertex = signatureToVertex.get(method);
    Preconditions.checkNotNull(methodVertex, "Node for " + method + " has not been added yet");
    return methodVertex;
  }

  @Nonnull
  protected DirectedPseudograph<Vertex, Call> getGraph() {
    return graph;
  }

  @Nonnull
  protected Map<MethodSignature, Vertex> getSignatureToVertex() {
    return signatureToVertex;
  }

  @Nonnull
  protected MethodSignature vertex2MethodSignature(@Nonnull Vertex vertex) {
    return vertex.getMethodSignature();
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
    StringBuilder stringBuilder = new StringBuilder("GraphBasedCallGraph(" + callCount() + ")");
    Set<MethodSignature> signatures = signatureToVertex.keySet();
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
                callTargetsFrom(method).stream()
                    .sorted(
                        Comparator.comparing((MethodSignature o) -> o.getDeclClassType().toString())
                            .thenComparing(SootClassMemberSignature::getName)
                            .thenComparing(o -> o.getParameterTypes().toString()))
                    .forEach(m -> stringBuilder.append("\tto ").append(m).append("\n"));
                callSourcesTo(method).stream()
                    .sorted(
                        Comparator.comparing((MethodSignature o) -> o.getDeclClassType().toString())
                            .thenComparing(SootClassMemberSignature::getName)
                            .thenComparing(o -> o.getParameterTypes().toString()))
                    .forEach(m -> stringBuilder.append("\tfrom ").append(m).append("\n"));
                stringBuilder.append("\n");
              });
    }
    return stringBuilder.toString();
  }

  @Nonnull
  public List<MethodSignature> getEntryMethods() {
    return entryMethods;
  }
}
