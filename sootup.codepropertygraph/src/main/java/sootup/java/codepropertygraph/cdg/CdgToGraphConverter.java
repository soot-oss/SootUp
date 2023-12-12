package sootup.java.codepropertygraph.cdg;

import java.util.*;

import sootup.core.graph.BasicBlock;
import sootup.core.graph.PostDominanceFinder;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.stmt.JGotoStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;

public class CdgToGraphConverter {
  public static CdgGraph convert(MethodCdg methodCdg) {
    CdgGraph cdgGraph = new CdgGraph();
    StmtGraph<?> stmtGraph = methodCdg.getStmtGraph();

    StmtGraphReverser reverser = new StmtGraphReverser();
    PostDominanceFinder postDominanceFinder = new PostDominanceFinder(stmtGraph);


    Iterator<BasicBlock<?>> iterator = stmtGraph.getBlockIterator();
    while (iterator.hasNext()) {
      BasicBlock<?> currBlock = iterator.next();
      // System.out.println(currBlock);
      for (BasicBlock<?> frontierBlock: postDominanceFinder.getPostDominanceFrontiers(currBlock)) {
        // System.out.println("\t-> " + frontierBlock);
        CdgNode source = new CdgNode(getStmtSource(frontierBlock.getTail(), methodCdg.getBody()), CdgNodeType.STMT, frontierBlock.getTail().getPositionInfo());
        for (Stmt srcStmt: currBlock.getStmts()) {
          CdgNode destination = new CdgNode(getStmtSource(srcStmt, methodCdg.getBody()), CdgNodeType.STMT, srcStmt.getPositionInfo());
          cdgGraph.addEdge(source, destination);
        }
      }

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
