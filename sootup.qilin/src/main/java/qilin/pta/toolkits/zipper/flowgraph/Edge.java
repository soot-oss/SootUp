package qilin.pta.toolkits.zipper.flowgraph;

import java.util.Objects;
import qilin.core.pag.PagNode;

public class Edge {
  private final Kind kind;
  private final PagNode source;
  private final PagNode target;
  private final int hashCode;

  public Edge(final Kind kind, final PagNode source, final PagNode target) {
    this.kind = kind;
    this.source = source;
    this.target = target;
    this.hashCode = Objects.hash(kind, source, target);
  }

  public Kind getKind() {
    return this.kind;
  }

  public PagNode getSource() {
    return this.source;
  }

  public PagNode getTarget() {
    return this.target;
  }

  @Override
  public String toString() {
    return this.kind + ": " + this.source + " --> " + this.target;
  }

  @Override
  public int hashCode() {
    return this.hashCode;
  }

  @Override
  public boolean equals(final Object other) {
    if (this.isOFGEdge()) {
      return this == other;
    }
    if (this == other) {
      return true;
    }
    if (!(other instanceof Edge)) {
      return false;
    }
    final Edge otherEdge = (Edge) other;
    return this.kind.equals(otherEdge.kind)
        && this.source.equals(otherEdge.source)
        && this.target.equals(otherEdge.target);
  }

  private boolean isOFGEdge() {
    return this.kind != Kind.WRAPPED_FLOW && this.kind != Kind.UNWRAPPED_FLOW;
  }
}
