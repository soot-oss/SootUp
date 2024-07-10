package sootup.java.codepropertygraph.propertygraph;

import java.util.ArrayList;
import java.util.List;
import sootup.java.codepropertygraph.propertygraph.nodes.MethodGraphNode;
import sootup.java.codepropertygraph.propertygraph.nodes.StmtGraphNode;

public class StmtMethodPropertyGraph implements PropertyGraph {
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
    if (!(node instanceof StmtGraphNode || node instanceof MethodGraphNode)) {
      throw new IllegalArgumentException("Graph can only contain statement or method nodes");
    }
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
}
