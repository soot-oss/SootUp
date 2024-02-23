package sootup.java.codepropertygraph.cdg;

import java.util.*;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.DominanceFinder;
import sootup.core.graph.PostDominanceFinder;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.java.codepropertygraph.MethodInfo;
import sootup.java.codepropertygraph.StmtUtils;
import sootup.java.codepropertygraph.propertygraph.NodeType;
import sootup.java.codepropertygraph.propertygraph.PropertyGraph;
import sootup.java.codepropertygraph.propertygraph.PropertyGraphNode;
import sootup.java.codepropertygraph.propertygraph.StmtPropertyGraphNode;

public class CdgCreator {
  public static PropertyGraph convert(MethodInfo methodInfo) {
    PropertyGraph cdgGraph = new PropertyGraph();
    StmtGraph<?> stmtGraph = methodInfo.getStmtGraph();

    DominanceFinder dominanceFinder = new DominanceFinder(stmtGraph);
    PostDominanceFinder postDominanceFinder = new PostDominanceFinder(stmtGraph);

    Iterator<BasicBlock<?>> iterator = stmtGraph.getBlockIterator();
    while (iterator.hasNext()) {
      BasicBlock<?> currBlock = iterator.next();
      // System.out.println(currBlock);
      for (BasicBlock<?> frontierBlock : postDominanceFinder.getPostDominanceFrontiers(currBlock)) {
        // System.out.println("\t-> " + frontierBlock);
        PropertyGraphNode source =
            new StmtPropertyGraphNode(
                StmtUtils.getStmtSource(frontierBlock.getTail(), methodInfo.getBody()),
                NodeType.STMT,
                frontierBlock.getTail().getPositionInfo(), frontierBlock.getTail());
        for (Stmt srcStmt : currBlock.getStmts()) {
          StmtPropertyGraphNode destination =
              new StmtPropertyGraphNode(
                  StmtUtils.getStmtSource(srcStmt, methodInfo.getBody()),
                  NodeType.STMT,
                  srcStmt.getPositionInfo(), srcStmt);
          cdgGraph.addEdge(source, destination, "CDG");
        }
      }
    }

    return cdgGraph;
  }
}
