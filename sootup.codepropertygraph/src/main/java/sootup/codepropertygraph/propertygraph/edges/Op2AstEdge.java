package sootup.codepropertygraph.propertygraph.edges;

import sootup.codepropertygraph.propertygraph.nodes.PropertyGraphNode;

public class Op2AstEdge extends AbstAstEdge {
  public Op2AstEdge(PropertyGraphNode source, PropertyGraphNode destination) {
    super(source, destination);
  }

  @Override
  public String getLabel() {
    return "ast_op1";
  }
}
