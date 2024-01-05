package sootup.java.codepropertygraph.propertygraph;

import java.util.ArrayList;
import java.util.List;

public class PropertyGraph {
  private final List<PropertyGraphNode> nodes;
  private final List<PropertyGraphEdge> edges;

  public PropertyGraph() {
    this.nodes = new ArrayList<>();
    this.edges = new ArrayList<>();
  }

  public void addEdge(PropertyGraphEdge edge) {
    addEdge(edge.getSource(), edge.getDestination(), edge.getLabel());
  }

  public void addEdge(PropertyGraphNode source, PropertyGraphNode destination, String label) {
    PropertyGraphEdge edge = new PropertyGraphEdge(source, destination, label);
    // TODO: Handle duplicates (data)
    if (!edges.contains(edge)) {
      addNode(source);
      addNode(destination);
      edges.add(edge);
    }
  }

  private void addNode(PropertyGraphNode node) {
    // TODO: Handle duplicates (data)
    if (!nodes.contains(node)) {
      nodes.add(node);
    }
  }

  public List<PropertyGraphNode> getNodes() {
    return nodes;
  }

  public List<PropertyGraphEdge> getEdges() {
    return edges;
  }

  public String toDotGraph(String graphName) {
    return PropertyGraphToDotConverter.convert(this, graphName);
  }
}
