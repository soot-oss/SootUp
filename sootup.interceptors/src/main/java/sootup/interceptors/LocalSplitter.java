package sootup.interceptors;

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
import org.jspecify.annotations.NonNull;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.jimple.basic.LValue;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.transform.BodyInterceptor;
import sootup.core.views.View;

/**
 * A BodyInterceptor that attempts to identify and separate uses of a local variable (definition)
 * that are independent of each other. This is necessary as the AsmMethodSource maps usages of the
 * same Local index to the same Local object, which can lead to wrong Jimple e.g. when a primitive
 * type gets merged with a reference-type and augmenting the type of the Local is less precise.
 *
 * <p>For example the code:
 *
 * <pre>
 *    l0 := @this Test
 *    l1 = 0
 *    l2 = 1
 *    l1 = l1 + 1
 *    l2 = l2 + 1
 *    return
 * </pre>
 *
 * <p>to:
 *
 * <pre>
 *    l0 := @this Test
 *    l1#0 = 0
 *    l2#0 = 1
 *    l1#1 = l1#0 + 1
 *    l2#1 = l2#0 + 1
 *    return
 * </pre>
 */
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
    @NonNull private final Map<T, T> parent = new HashMap<>();

    /** Stores the size of a tree under the key. Only updated for roots of trees. */
    @NonNull private final Map<T, Integer> sizes = new HashMap<>();

    /**
     * Creates a new set that only contains the {@code node}. Does nothing when the forest already
     * contains the {@code node}.
     */
    void add(@NonNull T node) {
      if (parent.containsKey(node)) {
        return;
      }

      parent.put(node, node);
      sizes.put(node, 1);
    }

    /** Finds the representative of the set that contains the {@code node}. */
    @NonNull T find(T node) {
      T parentNode = parent.get(node);
      if (parentNode == null) {
        throw new IllegalArgumentException("The DisjointSetForest does not contain the node.");
      }

      T itNode = node;
      while (parentNode != itNode) {
        // Path Halving to get amortized constant operations
        T grandparent = parent.get(parentNode);
        parent.put(itNode, grandparent);

        itNode = grandparent;
        parentNode = parent.get(grandparent);
      }
      return itNode;
    }

    /**
     * Combines the sets of {@code first} and {@code second}. Returns the representative of the
     * combined set.
     */
    void union(@NonNull T first, @NonNull T second) {
      first = find(first);
      second = find(second);

      if (first == second) {
        return;
      }

      final Integer firstSize = sizes.get(first);
      final Integer secondSize = sizes.get(second);

      T smaller, larger;
      if (firstSize > secondSize) {
        larger = first;
        smaller = second;
      } else {
        larger = second;
        smaller = first;
      }

      // adding the smaller subtree to the larger tree keeps the tree flatter
      parent.put(smaller, larger);
      sizes.put(larger, firstSize + secondSize);
      sizes.remove(smaller);
    }

    int getSetCount() {
      // `sizes` only contains values for the root of the trees,
      // so its size matches the total number of sets
      return sizes.size();
    }
  }

  static class PartialStmt {
    @NonNull final Stmt backingStmt;

    /**
     * Whether the partial statement refers to only the definitions of the inner statement or only
     * to the uses.
     */
    final boolean isDef;

    PartialStmt(@NonNull Stmt backingStmt, boolean isDef) {
      this.backingStmt = backingStmt;
      this.isDef = isDef;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      PartialStmt that = (PartialStmt) o;

      if (isDef != that.isDef) {
        return false;
      }
      return backingStmt.equals(that.backingStmt);
    }

    @Override
    public int hashCode() {
      int result = backingStmt.hashCode();
      result = 31 * result + (isDef ? 1 : 0);
      return result;
    }
  }

  @Override
  public void interceptBody(Body.@NonNull BodyBuilder builder, @NonNull View view) {
    MutableStmtGraph graph = builder.getStmtGraph();

    // Cache the stmts to not have to retrieve them for every local
    List<Stmt> stmts = graph.getStmts();
    // Maps every local to its assignment stmts.
    // Contains indices to the above list to reduce bookkeeping when modifying stmts.
    Map<Local, List<Integer>> assignmentsByLocal = groupAssignmentsByLocal(stmts);

    Set<Local> newLocals = new HashSet<>();

    final Set<Local> locals = builder.getLocals();
    for (Local local : locals) {
      // Use a disjoint set while walking the statement graph to union all uses of the local that
      // can be reached from each definition. This will automatically union definitions that have
      // overlapping uses and therefore can't be split.
      // It uses a `PartialStmt` instead of `Stmt` because a statement might contain both a
      // definition and use of a local, and they need to be processed separately.
      DisjointSetForest<PartialStmt> disjointSet = new DisjointSetForest<>();

      List<AbstractDefinitionStmt> assignments =
          assignmentsByLocal.getOrDefault(local, Collections.emptyList()).stream()
              .map(i -> (AbstractDefinitionStmt) stmts.get(i))
              .toList();

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

        Deque<Stmt> stack = new ArrayDeque<>(graph.getAllSuccessors(assignment));
        Set<Stmt> visited = new HashSet<>();

        while (!stack.isEmpty()) {
          Stmt stmt = stack.pop();
          if (!visited.add(stmt)) {
            continue;
          }

          if (stmt.getUses().anyMatch(l -> l == local)) {
            PartialStmt useStmt = new PartialStmt(stmt, false);
            disjointSet.add(useStmt);
            disjointSet.union(defStmt, useStmt);
          }

          // a new assignment to the local -> end walk here
          // otherwise continue by adding all successors to the stack
          Optional<LValue> defOpt = stmt.getDef();
          if (defOpt.isEmpty() || defOpt.get() != local) {
            stack.addAll(graph.getAllSuccessors(stmt));
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

      Function<PartialStmt, Local> getNewLocal =
          partialStmt ->
              representativeToNewLocal.computeIfAbsent(
                  disjointSet.find(partialStmt),
                  s -> {
                    Local newLocal;
                    do {
                      newLocal = local.withName(local.getName() + "#" + (nextId[0]++));
                    } while (locals.contains(newLocal));
                    return newLocal;
                  });

      for (int i = 0; i < stmts.size(); i++) {
        Stmt stmt = stmts.get(i);

        Optional<LValue> stmtDef = stmt.getDef();
        boolean localIsDef = stmtDef.isPresent() && stmtDef.get() == local;
        boolean localIsUse = stmt.getUses().anyMatch(l -> l == local);

        Stmt oldStmt = stmt;

        if (localIsDef) {
          Local newDefLocal = getNewLocal.apply(new PartialStmt(oldStmt, true));
          if (local != newDefLocal) {
            newLocals.add(newDefLocal);
            stmt = ((AbstractDefinitionStmt) stmt).withNewDef(newDefLocal);
          }
        }

        if (localIsUse) {
          Local newUseLocal = getNewLocal.apply(new PartialStmt(oldStmt, false));
          if (local != newUseLocal) {
            newLocals.add(newUseLocal);
            stmt = stmt.withNewUse(local, newUseLocal);
          }
        }

        if (oldStmt == stmt) {
          continue;
        }

        graph.replaceNode(oldStmt, stmt);
        stmts.set(i, stmt);
      }
    }

    builder.setLocals(newLocals);
  }

  @NonNull Map<Local, List<Integer>> groupAssignmentsByLocal(List<Stmt> statements) {
    Map<Local, List<Integer>> groupings = new HashMap<>();

    for (int i = 0; i < statements.size(); i++) {
      Stmt stmt = statements.get(i);
      if (!(stmt instanceof AbstractDefinitionStmt defStmt)) {
        continue;
      }

      LValue leftOp = defStmt.getLeftOp();
      if (!(leftOp instanceof Local)) {
        continue;
      }

      groupings.computeIfAbsent((Local) leftOp, x -> new ArrayList<>()).add(i);
    }

    return groupings;
  }
}
