package sootup.codepropertygraph.ddg;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.codepropertygraph.propertygraph.StmtMethodPropertyGraph;
import sootup.codepropertygraph.propertygraph.edges.DdgEdge;
import sootup.codepropertygraph.propertygraph.nodes.StmtGraphNode;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;

/**
 * This class is responsible for creating the Data Dependence Graph (DDG) property graph for a given
 * Soot method.
 */
public class DdgCreator {

  /**
   * Creates the DDG property graph for the given Soot method.
   *
   * @param method the Soot method
   * @return the DDG property graph
   */
  public PropertyGraph createGraph(SootMethod method) {
    PropertyGraph.Builder graphBuilder = new StmtMethodPropertyGraph.Builder();
    graphBuilder.setName("ddg_" + method.getName());

    if (method.isAbstract() || method.isNative()) {
      return graphBuilder.build();
    }

    StmtGraph<?> stmtGraph = method.getBody().getStmtGraph();
    Map<Stmt, List<Stmt>> reachingDefs = (new ReachingDefs(stmtGraph)).getReachingDefs();

    // Custom comparator for Stmt objects
    Comparator<Stmt> stmtComparator = Comparator.comparing(Stmt::toString);

    // Sort keys for deterministic order
    reachingDefs.keySet().stream()
        .sorted(stmtComparator)
        .forEach(
            key -> {
              StmtGraphNode destinationNode = new StmtGraphNode(key);
              List<Stmt> values = reachingDefs.get(key);

              // Sort values for deterministic order
              values.stream()
                  .sorted(stmtComparator)
                  .forEach(
                      value -> {
                        StmtGraphNode sourceNode = new StmtGraphNode(value);
                        graphBuilder.addEdge(new DdgEdge(sourceNode, destinationNode));
                      });
            });

    return graphBuilder.build();
  }
}
