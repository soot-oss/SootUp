package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * Iterates over a given StmtGraph (which is connected, so all Stmt nodes are reached - except
 * traphandler)
 *
 * @author Markus Schmidt
 */
public class CFGIterator implements Iterator<Stmt> {

  @Nonnull private final StmtGraph graph;
  @Nonnull protected final Set<Stmt> alreadyInsertedNodes;

  @Nonnull private final ArrayDeque<Stmt> currentBlockQueue = new ArrayDeque<>();
  @Nonnull private final ArrayDeque<Stmt> workQueue = new ArrayDeque<>();
  @Nonnull private final List<Stmt> trapHandler;

  public CFGIterator(@Nonnull StmtGraph graph, List<Trap> traps) {
    this(graph, graph.getStartingStmt(), traps);
  }

  public CFGIterator(StmtGraph graph, Stmt startingStmt, List<Trap> traps) {
    this.graph = graph;
    alreadyInsertedNodes = new LinkedHashSet<>(graph.nodes().size(), 1);

    currentBlockQueue.add(startingStmt);
    alreadyInsertedNodes.add(startingStmt);
    trapHandler = traps.stream().map(Trap::getHandlerStmt).collect(Collectors.toList());
  }

  @Override
  public Stmt next() {

    Stmt stmt;
    if (!workQueue.isEmpty()) {
      stmt = workQueue.pollFirst();
    } else if (!currentBlockQueue.isEmpty()) {
      stmt = currentBlockQueue.pollFirst();
    } else {
      stmt = trapHandler.remove(trapHandler.size() - 1);
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
    return !(currentBlockQueue.isEmpty() && workQueue.isEmpty() && trapHandler.isEmpty());
  }
}
