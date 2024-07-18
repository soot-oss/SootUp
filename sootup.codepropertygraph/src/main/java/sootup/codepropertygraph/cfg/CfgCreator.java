package sootup.codepropertygraph.cfg;

import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.codepropertygraph.propertygraph.StmtMethodPropertyGraph;
import sootup.codepropertygraph.propertygraph.edges.*;
import sootup.codepropertygraph.propertygraph.nodes.StmtGraphNode;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;
import sootup.core.model.SootMethod;

public class CfgCreator {
  public PropertyGraph createGraph(SootMethod method) {
    PropertyGraph cfgGraph = new StmtMethodPropertyGraph();
    StmtGraph<?> stmtGraph = method.getBody().getStmtGraph();

    stmtGraph.forEach(
        currStmt -> {
          int expectedCount = currStmt.getExpectedSuccessorCount();
          int successorIndex = 0;

          for (Stmt successor : stmtGraph.getAllSuccessors(currStmt)) {
            StmtGraphNode sourceNode = new StmtGraphNode(currStmt);
            StmtGraphNode destinationNode = new StmtGraphNode(successor);
            AbstCfgEdge edge = createEdge(currStmt, successorIndex, sourceNode, destinationNode);

            if (successorIndex >= expectedCount) {
              edge = new ExceptionalCfgEdge(sourceNode, destinationNode);
            }

            cfgGraph.addEdge(edge);
            successorIndex++;
          }
        });

    return cfgGraph;
  }

  private AbstCfgEdge createEdge(
      Stmt currStmt, int successorIndex, StmtGraphNode sourceNode, StmtGraphNode destinationNode) {
    if (currStmt instanceof JIfStmt) {
      return successorIndex == JIfStmt.TRUE_BRANCH_IDX
          ? new IfTrueCfgEdge(sourceNode, destinationNode)
          : new IfFalseCfgEdge(sourceNode, destinationNode);
    } else if (currStmt instanceof JSwitchStmt) {
      return new SwitchCfgEdge(sourceNode, destinationNode);
    } else if (currStmt instanceof JGotoStmt) {
      return new GotoCfgEdge(sourceNode, destinationNode);
    } else {
      return new NormalCfgEdge(sourceNode, destinationNode);
    }
  }
}
