package sootup.codepropertygraph.propertygraph;

import java.util.List;
import sootup.codepropertygraph.propertygraph.edges.PropertyGraphEdge;
import sootup.codepropertygraph.propertygraph.nodes.PropertyGraphNode;

public interface PropertyGraph {
  List<PropertyGraphNode> getNodes();

  List<PropertyGraphEdge> getEdges();

  String toDotGraph(String graphName);

  interface Builder {
    Builder addNode(PropertyGraphNode node);

    Builder addEdge(PropertyGraphEdge edge);

    PropertyGraph build();
  }
}
