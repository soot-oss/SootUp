package sootup.java.codepropertygraph.cfg;

import java.util.Objects;

public class CfgEdge {
    private final CfgNode source;
    private final CfgNode destination;

    public CfgEdge(CfgNode source, CfgNode destination) {
        this.source = source;
        this.destination = destination;
    }

    public CfgNode getSource() {
        return source;
    }

    public CfgNode getDestination() {
        return destination;
    }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CfgEdge cfgEdge = (CfgEdge) o;
    return Objects.equals(source, cfgEdge.source)
        && Objects.equals(destination, cfgEdge.destination);
  }

  @Override
  public int hashCode() {
    return Objects.hash(source, destination);
  }
}
