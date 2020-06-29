package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.*;
import javax.annotation.Nonnull;

/**
 * Iterates over a given StmtGraph (which is connected, so all Stmt nodes are reached) in Depth
 * First Order.
 *
 * @author Markus Schmidt
 */
public class DepthFirstIterator extends StmtGraphIterator {

  @Nonnull private final Stack<Stmt> stack = new Stack<>();

  DepthFirstIterator(@Nonnull StmtGraph graph) {
    this(graph, graph.getStartingStmt());
  }

  public DepthFirstIterator(StmtGraph graph, Stmt startingStmt) {
    super(graph);
    addToContainer(startingStmt);
    alreadyInsertedNodes.add(startingStmt);
  }

  @Override
  public boolean hasNext() {
    return !stack.isEmpty();
  }

  @Override
  protected void addToContainer(@Nonnull Stmt stmt) {
    stack.push(stmt);
  }

  @Override
  protected Stmt removeFromContainer() {
    return stack.pop();
  }
}
