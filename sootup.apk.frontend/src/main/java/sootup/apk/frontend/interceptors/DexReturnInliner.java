package sootup.apk.frontend.interceptors;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallee-Rai, Linghui Luo, Markus Schmidt and others
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.JGotoStmt;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.JReturnVoidStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * Dex code shares one return instruction between the branches of a method: the branches jump to it.
 * Like Soot's <code>DexReturnInliner</code>, this interceptor gives the branches their own returns:
 * a <code>goto</code> to a return becomes a copy of the return, and every <code>if</code> that
 * jumps to a return that is also reached otherwise jumps to its own copy. Afterwards, a return is
 * only shared by cases of a <code>switch</code> that jump to it directly, as in Soot, so constant
 * values can be propagated into returns.
 */
public class DexReturnInliner implements BodyInterceptor {

  private static boolean isReturn(Stmt stmt) {
    return stmt instanceof JReturnStmt || stmt instanceof JReturnVoidStmt;
  }

  private static Stmt copyReturn(Stmt ret, StmtPositionInfo position) {
    if (ret instanceof JReturnStmt) {
      return Jimple.newReturnStmt(((JReturnStmt) ret).getOp(), position);
    }
    return Jimple.newReturnVoidStmt(position);
  }

  /**
   * Whether {@code pred} reaches {@code ret} only by jumping to it, so that it can jump to a copy
   * of {@code ret} instead. A predecessor that falls into {@code ret} (a statement before it, or an
   * {@code if} whose false branch it is) or jumps to it in another way, e.g. a {@code switch},
   * keeps the original.
   */
  private static boolean canJumpToCopy(MutableControlFlowGraph graph, Stmt pred, Stmt ret) {
    if (!(pred instanceof JIfStmt)) {
      return false;
    }
    List<Stmt> successors = graph.successors(pred);
    return successors.get(JIfStmt.TRUE_BRANCH_IDX) == ret
        && successors.get(JIfStmt.FALSE_BRANCH_IDX) != ret;
  }

  @Override
  public void interceptBody(Body.@NonNull BodyBuilder builder, @NonNull View view) {
    MutableControlFlowGraph graph = builder.getControlFlowGraph();

    // Replace jumps to returns by copies of the returns. A jump to a jump to a return becomes a
    // jump to a return in the first round.
    boolean changed;
    do {
      changed = false;
      for (Stmt stmt : new ArrayList<>(graph.getNodes())) {
        if (!(stmt instanceof JGotoStmt)) {
          continue;
        }
        Stmt target = graph.successors(stmt).get(0);
        if (!isReturn(target)) {
          continue;
        }
        graph.replaceNode(stmt, copyReturn(target, stmt.getPositionInfo()));
        if (graph.predecessors(target).isEmpty()
            && graph.exceptionalPredecessors(target).isEmpty()
            && graph.getStartingStmt() != target) {
          graph.removeNode(target, false);
        }
        changed = true;
      }
    } while (changed);

    // Let conditional jumps to shared returns jump to copies of the returns
    for (Stmt ret : new ArrayList<>(graph.getNodes())) {
      if (!isReturn(ret)) {
        continue;
      }
      List<Stmt> predecessors = new ArrayList<>(graph.predecessors(ret));
      if (predecessors.size() < 2) {
        continue;
      }
      Map<ClassType, Stmt> traps = graph.exceptionalSuccessors(ret);
      // the predecessors that only jump to the return; if no other predecessor reaches it, the
      // first of them keeps it
      List<Stmt> redirected =
          predecessors.stream()
              .filter(pred -> canJumpToCopy(graph, pred, ret))
              .collect(Collectors.toList());
      if (redirected.size() == predecessors.size()) {
        redirected.remove(0);
      }
      for (Stmt pred : redirected) {
        Stmt copy = copyReturn(ret, ret.getPositionInfo());
        graph.addNode(copy, traps);
        List<Stmt> successors = new ArrayList<>(graph.successors(pred));
        successors.set(JIfStmt.TRUE_BRANCH_IDX, copy);
        graph.setEdges((JIfStmt) pred, successors);
      }
    }
  }
}
