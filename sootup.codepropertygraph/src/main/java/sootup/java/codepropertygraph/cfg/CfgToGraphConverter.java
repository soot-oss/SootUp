package sootup.java.codepropertygraph.cfg;

import static sootup.java.codepropertygraph.cfg.CfgNodeType.STMT;

import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.stmt.JGotoStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.java.codepropertygraph.ast.AstNode;
import sootup.java.codepropertygraph.ast.AstNodeType;

public class CfgToGraphConverter {
  public static CfgGraph convert(MethodCfg methodCfg) {
    CfgGraph cfgGraph = new CfgGraph();
    StmtGraph<?> stmtGraph = methodCfg.getStmtGraph();

    stmtGraph.forEach(
        currStmt ->
            stmtGraph
                .getAllSuccessors(currStmt)
                .forEach(
                    successor -> {
                        Body body = methodCfg.getBody();
                      CfgNode sourceNode = new CfgNode(getStmtSource(currStmt, body), STMT, currStmt.getPositionInfo());
                      CfgNode destinationNode = new CfgNode(getStmtSource(successor, body), STMT, successor.getPositionInfo());
                      cfgGraph.addEdge(sourceNode, destinationNode);
                    }));

    return cfgGraph;
  }

    private static String getStmtSource(Stmt currStmt, Body body) {

        if (currStmt.getClass().getSimpleName().equals("JGotoStmt")) {
            JGotoStmt stmt = (JGotoStmt) currStmt;
            int gotoPosition =
                    stmt.getTargetStmts(body).get(0).getPositionInfo().getStmtPosition().getFirstLine();
            return String.format("%s %d", stmt, gotoPosition);
        }
        return currStmt.toString();
    }
}
