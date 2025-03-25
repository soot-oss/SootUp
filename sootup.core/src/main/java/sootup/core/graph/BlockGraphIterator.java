package sootup.core.graph;

import java.util.*;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.jimple.common.stmt.JGotoStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.ClassType;
import sootup.core.util.DotExporter;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2024 Sahil Agichani
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

/** Iterates over the blocks */
public class BlockGraphIterator implements Iterator<BasicBlock<?>> {

  private final StmtGraph stmtGraph;
  @NonNull private final ArrayDeque<BasicBlock<?>> trapHandlerBlocks = new ArrayDeque<>();

  @NonNull private final ArrayDeque<BasicBlock<?>> nestedBlocks = new ArrayDeque<>();
  @NonNull private final ArrayDeque<BasicBlock<?>> otherBlocks = new ArrayDeque<>();
  @NonNull private final Set<BasicBlock<?>> iteratedBlocks;

  public BlockGraphIterator(StmtGraph stmtGraph) {
    this.stmtGraph = stmtGraph;
    final Collection<? extends BasicBlock<?>> blocks = stmtGraph.getBlocks();
    iteratedBlocks = new LinkedHashSet<>(blocks.size(), 1);
    Stmt startingStmt = stmtGraph.getStartingStmt();
    if (startingStmt != null) {
      final BasicBlock<?> startingBlock = stmtGraph.getStartingStmtBlock();
      updateFollowingBlocks(startingBlock);
      nestedBlocks.addFirst(startingBlock);
    }
  }

  @Nullable
  private BasicBlock<?> retrieveNextBlock() {
    BasicBlock<?> nextBlock;
    do {
      if (!nestedBlocks.isEmpty()) {
        nextBlock = nestedBlocks.pollFirst();
      } else if (!trapHandlerBlocks.isEmpty()) {
        nextBlock = trapHandlerBlocks.pollFirst();
      } else if (!otherBlocks.isEmpty()) {
        nextBlock = otherBlocks.pollFirst();
      } else {
        Collection<? extends BasicBlock<?>> blocks = stmtGraph.getBlocks();
        if (iteratedBlocks.size() < blocks.size()) {
          // graph is not connected! iterate/append all not connected blocks at the end in no
          // particular order.
          for (BasicBlock<?> block : blocks) {
            if (!iteratedBlocks.contains(block)) {
              nestedBlocks.addLast(block);
            }
          }
          if (!nestedBlocks.isEmpty()) {
            return nestedBlocks.pollFirst();
          }
        }

        return null;
      }

      // skip retrieved nextBlock if its already returned
    } while (iteratedBlocks.contains(nextBlock));
    return nextBlock;
  }

  @Override
  @NonNull
  public BasicBlock<?> next() {
    BasicBlock<?> currentBlock = retrieveNextBlock();
    if (currentBlock == null) {
      throw new NoSuchElementException("Iterator has no more Blocks.");
    }
    updateFollowingBlocks(currentBlock);
    iteratedBlocks.add(currentBlock);
    return currentBlock;
  }

  private void updateFollowingBlocks(BasicBlock<?> currentBlock) {
    // collect traps
    final Stmt tailStmt = currentBlock.getTail();
    for (Map.Entry<? extends ClassType, ? extends BasicBlock<?>> entry :
        currentBlock.getExceptionalSuccessors().entrySet()) {
      BasicBlock<?> trapHandlerBlock = entry.getValue();
      trapHandlerBlocks.addLast(trapHandlerBlock);
      nestedBlocks.addFirst(trapHandlerBlock);
    }

    final List<? extends BasicBlock<?>> successors = currentBlock.getSuccessors();

    for (int i = successors.size() - 1; i >= 0; i--) {
      if (i == 0 && tailStmt.fallsThrough()) {
        // non-branching successors i.e. not a BranchingStmt or is the first successor (i.e. its
        // false successor) of
        // JIfStmt
        nestedBlocks.addFirst(successors.get(0));
      } else {

        // create the longest FallsThroughStmt sequence possible
        final BasicBlock<?> successorBlock = successors.get(i);
        BasicBlock<?> leaderOfFallsthroughBlocks = successorBlock;
        while (true) {
          final List<? extends BasicBlock<?>> itPreds =
              leaderOfFallsthroughBlocks.getPredecessors();

          BasicBlock<?> finalLeaderOfFallsthroughBlocks = leaderOfFallsthroughBlocks;
          final Optional<? extends BasicBlock<?>> fallsthroughPredOpt =
              itPreds.stream()
                  .filter(
                      b ->
                          b.getTail().fallsThrough()
                              && b.getSuccessors().get(0) == finalLeaderOfFallsthroughBlocks)
                  .findAny();
          if (!fallsthroughPredOpt.isPresent()) {
            break;
          }
          BasicBlock<?> predecessorBlock = fallsthroughPredOpt.get();
          if (predecessorBlock.getTail().fallsThrough()
              && predecessorBlock.getSuccessors().get(0) == leaderOfFallsthroughBlocks) {
            leaderOfFallsthroughBlocks = predecessorBlock;
          } else {
            break;
          }
        }

        // find a return Stmt inside the current Block
        Stmt succTailStmt = successorBlock.getTail();
        boolean hasNoSuccessorStmts = succTailStmt.getExpectedSuccessorCount() == 0;
        boolean isExceptionFree = successorBlock.getExceptionalSuccessors().isEmpty();

        boolean isLastStmtCandidate = hasNoSuccessorStmts && isExceptionFree;
        // remember branching successors
        if (tailStmt instanceof JGotoStmt) {
          if (isLastStmtCandidate) {
            nestedBlocks.removeFirstOccurrence(currentBlock);
            otherBlocks.addLast(leaderOfFallsthroughBlocks);
          } else {
            otherBlocks.addFirst(leaderOfFallsthroughBlocks);
          }
        } else if (!nestedBlocks.contains(leaderOfFallsthroughBlocks)) {
          // JSwitchStmt, JIfStmt
          if (isLastStmtCandidate) {
            nestedBlocks.addLast(leaderOfFallsthroughBlocks);
          } else {
            nestedBlocks.addFirst(leaderOfFallsthroughBlocks);
          }
        }
      }
    }
  }

  @Override
  public boolean hasNext() {
    final boolean hasIteratorMoreElements;
    BasicBlock<?> b = retrieveNextBlock();
    if (b != null) {
      // reinsert at FIRST position -&gt; not great for performance - but easier handling in
      // next()
      nestedBlocks.addFirst(b);
      hasIteratorMoreElements = true;
    } else {
      hasIteratorMoreElements = false;
    }

    // "assertion" that all elements are iterated
    if (!hasIteratorMoreElements) {
      final int returnedSize = iteratedBlocks.size();
      final Collection<? extends BasicBlock<?>> blocks = stmtGraph.getBlocks();
      final int actualSize = blocks.size();
      if (returnedSize != actualSize) {
        String info =
            blocks.stream()
                .filter(n -> !iteratedBlocks.contains(n))
                .map(BasicBlock::getStmts)
                .collect(Collectors.toList())
                .toString();
        throw new IllegalStateException(
            "There are "
                + (actualSize - returnedSize)
                + " Blocks that are not iterated! i.e. the StmtGraph is not connected from its startingStmt!"
                + info
                + DotExporter.createUrlToWebeditor(stmtGraph));
      }
    }
    return hasIteratorMoreElements;
  }
}
