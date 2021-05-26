package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.graph.iterator.StmtGraphBlockIterator;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.*;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JSwitchStmt;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface StmtGraph extends Iterable<Stmt> {
  @Nullable
  Stmt getStartingStmt();

  /**
   * returns the nodes in this graph in no deterministic order (->Set) to get a linearized flow use
   * iterator().
   */
  @Nonnull
  Set<Stmt> nodes();

  boolean containsNode(@Nonnull Stmt node);

  /**
   * returns the ingoing flows to node as an List with no reliable/specific order and possibly
   * duplicate entries (like successors(Stmt).
   */
  @Nonnull
  List<Stmt> predecessors(@Nonnull Stmt node);

  /** returns the outgoing flows of node as ordered List. The List can have duplicate entries! */
  @Nonnull
  List<Stmt> successors(@Nonnull Stmt node);

  /** returns the amount of ingoing flows into node */
  int inDegree(@Nonnull Stmt node);

  /** returns the amount of flows that start from node */
  int outDegree(@Nonnull Stmt node);

  /** returns the amount of flows with node as source or target. */
  default int degree(@Nonnull Stmt node) {
    return inDegree(node) + outDegree(node);
  }

  /** returns true if there is a flow between source and target */
  boolean hasEdgeConnecting(@Nonnull Stmt source, @Nonnull Stmt target);

  /** returns a list of associated traps */
  @Nonnull
  List<Trap> getTraps();

  /**
   * returns a Collection of Stmts that leave the body (i.e. JReturnVoidStmt, JReturnStmt and
   * JThrowStmt)
   */
  @Nonnull
  default List<Stmt> getTails() {
    return nodes().stream().filter(stmt -> outDegree(stmt) == 0).collect(Collectors.toList());
  }

  /**
   * returns a Collection of all stmts in the graph that don't have an unexceptional ingoing flow or
   * are the starting Stmt.
   */
  @Nonnull
  default Collection<Stmt> getEntrypoints() {
    final ArrayList<Stmt> stmts = new ArrayList<>();
    stmts.add(getStartingStmt());
    getTraps().stream().map(Trap::getHandlerStmt).forEach(stmts::add);
    return stmts;
  }

  /** validates whether the each Stmt has the correct amount of outgoing flows. */
  default void validateStmtConnectionsInGraph() {
    for (Stmt stmt : nodes()) {
      final List<Stmt> successors = successors(stmt);
      final int successorCount = successors.size();

      if (predecessors(stmt).size() == 0) {
        if (!(stmt == getStartingStmt()
            || getTraps().stream()
                .map(Trap::getHandlerStmt)
                .anyMatch(handler -> handler == stmt))) {
          throw new RuntimeException(
              "Stmt '"
                  + stmt
                  + "' which is neither the StartingStmt nor a TrapHandler is missing a predecessor!");
        }
      }

      if (stmt instanceof BranchingStmt) {

        for (Stmt target : successors) {
          if (target == stmt) {
            throw new RuntimeException(stmt + ": a Stmt cannot branch to itself.");
          }
        }

        if (stmt instanceof JSwitchStmt) {
          if (successorCount != ((JSwitchStmt) stmt).getValueCount()) {
            throw new RuntimeException(
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
            throw new RuntimeException(
                stmt + ": Goto must have '1' outgoing flow but has '" + successorCount + "'.");
          }
        }

      } else if (stmt instanceof JReturnStmt
          || stmt instanceof JReturnVoidStmt
          || stmt instanceof JThrowStmt) {
        if (successorCount != 0) {
          throw new RuntimeException(
              stmt + ": must have '0' outgoing flow but has '" + successorCount + "'.");
        }
      } else {
        if (successorCount != 1) {
          throw new RuntimeException(
              stmt + ": must have '1' outgoing flow but has '" + successorCount + "'.");
        }
      }
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
  default List<Stmt> getExtendedBasicBlockPathBetween(@Nonnull Stmt from, @Nonnull Stmt to) {

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

    int psiMax = (outDegree(pathStack.get(0)));
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

      Stmt betweenStmt = (succs.get(p));

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
  @Nonnull
  default Iterator<Stmt> iterator() {
    return new StmtGraphBlockIterator(this, getTraps());
  }
}
