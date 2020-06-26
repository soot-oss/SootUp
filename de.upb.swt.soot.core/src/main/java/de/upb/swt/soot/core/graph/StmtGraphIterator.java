package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.*;
import javax.annotation.Nonnull;

/**
 * The StmtGraphIterator is the base class for iterating over a StmtGraph.
 *
 * @author Markus Schmidt
 */
public abstract class StmtGraphIterator implements Iterator<Stmt> {

  @Nonnull private final StmtGraph graph;
  @Nonnull protected final Set<Stmt> alreadyInsertedNodes;

  StmtGraphIterator(@Nonnull StmtGraph graph, @Nonnull Stmt firstStmt) {
    this.graph = graph;
    alreadyInsertedNodes = new HashSet<>(graph.nodes().size(), 1);

    addToContainer(firstStmt);
    alreadyInsertedNodes.add(firstStmt);
  }

  public static BreadthFirstIterator bfs(@Nonnull StmtGraph stmtGraph) {
    return new BreadthFirstIterator(stmtGraph);
  }

  public static DepthFirstIterator dfs(@Nonnull StmtGraph stmtGraph) {
    return new DepthFirstIterator(stmtGraph);
  }

  @Nonnull
  protected StmtGraph getGraph() {
    return graph;
  }

  protected abstract void addToContainer(@Nonnull Stmt stmt);

  protected abstract Stmt removeFromContainer();

  @Override
  public Stmt next() {

    Stmt stmt = removeFromContainer();
    final List<Stmt> successors = getGraph().successors(stmt);
    for (Stmt succ : successors) {
      if (!alreadyInsertedNodes.contains(succ)) {
        addToContainer(succ);
        alreadyInsertedNodes.add(succ);
      }
    }
    return stmt;
  }
}
