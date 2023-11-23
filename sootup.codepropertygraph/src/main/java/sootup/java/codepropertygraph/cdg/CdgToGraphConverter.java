package sootup.java.codepropertygraph.cdg;

import java.util.*;

import sootup.core.graph.BasicBlock;
import sootup.core.graph.DominanceFinder;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.stmt.JGotoStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;

public class CdgToGraphConverter {
  public static CdgGraph convert(MethodCdg methodCdg) {
    CdgGraph cdgGraph = new CdgGraph();
    StmtGraph<?> stmtGraph = methodCdg.getStmtGraph();

    StmtGraphReverser reverser = new StmtGraphReverser();
    // DominanceFinder dominanceFinder = new DominanceFinder(reverser.reverseGraph(stmtGraph));
    DominanceFinder dominanceFinder = new DominanceFinder(stmtGraph);

    Iterator<BasicBlock<?>> iterator = stmtGraph.getBlockIterator();
    while (iterator.hasNext()) {
      BasicBlock<?> currBlock = iterator.next();
      System.out.println(currBlock);
      for (BasicBlock<?> frontierBlock: dominanceFinder.getDominanceFrontiers(currBlock)) {
        System.out.println("\t-> " + frontierBlock);
      }
      // System.out.println("\t-< " + currBlock.getPredecessors());
    }

    return cdgGraph;
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
