package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.*;
import javax.annotation.Nonnull;

/**
 * Iterates over a given StmtGraph (which is connected, so all Stmt nodes are reached - except
 * traphandler)
 *
 * @author Markus Schmidt
 */
public class StmtGraphBlockIterator implements Iterator<Stmt> {

  @Nonnull private final StmtGraph graph;
  @Nonnull protected final Set<Stmt> alreadyInsertedNodes;

  @Nonnull private final ArrayDeque<Stmt> currentBlockQueue = new ArrayDeque<>();
  @Nonnull private final ArrayDeque<Stmt> workQueue = new ArrayDeque<>();
  @Nonnull private final ArrayDeque<Trap> traps;

  public StmtGraphBlockIterator(@Nonnull StmtGraph graph, List<Trap> traps) {
    this(graph, graph.getStartingStmt(), traps);
  }

  public StmtGraphBlockIterator(StmtGraph graph, Stmt startingStmt, List<Trap> traps) {
    this.graph = graph;
    alreadyInsertedNodes = new LinkedHashSet<>(graph.nodes().size(), 1);

    currentBlockQueue.add(startingStmt);
    alreadyInsertedNodes.add(startingStmt);
    this.traps = new ArrayDeque<>(traps);
  }

  @Override
  public Stmt next() {

    Stmt stmt;
    if (!currentBlockQueue.isEmpty()) {
      stmt = currentBlockQueue.pollFirst();
    } else if (!workQueue.isEmpty()) {
      stmt = workQueue.pollFirst();
    } else {
      throw new IndexOutOfBoundsException("No more elements to iterate over!");
    }

    alreadyInsertedNodes.add(stmt);

    while (!traps.isEmpty() && stmt == traps.peekFirst().getEndStmt()) {
      final Trap removedTrap = traps.removeFirst();
      currentBlockQueue.addLast(removedTrap.getHandlerStmt());
    }

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
