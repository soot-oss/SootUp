package sootup.java.codepropertygraph.propertygraph;

import java.util.List;

public interface PropertyGraph {
  List<PropertyGraphNode> getNodes();

  List<PropertyGraphEdge> getEdges();

  void addNode(PropertyGraphNode node);

  void addEdge(PropertyGraphEdge edge);
}
