package sootup.codepropertygraph.propertygraph.nodes;

public class AggregateGraphNode extends PropertyGraphNode {
  private final String name;

  public AggregateGraphNode(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    return this == o;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }

  @Override
  public String toString() {
    return name;
  }
}
