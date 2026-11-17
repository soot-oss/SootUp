package sootup.core.graph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Hazem Gamal and others
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
import sootup.core.jimple.common.stmt.Stmt;

/**
 * Statement-level dominators or post-dominators of a {@link ControlFlowGraph}, computed with the
 * iterative algorithm of Cooper, Harvey and Kennedy ("A Simple, Fast Dominance Algorithm").
 *
 * <p>A virtual root is used so that graphs with several heads (dominators) or several tails
 * (post-dominators, e.g. a method with more than one return or throw statement) are handled
 * correctly: a statement whose only (post-)dominator is the virtual root has no immediate
 * (post-)dominator.
 *
 * <p>Statements the virtual root does not reach have no (post-)dominators at all. For
 * post-dominators, these are the statements that cannot reach an exit, e.g. those of an infinite
 * loop, as in Soot's {@code MHGPostDominatorsFinder}. For dominators, every statement without a
 * predecessor is a head, so unreachable code without predecessors is dominated only by the virtual
 * root, and only unreachable code inside a cycle has no dominators.
 *
 * <p>Exceptional control flow is taken into account if requested: a statement then also flows to
 * the handlers of the traps that cover it.
 */
public final class StmtDominance {

  private static final int ROOT = 0;

  private final Map<Stmt, Integer> index = new IdentityHashMap<>();
  private final List<Stmt> stmts = new ArrayList<>();

  /** immediate dominator per node index; -1 if unreachable; ROOT for the virtual root itself */
  private final int[] idom;

  /** reverse post-order number per node index, used by the intersection */
  private final int[] order;

  private StmtDominance(ControlFlowGraph<?> graph, boolean post, boolean exceptionalEdges) {
    stmts.add(null); // the virtual root
    for (Stmt s : graph.getNodes()) {
      index.put(s, stmts.size());
      stmts.add(s);
    }
    int n = stmts.size();

    // successor lists of the (possibly reversed) graph, including the virtual root
    List<List<Integer>> succs = new ArrayList<>(n);
    List<List<Integer>> preds = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      succs.add(new ArrayList<>());
      preds.add(new ArrayList<>());
    }
    for (int i = 1; i < n; i++) {
      Stmt s = stmts.get(i);
      List<Stmt> targets = new ArrayList<>(graph.successors(s));
      if (exceptionalEdges) {
        targets.addAll(graph.exceptionalSuccessors(s).values());
      }
      for (Stmt t : targets) {
        Integer j = index.get(t);
        if (j == null) {
          continue;
        }
        if (post) {
          addEdge(succs, preds, j, i);
        } else {
          addEdge(succs, preds, i, j);
        }
      }
    }
    // the virtual root reaches every head of the (possibly reversed) graph
    Stmt start = graph.getStartingStmt();
    for (int i = 1; i < n; i++) {
      boolean head = preds.get(i).isEmpty() || (!post && stmts.get(i) == start);
      if (head) {
        addEdge(succs, preds, ROOT, i);
      }
    }

    // reverse post-order from the virtual root
    order = new int[n];
    Arrays.fill(order, -1);
    List<Integer> rpo = reversePostOrder(succs, n);
    for (int k = 0; k < rpo.size(); k++) {
      order[rpo.get(k)] = k;
    }

    idom = new int[n];
    Arrays.fill(idom, -1);
    idom[ROOT] = ROOT;
    boolean changed = true;
    while (changed) {
      changed = false;
      for (int k = 1; k < rpo.size(); k++) {
        int b = rpo.get(k);
        int newIdom = -1;
        for (int p : preds.get(b)) {
          if (idom[p] == -1) {
            continue;
          }
          newIdom = newIdom == -1 ? p : intersect(p, newIdom);
        }
        if (newIdom != -1 && idom[b] != newIdom) {
          idom[b] = newIdom;
          changed = true;
        }
      }
    }
  }

  private static void addEdge(List<List<Integer>> succs, List<List<Integer>> preds, int a, int b) {
    if (!succs.get(a).contains(b)) {
      succs.get(a).add(b);
      preds.get(b).add(a);
    }
  }

  private static List<Integer> reversePostOrder(List<List<Integer>> succs, int n) {
    boolean[] visited = new boolean[n];
    List<Integer> postOrder = new ArrayList<>(n);
    Deque<int[]> stack = new ArrayDeque<>();
    stack.push(new int[] {ROOT, 0});
    visited[ROOT] = true;
    while (!stack.isEmpty()) {
      int[] top = stack.peek();
      List<Integer> s = succs.get(top[0]);
      if (top[1] < s.size()) {
        int next = s.get(top[1]++);
        if (!visited[next]) {
          visited[next] = true;
          stack.push(new int[] {next, 0});
        }
      } else {
        postOrder.add(stack.pop()[0]);
      }
    }
    Collections.reverse(postOrder);
    return postOrder;
  }

  private int intersect(int a, int b) {
    while (a != b) {
      while (order[a] > order[b]) {
        a = idom[a];
      }
      while (order[b] > order[a]) {
        b = idom[b];
      }
    }
    return a;
  }

  /**
   * Computes the dominators of the statements in the given graph
   *
   * @param graph the control flow graph
   * @param exceptionalEdges whether statements also flow to the handlers of the traps covering them
   * @return the dominators
   */
  @NonNull
  public static StmtDominance dominators(
      @NonNull ControlFlowGraph<?> graph, boolean exceptionalEdges) {
    return new StmtDominance(graph, false, exceptionalEdges);
  }

  /**
   * Computes the post-dominators of the statements in the given graph. All exits of the graph
   * (return and throw statements) are joined by a virtual exit.
   *
   * @param graph the control flow graph
   * @param exceptionalEdges whether statements also flow to the handlers of the traps covering them
   * @return the post-dominators
   */
  @NonNull
  public static StmtDominance postDominators(
      @NonNull ControlFlowGraph<?> graph, boolean exceptionalEdges) {
    return new StmtDominance(graph, true, exceptionalEdges);
  }

  /**
   * Returns the immediate (post-)dominator of the given statement, or empty if the statement is a
   * head (tail) of the graph, is only (post-)dominated by the virtual root, or is not reached from
   * it (for post-dominators: cannot reach an exit).
   */
  @NonNull
  public Optional<Stmt> getImmediateDominator(@NonNull Stmt stmt) {
    Integer i = index.get(stmt);
    if (i == null || idom[i] == -1 || idom[i] == ROOT) {
      return Optional.empty();
    }
    return Optional.of(stmts.get(idom[i]));
  }

  /**
   * Returns all (post-)dominators of the given statement, starting with the statement itself and
   * ending with the head (tail) that (post-)dominates it, if any; empty if the virtual root does
   * not reach the statement (for post-dominators: it cannot reach an exit).
   */
  @NonNull
  public List<Stmt> getDominators(@NonNull Stmt stmt) {
    Integer i = index.get(stmt);
    if (i == null || idom[i] == -1) {
      return Collections.emptyList();
    }
    List<Stmt> result = new ArrayList<>();
    for (int cur = i; cur != ROOT; cur = idom[cur]) {
      result.add(stmts.get(cur));
    }
    return result;
  }

  /**
   * Returns whether {@code dominator} (post-)dominates {@code stmt}; a statement dominates itself.
   */
  public boolean dominates(@NonNull Stmt dominator, @NonNull Stmt stmt) {
    return getDominators(stmt).contains(dominator);
  }
}
