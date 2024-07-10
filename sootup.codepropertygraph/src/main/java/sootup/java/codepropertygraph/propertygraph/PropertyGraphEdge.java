package sootup.java.codepropertygraph.propertygraph;

import java.util.Objects;

public class PropertyGraphEdge {
  private final PropertyGraphNode source;
  private final PropertyGraphNode destination;
  private final EdgeType type;

  public PropertyGraphEdge(PropertyGraphNode source, PropertyGraphNode destination, EdgeType type) {
    this.source = source;
    this.destination = destination;
    this.type = type;
  }

  public PropertyGraphNode getSource() {
    return source;
  }

  public PropertyGraphNode getDestination() {
    return destination;
  }

  public EdgeType getType() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PropertyGraphEdge that = (PropertyGraphEdge) o;
    return Objects.equals(source, that.source)
        && Objects.equals(destination, that.destination)
        && type == that.type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(source, destination, type);
  }
}
