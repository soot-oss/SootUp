package sootup.java.codepropertygraph.ddg;

import java.util.List;
import java.util.Map;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.java.codepropertygraph.propertygraph.*;
import sootup.java.codepropertygraph.propertygraph.nodes.StmtGraphNode;

public class DdgCreator {
  public PropertyGraph createGraph(SootMethod method) {
    PropertyGraph ddgGraph = new StmtMethodPropertyGraph();
    StmtGraph<?> stmtGraph = method.getBody().getStmtGraph();

    Map<Stmt, List<Stmt>> reachingDefs = (new ReachingDefs(stmtGraph)).getReachingDefs();

    for (Stmt key : reachingDefs.keySet()) {
      StmtGraphNode destinationNode = new StmtGraphNode(key);
      List<Stmt> values = reachingDefs.get(key);
      values.forEach(
          value -> {
            StmtGraphNode sourceNode = new StmtGraphNode(value);
            ddgGraph.addEdge(new PropertyGraphEdge(sourceNode, destinationNode, EdgeType.DDG));
          });
    }

    return ddgGraph;
  }
}
