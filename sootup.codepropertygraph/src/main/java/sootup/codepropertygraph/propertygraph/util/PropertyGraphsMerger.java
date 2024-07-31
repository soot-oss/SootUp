package sootup.codepropertygraph.propertygraph.util;

import sootup.codepropertygraph.propertygraph.AstPropertyGraph;
import sootup.codepropertygraph.propertygraph.PropertyGraph;

public class PropertyGraphsMerger {
  public static PropertyGraph mergeGraphs(PropertyGraph graph1, PropertyGraph graph2) {
    PropertyGraph.Builder mergedGraphBuilder = new AstPropertyGraph.Builder();
    mergedGraphBuilder.setName("merged_graph");

    graph1.getNodes().forEach(mergedGraphBuilder::addNode);
    graph1.getEdges().forEach(mergedGraphBuilder::addEdge);
    graph2.getNodes().forEach(mergedGraphBuilder::addNode);
    graph2.getEdges().forEach(mergedGraphBuilder::addEdge);

    return mergedGraphBuilder.build();
  }
}
