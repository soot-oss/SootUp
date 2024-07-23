package sootup.codepropertygraph.propertygraph.edges;

import sootup.codepropertygraph.propertygraph.nodes.PropertyGraphNode;

public class SwitchKeyAstEdge extends AbstAstEdge {
  public SwitchKeyAstEdge(PropertyGraphNode source, PropertyGraphNode destination) {
    super(source, destination);
  }

  @Override
  public String getLabel() {
    return "ast_switch_key";
  }
}
