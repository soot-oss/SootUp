package sootup.codepropertygraph.propertygraph.edges;

import java.util.Objects;
import sootup.codepropertygraph.propertygraph.nodes.PropertyGraphNode;

public abstract class PropertyGraphEdge {
  private final PropertyGraphNode source;
  private final PropertyGraphNode destination;

  public PropertyGraphEdge(PropertyGraphNode source, PropertyGraphNode destination) {
    this.source = source;
    this.destination = destination;
  }

  public PropertyGraphNode getSource() {
    return source;
  }

  public PropertyGraphNode getDestination() {
    return destination;
  }

  public abstract String getLabel();

  public abstract String getColor();

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PropertyGraphEdge that = (PropertyGraphEdge) o;
    return source.equals(that.source)
        && destination.equals(that.destination)
        && getLabel().equals(that.getLabel());
  }

  @Override
  public int hashCode() {
    return Objects.hash(source, destination, getLabel());
  }
}
