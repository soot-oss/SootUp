package de.upb.soot.callgraph;

import com.google.common.base.Preconditions;
import de.upb.soot.signatures.MethodSignature;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.jgrapht.graph.DefaultDirectedGraph;

public final class GraphBasedCallGraph implements MutableCallGraph {

  private static class Vertex {
    @Nonnull final MethodSignature methodSignature;

    private Vertex(@Nonnull MethodSignature methodSignature) {
      this.methodSignature = methodSignature;
    }
  }

  private static class Edge {}

  @Nonnull private final DefaultDirectedGraph<Vertex, Edge> graph;
  @Nonnull private final Map<MethodSignature, Vertex> signatureToVertex;

  public GraphBasedCallGraph() {
    graph = new DefaultDirectedGraph<>(null, null, false);
    signatureToVertex = new HashMap<>();
  }

  private GraphBasedCallGraph(
      @Nonnull DefaultDirectedGraph<Vertex, Edge> graph,
      @Nonnull Map<MethodSignature, Vertex> signatureToVertex) {
    this.graph = graph;
    this.signatureToVertex = signatureToVertex;
  }

  @Override
  public void addNode(@Nonnull MethodSignature calledMethod) {
    Vertex v = new Vertex(calledMethod);
    graph.addVertex(v);
    signatureToVertex.put(calledMethod, v);
  }

  @Override
  public void addEdge(@Nonnull MethodSignature method, @Nonnull MethodSignature calledMethod) {
    graph.addEdge(vertexOf(method), vertexOf(calledMethod), new Edge());
  }

  @Nonnull
  @Override
  public Set<MethodSignature> getMethodSignatures() {
    return signatureToVertex.keySet();
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
  public boolean hasNode(@Nonnull MethodSignature method) {
    return signatureToVertex.containsKey(method);
  }

  @Override
  public boolean hasEdge(
      @Nonnull MethodSignature sourceMethod, @Nonnull MethodSignature targetMethod) {
    return graph.containsEdge(vertexOf(sourceMethod), vertexOf(targetMethod));
  }

  @Nonnull
  @Override
  public MutableCallGraph copy() {
    //noinspection unchecked (graph.clone() preserves generic properties)
    return new GraphBasedCallGraph(
        (DefaultDirectedGraph<Vertex, Edge>) graph.clone(), new HashMap<>(signatureToVertex));
  }

  @Nonnull
  private Vertex vertexOf(@Nonnull MethodSignature method) {
    Vertex methodVertex = signatureToVertex.get(method);
    Preconditions.checkNotNull(methodVertex, "Node for " + method + " has not been added yet");
    return methodVertex;
  }
}
