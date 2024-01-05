package sootup.java.codepropertygraph.ddg;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.stmt.JGotoStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;

public class DdgToGraphConverter {
  public static DdgGraph convert(MethodDdg methodDdg) {
    DdgGraph ddgGraph = new DdgGraph();
    StmtGraph<?> stmtGraph = methodDdg.getStmtGraph();
    Body body = methodDdg.getBody();

    Map<Stmt, List<Stmt>> reachingDefs = (new ReachingDefs(stmtGraph)).getReachingDefs();
    Iterator<Stmt> iterator = reachingDefs.keySet().iterator();

    while (iterator.hasNext()) {
      Stmt key = iterator.next();

      DdgNode destination =
          new DdgNode(getStmtSource(key, body), DdgNodeType.STMT, key.getPositionInfo());
      List<Stmt> values = reachingDefs.get(key);
      values.forEach(
          value -> {
            DdgNode source =
                new DdgNode(getStmtSource(value, body), DdgNodeType.STMT, value.getPositionInfo());
            ddgGraph.addEdge(source, destination);
          });
    }

    return ddgGraph;
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
