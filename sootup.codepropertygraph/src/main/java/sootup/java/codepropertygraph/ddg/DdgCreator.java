package sootup.java.codepropertygraph.ddg;

import java.util.List;
import java.util.Map;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.java.codepropertygraph.MethodInfo;
import sootup.java.codepropertygraph.StmtUtils;
import sootup.java.codepropertygraph.propertygraph.NodeType;
import sootup.java.codepropertygraph.propertygraph.PropertyGraph;
import sootup.java.codepropertygraph.propertygraph.StmtPropertyGraphNode;

public class DdgCreator {
  public static PropertyGraph convert(MethodInfo methodInfo) {
    PropertyGraph ddgGraph = new PropertyGraph();
    StmtGraph<?> stmtGraph = methodInfo.getStmtGraph();
    Body body = methodInfo.getBody();

    Map<Stmt, List<Stmt>> reachingDefs = (new ReachingDefs(stmtGraph)).getReachingDefs();

    for (Stmt key : reachingDefs.keySet()) {
      StmtPropertyGraphNode destination =
          new StmtPropertyGraphNode(
              StmtUtils.getStmtSource(key, body), NodeType.STMT, key.getPositionInfo());
      List<Stmt> values = reachingDefs.get(key);
      values.forEach(
          value -> {
            StmtPropertyGraphNode source =
                new StmtPropertyGraphNode(
                    StmtUtils.getStmtSource(value, body), NodeType.STMT, value.getPositionInfo());
            ddgGraph.addEdge(source, destination, "DDG");
          });
    }

    return ddgGraph;
  }
}
