package sootup.codepropertygraph.propertygraph;

import sootup.codepropertygraph.propertygraph.edges.PropertyGraphEdge;
import sootup.codepropertygraph.propertygraph.nodes.PropertyGraphNode;

import java.util.List;

public interface PropertyGraph {
  List<PropertyGraphNode> getNodes();

  List<PropertyGraphEdge> getEdges();

  void addNode(PropertyGraphNode node);

  void addEdge(PropertyGraphEdge edge);

  String toDotGraph(String graphName);
}
