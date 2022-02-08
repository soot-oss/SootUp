package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.*;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JSwitchStmt;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.util.GraphVizExporter;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface for control flow graphs on Jimple Stmts. A StmtGraph is directed and connected (except
 * for traphandlers - those are not connected to the unexceptional flow via StmtGraph). Its directed
 * edges represent flows between Stmts. If the edge starts in a branching Stmt there is an edge for
 * each flow to the target Stmt. This can include duplicate flows to the same target e.g. for
 * JSwitchStmt, so that every label has its own flow to a target.
 *
 * @author Markus Schmidt
 */
public abstract class StmtGraph<V extends BasicBlock<V>> implements Iterable<Stmt> {

  @Deprecated // hint: please use information for exceptional flows from BasicBlocks
  protected List<Trap> traps;

  public abstract Stmt getStartingStmt();

  public V getStartingStmtBlock() {
    return getBlockOf(getStartingStmt());
  }

  /**
   * returns the nodes in this graph in no deterministic order (->Set) to get a linearized flow use
   * iterator().
   */
  @Nonnull
  public abstract Collection<Stmt> nodes();

  @Nonnull
  public abstract List<V> getBlocks();

  public abstract V getBlockOf(@Nonnull Stmt stmt);

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

  /** returns true if there is a flow between source and target */
  public abstract boolean hasEdgeConnecting(@Nonnull Stmt source, @Nonnull Stmt target);

  /** returns a list of associated traps */
  @Nonnull
  @Deprecated
  public List<Trap> getTraps() {
    /* ms: makes no sense without stmt/block information */
    return traps;
  }

  /**
   * returns a Collection of Stmts that leave the body (i.e. JReturnVoidStmt, JReturnStmt and
   * JThrowStmt)
   */
  @Nonnull
  public List<Stmt> getTails() {
    return nodes().stream().filter(stmt -> outDegree(stmt) == 0).collect(Collectors.toList());
  }

  /**
   * returns a Collection of all stmts in the graph that don't have an unexceptional ingoing flow or
   * are the starting Stmt.
   */
  @Nonnull
  public Collection<Stmt> getEntrypoints() {
    final ArrayList<Stmt> stmts = new ArrayList<>();
    stmts.add(getStartingStmt());
    getTraps().stream().map(Trap::getHandlerStmt).forEach(stmts::add);
    return stmts;
  }

  /** validates whether the each Stmt has the correct amount of outgoing flows. */
  public void validateStmtConnectionsInGraph() {
    try {

      for (Stmt stmt : nodes()) {
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
                  stmt + ": must have '2' outgoing flow but has '" + successorCount + "'.");
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
      throw new IllegalStateException(
          "visualize invalid StmtGraph: " + GraphVizExporter.createUrlToWebeditor(this), e);
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
    StmtGraph otherGraph = (StmtGraph) o;

    if (getStartingStmt() != otherGraph.getStartingStmt()) {
      return false;
    }

    Collection<Stmt> nodes = nodes();
    final Collection<Stmt> otherNodes = otherGraph.nodes();
    if (nodes.size() != otherNodes.size()) {
      return false;
    }

    if (!getTraps().equals(otherGraph.getTraps())) {
      return false;
    }

    for (Stmt node : nodes) {
      if (!otherNodes.contains(node) || !successors(node).equals(otherGraph.successors(node))) {
        return false;
      }
    }

    return true;
  }

  @Nonnull
  public Iterator<Stmt> iterateUntilBranching(@Nonnull Stmt start) {
    return new Iterator<Stmt>() {
      Stmt nextElement = start;

      @Override
      public boolean hasNext() {
        return !nextElement.branches();
      }

      @Override
      public Stmt next() {
        final Stmt tmpElement = nextElement;
        final List<Stmt> successors = successors(nextElement);
        if (successors.size() > 0) {
          nextElement = successors.get(0);
        }
        return tmpElement;
      }
    };
  }

  /**
   * you're lazy - create your algorithm modifications more clean/precise for better performance ;-)
   */
  public boolean purgeUnconnectedEdges() {
    // TODO: implement pruning graph to remove unconnected edges
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  @Nonnull
  public Iterator<Stmt> iterator() {
    // TODO: remove comment   return new StmtGraphBlockIterator(this, Collections.emptyList());
    return new BlockStmtGraphIterator(this);
  }

  // FIXME: adapt to iterate over blocks and adapt traps at the end so that the list is not
  // enormously big i.e. merge same traps with neighbouring blocks
  // assumption: a Block has at least 1 Stmt
  private static class BlockStmtGraphIterator implements Iterator<Stmt> {

    @Nonnull private final StmtGraph<? extends BasicBlock<?>> graph;

    @Nonnull
    private final ArrayDeque<Map.Entry<ClassType, BasicBlock<?>>> traps = new ArrayDeque<>();

    @Nonnull private final ArrayDeque<BasicBlock<?>> nestedBlocks = new ArrayDeque<>();
    @Nonnull private final ArrayDeque<BasicBlock<?>> otherBlocks = new ArrayDeque<>();

    // caching the next Stmt to implement a simple hasNext() and skipping already returned Stmts
    @Nonnull private final Set<BasicBlock<?>> iteratedBlocks;
    @Nonnull private Iterator<Stmt> currentBlockIt;
    @Nullable private BasicBlock<?> currentBlock;
    private final List<Trap> collectedTraps = new ArrayList<>();

    public BlockStmtGraphIterator(@Nonnull StmtGraph<? extends BasicBlock<?>> graph) {
      this.graph = graph;
      final List<? extends BasicBlock<?>> blocks = graph.getBlocks();
      iteratedBlocks = new HashSet<>(blocks.size(), 1);
      Stmt startingStmt = graph.getStartingStmt();
      if (startingStmt != null) {
        final BasicBlock<?> startingBlock = graph.getStartingStmtBlock();
        iteratedBlocks.add(startingBlock);
        currentBlockIt = startingBlock.getStmts().iterator();
        currentBlock = startingBlock;
      } else {
        currentBlockIt = Collections.emptyIterator();
        currentBlock = null;
      }
    }

    @Nullable
    private BasicBlock<?> retrieveNextBlock() {
      BasicBlock<?> nextBlock;
      do {
        if (!nestedBlocks.isEmpty()) {
          nextBlock = nestedBlocks.pollFirst();
        } else if (!traps.isEmpty()) {
          nextBlock = traps.pollFirst().getValue();
        } else if (!otherBlocks.isEmpty()) {
          nextBlock = otherBlocks.pollFirst();
        } else {
          return null;
        }

        // skip retrieved nextBlock if its already returned
      } while (iteratedBlocks.contains(nextBlock));
      iteratedBlocks.add(nextBlock);

      return nextBlock;
    }

    @Override
    @Nonnull
    public Stmt next() {

      if (!currentBlockIt.hasNext()) {
        currentBlock = retrieveNextBlock();
        if (currentBlock == null) {
          throw new NoSuchElementException("Iterator has no more Stmts.");
        }
        currentBlockIt = currentBlock.getStmts().iterator();

        // collect traps
        final Stmt tailStmt = currentBlock.getTail();
        currentBlock
            .getExceptionalSuccessors()
            .forEach(
                (k, v) -> {
                  collectedTraps.add(new Trap(k, currentBlock.getHead(), tailStmt, v.getHead()));
                  // integrate trapHandler Block into 'queue'
                  // if (!iteratedBlocks.contains(v)) {
                  nestedBlocks.addFirst(v);
                  // }
                });

        final List<? extends BasicBlock<?>> successors = currentBlock.getSuccessors();

        for (int i = successors.size() - 1; i >= 0; i--) {
          if (i == 0 && tailStmt.fallsThrough()) {
            // non-branching successors i.e. not a BranchingStmt or is the first successor of
            // JIfStmt
            nestedBlocks.addFirst(successors.get(i));
          } else {

            // create the most unbranched block from basicblocks as possible
            BasicBlock<?> leaderOfUnbranchedBlocks = successors.get(0);
            while (true) {
              boolean flag = true;
              final List<? extends BasicBlock<?>> itPreds =
                  leaderOfUnbranchedBlocks.getPredecessors();
              for (BasicBlock<?> pred : itPreds) {
                if (pred.getTail().fallsThrough()
                    && pred.getSuccessors().get(0) == leaderOfUnbranchedBlocks) {
                  leaderOfUnbranchedBlocks = pred;
                  flag = false;
                  break;
                }
              }
              if (flag) {
                break;
              }
            }

            // find a return Stmt inside the current Block
            Stmt succTailStmt = successors.get(i).getTail();
            boolean isReturnBlock =
                succTailStmt instanceof JReturnVoidStmt || succTailStmt instanceof JReturnStmt;

            // remember branching successors
            if (tailStmt instanceof JGotoStmt) {
              if (isReturnBlock) {
                nestedBlocks.removeFirstOccurrence(currentBlock.getHead());
                otherBlocks.addLast(leaderOfUnbranchedBlocks);
              } else {
                otherBlocks.addFirst(leaderOfUnbranchedBlocks);
              }
            } else if (!nestedBlocks.contains(leaderOfUnbranchedBlocks)) {
              // JSwitchStmt, JIfStmt
              if (isReturnBlock) {
                nestedBlocks.addLast(leaderOfUnbranchedBlocks);
              } else {
                nestedBlocks.addFirst(leaderOfUnbranchedBlocks);
              }
            }
          }
        }
      }

      return currentBlockIt.next();
    }

    @Override
    public boolean hasNext() {
      final boolean hasIteratorMoreElements;
      if (currentBlockIt.hasNext()) {
        hasIteratorMoreElements = true;
      } else {
        BasicBlock<?> b = retrieveNextBlock();
        if (b != null) {
          // reinsert at FIRST position -> not great for performance! but easier handling in next()
          nestedBlocks.addFirst(b);
          hasIteratorMoreElements = true;
        } else {
          hasIteratorMoreElements = false;
        }
      }

      if (!hasIteratorMoreElements) {
        final int returnedSize = iteratedBlocks.size();
        final int actualSize = graph.getBlocks().size();
        if (returnedSize != actualSize) {
          String info =
              graph.getBlocks().stream()
                  .filter(n -> !iteratedBlocks.contains(n))
                  .map(BasicBlock::getStmts)
                  .collect(Collectors.toList())
                  .toString();
          throw new IllegalStateException(
              "There are "
                  + (actualSize - returnedSize)
                  + " Blocks (and their containing Stmts) that are not iterated! The StmtGraph is not connected from its startingStmt!"
                  + info);
        }
      }
      return hasIteratorMoreElements;
    }

    // for jimple serialization -> this is the info for the end of the method
    public List<Trap> getTraps() {
      return collectedTraps;
    }
  }
}
