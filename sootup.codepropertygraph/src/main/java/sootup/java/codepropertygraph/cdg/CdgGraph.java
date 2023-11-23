package sootup.java.codepropertygraph.cdg;

import java.util.ArrayList;
import java.util.List;

public class CdgGraph {
  private final List<CdgNode> nodes;
  private final List<CdgEdge> edges;

  public CdgGraph() {
    this.nodes = new ArrayList<>();
    this.edges = new ArrayList<>();
  }

  public void addEdge(CdgNode source, CdgNode destination) {
    CdgEdge edge = new CdgEdge(source, destination);
    // TODO: Handle duplicates (data)
    if (!edges.contains(edge)) {
      addNode(source);
      addNode(destination);
      edges.add(edge);
    }
  }

  private void addNode(CdgNode node) {
    // TODO: Handle duplicates (data)
    if (!nodes.contains(node)) {
      nodes.add(node);
    }
  }

  public List<CdgNode> getNodes() {
    return nodes;
  }

  public List<CdgEdge> getEdges() {
    return edges;
  }

  public String toDotFormat() {
    return CdgGraphToDotConverter.convertToDot(this);
  }
}
