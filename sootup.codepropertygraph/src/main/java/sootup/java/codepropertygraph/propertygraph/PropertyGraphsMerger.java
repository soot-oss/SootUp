package sootup.java.codepropertygraph.propertygraph;

public class PropertyGraphsMerger {
  public static PropertyGraph mergeGraphs(PropertyGraph graph1, PropertyGraph graph2) {
    PropertyGraph mergedGraph = new PropertyGraph();
    graph1.getEdges().forEach(mergedGraph::addEdge);
    graph2.getEdges().forEach(mergedGraph::addEdge);
    return mergedGraph;
  }
}
