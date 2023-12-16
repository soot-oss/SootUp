package sootup.java.codepropertygraph.cdg;

import java.util.Objects;

public class CdgEdge {
  private final CdgNode source;
  private final CdgNode destination;

  public CdgEdge(CdgNode source, CdgNode destination) {
    this.source = source;
    this.destination = destination;
  }

  public CdgNode getSource() {
    return source;
  }

  public CdgNode getDestination() {
    return destination;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CdgEdge cdgEdge = (CdgEdge) o;
    return Objects.equals(source, cdgEdge.source)
        && Objects.equals(destination, cdgEdge.destination);
  }

  @Override
  public int hashCode() {
    return Objects.hash(source, destination);
  }
}
