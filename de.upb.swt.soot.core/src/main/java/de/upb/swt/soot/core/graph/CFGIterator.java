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

  @Nonnull private final ArrayDeque<Stmt> currentBlockQueue = new ArrayDeque<>();
  @Nonnull private final ArrayDeque<Stmt> workQueue = new ArrayDeque<>();

  public CFGIterator(@Nonnull StmtGraph graph) {
    this(graph, graph.getStartingStmt());
  }

  public CFGIterator(StmtGraph graph, Stmt startingStmt) {
    this.graph = graph;
    alreadyInsertedNodes = new LinkedHashSet<>(graph.nodes().size(), 1);

    currentBlockQueue.add(startingStmt);
    alreadyInsertedNodes.add(startingStmt);
  }

  @Override
  public Stmt next() {

    Stmt stmt;
    if (currentBlockQueue.isEmpty()) {
      stmt = workQueue.pollFirst();
    } else {
      stmt = currentBlockQueue.pollFirst();
    }

    alreadyInsertedNodes.add(stmt);

    final List<Stmt> successors = graph.successors(stmt);
    for (int i = 0; i < successors.size(); i++) {
      Stmt succ = successors.get(i);
      if (i == 0 && stmt.fallsThrough()) {
        currentBlockQueue.addFirst(succ);
      } else {
        if (stmt.fallsThrough()) {
          workQueue.addFirst(succ);
        } else {
          workQueue.addLast(succ);
        }
      }
    }

    // skip already visited nodes
    Stmt skipAlreadyVisited;
    while (!currentBlockQueue.isEmpty()
        && (skipAlreadyVisited = currentBlockQueue.peekFirst()) != null
        && alreadyInsertedNodes.contains(skipAlreadyVisited)) {
      currentBlockQueue.pollFirst();
    }
    while (!workQueue.isEmpty()
        && (skipAlreadyVisited = workQueue.peekFirst()) != null
        && alreadyInsertedNodes.contains(skipAlreadyVisited)) {
      workQueue.pollFirst();
    }

    return stmt;
  }

  @Override
  public boolean hasNext() {
    return !(currentBlockQueue.isEmpty() && workQueue.isEmpty());
  }
}
