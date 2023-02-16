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
import com.google.common.collect.Iterators;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import sootup.core.jimple.basic.Trap;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;
import sootup.core.types.ClassType;
import sootup.core.util.DotExporter;
import sootup.core.util.EscapedWriter;
import sootup.core.util.printer.JimplePrinter;

/**
 * Interface for control flow graphs on Jimple Stmts. A StmtGraph is directed and connected (except
 * for traphandlers - those are not connected to the unexceptional flow via StmtGraph). Its directed
 * edges represent flows between Stmts. If the edge starts in a branching Stmt there is an edge for
 * each flow to the target Stmt. This can include duplicate flows to the same target e.g. for
 * JSwitchStmt, so that every label has its own flow to a target.
 *
 * <p>THe StmtGraph structure keeps the edge insertion order of each node to store information about
 * successor stmts in its edges for Branching. Ordered edges are necessary because we want to
 * associate the i-th item with the i-th branch case of a {@link BranchingStmt}. In a valid
 * StmtGraph it is not allowed to have unconnected Nodes.
 *
 * <pre>
 *  Stmt stmt1, stmt2;
 *  ...
 *  MutableStmtGraph graph = new MutableBlockStmtGraph();
 *  graph.setEntryPoint(stmt1);
 *  graph.addNode(stmt1);
 *  graph.addNode(stmt2);
 *  graph.putEdge(stmt1, stmt2);
 * </pre>
 *
 * @author Markus Schmidt
 */
public abstract class StmtGraph<V extends BasicBlock<V>> implements Iterable<Stmt> {

  public abstract Stmt getStartingStmt();

  public abstract BasicBlock<?> getStartingStmtBlock();
  /**
   * returns the nodes in this graph in a non-deterministic order (-&gt;Set) to get the nodes in
   * linearized, ordered manner use iterator() or getStmts.
   */
  @Nonnull
  public abstract Collection<Stmt> getNodes();

  public List<Stmt> getStmts() {
    final ArrayList<Stmt> res = new ArrayList<>();
    Iterators.addAll(res, iterator());
    return res;
  }

  @Nonnull
  public abstract Collection<? extends BasicBlock<?>> getBlocks();

  @Nonnull
  public abstract List<? extends BasicBlock<?>> getBlocksSorted();

  public Iterator<BasicBlock<?>> getBlockIterator() {
    return new BlockGraphIterator();
  }

  public abstract BasicBlock<?> getBlockOf(@Nonnull Stmt stmt);

  public abstract boolean containsNode(@Nonnull Stmt node);

  /**
   * returns the ingoing flows to node as an List with no reliable/specific order and possibly
   * duplicate entries i.e. if a JSwitchStmt has multiple cases that brnach to `node`
   */
  @Nonnull
  public abstract List<Stmt> predecessors(@Nonnull Stmt node);

  /** it is possible to reach traphandlers through inline code i.e. without any exceptional flow */
  @Nonnull
  public abstract List<Stmt> exceptionalPredecessors(@Nonnull Stmt node);

  /** returns the outgoing flows of node as ordered List. The List can have duplicate entries! */
  @Nonnull
  public abstract List<Stmt> successors(@Nonnull Stmt node);

  @Nonnull
  public abstract Map<ClassType, Stmt> exceptionalSuccessors(@Nonnull Stmt node);

  /**
   * Collects all successors i.e. unexceptional and exceptional successors of a given stmt into a
   * list.
   *
   * @param stmt in the given graph
   * @return a list containing the unexceptional+exceptional successors of the given stmt
   */
  @Nonnull
  public List<Stmt> getAllSuccessors(@Nonnull Stmt stmt) {
    final List<Stmt> successors = successors(stmt);
    final Map<ClassType, Stmt> exSuccessors = exceptionalSuccessors(stmt);
    List<Stmt> allSuccessors = new ArrayList<>(successors.size() + exSuccessors.size());
    allSuccessors.addAll(successors);
    allSuccessors.addAll(exSuccessors.values());
    return allSuccessors;
  }

  /** returns the amount of ingoing flows into node */
  public abstract int inDegree(@Nonnull Stmt node);

  /** returns the amount of flows that start from node */
  public abstract int outDegree(@Nonnull Stmt node);

  /** returns the amount of flows with node as source or target. */
  public int degree(@Nonnull Stmt node) {
    return inDegree(node) + outDegree(node);
  }

  /**
   * returns true if there is a flow between source and target throws an Exception if at least one
   * of the parameters is not contained in the graph.
   */
  public abstract boolean hasEdgeConnecting(@Nonnull Stmt source, @Nonnull Stmt target);

  /** returns a list of associated traps */
  @Deprecated
  public abstract List<Trap> getTraps();

  /**
   * returns a Collection of Stmts that leave the body (i.e. JReturnVoidStmt, JReturnStmt and
   * JThrowStmt)
   */
  @Nonnull
  public List<Stmt> getTails() {
    return getNodes().stream()
        .filter(stmt -> stmt.getExpectedSuccessorCount() == 0)
        .collect(Collectors.toList());
  }

  /**
   * returns a Collection of all stmts in the graph that don't have an unexceptional ingoing flow or
   * are the starting Stmt.
   */
  @Nonnull
  public Collection<Stmt> getEntrypoints() {
    final ArrayList<Stmt> stmts = new ArrayList<>();
    stmts.add(getStartingStmt());
    // TODO: [ms] memory/performance: instead of gettraps(): iterate through all stmts and add
    // startingStmt+@caughtexception/predecessors().size() == 0?
    getTraps().stream().map(Trap::getHandlerStmt).forEach(stmts::add);
    return stmts;
  }

  /** validates whether the each Stmt has the correct amount of outgoing flows. */
  public void validateStmtConnectionsInGraph() {
    try {

      for (Stmt stmt : getNodes()) {
        final List<Stmt> successors = successors(stmt);
        final int successorCount = successors.size();

        if (predecessors(stmt).size() == 0) {
          if (!(stmt == getStartingStmt()
              || getTraps().stream()
                  .map(Trap::getHandlerStmt)
                  .anyMatch(handler -> handler == stmt))) {
            throw new IllegalStateException(
                "Stmt '"
                    + stmt
                    + "' which is neither the StartingStmt nor a TrapHandler is missing a predecessor!");
          }
        }

        if (stmt instanceof BranchingStmt) {

          for (Stmt target : successors) {
            if (target == stmt) {
              throw new IllegalStateException(stmt + ": a Stmt cannot branch to itself.");
            }
          }

          if (stmt instanceof JSwitchStmt) {
            if (successorCount != ((JSwitchStmt) stmt).getValueCount()) {
              throw new IllegalStateException(
                  stmt
                      + ": size of outgoing flows (i.e. "
                      + successorCount
                      + ") does not match the amount of switch statements case labels (i.e. "
                      + ((JSwitchStmt) stmt).getValueCount()
                      + ").");
            }
          } else if (stmt instanceof JIfStmt) {
            if (successorCount != 2) {
              throw new IllegalStateException(
                  stmt + ": If must have '2' outgoing flow but has '" + successorCount + "'.");
            }
          } else if (stmt instanceof JGotoStmt) {
            if (successorCount != 1) {
              throw new IllegalStateException(
                  stmt + ": Goto must have '1' outgoing flow but has '" + successorCount + "'.");
            }
          }

        } else if (stmt instanceof JReturnStmt
            || stmt instanceof JReturnVoidStmt
            || stmt instanceof JThrowStmt) {
          if (successorCount != 0) {
            throw new IllegalStateException(
                stmt + ": must have '0' outgoing flow but has '" + successorCount + "'.");
          }
        } else {
          if (successorCount != 1) {
            throw new IllegalStateException(
                stmt + ": must have '1' outgoing flow but has '" + successorCount + "'.");
          }
        }
      }

    } catch (Exception e) {
      final String urlToWebeditor = DotExporter.createUrlToWebeditor(this);
      throw new IllegalStateException("visualize invalid StmtGraph: " + urlToWebeditor, e);
    }
  }

  /**
   * Look for a path in graph, from def to use. This path has to lie inside an extended basic block
   * (and this property implies uniqueness.). The path returned includes from and to.
   *
   * @param from start point for the path.
   * @param to end point for the path.
   * @return null if there is no such path.
   */
  @Nullable
  public List<Stmt> getExtendedBasicBlockPathBetween(@Nonnull Stmt from, @Nonnull Stmt to) {

    // if this holds, we're doomed to failure!!!
    if (inDegree(to) > 1) {
      return null;
    }

    // pathStack := list of succs lists
    // pathStackIndex := last visited index in pathStack
    List<Stmt> pathStack = new ArrayList<>();
    List<Integer> pathStackIndex = new ArrayList<>();

    pathStack.add(from);
    pathStackIndex.add(0);

    int psiMax = outDegree(pathStack.get(0));
    int level = 0;
    while (pathStackIndex.get(0) != psiMax) {
      int p = pathStackIndex.get(level);

      List<Stmt> succs = successors((pathStack.get(level)));
      if (p >= succs.size()) {
        // no more succs - backtrack to previous level.

        pathStack.remove(level);
        pathStackIndex.remove(level);

        level--;
        int q = pathStackIndex.get(level);
        pathStackIndex.set(level, q + 1);
        continue;
      }

      Stmt betweenStmt = succs.get(p);

      // we win!
      if (betweenStmt == to) {
        pathStack.add(to);
        return pathStack;
      }

      // check preds of betweenStmt to see if we should visit its kids.
      if (inDegree(betweenStmt) > 1) {
        pathStackIndex.set(level, p + 1);
        continue;
      }

      // visit kids of betweenStmt.
      level++;
      pathStackIndex.add(0);
      pathStack.add(betweenStmt);
    }
    return null;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }

    if (!(o instanceof StmtGraph)) {
      return false;
    }
    StmtGraph<?> otherGraph = (StmtGraph<?>) o;

    if (getStartingStmt() != otherGraph.getStartingStmt()) {
      return false;
    }

    Collection<Stmt> nodes = getNodes();
    final Collection<Stmt> otherNodes = otherGraph.getNodes();
    if (nodes.size() != otherNodes.size()) {
      return false;
    }

    if (!getTraps().equals(otherGraph.getTraps())) {
      return false;
    }

    for (Stmt node : nodes) {
      if (!otherNodes.contains(node)) {
        return false;
      }
      final List<Stmt> successors = successors(node);
      final List<Stmt> otherSuccessors = otherGraph.successors(node);
      if (!successors.equals(otherSuccessors)) {
        return false;
      }
    }

    return true;
  }

  @Override
  @Nonnull
  public Iterator<Stmt> iterator() {
    return new BlockStmtGraphIterator();
  }

  public List<Stmt> getBranchTargetsOf(BranchingStmt fromStmt) {
    final List<Stmt> successors = successors(fromStmt);
    if (fromStmt instanceof JIfStmt) {
      // remove the first successor as if its a fallsthrough stmt and not a branch target
      return Collections.singletonList(successors.get(1));
    }
    return successors;
  }

  public boolean isStmtBranchTarget(@Nonnull Stmt targetStmt) {
    final List<Stmt> predecessors = predecessors(targetStmt);
    if (predecessors.size() > 1) {
      // join node i.e. at least one is a branch
      return true;
    }

    final Iterator<Stmt> iterator = predecessors.iterator();
    if (iterator.hasNext()) {
      Stmt pred = iterator.next();
      if (pred.branches()) {
        if (pred instanceof JIfStmt) {
          // [ms] bounds are validated in Body
          return getBranchTargetsOf((JIfStmt) pred).get(0) == targetStmt;
        }
        return true;
      }
    }

    return false;
  }

  /** Iterates the Stmts according to the jimple output order. */
  private class BlockStmtGraphIterator implements Iterator<Stmt> {

    private final BlockGraphIterator blockIt;
    @Nonnull private Iterator<Stmt> currentBlockIt = Collections.emptyIterator();

    public BlockStmtGraphIterator() {
      this(new BlockGraphIterator());
    }

    public BlockStmtGraphIterator(@Nonnull BlockGraphIterator blockIterator) {
      blockIt = blockIterator;
    }

    @Override
    public boolean hasNext() {
      // hint: a BasicBlock has at least 1 Stmt or should not be in a StmtGraph!
      return currentBlockIt.hasNext() || blockIt.hasNext();
    }

    @Override
    public Stmt next() {
      if (!currentBlockIt.hasNext()) {
        if (!blockIt.hasNext()) {
          throw new NoSuchElementException("Iterator has no more Stmts.");
        }
        BasicBlock<?> currentBlock = blockIt.next();
        currentBlockIt = currentBlock.getStmts().iterator();
      }
      return currentBlockIt.next();
    }
  }

  /** Iterates over the Blocks and collects/aggregates Trap information */
  public class BlockGraphIteratorAndTrapAggregator extends BlockGraphIterator {

    @Nonnull private final List<Trap> collectedTraps = new ArrayList<>();

    Map<ClassType, Stmt> trapStarts = new HashMap<>();
    BasicBlock<?> lastIteratedBlock; // dummy value to remove n-1 unnecessary null-checks

    /*
     * @param dummyBlock is just an empty instantiation of type V - as neither BasicBlock nor V instantiable we need a concrete object from the using subclass itclass.
     * */
    public BlockGraphIteratorAndTrapAggregator(V dummyBlock) {
      super();
      lastIteratedBlock = dummyBlock;
    }

    @Nonnull
    @Override
    public BasicBlock<?> next() {
      final BasicBlock<?> block = super.next();

      final Map<? extends ClassType, ? extends BasicBlock<?>> currentBlocksExceptions =
          block.getExceptionalSuccessors();
      final Map<? extends ClassType, ? extends BasicBlock<?>> lastBlocksExceptions =
          lastIteratedBlock.getExceptionalSuccessors();

      // former trap info is not in the current blocks info -&gt; add it to the trap collection
      lastBlocksExceptions.forEach(
          (type, trapHandlerBlock) -> {
            if (trapHandlerBlock != block.getExceptionalSuccessors().get(type)) {
              final Stmt trapBeginStmt = trapStarts.remove(type);
              if (trapBeginStmt == null) {
                throw new IllegalStateException("Trap start for '" + type + "' is not in the Map!");
              }
              // trapend is exclusive!
              collectedTraps.add(
                  new Trap(type, trapBeginStmt, block.getHead(), trapHandlerBlock.getHead()));
            }
          });

      // is there a new trap in the current block -&gt; add it to currentTraps
      block
          .getExceptionalSuccessors()
          .forEach(
              (type, trapHandlerBlock) -> {
                if (trapHandlerBlock != lastBlocksExceptions.get(type)) {
                  trapStarts.put(type, block.getHead());
                }
              });

      lastIteratedBlock = block;
      return block;
    }

    /**
     * for jimple serialization -&gt; this is the info for the end of the method contains only
     * valid/useful information when all stmts are iterated i.e. hasNext() == false!
     *
     * @return List of Traps
     */
    public List<Trap> getTraps() {
      // aggregate dangling trap data
      trapStarts.forEach(
          (type, trapStart) -> {
            final BasicBlock<?> trapHandler =
                lastIteratedBlock.getExceptionalSuccessors().get(type);
            if (trapHandler == null) {
              throw new IllegalStateException(
                  "No matching Trap info found for '"
                      + type
                      + "' in ExceptionalSucessors() of the last iterated Block!");
            }
            collectedTraps.add(
                new Trap(type, trapStart, lastIteratedBlock.getTail(), trapHandler.getTail()));
          });
      trapStarts.clear();
      return collectedTraps;
    }
  }

  /** Iterates over the blocks */
  public class BlockGraphIterator implements Iterator<BasicBlock<?>> {

    @Nonnull private final ArrayDeque<BasicBlock<?>> trapHandlerBlocks = new ArrayDeque<>();

    @Nonnull private final ArrayDeque<BasicBlock<?>> nestedBlocks = new ArrayDeque<>();
    @Nonnull private final ArrayDeque<BasicBlock<?>> otherBlocks = new ArrayDeque<>();
    @Nonnull private final Set<BasicBlock<?>> iteratedBlocks;

    public BlockGraphIterator() {
      final Collection<? extends BasicBlock<?>> blocks = getBlocks();
      iteratedBlocks = new HashSet<>(blocks.size(), 1);
      Stmt startingStmt = getStartingStmt();
      if (startingStmt != null) {
        final BasicBlock<?> startingBlock = getStartingStmtBlock();
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
          Collection<? extends BasicBlock<?>> blocks = getBlocks();
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
    @Nonnull
    public BasicBlock<?> next() {
      BasicBlock<?> currentBlock = retrieveNextBlock();
      if (currentBlock == null) {
        throw new NoSuchElementException("Iterator has no more Blocks.");
      }
      updateFollowingBlocks(currentBlock);
      iteratedBlocks.add(currentBlock);
      return currentBlock;
    }

    //
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

          // create the most biggest fallsthrough sequence of basicblocks as possible -&gt; go to
          // the
          // top until
          // predecessor is not a fallsthrough stmt anymore and then the iterator will iterate
          // from there.
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
          boolean isReturnBlock =
              succTailStmt instanceof JReturnVoidStmt || succTailStmt instanceof JReturnStmt;

          // remember branching successors
          if (tailStmt instanceof JGotoStmt) {
            if (isReturnBlock) {
              nestedBlocks.removeFirstOccurrence(currentBlock);
              otherBlocks.addLast(leaderOfFallsthroughBlocks);
            } else {
              otherBlocks.addFirst(leaderOfFallsthroughBlocks);
            }
          } else if (!nestedBlocks.contains(leaderOfFallsthroughBlocks)) {
            // JSwitchStmt, JIfStmt
            if (isReturnBlock) {
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
        final Collection<? extends BasicBlock<?>> blocks = getBlocks();
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
                  + DotExporter.createUrlToWebeditor(StmtGraph.this));
        }
      }
      return hasIteratorMoreElements;
    }
  }

  /**
   * Returns the result of iterating through all Stmts in this body. All Stmts thus found are
   * returned. Branching Stmts and statements which use PhiExpr will have Stmts; a Stmt contains a
   * Stmt that is either a target of a branch or is being used as a pointer to the end of a CFG
   * block.
   *
   * <p>This method was typically used for pointer patching, e.g. when the unit chain is cloned.
   *
   * @return A collection of all the Stmts that are targets of a BranchingStmt
   */
  @Nonnull
  public Collection<Stmt> getLabeledStmts() {
    Set<Stmt> stmtList = new HashSet<>();
    for (Stmt stmt : getNodes()) {
      if (stmt instanceof BranchingStmt) {
        if (stmt instanceof JIfStmt) {
          // [ms] bounds are validated in Body
          stmtList.add(getBranchTargetsOf((JIfStmt) stmt).get(0));
        } else if (stmt instanceof JGotoStmt) {
          // [ms] bounds are validated in Body if its a valid StmtGraph
          stmtList.add(getBranchTargetsOf((JGotoStmt) stmt).get(0));
        } else if (stmt instanceof JSwitchStmt) {
          stmtList.addAll(getBranchTargetsOf((BranchingStmt) stmt));
        }
      }
    }

    for (Trap trap : getTraps()) {
      stmtList.add(trap.getBeginStmt());
      stmtList.add(trap.getEndStmt());
      stmtList.add(trap.getHandlerStmt());
    }

    return stmtList;
  }

  @Override
  public String toString() {
    StringWriter writer = new StringWriter();
    try (PrintWriter writerOut = new PrintWriter(new EscapedWriter(writer))) {
      new JimplePrinter().printTo(this, writerOut);
    }
    return writer.toString();
  }
}
