package qilin.pta.toolkits.zipper.flowgraph;

import java.util.Set;
import qilin.core.pag.PagNode;

public interface IObjectFlowGraph {
  Set<Edge> outEdgesOf(final PagNode p0);

  Set<PagNode> allNodes();
}
