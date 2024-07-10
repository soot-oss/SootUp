package sootup.java.codepropertygraph.cdg;

import java.util.*;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.PostDominanceFinder;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.java.codepropertygraph.propertygraph.*;
import sootup.java.codepropertygraph.propertygraph.nodes.StmtGraphNode;

public class CdgCreator {
  public PropertyGraph createGraph(SootMethod method) {
    PropertyGraph cdgGraph = new StmtMethodPropertyGraph();
    StmtGraph<?> stmtGraph = method.getBody().getStmtGraph();

    PostDominanceFinder postDominanceFinder = new PostDominanceFinder(stmtGraph);

    Iterator<BasicBlock<?>> iterator = stmtGraph.getBlockIterator();
    while (iterator.hasNext()) {
      BasicBlock<?> currBlock = iterator.next();
      for (BasicBlock<?> frontierBlock : postDominanceFinder.getDominanceFrontiers(currBlock)) {
        StmtGraphNode sourceNode = new StmtGraphNode(frontierBlock.getTail());
        for (Stmt srcStmt : currBlock.getStmts()) {
          StmtGraphNode destinationNode = new StmtGraphNode(srcStmt);
          cdgGraph.addEdge(new PropertyGraphEdge(sourceNode, destinationNode, EdgeType.CDG));
        }
      }
    }

    return cdgGraph;
  }
}
