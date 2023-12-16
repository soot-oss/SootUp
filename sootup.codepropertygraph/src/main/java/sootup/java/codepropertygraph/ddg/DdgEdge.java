package sootup.java.codepropertygraph.ddg;

import java.util.Objects;

public class DdgEdge {
  private final DdgNode source;
  private final DdgNode destination;

  public DdgEdge(DdgNode source, DdgNode destination) {
    this.source = source;
    this.destination = destination;
  }

  public DdgNode getSource() {
    return source;
  }

  public DdgNode getDestination() {
    return destination;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DdgEdge ddgEdge = (DdgEdge) o;
    return Objects.equals(source, ddgEdge.source)
        && Objects.equals(destination, ddgEdge.destination);
  }

  @Override
  public int hashCode() {
    return Objects.hash(source, destination);
  }
}
