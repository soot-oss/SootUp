package de.upb.swt.soot.java.bytecode.interceptors;

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

import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.*;
import javax.annotation.Nonnull;

/** @auther Zun Wang */
public class LocalLivenessAnalyser {

  // Each stmt(node) has out-edges and in-edges
  // A local is live-in at a node if it is live on any its in-edges
  private final Map<Stmt, Set<Local>> liveIn = new HashMap<>();
  // A local is live-out at a node if it is live on any of its out-edges.
  // e.g: a = b + c; live-in={b,c}  live-out={a,b,c}
  private final Map<Stmt, Set<Local>> liveOut = new HashMap<>();

  public LocalLivenessAnalyser(@Nonnull StmtGraph graph) {
    // initial liveIn and liveOut
    List<Stmt> startingStmts = new ArrayList<>();
    for (Stmt stmt : graph.nodes()) {
      liveIn.put(stmt, Collections.emptySet());
      liveOut.put(stmt, Collections.emptySet());
      if (graph.successors(stmt).isEmpty()) {
        startingStmts.add(stmt);
      }
    }

    boolean fixed = false;
    while (!fixed) {
      fixed = true;
      Deque<Stmt> queue = new ArrayDeque<>(startingStmts);
      HashSet<Stmt> visitedStmts = new HashSet<>();
      while (!queue.isEmpty()) {
        Stmt stmt = queue.removeFirst();
        visitedStmts.add(stmt);

        Set<Local> out = new HashSet<>(liveOut.get(stmt));
        for (Stmt succ : graph.successors(stmt)) {
          out = merge(out, liveIn.get(succ));
        }
        if (!isEqual(out, liveOut.get(stmt))) {
          fixed = false;
          liveOut.put(stmt, new HashSet<>(out));
        }

        Set<Local> in = new HashSet<>();
        for (Value use : stmt.getUses()) {
          if (use instanceof Local) {
            in.add((Local) use);
          }
        }
        if (!stmt.getDefs().isEmpty() && stmt.getDefs().get(0) instanceof Local) {
          out.remove(stmt.getDefs().get(0));
        }
        in = merge(in, out);
        if (!isEqual(in, liveIn.get(stmt))) {
          fixed = false;
          liveIn.put(stmt, in);
        }
        for (Stmt pred : graph.predecessors(stmt)) {
          if (!visitedStmts.contains(pred)) {
            queue.addLast(pred);
          }
        }
      }
    }
  }

  /**
   * Get all live locals before the given stmt.
   *
   * @param stmt
   * @return
   */
  @Nonnull
  public Set<Local> getLiveLocalsBeforeStmt(@Nonnull Stmt stmt) {
    if (!liveIn.containsKey(stmt)) {
      throw new RuntimeException("Stmt: " + stmt.toString() + " is not in StmtGraph!");
    }
    return liveIn.get(stmt);
  }

  /**
   * Get all live locals after the given stmt.
   *
   * @param stmt
   * @return
   */
  @Nonnull
  public Set<Local> getLiveLocalsAfterStmt(@Nonnull Stmt stmt) {
    if (!liveOut.containsKey(stmt)) {
      throw new RuntimeException("Stmt: " + stmt.toString() + " is not in StmtGraph!");
    }
    return liveOut.get(stmt);
  }

  /**
   * Merge two local sets into one set
   *
   * @param set1
   * @param set2
   * @return a merged local set
   */
  @Nonnull
  private Set<Local> merge(@Nonnull Set<Local> set1, @Nonnull Set<Local> set2) {
    if (set1.isEmpty()) {
      return set2;
    } else {
      for (Local local : set2) {
        if (!set1.contains(local)) {
          set1.add(local);
        }
      }
      return set1;
    }
  }

  /**
   * Check whether two sets contains same locals
   *
   * @param set1
   * @param set2
   * @return if same return true, else return false;
   */
  @Nonnull
  private boolean isEqual(@Nonnull Set<Local> set1, @Nonnull Set<Local> set2) {
    if (set1.size() != set2.size()) {
      return false;
    } else {
      for (Local local : set1) {
        if (!set2.contains(local)) {
          return false;
        }
      }
    }
    return true;
  }
}
