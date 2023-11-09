package sootup.java.codepropertygraph.ast;

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
}
