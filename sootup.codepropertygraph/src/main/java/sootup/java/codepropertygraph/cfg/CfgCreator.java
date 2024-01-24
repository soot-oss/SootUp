package sootup.java.codepropertygraph.cfg;

import sootup.core.graph.StmtGraph;
import sootup.core.model.Body;
import sootup.java.codepropertygraph.MethodInfo;
import sootup.java.codepropertygraph.StmtUtils;
import sootup.java.codepropertygraph.propertygraph.NodeType;
import sootup.java.codepropertygraph.propertygraph.PropertyGraph;
import sootup.java.codepropertygraph.propertygraph.StmtPropertyGraphNode;

public class CfgCreator {
  public static PropertyGraph convert(MethodInfo methodInfo) {
    PropertyGraph cfgGraph = new PropertyGraph();
    StmtGraph<?> stmtGraph = methodInfo.getStmtGraph();

    stmtGraph.forEach(
        currStmt ->
            stmtGraph
                .getAllSuccessors(currStmt)
                .forEach(
                    successor -> {
                      Body body = methodInfo.getBody();
                      StmtPropertyGraphNode sourceNode =
                          new StmtPropertyGraphNode(
                              StmtUtils.getStmtSource(currStmt, body),
                              NodeType.STMT,
                              currStmt.getPositionInfo());
                      StmtPropertyGraphNode destinationNode =
                          new StmtPropertyGraphNode(
                              StmtUtils.getStmtSource(successor, body),
                              NodeType.STMT,
                              successor.getPositionInfo());
                      cfgGraph.addEdge(sourceNode, destinationNode, "CFG");
                    }));

    return cfgGraph;
  }
}
