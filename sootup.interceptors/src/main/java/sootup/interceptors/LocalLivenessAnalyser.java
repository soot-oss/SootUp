package sootup.interceptors;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2021 Raja Vallee-Rai, Zun Wang
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
import sootup.core.jimple.common.LValue;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.stmt.Stmt;

/**
 * @author Zun Wang
 */
public class LocalLivenessAnalyser {

  // Each stmt(node) has out-edges and in-edges
  // A local is live-in at a node if it is live on any its in-edges
  private final Map<Stmt, Set<Local>> liveIn = new HashMap<>();
  // A local is live-out at a node if it is live on any of its out-edges.
  // e.g: a = b + c; live-in={b,c}  live-out={a,b,c}
  private final Map<Stmt, Set<Local>> liveOut = new HashMap<>();

  public LocalLivenessAnalyser(@NonNull ControlFlowGraph<?> graph) {
    // Standard worklist-based backwards dataflow analysis.
    // 1. track pending nodes using 'inWorklist' (Set<Stmt>) to prevent duplicate entries in
    //    'worklist'.
    //    Without membership tracking, merge points in sequential conditional branches (e.g. 40+
    //    chained if-else diamonds) cause predecessors to be enqueued exponentially (2^N), leading
    //    to severe performance degradation and OutOfMemoryError.
    // 2. initialize the worklist with all CFG nodes up front. This guarantees that all
    //    subgraphs, including exitless infinite loops or disconnected components, are visited and
    //    analyzed.
    // 3. Predecessors are only re-enqueued when a statement's live-in set actually changes
    //    (!in.equals(liveIn.get(stmt))), ensuring monotonic convergence to a fixed point in
    //    O(|V| + |E|).
    Deque<Stmt> worklist = new ArrayDeque<>();
    Set<Stmt> inWorklist = new HashSet<>();
    for (Stmt stmt : graph.getNodes()) {
      liveIn.put(stmt, Collections.emptySet());
      liveOut.put(stmt, Collections.emptySet());
      worklist.addLast(stmt);
      inWorklist.add(stmt);
    }

    while (!worklist.isEmpty()) {
      Stmt stmt = worklist.removeFirst();
      inWorklist.remove(stmt);

      // Compute OUT[stmt] by taking a fresh union of successors' liveIn sets.
      Set<Local> out = new HashSet<>();
      for (Stmt succ : graph.successors(stmt)) {
        Set<Local> succIn = liveIn.get(succ);
        if (succIn != null) {
          out.addAll(succIn);
        }
      }
      for (Stmt esucc : graph.exceptionalSuccessors(stmt).values()) {
        Set<Local> esuccIn = liveIn.get(esucc);
        if (esuccIn != null) {
          out.addAll(esuccIn);
        }
      }
      liveOut.put(stmt, out);

      // IN[s] = USE[s] Union (OUT[s] - DEF[s])
      Set<Local> in = new HashSet<>();
      for (Iterator<Value> iterator = stmt.getUses().iterator(); iterator.hasNext(); ) {
        Value use = iterator.next();
        if (use instanceof Local) {
          in.add((Local) use);
        }
      }
      // Clone OUT into a fresh set before removing DEFs.
      // This ensures out and succ.liveIn are never mutated in-place when computing (OUT - DEF).
      Set<Local> outMinusDef = new HashSet<>(out);
      final Optional<LValue> def = stmt.getDef();
      if (def.isPresent()) {
        final Value value = def.get();
        if (value instanceof Local) {
          outMinusDef.remove(value);
        }
      }
      in.addAll(outMinusDef);

      // Only propagate backwards if the live-in set changed
      if (!in.equals(liveIn.get(stmt))) {
        liveIn.put(stmt, in);
        for (Stmt pred : graph.predecessors(stmt)) {
          if (inWorklist.add(pred)) {
            worklist.addLast(pred);
          }
        }
        for (Stmt epred : graph.exceptionalPredecessors(stmt)) {
          if (inWorklist.add(epred)) {
            worklist.addLast(epred);
          }
        }
      }
    }
  }

  /** Get all live locals before the given stmt. */
  @NonNull
  public Set<Local> getLiveLocalsBeforeStmt(@NonNull Stmt stmt) {
    if (!liveIn.containsKey(stmt)) {
      throw new RuntimeException("Stmt: " + stmt + " is not in ControlFlowGraph!");
    }
    return liveIn.get(stmt);
  }

  /** Get all live locals after the given stmt. */
  @NonNull
  public Set<Local> getLiveLocalsAfterStmt(@NonNull Stmt stmt) {
    if (!liveOut.containsKey(stmt)) {
      throw new RuntimeException("Stmt: " + stmt + " is not in ControlFlowGraph!");
    }
    return liveOut.get(stmt);
  }
}
