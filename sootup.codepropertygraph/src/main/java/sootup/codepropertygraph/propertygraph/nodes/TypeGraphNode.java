package sootup.codepropertygraph.propertygraph.nodes;

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
  public boolean equals(Object o) {
    return this == o;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }

  @Override
  public String toString() {
    return type.toString();
  }
}
