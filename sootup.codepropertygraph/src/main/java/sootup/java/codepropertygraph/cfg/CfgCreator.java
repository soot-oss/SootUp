package sootup.java.codepropertygraph.cfg;

import sootup.core.graph.StmtGraph;
import sootup.core.model.SootMethod;
import sootup.java.codepropertygraph.propertygraph.*;
import sootup.java.codepropertygraph.propertygraph.nodes.StmtGraphNode;

public class CfgCreator {
  public PropertyGraph createGraph(SootMethod method) {
    PropertyGraph cfgGraph = new StmtMethodPropertyGraph();
    StmtGraph<?> stmtGraph = method.getBody().getStmtGraph();

    stmtGraph.forEach(
        currStmt ->
            stmtGraph
                .getAllSuccessors(currStmt)
                .forEach(
                    successor -> {
                      StmtGraphNode sourceNode = new StmtGraphNode(currStmt);
                      StmtGraphNode destinationNode = new StmtGraphNode(successor);
                      cfgGraph.addEdge(
                          new PropertyGraphEdge(sourceNode, destinationNode, EdgeType.CFG));
                    }));

    return cfgGraph;
  }
}
