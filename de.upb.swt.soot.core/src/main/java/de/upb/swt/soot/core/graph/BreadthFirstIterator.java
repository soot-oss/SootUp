package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.*;
import javax.annotation.Nonnull;

/**
 * Iterates over a given StmtGraph (which is connected, so all Stmt nodes are reached) in Breadth
 * First Order.
 *
 * @author Markus Schmidt
 */
public class BreadthFirstIterator extends StmtGraphIterator {

  @Nonnull private final Queue<Stmt> q = new ArrayDeque<>();

  BreadthFirstIterator(@Nonnull StmtGraph graph) {
    this(graph, graph.getStartingStmt());
  }

  BreadthFirstIterator(@Nonnull StmtGraph graph, @Nonnull Stmt startingStmt) {
    super(graph);
    addToContainer(startingStmt);
    alreadyInsertedNodes.add(startingStmt);
  }

  @Override
  public boolean hasNext() {
    return !q.isEmpty();
  }

  @Override
  protected void addToContainer(@Nonnull Stmt stmt) {
    q.add(stmt);
  }

  @Override
  protected Stmt removeFromContainer() {
    return q.remove();
  }
}
