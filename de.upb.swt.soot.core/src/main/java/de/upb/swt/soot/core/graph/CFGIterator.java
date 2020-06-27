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
public class CFGIterator implements Iterator<Stmt> {

  @Nonnull private final StmtGraph graph;
  @Nonnull protected final Set<Stmt> alreadyInsertedNodes;

  @Nonnull private final ArrayDeque<Stmt> stack = new ArrayDeque<>();

  public CFGIterator(@Nonnull StmtGraph graph) {
    this(graph, graph.getStartingStmt());
  }

  public CFGIterator(StmtGraph graph, Stmt startingStmt) {
    this.graph = graph;
    alreadyInsertedNodes = new LinkedHashSet<>(graph.nodes().size(), 1);

    stack.add(startingStmt);
    alreadyInsertedNodes.add(startingStmt);
  }

  @Override
  public Stmt next() {

    Stmt stmt = stack.pollFirst();
    alreadyInsertedNodes.add(stmt);

    final List<Stmt> successors = graph.successors(stmt);
    for (int i = successors.size() - 1; i >= 0; i--) {
      Stmt succ = successors.get(i);
      stack.addFirst(succ);
    }

    // skip already visited nodes
    Stmt skipAlreadyVisited = stack.peekFirst();
    while (alreadyInsertedNodes.contains(skipAlreadyVisited) && !stack.isEmpty()) {
      stack.pollFirst();
      skipAlreadyVisited = stack.peekFirst();
    }

    return stmt;
  }

  @Override
  public boolean hasNext() {
    return !stack.isEmpty();
  }
}
