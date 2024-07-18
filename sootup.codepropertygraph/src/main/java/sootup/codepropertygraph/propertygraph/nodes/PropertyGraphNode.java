package sootup.codepropertygraph.propertygraph.nodes;

public abstract class PropertyGraphNode {

  @Override
  public abstract boolean equals(Object o);

  @Override
  public abstract int hashCode();

  @Override
  public abstract String toString();
}
