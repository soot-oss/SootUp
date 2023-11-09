package sootup.java.codepropertygraph.ast;

import java.util.HashSet;
import java.util.Set;

public class AstGraph {
  private final Set<AstNode> nodes;
  private final Set<AstEdge> edges;

  public AstGraph() {
    this.nodes = new HashSet<>();
    this.edges = new HashSet<>();
  }

  private void addNode(AstNode node) {
    nodes.add(node);
  }

  public void addEdge(AstNode source, AstNode destination) {
    addNode(source);
    addNode(destination);
    edges.add(new AstEdge(source, destination));
  }

  public Set<AstNode> getNodes() {
    return nodes;
  }

  public Set<AstEdge> getEdges() {
    return edges;
  }

  public String toDotFormat() {
    return AstGraphToDotConverter.convertToDot(this);
  }
}
