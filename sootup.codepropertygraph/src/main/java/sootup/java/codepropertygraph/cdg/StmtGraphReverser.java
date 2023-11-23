package sootup.java.codepropertygraph.cdg;

import org.apache.commons.lang3.StringUtils;
import sootup.core.graph.MutableBlockStmtGraph;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.stmt.Stmt;

public class StmtGraphReverser {

  public MutableStmtGraph reverseGraph(StmtGraph<?> originalGraph) {
    MutableStmtGraph reversedGraph = new MutableBlockStmtGraph();

    // First, add all nodes without any edges
    for (Stmt node : originalGraph.getNodes()) {
      reversedGraph.addNode(node);
    }

    // Then, reverse the edges
    for (Stmt originalNode : originalGraph.getNodes()) {
      for (Stmt successor : originalGraph.getAllSuccessors(originalNode)) {
        // Only add an edge if it does not already exist
        if (!reversedGraph.hasEdgeConnecting(successor, originalNode)) {
          reversedGraph.putEdge(successor, originalNode);
          System.out.println(StringUtils.rightPad(successor.toString(), 30) + "-> " + originalNode);
        }
      }
    }

    return reversedGraph;
  }
}
