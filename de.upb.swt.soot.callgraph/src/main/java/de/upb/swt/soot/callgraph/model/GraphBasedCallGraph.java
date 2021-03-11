package de.upb.swt.soot.callgraph.model;

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
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.graph.DefaultDirectedGraph;

public final class GraphBasedCallGraph implements MutableCallGraph {

  @Nonnull private final DefaultDirectedGraph<CallGraphVertex, CallGraphEdge> graph;
  @Nonnull private final Map<MethodSignature, CallGraphVertex> signatureToVertex;
  // TODO: [ms] typeToVertices is not used in a useful way, yet?
  @Nonnull private final Map<JavaClassType, Set<CallGraphVertex>> typeToVertices;

  public GraphBasedCallGraph() {
    graph = new DefaultDirectedGraph<>(null, null, false);
    signatureToVertex = new HashMap<>();
    typeToVertices = new HashMap<>();
  }

  private GraphBasedCallGraph(
      @Nonnull DefaultDirectedGraph<CallGraphVertex, CallGraphEdge> graph,
      @Nonnull Map<MethodSignature, CallGraphVertex> signatureToVertex,
      @Nonnull Map<JavaClassType, Set<CallGraphVertex>> typeToVertices) {
    this.graph = graph;
    this.signatureToVertex = signatureToVertex;
    this.typeToVertices = typeToVertices;
  }

  @Override
  public void addMethod(@Nonnull MethodSignature calledMethod) {
    CallGraphVertex v = new CallGraphVertex(calledMethod);
    graph.addVertex(v);
    signatureToVertex.put(calledMethod, v);
  }

  @Override
  public void addCall(
      @Nonnull MethodSignature sourceMethod, @Nonnull MethodSignature targetMethod) {
    // TODO: would it be better if targetMethod is always CalleeMethodSignature?
    if(targetMethod instanceof CalleeMethodSignature){
      graph.addEdge(vertexOf(sourceMethod), vertexOf(targetMethod), new CallGraphEdge(((CalleeMethodSignature)targetMethod).getEdgeType()));
    } else{
      graph.addEdge(vertexOf(sourceMethod), vertexOf(targetMethod), new CallGraphEdge(null));
    }

  }

  @Nonnull
  @Override
  public Set<MethodSignature> getMethodSignatures() {
    return signatureToVertex.keySet();
  }

  @Nonnull
  @Override
  public Set<Pair<MethodSignature, CalleeMethodSignature>> getEdges() {
    Set<Pair<MethodSignature, CalleeMethodSignature>> edges = new HashSet<>();
    for (CallGraphEdge edge : graph.edgeSet()) {
      CallGraphVertex edgeSource = graph.getEdgeSource(edge);
      CallGraphVertex edgeTarget = graph.getEdgeTarget(edge);
      Pair<MethodSignature, CalleeMethodSignature> pair =
          new ImmutablePair<>(edgeSource.methodSignature, new CalleeMethodSignature(edgeTarget.methodSignature, edge.getEdgeType()));
      edges.add(pair);
    }
    return edges;
  }

  @Nonnull
  @Override
  public Set<MethodSignature> callsFrom(@Nonnull MethodSignature sourceMethod) {
    return graph.outgoingEdgesOf(vertexOf(sourceMethod)).stream()
        .map(graph::getEdgeTarget)
        .map(targetVertex -> targetVertex.methodSignature)
        .collect(Collectors.toSet());
  }

  @Nonnull
  @Override
  public Set<MethodSignature> callsTo(@Nonnull MethodSignature targetMethod) {
    return graph.incomingEdgesOf(vertexOf(targetMethod)).stream()
        .map(graph::getEdgeSource)
        .map(targetVertex -> targetVertex.methodSignature)
        .collect(Collectors.toSet());
  }

  @Override
  public boolean containsMethod(@Nonnull MethodSignature method) {
    return signatureToVertex.containsKey(method);
  }

  @Override
  public boolean containsCall(
      @Nonnull MethodSignature sourceMethod, @Nonnull MethodSignature targetMethod) {
    return graph.containsEdge(vertexOf(sourceMethod), vertexOf(targetMethod));
  }

  @Override
  public int callCount() {
    return graph.edgeSet().size();
  }

  @SuppressWarnings("unchecked") // (graph.clone() preserves generic properties)
  @Nonnull
  @Override
  public MutableCallGraph copy() {
    return new GraphBasedCallGraph(
        (DefaultDirectedGraph<CallGraphVertex, CallGraphEdge>) graph.clone(),
        new HashMap<>(signatureToVertex),
        new HashMap<>(typeToVertices));
  }

  @Nonnull
  private CallGraphVertex vertexOf(@Nonnull MethodSignature method) {
    CallGraphVertex methodVertex = signatureToVertex.get(method);
    Preconditions.checkNotNull(methodVertex, "Node for " + method + " has not been added yet");
    return methodVertex;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("GraphBasedCallGraph(" + callCount() + ")");
    if (signatureToVertex.keySet().isEmpty()) {
      sb.append(" is empty");
    } else {
      sb.append(":\n");
      for (MethodSignature method : signatureToVertex.keySet()) {
        sb.append(method.toString()).append(":\n");
        callsFrom(method)
            .forEach(
                (m) -> {
                  sb.append("\tto ").append(m).append("\n");
                });
        callsTo(method)
            .forEach(
                (m) -> {
                  sb.append("\tfrom   ").append(m).append("\n");
                });
        sb.append("\n");
      }
    }
    return sb.toString();
  }
}
