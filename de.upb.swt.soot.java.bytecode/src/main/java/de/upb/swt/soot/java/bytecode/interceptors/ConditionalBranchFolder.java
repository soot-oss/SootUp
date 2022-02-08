package de.upb.swt.soot.java.bytecode.interceptors;
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

import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.stmt.JIfStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import javax.annotation.Nonnull;

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
  public void interceptBody(@Nonnull Body.BodyBuilder builder) {

    final StmtGraph<?> builderStmtGraph = builder.getStmtGraph();
    final StmtGraph<?> stmtGraph = builder.getStmtGraph();

    builder.enableDeferredStmtGraphChanges();
    for (Stmt stmt : stmtGraph.nodes()) {
      if (stmt instanceof JIfStmt) {
        JIfStmt ifStmt = (JIfStmt) stmt;
        // check for constant-valued conditions
        Value condition = ifStmt.getCondition();
        if (Evaluator.isValueConstantValue(condition)) {
          condition = Evaluator.getConstantValueOf(condition);

          if (((IntConstant) condition).getValue() == 1) {
            // the evaluated if condition is always true: redirect all predecessors to the successor
            // of this if-statement and prune the "true"-block stmt tree until another branch flows
            // to a Stmt

            // link previous stmt with branch target of if-Stmt
            final List<Stmt> ifSuccessors = stmtGraph.successors(ifStmt);
            final Stmt fallsThroughStmt = ifSuccessors.get(0);
            Stmt branchTarget = ifSuccessors.get(1);

            builder.removeFlow(ifStmt, fallsThroughStmt);
            builder.removeFlow(ifStmt, branchTarget);

            for (Stmt predecessor : stmtGraph.predecessors(ifStmt)) {
              builder.removeFlow(predecessor, ifStmt);
              builder.addFlow(predecessor, branchTarget);
            }

            Deque<Stmt> stack = new ArrayDeque<>();
            stack.addFirst(fallsThroughStmt);
            // remove all now unreachable stmts from "true"-block
            while (!stack.isEmpty()) {
              Stmt itStmt = stack.pollFirst();
              if (builderStmtGraph.containsNode(itStmt)
                  && builderStmtGraph.predecessors(itStmt).size() < 1) {
                for (Stmt succ : stmtGraph.successors(itStmt)) {
                  builder.removeFlow(itStmt, succ);
                  stack.add(succ);
                }
              }
            }
          }
        }
      }
    }
    builder.commitDeferredStmtGraphChanges();
  }
}
