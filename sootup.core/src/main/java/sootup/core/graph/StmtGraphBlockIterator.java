package sootup.core.graph;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2020 Markus Schmidt
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import sootup.core.jimple.basic.Trap;
import sootup.core.jimple.common.stmt.JGotoStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.JReturnVoidStmt;
import sootup.core.jimple.common.stmt.Stmt;

/**
 * Iterates over a given StmtGraph (which is connected, so all Stmt nodes are reached - except
 * traphandler) so the returned Jimple Stmts are returned as valid, linearized code blocks that are
 * intuitive to navigate.
 *
 * @author Markus Schmidt
 */
public class StmtGraphBlockIterator implements Iterator<Stmt> {

  @Nonnull private final StmtGraph<?> graph;
  @Nonnull private final List<Trap> traps;
  private int trapIdx = 0;

  @Nonnull private final ArrayDeque<Stmt> currentUnbranchedBlock = new ArrayDeque<>();
  @Nonnull private final ArrayDeque<Stmt> nestedBlocks = new ArrayDeque<>();
  @Nonnull private final ArrayDeque<Stmt> otherBlocks = new ArrayDeque<>();

  // caching the next Stmt to implement a simple hasNext() and skipping already returned Stmts
  @Nullable private Stmt cachedNextStmt;

  // TODO: [ms] improve memory consumption: only add Stmts with multiple predecessors; count
  // returnednodes with int;
  @Nonnull private final Set<Stmt> returnedNodes;

  public StmtGraphBlockIterator(@Nonnull StmtGraph graph, @Nonnull List<Trap> traps) {
    this.graph = graph;
    returnedNodes = new HashSet<>(graph.getNodes().size(), 1);
    Stmt startingStmt = graph.getStartingStmt();
    if (startingStmt != null) {
      returnedNodes.add(startingStmt);
    }
    cachedNextStmt = startingStmt;
    this.traps = traps;
  }

  @Nullable
  private Stmt retrieveNextStmt() {
    Stmt stmt;
    do {

      if (!currentUnbranchedBlock.isEmpty()) {
        stmt = currentUnbranchedBlock.pollFirst();
      } else if (!nestedBlocks.isEmpty()) {
        stmt = nestedBlocks.pollFirst();
      } else if (trapIdx < traps.size()) {
        stmt = traps.get(trapIdx++).getHandlerStmt();
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
      throw new NoSuchElementException("Iterator has no more Stmts.");
    }

    if (trapIdx < traps.size()) {
      final Trap nextTrap = traps.get(trapIdx);
      if (stmt == nextTrap.getEndStmt()) {
        currentUnbranchedBlock.addFirst(nextTrap.getHandlerStmt());
        trapIdx++;
      }
    }

    final List<Stmt> successors = graph.successors(stmt);
    for (int i = successors.size() - 1; i >= 0; i--) {
      Stmt succ = successors.get(i);
      {
        if (i == 0 && stmt.fallsThrough()) {
          // non-branching successors i.e. not a BranchingStmt or is the first successor of JIfStmt
          currentUnbranchedBlock.addFirst(succ);
        } else {

          // find leader of unbranching/fallsThrough Block and add that
          Stmt leaderStmt = succ;
          while (true) {
            boolean flag = true;
            final List<Stmt> itPreds = graph.predecessors(leaderStmt);
            for (Stmt pred : itPreds) {
              if (pred.fallsThrough() && graph.successors(pred).get(0) == leaderStmt) {
                leaderStmt = pred;
                flag = false;
                break;
              }
            }
            if (flag) {
              break;
            }
          }

          // find a return Stmt inside the target Block
          boolean isReturnBlock = false;
          {
            Stmt itReturnStmt = succ;
            while (true) {
              if (itReturnStmt.fallsThrough()) {
                itReturnStmt = graph.successors(itReturnStmt).get(0);
              } else {
                if (itReturnStmt instanceof JReturnVoidStmt
                    || itReturnStmt instanceof JReturnStmt) {
                  isReturnBlock = true;
                }
                break;
              }
            }
          }

          // remember branching successors
          if (stmt instanceof JGotoStmt) {
            if (isReturnBlock) {
              nestedBlocks.removeFirstOccurrence(leaderStmt);
              otherBlocks.addLast(leaderStmt);
            } else {
              otherBlocks.addFirst(leaderStmt);
            }
          } else if (!nestedBlocks.contains(leaderStmt)) {
            // JSwitchStmt, JIfStmt
            if (isReturnBlock) {
              nestedBlocks.addLast(leaderStmt);
            } else {
              nestedBlocks.addFirst(leaderStmt);
            }
          }
        }
      }
    }

    // prefetch next Stmt
    cachedNextStmt = retrieveNextStmt();
    return stmt;
  }

  @Override
  public boolean hasNext() {
    final boolean hasIteratorMoreElements = cachedNextStmt != null;
    final int returnedSize = returnedNodes.size();
    final int actualSize = graph.getNodes().size();
    if (!hasIteratorMoreElements && returnedSize != actualSize) {
      String info =
          graph.getNodes().stream()
              .filter(n -> !returnedNodes.contains(n))
              .collect(Collectors.toList())
              .toString();
      throw new RuntimeException(
          "There are "
              + (actualSize - returnedSize)
              + " stmts that are not iterated! StmtGraph is not connected from startingStmt!"
              + info);
    }
    return hasIteratorMoreElements;
  }
}
