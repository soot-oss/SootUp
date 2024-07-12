package sootup.callgraph;

import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import sootup.core.signatures.MethodSignature;

public class CallGraphDifference {

  private final CallGraph baseCallGraph;
  private final CallGraph otherCallGraph;

  private final List<Pair<MethodSignature, MethodSignature>> baseCallGraphEdges;
  private final List<Pair<MethodSignature, MethodSignature>> otherCallGraphEdges;

  public CallGraphDifference(CallGraph baseCallGraph, CallGraph otherCallGraph) {
    this.baseCallGraph = baseCallGraph;
    this.otherCallGraph = otherCallGraph;
    this.baseCallGraphEdges = constructEdges(baseCallGraph);
    this.otherCallGraphEdges = constructEdges(otherCallGraph);
  }

  private List<Pair<MethodSignature, MethodSignature>> constructEdges(CallGraph cg) {
    List<Pair<MethodSignature, MethodSignature>> cgEdges = new ArrayList<>();
    for (MethodSignature srcNode : cg.getMethodSignatures()) {
      Set<MethodSignature> outNodes = cg.callsFrom(srcNode);
      for (MethodSignature targetNode : outNodes) {
        cgEdges.add(new MutablePair<>(srcNode, targetNode));
      }
    }
    return cgEdges;
  }

  public CallGraph getBaseCallGraph() {
    return baseCallGraph;
  }

  public CallGraph getOtherCallGraph() {
    return otherCallGraph;
  }

  public List<Pair<MethodSignature, MethodSignature>> getBaseCallGraphEdges() {
    return baseCallGraphEdges;
  }

  public List<Pair<MethodSignature, MethodSignature>> getOtherCallGraphEdges() {
    return otherCallGraphEdges;
  }

  /*
      In the intersectedCalls() function, we iterate over each edge in both call graphs and
      return the intersection of the edges.
  */
  public List<Pair<MethodSignature, MethodSignature>> intersectedCalls() {
    return baseCallGraphEdges.stream()
        .filter(otherCallGraphEdges::contains)
        .collect(Collectors.toList());
  }

  /*
      In the intersectedMethods() function, we iterate over each node in both call graphs and
      return the intersection of the nodes.
  */
  public List<MethodSignature> intersectedMethods() {
    return baseCallGraph.getMethodSignatures().stream()
        .filter(otherCallGraph.getMethodSignatures()::contains)
        .collect(Collectors.toList());
  }

  /*
      In the uniqueBaseGraphCalls() function, we iterate over each edges in base call graph and
      return the unique edges present in the base call graph.
  */
  public List<Pair<MethodSignature, MethodSignature>> uniqueBaseGraphCalls() {
    return baseCallGraphEdges.stream()
        .filter(edge -> !otherCallGraphEdges.contains(edge))
        .collect(Collectors.toList());
  }

  /*
      In the uniqueBaseGraphMethods() function, we iterate over each node in base call graph and
      return the unique nodes present in the base call graph.
  */
  public List<MethodSignature> uniqueBaseGraphMethods() {
    return baseCallGraph.getMethodSignatures().stream()
        .filter(node -> !otherCallGraph.getMethodSignatures().contains(node))
        .collect(Collectors.toList());
  }

  /*
      In the uniqueOtherGraphCalls() function, we iterate over each edges in other call graph and
      return the unique edges present in the other call graph.
  */
  public List<Pair<MethodSignature, MethodSignature>> uniqueOtherGraphCalls() {
    return otherCallGraphEdges.stream()
        .filter(edge -> !baseCallGraphEdges.contains(edge))
        .collect(Collectors.toList());
  }

  /*
      In the uniqueOtherGraphMethods() function, we iterate over each node in other call graph and
      return the unique nodes present in the other call graph.
  */
  public List<MethodSignature> uniqueOtherGraphMethods() {
    return otherCallGraph.getMethodSignatures().stream()
        .filter(node -> !baseCallGraph.getMethodSignatures().contains(node))
        .collect(Collectors.toList());
  }
}
