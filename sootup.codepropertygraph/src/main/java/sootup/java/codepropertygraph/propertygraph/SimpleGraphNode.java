package sootup.java.codepropertygraph.propertygraph;

public class SimpleGraphNode extends PropertyGraphNode {
  private final String name;

  public SimpleGraphNode(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
