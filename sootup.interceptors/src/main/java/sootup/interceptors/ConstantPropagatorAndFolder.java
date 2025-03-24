package sootup.interceptors;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Christian Brüggemann
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import org.jspecify.annotations.NonNull;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.Constant;
import sootup.core.jimple.common.constant.NullConstant;
import sootup.core.jimple.common.constant.NumericConstant;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.jimple.common.expr.AbstractBinopExpr;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.transform.BodyInterceptor;
import sootup.core.views.View;

/**
 * Does constant propagation and folding. Constant folding is the compile-time evaluation of
 * constant expressions (i.e. 2 * 3).
 *
 * @author Marcus Nachtigall
 */
public class ConstantPropagatorAndFolder implements BodyInterceptor {

  @Override
  public void interceptBody(Body.@NonNull BodyBuilder builder, @NonNull View view) {
    List<Stmt> defs = new ArrayList<>();

    // Perform a constant/local propagation pass
    // go through each use in each statement
    MutableStmtGraph stmtGraph = builder.getStmtGraph();
    for (Stmt stmt : Lists.newArrayList(stmtGraph)) {
      // propagation pass
      if (stmt instanceof JAssignStmt) {
        Value rhs = ((AbstractDefinitionStmt) stmt).getRightOp();
        if (rhs instanceof AbstractBinopExpr) {
          Value op1 = ((AbstractBinopExpr) rhs).getOp1();
          Value op2 = ((AbstractBinopExpr) rhs).getOp2();

          if (op1 instanceof NumericConstant && op2 instanceof NumericConstant) {
            defs.add(stmt);
          }
        }

        // folding
        BiConsumer<Constant, Stmt> constantStmtBiConsumer =
            (Constant evaluatedValue, Stmt foldingStmt) -> {
              JAssignStmt assignStmt = ((JAssignStmt) foldingStmt).withRValue(evaluatedValue);
              stmtGraph.replaceNode(foldingStmt, assignStmt);
              defs.remove(foldingStmt);
              defs.add(assignStmt);
            };

        fold(stmt, constantStmtBiConsumer);

      } else if (stmt instanceof JReturnStmt) {
        for (Iterator<Value> iterator = stmt.getUses().iterator(); iterator.hasNext(); ) {
          Value value = iterator.next();
          if (!(value instanceof Local)) {
            continue;
          }
          List<AbstractDefinitionStmt> defsOfUse = ((Local) value).getDefs(defs);
          if (defsOfUse.size() != 1) {
            continue;
          }

          AbstractDefinitionStmt definitionStmt = defsOfUse.get(0);
          Value rhs = definitionStmt.getRightOp();
          if (rhs instanceof NumericConstant
              || rhs instanceof StringConstant
              || rhs instanceof NullConstant) {
            JReturnStmt returnStmt = ((JReturnStmt) stmt).withReturnValue((Immediate) rhs);
            stmtGraph.replaceNode(stmt, returnStmt);
            stmt = returnStmt;
            defs.add(returnStmt); // [ms]: JReturnStmt seems weird as a def? but Soot had it, too.
          }
        }

        // folding
        BiConsumer<Constant, Stmt> constantStmtBiConsumer =
            (Constant evaluatedValue, Stmt foldingStmt) -> {
              JReturnStmt returnStmt = ((JReturnStmt) foldingStmt).withReturnValue(evaluatedValue);
              stmtGraph.replaceNode(foldingStmt, returnStmt);
            };

        fold(stmt, constantStmtBiConsumer);
      }
    }
  }

  private static void fold(Stmt stmt, BiConsumer<Constant, Stmt> constantStmtBiConsumer) {
    for (Iterator<Value> iterator = stmt.getUses().iterator(); iterator.hasNext(); ) {
      Value value = iterator.next();

      Constant evaluatedValue = Evaluator.getConstantValueOf(value);
      if (evaluatedValue == null) {
        continue;
      }

      if (evaluatedValue == value) {
        // nothing was simplified
        continue;
      }

      constantStmtBiConsumer.accept(evaluatedValue, stmt);
    }
  }
}
