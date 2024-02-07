package sootup.java.bytecode.interceptors;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Christian Brüggemann
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
import sootup.core.graph.*;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.transform.BodyInterceptor;
import sootup.core.views.View;

public class LocalSplitter implements BodyInterceptor {

  /**
   * Contains disjoint sets of nodes which are implemented as trees. Every set is represented by a
   * tree in the forest. Each set is identified by the root node of its tree, also known as its
   * representative.
   *
   * <p><a href="https://en.wikipedia.org/wiki/Disjoint-set_data_structure">Disjoint-set data
   * structure</a>
   */
  static class DisjointSetForest<T> {
    /** Every node points to its parent in its tree. Roots of trees point to themselves. */
    private final Map<T, T> parent = new HashMap<>();

    /** Stores the size of a tree under the key. Only updated for roots of trees. */
    private final Map<T, Integer> sizes = new HashMap<>();

    private int setCount = 0;

    /**
     * Creates a new set that only contains the {@code node}. Does nothing when the forest already
     * contains the {@code node}.
     */
    void add(T node) {
      if (parent.containsKey(node)) return;

      parent.put(node, node);
      sizes.put(node, 1);
      setCount++;
    }

    /** Finds the representative of the set that contains the {@code node}. */
    T find(T node) {
      if (!parent.containsKey(node))
        throw new IllegalArgumentException("The DisjointSetForest does not contain the node.");

      while (parent.get(node) != node) {
        // Path Halving to get amortized constant operations
        T grandparent = parent.get(parent.get(node));
        parent.put(node, grandparent);

        node = grandparent;
      }
      return node;
    }

    /**
     * Combines the sets of {@code first} and {@code second}. Returns the representative of the
     * combined set.
     */
    T union(T first, T second) {
      first = find(first);
      second = find(second);

      if (first == second) return first;

      T smaller = (sizes.get(first) > sizes.get(second)) ? second : first;
      T larger = (smaller == first) ? second : first;

      // adding the smaller subtree to the larger tree keeps the tree flatter
      parent.put(smaller, larger);
      sizes.put(larger, sizes.get(smaller) + sizes.get(larger));
      sizes.remove(smaller);

      setCount--;

      return larger;
    }

    int getSetCount() {
      return setCount;
    }
  }

  static class WrappedStmt {
    @Nonnull final Stmt inner;
    final boolean isDef;

    WrappedStmt(@Nonnull Stmt inner, boolean isDef) {
      this.inner = inner;
      this.isDef = isDef;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      WrappedStmt that = (WrappedStmt) o;

      if (isDef != that.isDef) return false;
      return inner.equals(that.inner);
    }

    @Override
    public int hashCode() {
      int result = inner.hashCode();
      result = 31 * result + (isDef ? 1 : 0);
      return result;
    }
  }

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder, @Nonnull View view) {
    MutableStmtGraph graph = builder.getStmtGraph();

    // `#` is used as a special character for splitting the locals,
    // so it can't be in any of the original local names since it might cause name collisions
    assert builder.getLocals().stream().noneMatch(local -> local.getName().contains("#"));

    Set<Local> newLocals = new HashSet<>();

    for (Local local : builder.getLocals()) {
      // TODO explain why a disjoint set is used here
      DisjointSetForest<WrappedStmt> disjointSet = new DisjointSetForest<>();

      List<AbstractDefinitionStmt> assignments = findAllAssignmentsToLocal(builder, local);
      for (AbstractDefinitionStmt assignment : assignments) {
        WrappedStmt defStmt = new WrappedStmt(assignment, true);
        disjointSet.add(defStmt);

        Queue<Stmt> queue = new ArrayDeque<>(graph.successors(assignment));
        Set<Stmt> visited = new HashSet<>();

        while (!queue.isEmpty()) {
          Stmt stmt = queue.remove();
          if (!visited.add(stmt)) {
            continue;
          }

          if (stmt.getUses().contains(local)) {
            // TODO might be able to stop short when running into a non-trivial set
            //  since from that point onward the previous walk that crated
            //  that set already walked everything following the current statement
            WrappedStmt useStmt = new WrappedStmt(stmt, false);
            disjointSet.add(useStmt);
            disjointSet.union(defStmt, useStmt);
          }

          // a new assignment to the local -> end walk here
          // otherwise continue by adding all successors to the queue
          if (!stmt.getDefs().contains(local)) {
            queue.addAll(graph.successors(stmt));
          }
        }
      }

      if (disjointSet.getSetCount() <= 1) {
        // There is only a single that local that can't be split
        newLocals.add(local);
        continue;
      }

      Map<WrappedStmt, Local> representativeToNewLocal = new HashMap<>();
      final int[] nextId = {0};

      for (Stmt stmt : builder.getStmts()) {
        if (!stmt.getUsesAndDefs().contains(local)) {
          continue;
        }

        Stmt oldStmt = stmt;

        if (stmt.getDefs().contains(local)) {
          Local newDefLocal =
              representativeToNewLocal.computeIfAbsent(
                  disjointSet.find(new WrappedStmt(oldStmt, true)),
                  s -> local.withName(local.getName() + "#" + (nextId[0]++)));
          newLocals.add(newDefLocal);
          stmt = ((AbstractDefinitionStmt) stmt).withNewDef(newDefLocal);
        }

        if (stmt.getUses().contains(local)) {
          Local newUseLocal =
              representativeToNewLocal.computeIfAbsent(
                  disjointSet.find(new WrappedStmt(oldStmt, false)),
                  s -> local.withName(local.getName() + "#" + (nextId[0]++)));
          newLocals.add(newUseLocal);
          stmt = stmt.withNewUse(local, newUseLocal);
        }

        graph.replaceNode(oldStmt, stmt);
      }
    }

    builder.setLocals(newLocals);
  }

  List<AbstractDefinitionStmt> findAllAssignmentsToLocal(
      @Nonnull Body.BodyBuilder builder, Local local) {
    return builder.getStmts().stream()
        .filter(stmt -> stmt instanceof AbstractDefinitionStmt)
        .map(stmt -> (AbstractDefinitionStmt) stmt)
        .filter(stmt -> stmt.getLeftOp() == local)
        .collect(Collectors.toList());
  }
}
