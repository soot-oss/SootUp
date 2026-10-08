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
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.ref.JParameterRef;
import sootup.core.jimple.common.ref.JThisRef;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.ReferenceType;

/**
 * Intraprocedural must-alias analysis for locals of reference type, ported from Soot's {@code
 * LocalMustAliasAnalysis} and {@code StrongLocalMustAliasAnalysis}.
 *
 * <p>Every local is mapped to a value number before each statement: {@code this} and the parameters
 * have fixed numbers, every other right-hand side (e.g. an allocation or a method call) gets a
 * number of its own, and copies propagate the number. Where two branches assign different numbers
 * to a local, the local gets a number that is unique to that merge point, so that copies made after
 * the merge are still recognized as aliases. Two locals must-alias at a statement if they have the
 * same number before it.
 *
 * <p>In strong mode, numbers assigned inside loops are discarded: such a number stands for the
 * objects of all iterations, and two locals with the same number may refer to objects of different
 * iterations.
 */
public class LocalMustAliasAnalysis {

  private final Map<Stmt, Map<Local, Integer>> flowBefore = new IdentityHashMap<>();
  private final Set<Integer> invalidNumbers = new HashSet<>();
  private final Map<Value, Integer> rhsToNumber = new IdentityHashMap<>();
  private final Map<Stmt, Map<Local, Integer>> mergeNumbers = new IdentityHashMap<>();
  private int nextNumber = 1;

  /**
   * Creates and runs the analysis
   *
   * @param graph the control flow graph of the method
   * @param exceptionalEdges whether statements also flow to the handlers of the traps covering them
   * @param strong whether numbers assigned inside loops are discarded, as in Soot's {@code
   *     StrongLocalMustAliasAnalysis}
   */
  public LocalMustAliasAnalysis(
      @NonNull ControlFlowGraph<?> graph, boolean exceptionalEdges, boolean strong) {
    Map<Stmt, List<Stmt>> succs = new IdentityHashMap<>();
    Map<Stmt, List<Stmt>> preds = new IdentityHashMap<>();
    for (Stmt s : graph.getNodes()) {
      succs.put(s, new ArrayList<>());
      preds.put(s, new ArrayList<>());
    }
    for (Stmt s : graph.getNodes()) {
      List<Stmt> targets = new ArrayList<>(graph.successors(s));
      if (exceptionalEdges) {
        targets.addAll(graph.exceptionalSuccessors(s).values());
      }
      for (Stmt t : targets) {
        if (succs.containsKey(t) && !succs.get(s).contains(t)) {
          succs.get(s).add(t);
          preds.get(t).add(s);
        }
      }
    }
    solve(graph, succs, preds);
    if (strong) {
      invalidateLoopNumbers(succs);
    }
    rhsToNumber.clear();
    mergeNumbers.clear();
  }

  private void solve(
      ControlFlowGraph<?> graph, Map<Stmt, List<Stmt>> succs, Map<Stmt, List<Stmt>> preds) {
    Map<Stmt, Map<Local, Integer>> flowAfter = new IdentityHashMap<>();
    Deque<Stmt> worklist = new ArrayDeque<>();
    Set<Stmt> queued = Collections.newSetFromMap(new IdentityHashMap<>());
    for (Stmt s : graph.getStmts()) {
      worklist.add(s);
      queued.add(s);
    }
    while (!worklist.isEmpty()) {
      Stmt s = worklist.poll();
      queued.remove(s);

      // merge the flows of all predecessors
      Map<Local, Integer> in = new HashMap<>();
      boolean first = true;
      for (Stmt p : preds.get(s)) {
        Map<Local, Integer> pOut = flowAfter.get(p);
        if (pOut == null) {
          continue;
        }
        if (first) {
          in.putAll(pOut);
          first = false;
        } else {
          in = merge(s, in, pOut);
        }
      }
      flowBefore.put(s, in);

      Map<Local, Integer> out = flowThrough(in, s);
      if (!out.equals(flowAfter.get(s))) {
        flowAfter.put(s, out);
        for (Stmt t : succs.get(s)) {
          if (queued.add(t)) {
            worklist.add(t);
          }
        }
      }
    }
  }

  private Map<Local, Integer> merge(Stmt mergePoint, Map<Local, Integer> a, Map<Local, Integer> b) {
    Map<Local, Integer> out = new HashMap<>();
    Set<Local> locals = new HashSet<>(a.keySet());
    locals.addAll(b.keySet());
    for (Local l : locals) {
      Integer i1 = a.get(l);
      Integer i2 = b.get(l);
      if (i1 == null) {
        out.put(l, i2);
      } else if (i2 == null || i1.equals(i2)) {
        out.put(l, i1);
      } else {
        // a number that is unique to this merge point, so that copies of l made after the merge
        // are still known to alias
        out.put(
            l,
            mergeNumbers
                .computeIfAbsent(mergePoint, k -> new HashMap<>())
                .computeIfAbsent(l, k -> nextNumber++));
      }
    }
    return out;
  }

  private Map<Local, Integer> flowThrough(Map<Local, Integer> in, Stmt s) {
    Map<Local, Integer> out = new HashMap<>(in);
    if (s instanceof AbstractDefinitionStmt) {
      AbstractDefinitionStmt ds = (AbstractDefinitionStmt) s;
      Value lhs = ds.getLeftOp();
      Value rhs = ds.getRightOp();
      if (rhs instanceof JCastExpr) {
        rhs = ((JCastExpr) rhs).getOp();
      }
      if (lhs instanceof Local && lhs.getType() instanceof ReferenceType) {
        Local l = (Local) lhs;
        if (rhs instanceof Local) {
          Integer val = in.get(rhs);
          if (val != null) {
            out.put(l, val);
          } else {
            out.remove(l);
          }
        } else if (rhs instanceof JThisRef) {
          out.put(l, thisRefNumber());
        } else if (rhs instanceof JParameterRef) {
          out.put(l, parameterRefNumber((JParameterRef) rhs));
        } else {
          out.put(l, rhsToNumber.computeIfAbsent(rhs, k -> nextNumber++));
        }
      }
    }
    return out;
  }

  /** Discards the numbers of locals that are defined inside a loop. */
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
            Integer before = flowBefore.getOrDefault(s, Collections.emptyMap()).get(lhs);
            if (before != null) {
              invalidNumbers.add(before);
            }
            for (Stmt t : succs.get(s)) {
              Integer after = flowBefore.getOrDefault(t, Collections.emptyMap()).get(lhs);
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

  public static int thisRefNumber() {
    return 0;
  }

  public static int parameterRefNumber(@NonNull JParameterRef r) {
    return -1 - r.getIndex();
  }

  /**
   * Returns whether the local {@code l1} before statement {@code s1} must refer to the same object
   * as the local {@code l2} before statement {@code s2}.
   */
  public boolean mustAlias(
      @NonNull Local l1, @NonNull Stmt s1, @NonNull Local l2, @NonNull Stmt s2) {
    Map<Local, Integer> before1 = flowBefore.get(s1);
    Map<Local, Integer> before2 = flowBefore.get(s2);
    if (before1 == null || before2 == null) {
      return false;
    }
    Integer n1 = before1.get(l1);
    Integer n2 = before2.get(l2);
    return n1 != null
        && n2 != null
        && !invalidNumbers.contains(n1)
        && !invalidNumbers.contains(n2)
        && n1.equals(n2);
  }

  /**
   * Returns a key that identifies the object the local refers to before the statement, or empty if
   * the analysis knows nothing about it.
   */
  @NonNull
  public Optional<String> instanceKeyString(@NonNull Local l, @NonNull Stmt s) {
    Integer n = flowBefore.getOrDefault(s, Collections.emptyMap()).get(l);
    if (n == null) {
      return Optional.empty();
    }
    return Optional.of(invalidNumbers.contains(n) ? "UNKNOWN" : n.toString());
  }
}
