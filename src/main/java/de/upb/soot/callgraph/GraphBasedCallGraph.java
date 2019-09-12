package de.upb.soot.callgraph;

import com.google.common.base.Preconditions;
import de.upb.soot.signatures.MethodSignature;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;

public class GraphBasedCallGraph implements CallGraph {

  private static class Vertex {
    @Nonnull final MethodSignature methodSignature;

    private Vertex(@Nonnull MethodSignature methodSignature) {
      this.methodSignature = methodSignature;
    }
  }

  private static class Edge {}

  @Nonnull private final Graph<Vertex, Edge> graph = new DefaultDirectedGraph<>(null, null, false);
  @Nonnull private final Map<MethodSignature, Vertex> signatureToVertex = new HashMap<>();

  @Override
  public void addNode(@Nonnull MethodSignature calledMethod) {
    Vertex v = new Vertex(calledMethod);
    graph.addVertex(v);
    signatureToVertex.put(calledMethod, v);
  }

  @Override
  public void addEdge(@Nonnull MethodSignature method, @Nonnull MethodSignature calledMethod) {
    Vertex methodVertex = signatureToVertex.get(method);
    Preconditions.checkNotNull(methodVertex, "Node for " + method + " has not been added yet");
    Vertex calledMethodVertex = signatureToVertex.get(calledMethod);
    Preconditions.checkNotNull(
        calledMethodVertex, "Node for " + calledMethod + " has not been added yet");

    graph.addEdge(methodVertex, calledMethodVertex, new Edge());
  }

  @Nonnull
  @Override
  public Set<MethodSignature> getMethodSignatures() {
    return signatureToVertex.keySet();
  }

  @Nonnull
  @Override
  public Set<MethodSignature> callsFrom(@Nonnull MethodSignature sourceMethod) {
    Vertex sourceMethodVertex = signatureToVertex.get(sourceMethod);
    Preconditions.checkNotNull(
        sourceMethodVertex, "Node for " + sourceMethod + " has not been added yet");

    return graph.outgoingEdgesOf(sourceMethodVertex).stream()
        .map(graph::getEdgeTarget)
        .map(targetVertex -> targetVertex.methodSignature)
        .collect(Collectors.toSet());
  }

  @Nonnull
  @Override
  public Set<MethodSignature> callsTo(@Nonnull MethodSignature targetMethod) {
    Vertex targetMethodVertex = signatureToVertex.get(targetMethod);
    Preconditions.checkNotNull(
        targetMethodVertex, "Node for " + targetMethod + " has not been added yet");

    return graph.incomingEdgesOf(targetMethodVertex).stream()
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
    Vertex sourceMethodVertex = signatureToVertex.get(sourceMethod);
    Preconditions.checkNotNull(
        sourceMethodVertex, "Node for " + sourceMethod + " has not been added yet");
    Vertex targetMethodVertex = signatureToVertex.get(targetMethod);
    Preconditions.checkNotNull(
        targetMethodVertex, "Node for " + targetMethod + " has not been added yet");

    return graph.containsEdge(sourceMethodVertex, targetMethodVertex);
  }
}
