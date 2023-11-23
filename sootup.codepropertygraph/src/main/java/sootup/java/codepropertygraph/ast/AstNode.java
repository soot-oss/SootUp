package sootup.java.codepropertygraph.ast;

import java.util.Objects;

public class AstNode {
  private final String name;
  private final AstNodeType type;

  public AstNode(String name, AstNodeType type) {
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public AstNodeType getType() {
    return type;
  }

  /*@Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AstNode astNode = (AstNode) o;
    return Objects.equals(name, astNode.name) && type == astNode.type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type);
  }*/
}
