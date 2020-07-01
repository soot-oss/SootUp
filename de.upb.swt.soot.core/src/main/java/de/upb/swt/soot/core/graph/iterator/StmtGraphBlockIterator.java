package de.upb.swt.soot.core.graph.iterator;

import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.JGotoStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Iterates over a given StmtGraph (which is connected, so all Stmt nodes are reached - except
 * traphandler) so the returned Jimple Stmts are returned as valid, linearized code blocks that are
 * intuitive to navigate.
 *
 * @author Markus Schmidt
 */
public class StmtGraphBlockIterator implements Iterator<Stmt> {

  @Nonnull private final StmtGraph graph;
  @Nonnull private final Set<Stmt> returnedNodes;
  @Nonnull private final ArrayDeque<Trap> traps;

  @Nonnull private final ArrayDeque<Stmt> currentBlock = new ArrayDeque<>();
  @Nonnull private final ArrayDeque<Stmt> nestedBlocks = new ArrayDeque<>();
  @Nonnull private final ArrayDeque<Stmt> otherBlocks = new ArrayDeque<>();

  // caching the next Stmt to implement a simple hasNext() and skipping already returned Stmts
  @Nullable private Stmt cachedNextStmt;

  public StmtGraphBlockIterator(@Nonnull StmtGraph graph, List<Trap> traps) {
    this(graph, graph.getStartingStmt(), traps);
  }

  private StmtGraphBlockIterator(
      @Nonnull StmtGraph graph, @Nonnull Stmt startingStmt, @Nonnull List<Trap> traps) {
    this.graph = graph;
    returnedNodes = new HashSet<>(graph.nodes().size(), 1);

    this.traps = new ArrayDeque<>(traps);
    cachedNextStmt = startingStmt;
    returnedNodes.add(startingStmt);
  }

  @Nullable
  private Stmt retrieveNextStmt() {
    Stmt stmt;
    do {

      if (!currentBlock.isEmpty()) {
        stmt = currentBlock.pollFirst();
      } else if (!nestedBlocks.isEmpty()) {
        stmt = nestedBlocks.pollFirst();
      } else if (!otherBlocks.isEmpty()) {
        stmt = otherBlocks.pollFirst();
      } else {
        return null;
      }

      // skip retrieved stmt if its already returned
    } while (returnedNodes.contains(stmt));
    returnedNodes.add(stmt);

    return stmt;
  }

  @Override
  @Nonnull
  public Stmt next() {

    Stmt stmt = cachedNextStmt;
    if (stmt == null) {
      throw new NoSuchElementException("Iterator has no more Stmt's.");
    }

    // integrate trap handler blocks
    for (Iterator<Trap> iterator = traps.iterator(); iterator.hasNext(); ) {
      Trap trap = iterator.next();
      if (stmt == trap.getEndStmt()) {
        iterator.remove();
        currentBlock.addLast(trap.getHandlerStmt());
      }
    }

    final List<Stmt> successors = graph.successors(stmt);
    for (int i = successors.size() - 1; i >= 0; i--) {
      Stmt succ = successors.get(i);
      {
        // i.e. is not a BranchingStmt or is the first successor of JIfStmt
        if (i == 0 && stmt.fallsThrough()) {
          // remember non-branching successors
          currentBlock.addFirst(succ);
        } else {
          // remember branching successors
          if (stmt instanceof JGotoStmt) {
            otherBlocks.addFirst(succ);
          } else if (!nestedBlocks.contains(succ)) {
            // JSwitchStmt, JIfStmt
            nestedBlocks.addFirst(succ);
          }
        }
      }
    }

    cachedNextStmt = retrieveNextStmt();
    return stmt;
  }

  @Override
  public boolean hasNext() {
    return cachedNextStmt != null;
  }
}
