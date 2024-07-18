package sootup.codepropertygraph.propertygraph;

import sootup.codepropertygraph.propertygraph.edges.PropertyGraphEdge;
import sootup.codepropertygraph.propertygraph.nodes.PropertyGraphNode;
import sootup.codepropertygraph.propertygraph.utils.PropertyGraphToDotConverter;

import java.util.ArrayList;
import java.util.List;

public class AstPropertyGraph implements PropertyGraph {
  private final List<PropertyGraphNode> nodes = new ArrayList<>();
  private final List<PropertyGraphEdge> edges = new ArrayList<>();

  @Override
  public List<PropertyGraphNode> getNodes() {
    return nodes;
  }

  @Override
  public List<PropertyGraphEdge> getEdges() {
    return edges;
  }

  @Override
  public void addNode(PropertyGraphNode node) {
    if (!nodes.contains(node)) {
      nodes.add(node);
    }
  }

  @Override
  public void addEdge(PropertyGraphEdge edge) {
    addNode(edge.getSource());
    addNode(edge.getDestination());
    if (!edges.contains(edge)) {
      edges.add(edge);
    }
  }

  @Override
  public String toDotGraph(String graphName) {
    return PropertyGraphToDotConverter.convert(this, graphName);
  }
}
