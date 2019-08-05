package de.upb.soot.callgraph;

import de.upb.soot.core.SootMethod;
import java.util.*;

public class AdjacencyList implements CallGraph {

  Map<SootMethod, Set<SootMethod>> graph = new HashMap<>();

  @Override
  public void addNode(SootMethod calledMethod) {
    graph.put(calledMethod, new HashSet<>());
  }

  @Override
  public void addEdge(SootMethod method, SootMethod calledMethod) {
    Set<SootMethod> transitions = graph.get(method);
    if (transitions == null) {
      // fix missing Node
      addNode(method);
    }
    transitions.add(calledMethod);
  }

  @Override
  public Set<SootMethod> getNodes() {
    return graph.keySet();
  }

  @Override
  public Set<SootMethod> getTransitions(SootMethod method) {
    return graph.get(method);
  }

  @Override
  public boolean hasNode(SootMethod sootMethod) {
    return graph.containsKey(sootMethod);
  }

  @Override
  public boolean hasEdge(SootMethod method, SootMethod calledMethod) {
    Set<SootMethod> transitions = graph.get(method);
    return transitions != null && transitions.contains(calledMethod);
  }
}
