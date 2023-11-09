package sootup.java.codepropertygraph.ast;

public class AstNode {
  private final String name;
  private final String type;

  public AstNode(String name, String type) {
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }
}
