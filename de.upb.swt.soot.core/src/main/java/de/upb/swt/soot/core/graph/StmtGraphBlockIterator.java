package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.JIfStmt;
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
  @Nonnull protected final Set<Stmt> finishedNodes;

  @Nonnull private ArrayDeque<Stmt> currentBlock = new ArrayDeque<>();
  @Nonnull private ArrayDeque<Stmt> currentBlockBranches = new ArrayDeque<>();
  @Nonnull private final ArrayDeque<ArrayDeque<Stmt>> moreBlockBranches = new ArrayDeque<>();

  @Nonnull private final ArrayDeque<Trap> traps;

  public StmtGraphBlockIterator(@Nonnull StmtGraph graph, List<Trap> traps) {
    this(graph, graph.getStartingStmt(), traps);
  }

  public StmtGraphBlockIterator(StmtGraph graph, Stmt startingStmt, List<Trap> traps) {
    this.graph = graph;
    finishedNodes = new LinkedHashSet<>(graph.nodes().size(), 1);

    currentBlock.add(startingStmt);
    this.traps = new ArrayDeque<>(traps);
    cachedStmt = retrieveNextStmt();
  }

  // cache it for the next call to skip already retrieved nodes easily + simple hasNext()
  Stmt cachedStmt;

  @Override
  public Stmt next() {

    Stmt stmt = cachedStmt;

    /*
    for (Iterator<Trap> iterator = traps.iterator(); iterator.hasNext(); ) {
      Trap trap = iterator.next();
      if (stmt == trap.getEndStmt()) {
        iterator.remove();
        workQueue.addLast(trap.getHandlerStmt());
      }
    }*/

    final List<Stmt> successors = graph.successors(stmt);
    for (int i = 0; i < successors.size(); i++) {
      Stmt succ = successors.get(i);
      // if (!alreadyInsertedNodes.contains(succ))
      {
        // i.e. is not a BranchingStmt or is the first successor of JIfStmt
        if (i == 0 && stmt.fallsThrough()) {
          // remember non-branching successors
          currentBlock.addFirst(succ);
        } else {
          // remember branching successors
          currentBlockBranches.addLast(succ);
          System.out.print("----> " + succ + " ");
        }
        //         alreadyInsertedNodes.add(succ);
      }
    }
    System.out.println();

    cachedStmt = retrieveNextStmt();
    return stmt;
  }

  private Stmt retrieveNextStmt() {
    Stmt stmt;
    do {

      if (!currentBlock.isEmpty()) {
        stmt = currentBlock.pollFirst();
      } else if (!currentBlockBranches.isEmpty()) {
        // already empty ;) currentBlock = new ArrayDeque<>();

        System.out.println("#remove laver");
        currentBlock = currentBlockBranches;
        currentBlockBranches = new ArrayDeque<>();

        stmt = currentBlock.pollFirst();
      } else if (!moreBlockBranches.isEmpty()) {
        currentBlock = moreBlockBranches.pollFirst();
        // already empty ;) currentBlockBranches = new ArrayDeque<>();
        stmt = currentBlock.pollFirst();
      } else {
        return null;
      }

      // skip retreived stmt if its already finished
    } while (finishedNodes.contains(stmt));
    finishedNodes.add(stmt);

    // push layer
    if (stmt instanceof JIfStmt) {
      if (!currentBlockBranches.isEmpty()) {
        moreBlockBranches.addFirst(currentBlockBranches);
        currentBlockBranches = new ArrayDeque<>();
        System.out.println("#new layer: " + moreBlockBranches.size());
      }
    }

    System.out.print(stmt + " ");
    return stmt;
  }

  @Override
  public boolean hasNext() {
    return cachedStmt != null;
  }
}
