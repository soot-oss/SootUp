package sootup.codepropertygraph.propertygraph.edges;

import sootup.codepropertygraph.propertygraph.nodes.PropertyGraphNode;
import sootup.codepropertygraph.propertygraph.nodes.StmtGraphNode;

public class RightOpAstEdge extends AbstAstEdge {
  public RightOpAstEdge(StmtGraphNode source, PropertyGraphNode destination) {
    super(source, destination);
  }

  @Override
  public String getLabel() {
    return "ast_rightOp";
  }
}
