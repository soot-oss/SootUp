package sootup.analysis.intraprocedural;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2026 Patrick Lam, Eric Bodden, Hazem Gamal and others
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
import org.jspecify.annotations.NonNull;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.ReferenceType;

/**
 * {@link LocalMustAliasAnalysis} that discards the value numbers assigned inside loops, ported from
 * Soot's {@code StrongLocalMustAliasAnalysis}. Such a number stands for the objects of all
 * iterations, so two locals with that number may refer to objects of different iterations; for
 * them, {@link #mustAlias} is false and {@link #instanceKeyString} is {@code "UNKNOWN"}. A loop is
 * a strongly connected component of the control flow graph with more than one statement, or a
 * statement that is its own successor.
 */
public class StrongLocalMustAliasAnalysis extends LocalMustAliasAnalysis {

  private final Set<Integer> invalidNumbers = new HashSet<>();

  /**
   * Creates and runs the analysis
   *
   * @param graph the control flow graph of the method
   * @param exceptionalEdges whether statements also flow to the handlers of the traps covering them
   */
  public StrongLocalMustAliasAnalysis(
      @NonNull ControlFlowGraph<?> graph, boolean exceptionalEdges) {
    super(graph, exceptionalEdges);
    invalidateLoopNumbers(successors());
  }

  @Override
  protected boolean isValid(int number) {
    return !invalidNumbers.contains(number);
  }

  /** Collects the numbers of locals that are defined inside a loop. */
  private void invalidateLoopNumbers(Map<Stmt, List<Stmt>> succs) {
    for (Set<Stmt> scc : stronglyConnectedComponents(succs)) {
      boolean loop =
          scc.size() > 1 || succs.get(scc.iterator().next()).contains(scc.iterator().next());
      if (!loop) {
        continue;
      }
      for (Stmt s : scc) {
        if (s instanceof AbstractDefinitionStmt) {
          Value lhs = ((AbstractDefinitionStmt) s).getLeftOp();
          if (lhs instanceof Local && lhs.getType() instanceof ReferenceType) {
            Integer before = numberBefore((Local) lhs, s);
            if (before != null) {
              invalidNumbers.add(before);
            }
            for (Stmt t : succs.get(s)) {
              Integer after = numberBefore((Local) lhs, t);
              if (after != null) {
                invalidNumbers.add(after);
              }
            }
          }
        }
      }
    }
  }

  /** Tarjan's algorithm, iterative */
  private static List<Set<Stmt>> stronglyConnectedComponents(Map<Stmt, List<Stmt>> succs) {
    Map<Stmt, Integer> index = new IdentityHashMap<>();
    Map<Stmt, Integer> lowLink = new IdentityHashMap<>();
    Deque<Stmt> stack = new ArrayDeque<>();
    Set<Stmt> onStack = Collections.newSetFromMap(new IdentityHashMap<>());
    List<Set<Stmt>> result = new ArrayList<>();
    int[] counter = {0};
    for (Stmt root : succs.keySet()) {
      if (index.containsKey(root)) {
        continue;
      }
      Deque<Object[]> work = new ArrayDeque<>();
      work.push(new Object[] {root, 0});
      index.put(root, counter[0]);
      lowLink.put(root, counter[0]++);
      stack.push(root);
      onStack.add(root);
      while (!work.isEmpty()) {
        Object[] frame = work.peek();
        Stmt v = (Stmt) frame[0];
        int i = (Integer) frame[1];
        List<Stmt> next = succs.get(v);
        if (i < next.size()) {
          frame[1] = i + 1;
          Stmt w = next.get(i);
          if (!index.containsKey(w)) {
            index.put(w, counter[0]);
            lowLink.put(w, counter[0]++);
            stack.push(w);
            onStack.add(w);
            work.push(new Object[] {w, 0});
          } else if (onStack.contains(w)) {
            lowLink.put(v, Math.min(lowLink.get(v), index.get(w)));
          }
        } else {
          work.pop();
          if (!work.isEmpty()) {
            Stmt parent = (Stmt) work.peek()[0];
            lowLink.put(parent, Math.min(lowLink.get(parent), lowLink.get(v)));
          }
          if (lowLink.get(v).equals(index.get(v))) {
            Set<Stmt> scc = Collections.newSetFromMap(new IdentityHashMap<>());
            Stmt w;
            do {
              w = stack.pop();
              onStack.remove(w);
              scc.add(w);
            } while (w != v);
            result.add(scc);
          }
        }
      }
    }
    return result;
  }
}
