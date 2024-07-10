package sootup.java.codepropertygraph.propertygraph.nodes;

import sootup.core.jimple.common.ref.Ref;
import sootup.java.codepropertygraph.propertygraph.PropertyGraphNode;

public class RefGraphNode extends PropertyGraphNode {
  private final Ref ref;

  public RefGraphNode(Ref ref) {
    this.ref = ref;
  }

  public Ref getRef() {
    return ref;
  }

  @Override
  public String toString() {
    return ref.toString();
  }
}
