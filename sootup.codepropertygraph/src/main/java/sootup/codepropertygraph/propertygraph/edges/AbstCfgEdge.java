package sootup.codepropertygraph.propertygraph.edges;

import sootup.codepropertygraph.propertygraph.nodes.PropertyGraphNode;

public abstract class AbstCfgEdge extends PropertyGraphEdge {
  public AbstCfgEdge(PropertyGraphNode source, PropertyGraphNode destination) {
    super(source, destination);
  }

  public abstract String getLabel();

  public String getColor() {
      return "black";
  }
}
