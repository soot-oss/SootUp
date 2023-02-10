package sootup.java.bytecode.interceptors;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Marcus Nachtigall, Markus Schmidt and others
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

import com.google.common.collect.Lists;
import java.util.*;
import javax.annotation.Nonnull;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.constant.Constant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.transform.BodyInterceptor;
import sootup.core.views.View;

/**
 * Statically evaluates the conditional expression of Jimple if statements. If the condition is
 * identically true or false, the Folder replaces the conditional branch statement with an
 * unconditional goto statement
 *
 * @author Marcus Nachtigall
 * @author Markus Schmidt
 */
public class ConditionalBranchFolder implements BodyInterceptor {

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder, @Nonnull View<?> view) {

    final MutableStmtGraph stmtGraph = builder.getStmtGraph();

    for (Stmt stmt : Lists.newArrayList(stmtGraph.getNodes())) {
      if (!(stmt instanceof JIfStmt)) {
        continue;
      }

      JIfStmt ifStmt = (JIfStmt) stmt;
      // check for constant-valued conditions
      Constant evaluatedCondition = Evaluator.getConstantValueOf(ifStmt.getCondition());
      if (evaluatedCondition == null) {
        // not or not "easy" evaluatable
        continue;
      }

      final List<Stmt> ifSuccessors = stmtGraph.successors(ifStmt);
      final Stmt tautologicSuccessor;
      final Stmt neverReachedSucessor;
      if (((IntConstant) evaluatedCondition).getValue() == 1) {
        // the evaluated if evaluatedCondition is always true: redirect all predecessors to the
        // successor
        // of this if-statement and prune the "true"-block stmt tree until another branch flows
        // to a Stmt
        tautologicSuccessor = ifSuccessors.get(0);
        neverReachedSucessor = ifSuccessors.get(1);
      } else if (((IntConstant) evaluatedCondition).getValue() == 0) {
        // the evaluated evaluatedCondition is always false remove the fallsthrough successor etc.
        tautologicSuccessor = ifSuccessors.get(1);
        neverReachedSucessor = ifSuccessors.get(0);
      } else {
        // should not occur?
        continue;
      }

      // link previous stmt with always-reached successor of the if-Stmt
      for (Stmt predecessor : stmtGraph.predecessors(ifStmt)) {
        builder.removeFlow(predecessor, ifStmt);
        builder.addFlow(predecessor, tautologicSuccessor);
      }

      // removeFlow calls should be obsolete as of following removeStmt
      builder.removeFlow(ifStmt, tautologicSuccessor);
      builder.removeFlow(ifStmt, neverReachedSucessor);

      builder.removeStmt(ifStmt);

      pruneExclusivelyReachableStmts(stmtGraph, neverReachedSucessor);
    }
  }

  private void pruneExclusivelyReachableStmts(
      @Nonnull MutableStmtGraph stmtGraph, @Nonnull Stmt fallsThroughStmt) {
    Set<Stmt> reachedBranchingStmts = new HashSet<>();
    Deque<Stmt> q = new ArrayDeque<>();

    q.addFirst(fallsThroughStmt);
    // stmts we want to remove
    // remove all now unreachable stmts from "true"-block
    while (!q.isEmpty()) {
      Stmt itStmt = q.pollFirst();
      if (itStmt.branches()) {
        // reachable branching stmts that may or may not branch to another reachable stmt is all we
        // are actually interested in
        reachedBranchingStmts.add(itStmt);
      }
      if (stmtGraph.containsNode(itStmt)) {
        final List<Stmt> predecessors = stmtGraph.predecessors(itStmt);
        if (predecessors.size() <= 1) {
          q.addAll(stmtGraph.successors(itStmt));
        }
      }
    }
    // now iterate again and remove if possible: ie predecessor.size() < 1
    q.addFirst(fallsThroughStmt);
    while (!q.isEmpty()) {
      Stmt itStmt = q.pollFirst();
      if (stmtGraph.containsNode(itStmt)) {
        // hint: predecessor could also be already removed
        if (isExclusivelyReachable(stmtGraph, itStmt, reachedBranchingStmts)) {
          q.addAll(stmtGraph.successors(itStmt));
          stmtGraph.removeNode(itStmt);
        }
      }
    }
  }

  /** reachedStmts contains all reached Stmts from entrypoint which ALSO do branch! */
  private boolean isExclusivelyReachable(
      @Nonnull StmtGraph<?> graph, @Nonnull Stmt stmt, @Nonnull Set<Stmt> reachedStmts) {
    final List<Stmt> predecessors = graph.predecessors(stmt);
    final int predecessorSize = predecessors.size();
    int amount = predecessorSize;
    if (predecessorSize <= 1) {
      // we already reached this stmt somehow via reachable stmts so at least one predecessor was
      // reachable which makes it exclusively reachable if there are no other ingoing flows
      // hint: <= because a predecessor could already be removed
      return true;
    }
    for (Stmt predecessor : predecessors) {
      if (predecessor.fallsThrough()) {
        if (predecessor instanceof JIfStmt) {
          final List<Stmt> predsSuccessors = graph.successors(predecessor);
          if (predsSuccessors.size() > 0 && predsSuccessors.get(0) == stmt) {
            // TODO: hint: possible problem occurs with partial removed targets as they change the
            // idx positions..
            amount--;
            continue;
          }
        } else {
          // "usual" fallsthrough
          amount--;
          continue;
        }
      }
      // was a branching predecessor reachable?
      if (reachedStmts.contains(predecessor)) {
        amount--;
        continue;
      }
    }
    return amount == 0;
  }
}
