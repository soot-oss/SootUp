package sootup.java.codepropertygraph.cfg;

import java.util.ArrayList;
import java.util.List;

public class CfgGraph {
  private final List<CfgNode> nodes;
  private final List<CfgEdge> edges;

  public CfgGraph() {
    this.nodes = new ArrayList<>();
    this.edges = new ArrayList<>();
  }

  public void addEdge(CfgNode source, CfgNode destination) {
    CfgEdge edge = new CfgEdge(source, destination);
    // TODO: Handle duplicates (data)
    if (!edges.contains(edge)) {
      addNode(source);
      addNode(destination);
      edges.add(edge);
    }
  }

  private void addNode(CfgNode node) {
    // TODO: Handle duplicates (data)
    if (!nodes.contains(node)) {
      nodes.add(node);
    }
  }

  public List<CfgNode> getNodes() {
    return nodes;
  }

  public List<CfgEdge> getEdges() {
    return edges;
  }

  public String toDotFormat() {
    return CfgGraphToDotConverter.convertToDot(this);
  }
}
