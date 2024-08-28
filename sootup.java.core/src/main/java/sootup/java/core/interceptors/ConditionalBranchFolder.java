package sootup.java.core.interceptors;
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
import sootup.core.jimple.Jimple;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.stmt.JGotoStmt;
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
  public void interceptBody(@Nonnull Body.BodyBuilder builder, @Nonnull View view) {

    final MutableStmtGraph stmtGraph = builder.getStmtGraph();

    for (Stmt stmt : Lists.newArrayList(stmtGraph.getNodes())) {
      if (!(stmt instanceof JIfStmt)) {
        continue;
      }

      JIfStmt ifStmt = (JIfStmt) stmt;
      // check for constant-valued conditions
      Constant evaluatedCondition = Evaluator.getConstantValueOf(ifStmt.getCondition());

      boolean removeTrueBranch;
      if (evaluatedCondition instanceof BooleanConstant) {
        removeTrueBranch = evaluatedCondition == BooleanConstant.getTrue();
      } else if (evaluatedCondition instanceof IntConstant) {
        removeTrueBranch =
            IntConstant.getInstance(0).equalEqual(((IntConstant) evaluatedCondition))
                == BooleanConstant.getFalse();
      }
      /* TODO: check if the following Constant types are even possible in valid Jimple */
      else if (evaluatedCondition instanceof DoubleConstant) {
        removeTrueBranch =
            DoubleConstant.getInstance(0).equalEqual((DoubleConstant) evaluatedCondition)
                == BooleanConstant.getFalse();
      } else if (evaluatedCondition instanceof FloatConstant) {
        removeTrueBranch =
            FloatConstant.getInstance(0).equalEqual((FloatConstant) evaluatedCondition)
                == BooleanConstant.getFalse();
      } else if (evaluatedCondition instanceof LongConstant) {
        removeTrueBranch =
            LongConstant.getInstance(0).equalEqual((LongConstant) evaluatedCondition)
                == BooleanConstant.getFalse();
      } else {
        // not or not "easy" evaluatable
        continue;
      }

      List<Stmt> ifSuccessors = stmtGraph.successors(ifStmt);
      // The successors of IfStmt have true branch at index 0 & false branch at index 1.
      // However, in other parts of code, TRUE_BRANCH_IDX is defined as 1 & FALSE_BRANCH_IDX as 0.
      // To maintain consistency, we need to reverse the order of the successors.
      ifSuccessors = Lists.reverse(ifSuccessors);
      final Stmt tautologicSuccessor;
      final Stmt neverReachedSucessor;

      if (removeTrueBranch) {
        // the if evaluatedCondition is always true: redirect all predecessors to the
        // successor
        // of this if-statement and prune the "true"-block stmt tree until another branch flows
        // to a Stmt
        tautologicSuccessor = ifSuccessors.get(JIfStmt.FALSE_BRANCH_IDX);
        neverReachedSucessor = ifSuccessors.get(JIfStmt.TRUE_BRANCH_IDX);
      } else {
        // the evaluatedCondition is always false remove the fallsthrough successor etc.
        tautologicSuccessor = ifSuccessors.get(JIfStmt.TRUE_BRANCH_IDX);
        neverReachedSucessor = ifSuccessors.get(JIfStmt.FALSE_BRANCH_IDX);
      }

      // remove edge from ifStmt to neverReachedSucessor
      stmtGraph.unLinkNodes(ifStmt, neverReachedSucessor);
      // replace ifStmt block by gotoStmt
      JGotoStmt gotoStmt = Jimple.newGotoStmt(ifStmt.getPositionInfo());
      stmtGraph.replaceStmt(ifStmt, gotoStmt);

      // Call Unreachable Code Eliminator for pruning unreachable blocks
      new UnreachableCodeEliminator().interceptBody(builder, view);
    }
  }
}
