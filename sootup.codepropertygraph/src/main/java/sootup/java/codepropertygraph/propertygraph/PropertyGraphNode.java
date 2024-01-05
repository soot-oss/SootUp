package sootup.java.codepropertygraph.propertygraph;

import java.util.Objects;

public class PropertyGraphNode {

  private final String name;
  private final NodeType type;

  public PropertyGraphNode(String name, NodeType type) {
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public NodeType getType() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PropertyGraphNode node = (PropertyGraphNode) o;
    return Objects.equals(name, node.name) && Objects.equals(type, node.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type);
  }
}
