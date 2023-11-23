package sootup.java.codepropertygraph.ast;

import java.util.ArrayList;
import java.util.List;

public class AstGraph {
  private final List<AstNode> nodes;
  private final List<AstEdge> edges;

  public AstGraph() {
    this.nodes = new ArrayList<>();
    this.edges = new ArrayList<>();
  }

  public void addEdge(AstNode source, AstNode destination) {
    AstEdge edge = new AstEdge(source, destination);
    // TODO: Handle duplicates (data)
    if (!edges.contains(edge)) {
      addNode(source);
      addNode(destination);
      edges.add(edge);
    }
  }

  private void addNode(AstNode node) {
    // TODO: Handle duplicates (data)
    if (!nodes.contains(node)) {
      nodes.add(node);
    }
  }

  public List<AstNode> getNodes() {
    return nodes;
  }

  public List<AstEdge> getEdges() {
    return edges;
  }

  public String toDotFormat() {
    return AstGraphToDotConverter.convertToDot(this);
  }
}
