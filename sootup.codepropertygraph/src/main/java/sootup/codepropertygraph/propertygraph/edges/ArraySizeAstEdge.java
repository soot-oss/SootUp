package sootup.codepropertygraph.propertygraph.edges;

import sootup.codepropertygraph.propertygraph.nodes.PropertyGraphNode;

public class ArraySizeAstEdge extends AbstAstEdge {
  public ArraySizeAstEdge(PropertyGraphNode source, PropertyGraphNode destination) {
    super(source, destination);
  }

  @Override
  public String getLabel() {
    return "ast_array_size";
  }
}
