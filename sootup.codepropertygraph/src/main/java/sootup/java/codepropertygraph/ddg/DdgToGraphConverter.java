package sootup.java.codepropertygraph.ddg;

import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.stmt.JGotoStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;

public class DdgToGraphConverter {
  public static DdgGraph convert(MethodDdg methodDdg) {
    DdgGraph ddgGraph = new DdgGraph();
    StmtGraph<?> stmtGraph = methodDdg.getStmtGraph();

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
