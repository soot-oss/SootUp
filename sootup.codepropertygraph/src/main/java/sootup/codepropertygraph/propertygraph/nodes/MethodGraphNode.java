package sootup.codepropertygraph.propertygraph.nodes;

import sootup.core.model.SootMethod;

public class MethodGraphNode extends PropertyGraphNode {
  private final SootMethod method;

  public MethodGraphNode(SootMethod method) {
    this.method = method;
  }

  public SootMethod getMethod() {
    return method;
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
    return method.getName();
  }
}
