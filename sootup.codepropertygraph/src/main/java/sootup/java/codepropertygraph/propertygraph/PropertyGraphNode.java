package sootup.java.codepropertygraph.propertygraph;

import java.util.Objects;

public abstract class PropertyGraphNode {

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PropertyGraphNode node = (PropertyGraphNode) o;
    return Objects.equals(this.toString(), node.toString());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.toString());
  }

  @Override
  public abstract String toString();
}
