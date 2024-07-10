package sootup.java.codepropertygraph.propertygraph;

import sootup.core.types.Type;

public class TypeGraphNode extends PropertyGraphNode {
  private final Type type;

  public TypeGraphNode(Type type) {
    this.type = type;
  }

  public Type getType() {
    return type;
  }

  @Override
  public String toString() {
    return type.toString();
  }
}
