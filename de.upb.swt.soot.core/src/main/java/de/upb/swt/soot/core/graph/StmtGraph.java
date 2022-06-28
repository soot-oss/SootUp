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
  public abstract List<Trap> getTraps();

  /**
   * returns a Collection of Stmts that leave the body (i.e. JReturnVoidStmt, JReturnStmt and
   * JThrowStmt)
   */
  @Nonnull
  public List<Stmt> getTails() {
    return nodes().stream()
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
      final String urlToWebeditor = GraphVizExporter.createUrlToWebeditor(this);
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

  /**
   * you're lazy - create your algorithm modifications more clean/precise for better performance ;-)
   *
   * <p>public boolean purgeUnconnectedEdges() { // TODO: implement pruning graph to remove
   * unconnected edges throw new UnsupportedOperationException("not implemented yet"); }
   */
  @Override
  @Nonnull
  public abstract Iterator<Stmt> iterator();
}
