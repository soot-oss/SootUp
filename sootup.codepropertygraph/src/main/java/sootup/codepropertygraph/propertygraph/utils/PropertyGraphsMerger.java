package sootup.codepropertygraph.propertygraph.utils;

import sootup.codepropertygraph.propertygraph.AstPropertyGraph;
import sootup.codepropertygraph.propertygraph.PropertyGraph;

public class PropertyGraphsMerger {
  public static PropertyGraph mergeGraphs(PropertyGraph graph1, PropertyGraph graph2) {
    PropertyGraph mergedGraph = new AstPropertyGraph();
    graph1.getEdges().forEach(mergedGraph::addEdge);
    graph2.getEdges().forEach(mergedGraph::addEdge);
    return mergedGraph;
  }
}
