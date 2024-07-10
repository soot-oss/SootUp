package sootup.java.codepropertygraph.propertygraph.nodes;

import sootup.core.jimple.basic.Immediate;
import sootup.java.codepropertygraph.propertygraph.PropertyGraphNode;

public class ImmediateGraphNode extends PropertyGraphNode {
  private final Immediate immediate;

  public ImmediateGraphNode(Immediate immediate) {
    this.immediate = immediate;
  }

  public Immediate getImmediate() {
    return immediate;
  }

  @Override
  public String toString() {
    return immediate.toString();
  }
}
