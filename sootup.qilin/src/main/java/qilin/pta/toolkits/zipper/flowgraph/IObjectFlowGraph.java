package qilin.pta.toolkits.zipper.flowgraph;

import java.util.Set;
import qilin.core.pag.Node;

public interface IObjectFlowGraph {
  Set<Edge> outEdgesOf(final Node p0);

  Set<Node> allNodes();
}
