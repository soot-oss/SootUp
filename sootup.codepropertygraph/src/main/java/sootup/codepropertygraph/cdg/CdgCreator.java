package sootup.codepropertygraph.cdg;

import java.util.*;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.codepropertygraph.propertygraph.StmtMethodPropertyGraph;
import sootup.codepropertygraph.propertygraph.edges.CdgEdge;
import sootup.codepropertygraph.propertygraph.nodes.StmtGraphNode;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.PostDominanceFinder;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;

public class CdgCreator {
  public PropertyGraph createGraph(SootMethod method) {
    PropertyGraph.Builder graphBuilder = new StmtMethodPropertyGraph.Builder();
    graphBuilder.setName("cdg_" + method.getName());

    StmtGraph<?> stmtGraph = method.getBody().getStmtGraph();
    PostDominanceFinder postDominanceFinder = new PostDominanceFinder(stmtGraph);

    Iterator<BasicBlock<?>> iterator = stmtGraph.getBlockIterator();
    while (iterator.hasNext()) {
      BasicBlock<?> currBlock = iterator.next();
      for (BasicBlock<?> frontierBlock : postDominanceFinder.getDominanceFrontiers(currBlock)) {
        StmtGraphNode sourceNode = new StmtGraphNode(frontierBlock.getTail());
        for (Stmt srcStmt : currBlock.getStmts()) {
          StmtGraphNode destinationNode = new StmtGraphNode(srcStmt);
          graphBuilder.addEdge(new CdgEdge(sourceNode, destinationNode));
        }
      }
    }

    return graphBuilder.build();
  }
}
