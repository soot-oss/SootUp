package sootup.codepropertygraph.propertygraph.edges;

import sootup.codepropertygraph.propertygraph.nodes.PropertyGraphNode;

public abstract class AbstAstEdge extends PropertyGraphEdge {
  public AbstAstEdge(PropertyGraphNode source, PropertyGraphNode destination) {
    super(source, destination);
  }

  public abstract String getLabel();

  public String getColor() {
      return "darkseagreen4";
  }
}
