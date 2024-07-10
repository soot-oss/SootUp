package sootup.java.codepropertygraph.propertygraph.nodes;

import sootup.core.jimple.basic.Value;
import sootup.core.types.Type;
import sootup.java.codepropertygraph.propertygraph.PropertyGraphNode;

public class ValueGraphNode extends PropertyGraphNode {
  private final Value value;

  public ValueGraphNode(Value value) {
    this.value = value;
  }

  public Value getValue() {
    return value;
  }

  public Type getJimpleType() {
    return value.getType();
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
