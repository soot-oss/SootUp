package sootup.apk.frontend.interceptors;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2022 - 2024 Kadiray Karakaya, Markus Schmidt, Jonas Klauke, Stefan Schott, Palaniappan Muthuraman, Marcus Hüwe and others
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.views.View;

/**
 * Inlines constant array indices.
 *
 * <p>Dex has no array instruction with an immediate index: {@code arr[1] = x} compiles to {@code
 * const/4 v0, 1; aput v1, v2, v0}, so the Jimple index is a local ({@code arr[v0] = x}) where
 * javac-compiled bytecode has the constant ({@code arr[1] = x}). This rewrites an array index local
 * to its constant when every definition reaching that use assigns the same int constant, so dex
 * bodies get the same shape as bytecode bodies and an analysis can tell array elements apart.
 *
 * <p>Reaching definitions follow normal and exceptional predecessors, so a definition that reaches
 * a handler only through an exception is not missed. A use with any non-constant reaching
 * definition, or with none found, is left unchanged.
 */
public class DexArrayIndexConstantInliner implements BodyInterceptor {

  @Override
  public void interceptBody(Body.@NonNull BodyBuilder builder, @NonNull View view) {
    MutableControlFlowGraph graph = builder.getControlFlowGraph();
    for (Stmt stmt : new ArrayList<>(graph.getNodes())) {
      if (!(stmt instanceof JAssignStmt)) {
        continue;
      }
      JAssignStmt assign = (JAssignStmt) stmt;
      JAssignStmt replacement = assign;
      if (assign.getLeftOp() instanceof JArrayRef) {
        JArrayRef ref = (JArrayRef) assign.getLeftOp();
        IntConstant index = constantIndex(graph, stmt, ref);
        if (index != null) {
          replacement = replacement.withVariable(ref.withIndex(index));
        }
      }
      if (assign.getRightOp() instanceof JArrayRef) {
        JArrayRef ref = (JArrayRef) assign.getRightOp();
        IntConstant index = constantIndex(graph, stmt, ref);
        if (index != null) {
          replacement = replacement.withRValue(ref.withIndex(index));
        }
      }
      if (replacement != assign) {
        graph.replaceNode(stmt, replacement);
      }
    }
  }

  /**
   * The constant {@code ref}'s index local holds at {@code use}, or null if it isn't the same known
   * constant on every path.
   *
   * <p>A backward walk over two kinds of program points: the state <em>after</em> a statement
   * completed (where its definition counts) and the state <em>before</em> a statement. A statement
   * reaches its successors completed, but a handler only before it: an exceptional predecessor
   * threw, so its own definition never happened.
   */
  private static @Nullable IntConstant constantIndex(
      @NonNull MutableControlFlowGraph graph, @NonNull Stmt use, @NonNull JArrayRef ref) {
    if (!(ref.getIndex() instanceof Local)) {
      return null;
    }
    Local index = (Local) ref.getIndex();

    IntConstant constant = null;
    Deque<Stmt> after = new ArrayDeque<>();
    Deque<Stmt> before = new ArrayDeque<>();
    Set<Stmt> visitedAfter = new HashSet<>();
    Set<Stmt> visitedBefore = new HashSet<>();
    before.add(use);
    while (!after.isEmpty() || !before.isEmpty()) {
      while (!before.isEmpty()) {
        Stmt stmt = before.removeFirst();
        if (!visitedBefore.add(stmt)) {
          continue;
        }
        List<Stmt> normal = graph.predecessors(stmt);
        List<Stmt> exceptional = graph.exceptionalPredecessors(stmt);
        if (normal.isEmpty() && exceptional.isEmpty()) {
          // Method entry reached without a definition: the index is not known on this path.
          return null;
        }
        after.addAll(normal);
        before.addAll(exceptional);
      }
      while (!after.isEmpty()) {
        Stmt stmt = after.removeFirst();
        if (!visitedAfter.add(stmt)) {
          continue;
        }
        if (stmt instanceof AbstractDefinitionStmt
            && ((AbstractDefinitionStmt) stmt).getLeftOp().equivTo(index)) {
          if (!(stmt instanceof JAssignStmt)
              || !(((JAssignStmt) stmt).getRightOp() instanceof IntConstant)) {
            return null;
          }
          IntConstant value = (IntConstant) ((JAssignStmt) stmt).getRightOp();
          if (constant != null && constant.getValue() != value.getValue()) {
            return null;
          }
          constant = value;
        } else {
          before.add(stmt);
        }
      }
    }
    return constant;
  }
}
