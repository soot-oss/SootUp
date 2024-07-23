package sootup.codepropertygraph.propertygraph.edges;

import sootup.codepropertygraph.propertygraph.nodes.PropertyGraphNode;

public class SwitchTargetAstEdge extends AbstAstEdge {
  public SwitchTargetAstEdge(PropertyGraphNode source, PropertyGraphNode destination) {
    super(source, destination);
  }

  @Override
  public String getLabel() {
    return "ast_switch_target";
  }
}
