package sootup.codepropertygraph.propertygraph.nodes;

import sootup.core.jimple.basic.Immediate;

public class ImmediateGraphNode extends PropertyGraphNode {
  private final Immediate immediate;

  public ImmediateGraphNode(Immediate immediate) {
    this.immediate = immediate;
  }

  public Immediate getImmediate() {
    return immediate;
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
    return immediate.toString();
  }
}
