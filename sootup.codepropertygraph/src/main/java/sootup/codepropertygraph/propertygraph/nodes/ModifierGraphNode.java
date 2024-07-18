package sootup.codepropertygraph.propertygraph.nodes;

import sootup.core.model.MethodModifier;

public class ModifierGraphNode extends PropertyGraphNode {
  private final MethodModifier modifier;

  public ModifierGraphNode(MethodModifier modifier) {
    this.modifier = modifier;
  }

  public MethodModifier getModifier() {
    return modifier;
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
    return modifier.toString();
  }
}
