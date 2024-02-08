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
import java.util.function.Function;
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

    /**
     * Creates a new set that only contains the {@code node}. Does nothing when the forest already
     * contains the {@code node}.
     */
    void add(T node) {
      if (parent.containsKey(node)) return;

      parent.put(node, node);
      sizes.put(node, 1);
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
    void union(T first, T second) {
      first = find(first);
      second = find(second);

      if (first == second) return;

      T smaller = (sizes.get(first) > sizes.get(second)) ? second : first;
      T larger = (smaller == first) ? second : first;

      // adding the smaller subtree to the larger tree keeps the tree flatter
      parent.put(smaller, larger);
      sizes.put(larger, sizes.get(smaller) + sizes.get(larger));
      sizes.remove(smaller);
    }

    int getSetCount() {
      // `sizes` only contains values for the root of the trees,
      // so its size matches the total number of sets
      return sizes.size();
    }
  }

  static class PartialStmt {
    @Nonnull final Stmt inner;

    /**
     * Whether the partial statement refers to only the definitions of the inner statement or only
     * to the uses.
     */
    final boolean isDef;

    PartialStmt(@Nonnull Stmt inner, boolean isDef) {
      this.inner = inner;
      this.isDef = isDef;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      PartialStmt that = (PartialStmt) o;

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

    // Cache the statements to not have to retrieve them for every local
    List<Stmt> statements = new ArrayList<>(graph.getStmts());
    // Maps every local to its assignment statements.
    // Contains indices to the above list to reduce bookkeeping when modifying statements.
    Map<Local, List<Integer>> assignmentsByLocal = groupAssignmentsByLocal(statements);

    Set<Local> newLocals = new HashSet<>();

    for (Local local : builder.getLocals()) {
      // Use a disjoint set while walking the statement graph to union all uses of the local that
      // can be reached from each definition. This will automatically union definitions that have
      // overlapping uses and therefore can't be split.
      // It uses a `PartialStmt` instead of `Stmt` because a statement might contain both a
      // definition and use of a local, and they need to be processed separately.
      DisjointSetForest<PartialStmt> disjointSet = new DisjointSetForest<>();

      List<AbstractDefinitionStmt> assignments =
          assignmentsByLocal.getOrDefault(local, Collections.emptyList()).stream()
              .map(i -> (AbstractDefinitionStmt) statements.get(i))
              .collect(Collectors.toList());

      if (assignments.size() <= 1) {
        // There is only a single assignment to the local, so no splitting is necessary
        newLocals.add(local);
        continue;
      }

      // Walk the statement graph starting from every definition and union all uses until a
      // different definition is encountered.
      for (AbstractDefinitionStmt assignment : assignments) {
        PartialStmt defStmt = new PartialStmt(assignment, true);
        disjointSet.add(defStmt);

        List<Stmt> stack = new ArrayList<>(graph.successors(assignment));
        stack.addAll(graph.exceptionalSuccessors(assignment).values());
        Set<Stmt> visited = new HashSet<>();

        while (!stack.isEmpty()) {
          Stmt stmt = stack.remove(stack.size() - 1);
          if (!visited.add(stmt)) {
            continue;
          }

          if (stmt.getUses().contains(local)) {
            PartialStmt useStmt = new PartialStmt(stmt, false);
            disjointSet.add(useStmt);
            disjointSet.union(defStmt, useStmt);
          }

          // a new assignment to the local -> end walk here
          // otherwise continue by adding all successors to the stack
          if (!stmt.getDefs().contains(local)) {
            stack.addAll(graph.successors(stmt));
            stack.addAll(graph.exceptionalSuccessors(stmt).values());
          }
        }
      }

      if (disjointSet.getSetCount() <= 1) {
        // There is only a single that local that can't be split
        newLocals.add(local);
        continue;
      }

      // Split locals, according to the disjoint sets found above.
      Map<PartialStmt, Local> representativeToNewLocal = new HashMap<>();
      final int[] nextId = {0}; // Java quirk; just an `int` doesn't work

      for (int i = 0; i < statements.size(); i++) {
        Stmt stmt = statements.get(i);
        if (!stmt.getUsesAndDefs().contains(local)) {
          continue;
        }

        Stmt oldStmt = stmt;

        Function<Boolean, Local> getNewLocal =
            isDef ->
                representativeToNewLocal.computeIfAbsent(
                    disjointSet.find(new PartialStmt(oldStmt, isDef)),
                    s -> local.withName(local.getName() + "#" + (nextId[0]++)));

        if (stmt.getDefs().contains(local)) {
          Local newDefLocal = getNewLocal.apply(true);
          newLocals.add(newDefLocal);
          stmt = ((AbstractDefinitionStmt) stmt).withNewDef(newDefLocal);
        }

        if (stmt.getUses().contains(local)) {
          Local newUseLocal = getNewLocal.apply(false);
          newLocals.add(newUseLocal);
          stmt = stmt.withNewUse(local, newUseLocal);
        }

        graph.replaceNode(oldStmt, stmt);
        statements.set(i, stmt);
      }
    }

    builder.setLocals(newLocals);
  }

  Map<Local, List<Integer>> groupAssignmentsByLocal(List<Stmt> statements) {
    Map<Local, List<Integer>> groupings = new HashMap<>();

    for (int i = 0; i < statements.size(); i++) {
      Stmt stmt = statements.get(i);
      if (!(stmt instanceof AbstractDefinitionStmt)) continue;
      AbstractDefinitionStmt defStmt = (AbstractDefinitionStmt) stmt;
      if (!(defStmt.getLeftOp() instanceof Local)) continue;

      groupings.computeIfAbsent((Local) defStmt.getLeftOp(), x -> new ArrayList<>()).add(i);
    }

    return groupings;
  }
}
