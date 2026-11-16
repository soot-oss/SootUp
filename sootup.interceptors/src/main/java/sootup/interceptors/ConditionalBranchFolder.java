package sootup.interceptors;

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
import org.jspecify.annotations.NonNull;
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.stmt.JGotoStmt;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
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
  public void interceptBody(Body.@NonNull BodyBuilder builder, @NonNull View view) {

    final MutableControlFlowGraph controlFlowGraph = builder.getControlFlowGraph();

    boolean modified = false;
    for (Stmt stmt : Lists.newArrayList(controlFlowGraph.getNodes())) {
      if (!(stmt instanceof JIfStmt ifStmt)) {
        continue;
      }

      // check for constant-valued conditions
      Constant evaluatedCondition = Evaluator.getConstantValueOf(ifStmt.getCondition());

      boolean removeTrueBranch;
      if (evaluatedCondition instanceof BooleanConstant) {
        removeTrueBranch = evaluatedCondition == BooleanConstant.getFalse();
      } else if (evaluatedCondition instanceof IntConstant) {
        removeTrueBranch =
            IntConstant.getInstance(0).equalEqual(((IntConstant) evaluatedCondition))
                == BooleanConstant.getTrue();
      }
      /* TODO: check if the following Constant types are even possible in valid Jimple */
      else if (evaluatedCondition instanceof DoubleConstant) {
        removeTrueBranch =
            DoubleConstant.getInstance(0).equalEqual((DoubleConstant) evaluatedCondition)
                == BooleanConstant.getTrue();
      } else if (evaluatedCondition instanceof FloatConstant) {
        removeTrueBranch =
            FloatConstant.getInstance(0).equalEqual((FloatConstant) evaluatedCondition)
                == BooleanConstant.getTrue();
      } else if (evaluatedCondition instanceof LongConstant) {
        removeTrueBranch =
            LongConstant.getInstance(0).equalEqual((LongConstant) evaluatedCondition)
                == BooleanConstant.getTrue();
      } else {
        // not or not "easy" evaluatable
        continue;
      }

      final List<Stmt> ifSuccessors = controlFlowGraph.successors(ifStmt);
      final Stmt tautologicSuccessor;
      final Stmt neverReachedSucessor;

      if (removeTrueBranch) {
        // the evaluatedCondition is always false: keep the fallsthrough successor
        tautologicSuccessor = ifSuccessors.get(JIfStmt.FALSE_BRANCH_IDX);
        neverReachedSucessor = ifSuccessors.get(JIfStmt.TRUE_BRANCH_IDX);
      } else {
        // the evaluatedCondition is always true: keep the branching successor
        tautologicSuccessor = ifSuccessors.get(JIfStmt.TRUE_BRANCH_IDX);
        neverReachedSucessor = ifSuccessors.get(JIfStmt.FALSE_BRANCH_IDX);
      }

      // drop the never reached flow and replace the if-Stmt by a goto to the remaining successor
      // (keeps all incoming flows untouched)
      controlFlowGraph.removeEdge(ifStmt, neverReachedSucessor);
      JGotoStmt gotoStmt = Jimple.newGotoStmt(ifStmt.getPositionInfo());
      controlFlowGraph.replaceNode(ifStmt, gotoStmt);
      if (controlFlowGraph.successors(gotoStmt).isEmpty()) {
        // both branches targeted the same Stmt -> removeEdge dropped both flows
        controlFlowGraph.putEdge(gotoStmt, JGotoStmt.BRANCH_IDX, tautologicSuccessor);
      }
      modified = true;
    }

    if (modified) {
      // prune blocks that are not reachable anymore
      new UnreachableCodeEliminator().interceptBody(builder, view);
    }
  }
}
