package sootup.codepropertygraph.propertygraph.edges;

import sootup.codepropertygraph.propertygraph.nodes.PropertyGraphNode;

public abstract class AbstCdgEdge extends PropertyGraphEdge {
  public AbstCdgEdge(PropertyGraphNode source, PropertyGraphNode destination) {
    super(source, destination);
  }

  public abstract String getLabel();
}
