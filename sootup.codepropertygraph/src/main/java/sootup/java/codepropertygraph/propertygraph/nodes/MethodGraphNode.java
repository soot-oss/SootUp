package sootup.java.codepropertygraph.propertygraph.nodes;

import sootup.core.model.SootMethod;
import sootup.core.types.Type;
import sootup.java.codepropertygraph.propertygraph.PropertyGraphNode;

public class MethodGraphNode extends PropertyGraphNode {
  private final SootMethod method;

  public MethodGraphNode(SootMethod method) {
    this.method = method;
  }

  public SootMethod getMethod() {
    return method;
  }

  public Type getReturnType() {
    return method.getReturnType();
  }

  @Override
  public String toString() {
    return method.getName();
  }
}
