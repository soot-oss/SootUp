package de.upb.swt.soot.callgraph;

import com.google.common.base.Preconditions;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.JavaClassType;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.jgrapht.graph.DefaultDirectedGraph;

/** @author Christian Brüggemann */
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
  @Nonnull private final Map<JavaClassType, Set<Vertex>> typeToVertices;

  GraphBasedCallGraph() {
    graph = new DefaultDirectedGraph<>(null, null, false);
    signatureToVertex = new HashMap<>();
    typeToVertices = new HashMap<>();
  }

  private GraphBasedCallGraph(
      @Nonnull DefaultDirectedGraph<Vertex, Edge> graph,
      @Nonnull Map<MethodSignature, Vertex> signatureToVertex,
      @Nonnull Map<JavaClassType, Set<Vertex>> typeToVertices) {
    this.graph = graph;
    this.signatureToVertex = signatureToVertex;
    this.typeToVertices = typeToVertices;
  }

  @Override
  public void addMethod(@Nonnull MethodSignature calledMethod) {
    Vertex v = new Vertex(calledMethod);
    graph.addVertex(v);
    signatureToVertex.put(calledMethod, v);
  }

  @Override
  public void addCall(
      @Nonnull MethodSignature sourceMethod, @Nonnull MethodSignature targetMethod) {
    graph.addEdge(vertexOf(sourceMethod), vertexOf(targetMethod), new Edge());
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
  public boolean containsMethod(@Nonnull MethodSignature method) {
    return signatureToVertex.containsKey(method);
  }

  @Override
  public boolean containsCall(
      @Nonnull MethodSignature sourceMethod, @Nonnull MethodSignature targetMethod) {
    return graph.containsEdge(vertexOf(sourceMethod), vertexOf(targetMethod));
  }

  @SuppressWarnings("unchecked") // (graph.clone() preserves generic properties)
  @Nonnull
  @Override
  public MutableCallGraph copy() {
    return new GraphBasedCallGraph(
        (DefaultDirectedGraph<Vertex, Edge>) graph.clone(),
        new HashMap<>(signatureToVertex),
        new HashMap<>(typeToVertices));
  }

  @Nonnull
  private Vertex vertexOf(@Nonnull MethodSignature method) {
    Vertex methodVertex = signatureToVertex.get(method);
    Preconditions.checkNotNull(methodVertex, "Node for " + method + " has not been added yet");
    return methodVertex;
  }
}
