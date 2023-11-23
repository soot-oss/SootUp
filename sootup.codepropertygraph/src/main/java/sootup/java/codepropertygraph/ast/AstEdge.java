package sootup.java.codepropertygraph.ast;

import java.util.Objects;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AstEdge astEdge = (AstEdge) o;
    return Objects.equals(source, astEdge.source)
        && Objects.equals(destination, astEdge.destination);
  }

  @Override
  public int hashCode() {
    return Objects.hash(source, destination);
  }
}
