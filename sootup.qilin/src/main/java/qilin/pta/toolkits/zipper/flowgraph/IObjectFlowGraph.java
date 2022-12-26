package qilin.pta.toolkits.zipper.flowgraph;

import qilin.core.pag.Node;

import java.util.Set;

public interface IObjectFlowGraph {
    Set<Edge> outEdgesOf(final Node p0);

    Set<Node> allNodes();
}
