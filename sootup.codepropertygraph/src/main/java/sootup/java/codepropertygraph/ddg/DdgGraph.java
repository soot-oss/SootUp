package sootup.java.codepropertygraph.ddg;

import java.util.ArrayList;
import java.util.List;

public class DdgGraph {
  private final List<DdgNode> nodes;
  private final List<DdgEdge> edges;

  public DdgGraph() {
    this.nodes = new ArrayList<>();
    this.edges = new ArrayList<>();
  }

  public void addEdge(DdgNode source, DdgNode destination) {
    DdgEdge edge = new DdgEdge(source, destination);
    // TODO: Handle duplicates (data)
    if (!edges.contains(edge)) {
      addNode(source);
      addNode(destination);
      edges.add(edge);
    }
  }

  private void addNode(DdgNode node) {
    // TODO: Handle duplicates (data)
    if (!nodes.contains(node)) {
      nodes.add(node);
    }
  }

  public List<DdgNode> getNodes() {
    return nodes;
  }

  public List<DdgEdge> getEdges() {
    return edges;
  }

  public String toDotFormat() {
    return DdgGraphToDotConverter.convertToDot(this);
  }
}
