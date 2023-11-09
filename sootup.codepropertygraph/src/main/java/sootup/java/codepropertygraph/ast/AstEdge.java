package sootup.java.codepropertygraph.ast;

public class AstEdge {
  private final AstNode source;
  private final AstNode destination;

  public AstEdge(AstNode source, AstNode destination) {
    this.source = source;
    this.destination = destination;
  }

  public AstNode getSource() {
    return source;
  }

  public AstNode getDestination() {
    return destination;
  }
}
