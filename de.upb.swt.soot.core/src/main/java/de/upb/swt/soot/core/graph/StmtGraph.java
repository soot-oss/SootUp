package de.upb.swt.soot.core.graph;
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
import de.upb.swt.soot.core.graph.iterator.StmtGraphBlockIterator;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.*;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JSwitchStmt;
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
public abstract class StmtGraph implements Iterable<Stmt> {

  @Nullable
  public abstract Stmt getStartingStmt();

  /**
   * returns the nodes in this graph in no deterministic order (->Set) to get a linearized flow use
   * iterator().
   */
  @Nonnull
  public abstract Set<Stmt> nodes();

  public abstract boolean containsNode(@Nonnull Stmt node);

  /**
   * returns the ingoing flows to node as an List with no reliable/specific order and possibly
   * duplicate entries (like successors(Stmt).
   */
  @Nonnull
  public abstract List<Stmt> predecessors(@Nonnull Stmt node);

  /** returns the outgoing flows of node as ordered List. The List can have duplicate entries! */
  @Nonnull
  public abstract List<Stmt> successors(@Nonnull Stmt node);

  /** returns the amount of flows with node as source or target. */
  public int degree(@Nonnull Stmt node) {
    return inDegree(node) + outDegree(node);
  }

  /** returns the amount of ingoing flows into node */
  public abstract int inDegree(@Nonnull Stmt node);

  /** returns the amount of flows that start from node */
  public abstract int outDegree(@Nonnull Stmt node);

  /** returns true if there is a flow between source and target */
  public abstract boolean hasEdgeConnecting(@Nonnull Stmt source, @Nonnull Stmt target);

  /** returns a list of associated traps */
  @Nonnull
  public abstract List<Trap> getTraps();

  /**
   * returns a Collection of Stmts that leave the body (i.e. JReturnVoidStmt, JReturnStmt and
   * JThrowStmt)
   */
  @Nonnull
  public Collection<Stmt> getTails() {
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

  /**
   * Look for a path in graph, from def to use. This path has to lie inside an extended basic block
   * (and this property implies uniqueness.). The path returned includes from and to.
   *
   * @param from start point for the path.
   * @param to end point for the path.
   * @return null if there is no such path.
   */
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

  public boolean equivTo(Object o) {
    if (o == this) {
      return true;
    }

    if (!(o instanceof StmtGraph)) {
      return false;
    }
    StmtGraph otherGraph = (StmtGraph) o;

    Set<Stmt> nodes = nodes();
    final Set<Stmt> otherNodes = otherGraph.nodes();
    if (nodes.size() != otherNodes.size()) {
      return false;
    }

    if (getTraps().size() != otherGraph.getTraps().size()) {
      return false;
    }

    final Iterator<Stmt> iterator = iterator();
    final Iterator<Stmt> otherIterator = otherGraph.iterator();

    while (iterator.hasNext()) {
      if (!otherIterator.hasNext()) {
        return false;
      }
      if (!iterator.next().equivTo(otherIterator.next())) {
        return false;
      }
    }

    return true;
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

    Set<Stmt> nodes = nodes();
    final Set<Stmt> otherNodes = otherGraph.nodes();
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

  /** validates whether the each Stmt has the correct amount of outgoing flows. */
  public void validateStmtConnectionsInGraph() {
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

  @Override
  @Nonnull
  public Iterator<Stmt> iterator() {
    return new StmtGraphBlockIterator(this, getTraps());
  }
}
