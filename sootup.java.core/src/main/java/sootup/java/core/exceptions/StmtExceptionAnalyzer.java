package sootup.java.core.exceptions;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2025 Zun Wang
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

import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.JThrowStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.*;

/** An analyzer for a <code>Stmt</code> to determine the exceptions it might throw. */
public class StmtExceptionAnalyzer {

  private final TypeHierarchy hierarchy;

  public StmtExceptionAnalyzer(TypeHierarchy hierarchy) {
    this.hierarchy = hierarchy;
  }

  public ExceptionInferResult mightThrow(
      @NonNull Stmt stmt, @NonNull StmtGraph<? extends BasicBlock<?>> graph) {
    if (stmt instanceof JThrowStmt) {
      return mightThrowExplicitly((JThrowStmt) stmt, graph);
    } else {
      return mightThrowImplicitly(stmt);
    }
  }

  public ExceptionInferResult mightThrowExplicitly(
      @NonNull JThrowStmt throwStmt, @NonNull StmtGraph<? extends BasicBlock<?>> graph) {
    Immediate throwExpression = throwStmt.getOp();
    if (!(throwExpression instanceof Local)) {
      throw new IllegalStateException(
          "The given throwStmt: \"" + throwStmt + "\" doesn't throw a local!");
    }
    Local exceptionLocal = (Local) throwExpression;
    Type throwType = exceptionLocal.getType();
    if (throwType == null || throwType instanceof UnknownType) {
      return ExceptionInferResult.createThrowableExceptions();
    }
    if (throwType instanceof NullType) {
      return ExceptionInferResult.createNullPointerException();
    }
    if (!(throwType instanceof ClassType)) {
      throw new IllegalStateException("The type of " + throwStmt + " is not a ClassType!");
    }
    Type preciserType = findPreciserType(exceptionLocal, graph);
    if (preciserType != null) {
      if (!(preciserType instanceof ClassType)) {
        throw new IllegalStateException("The type of " + preciserType + " is not a ClassType!");
      }
      throwType = preciserType;
    }
    return ExceptionInferResult.createSingleException((ClassType) throwType, hierarchy);
  }

  private Type findPreciserType(
      @NonNull Local local, @NonNull StmtGraph<? extends BasicBlock<?>> graph) {
    Type preciserType = null;
    Set<AbstractDefinitionStmt> defStmtsOfLocal =
        graph.getStmts().stream()
            .filter(AbstractDefinitionStmt.class::isInstance)
            .map(AbstractDefinitionStmt.class::cast)
            .filter(def -> def.getLeftOp() == local)
            .collect(Collectors.toSet());
    Set<Value> aliasesOfLocal =
        defStmtsOfLocal.stream()
            .map(AbstractDefinitionStmt::getRightOp)
            .collect(Collectors.toSet());
    Set<Type> allocationTypes =
        aliasesOfLocal.stream()
            .filter(JNewExpr.class::isInstance)
            .map(Value::getType)
            .collect(Collectors.toSet());
    if (allocationTypes.size() == 1) {
      preciserType = allocationTypes.iterator().next();
    }
    return preciserType;
  }

  public ExceptionInferResult mightThrowImplicitly(Stmt stmt) {
    ExceptionInferStmtVisitor stmtVisitor = new ExceptionInferStmtVisitor(hierarchy);
    stmt.accept(stmtVisitor);
    return stmtVisitor.getResult();
  }
}
