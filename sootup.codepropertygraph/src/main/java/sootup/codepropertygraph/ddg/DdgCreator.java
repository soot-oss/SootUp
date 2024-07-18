package sootup.codepropertygraph.ddg;

import java.util.List;
import java.util.Map;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.codepropertygraph.propertygraph.StmtMethodPropertyGraph;
import sootup.codepropertygraph.propertygraph.edges.DdgEdge;
import sootup.codepropertygraph.propertygraph.nodes.StmtGraphNode;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;

public class DdgCreator {
  public PropertyGraph createGraph(SootMethod method) {
    PropertyGraph.Builder graphBuilder = new StmtMethodPropertyGraph.Builder();
    graphBuilder.setName("ddg_" + method.getName());

    if (method.isAbstract() || method.isNative()) {
      return graphBuilder.build();
    }

    StmtGraph<?> stmtGraph = method.getBody().getStmtGraph();
    Map<Stmt, List<Stmt>> reachingDefs = (new ReachingDefs(stmtGraph)).getReachingDefs();

    for (Stmt key : reachingDefs.keySet()) {
      StmtGraphNode destinationNode = new StmtGraphNode(key);
      List<Stmt> values = reachingDefs.get(key);
      values.forEach(
          value -> {
            StmtGraphNode sourceNode = new StmtGraphNode(value);
            graphBuilder.addEdge(new DdgEdge(sourceNode, destinationNode));
          });
    }

    return graphBuilder.build();
  }
}
