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
 * LocalMustAliasAnalysis}.
 *
 * <p>Every local is mapped to a value number before each statement: {@code this} and the parameters
 * have fixed numbers, every other right-hand side (e.g. an allocation or a method call) gets a
 * number of its own, and copies propagate the number. Where two branches assign different numbers
 * to a local, the local gets a number that is unique to that merge point, so that copies made after
 * the merge are still recognized as aliases. Two locals must-alias at a statement if they have the
 * same number before it.
 *
 * <p>A number assigned inside a loop stands for the objects of all iterations, so two locals with
 * that number may refer to objects of different iterations; {@link StrongLocalMustAliasAnalysis}
 * discards such numbers.
 */
public class LocalMustAliasAnalysis {

  private final Map<Stmt, Map<Local, Integer>> flowBefore = new IdentityHashMap<>();
  private final Map<Stmt, List<Stmt>> succs = new IdentityHashMap<>();
  private final Map<Value, Integer> rhsToNumber = new IdentityHashMap<>();
  private final Map<Stmt, Map<Local, Integer>> mergeNumbers = new IdentityHashMap<>();
  private int nextNumber = 1;

  /**
   * Creates and runs the analysis
   *
   * @param graph the control flow graph of the method
   * @param exceptionalEdges whether statements also flow to the handlers of the traps covering them
   */
  public LocalMustAliasAnalysis(@NonNull ControlFlowGraph<?> graph, boolean exceptionalEdges) {
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

  /** The successors of each statement, including exceptional ones if requested. */
  @NonNull
  protected Map<Stmt, List<Stmt>> successors() {
    return Collections.unmodifiableMap(succs);
  }

  /** The value number of {@code l} before {@code s}, or null if the analysis knows none. */
  protected Integer numberBefore(@NonNull Local l, @NonNull Stmt s) {
    return flowBefore.getOrDefault(s, Collections.emptyMap()).get(l);
  }

  /**
   * Whether a value number identifies one object, so that locals with that number must-alias.
   * Always true here; {@link StrongLocalMustAliasAnalysis} rejects the numbers of loops.
   */
  protected boolean isValid(int number) {
    return true;
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
    return n1 != null && n2 != null && isValid(n1) && isValid(n2) && n1.equals(n2);
  }

  /**
   * Returns a key that identifies the object the local refers to before the statement, or empty if
   * the analysis knows nothing about it.
   */
  @NonNull
  public Optional<String> instanceKeyString(@NonNull Local l, @NonNull Stmt s) {
    Integer n = numberBefore(l, s);
    if (n == null) {
      return Optional.empty();
    }
    return Optional.of(isValid(n) ? n.toString() : "UNKNOWN");
  }
}
