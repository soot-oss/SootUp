package qilin.pta.toolkits.zipper.flowgraph;

import qilin.core.pag.Node;

import java.util.Objects;

public class Edge {
    private final Kind kind;
    private final Node source;
    private final Node target;
    private final int hashCode;

    public Edge(final Kind kind, final Node source, final Node target) {
        this.kind = kind;
        this.source = source;
        this.target = target;
        this.hashCode = Objects.hash(kind, source, target);
    }

    public Kind getKind() {
        return this.kind;
    }

    public Node getSource() {
        return this.source;
    }

    public Node getTarget() {
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
        if (!(other instanceof final Edge otherEdge)) {
            return false;
        }
        return this.kind.equals(otherEdge.kind) && this.source.equals(otherEdge.source) && this.target.equals(otherEdge.target);
    }

    private boolean isOFGEdge() {
        return this.kind != Kind.WRAPPED_FLOW && this.kind != Kind.UNWRAPPED_FLOW;
    }
}
