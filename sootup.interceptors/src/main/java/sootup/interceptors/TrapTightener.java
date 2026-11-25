package sootup.interceptors;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2026 John Jorgensen, Zun Wang
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
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.stmt.JThrowStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.core.exceptions.ExceptionInferResult.ExceptionType;
import sootup.java.core.exceptions.StmtExceptionAnalyzer;

/**
 * Removes exceptional flows that can not be taken at runtime: an exceptional edge of a Stmt to a
 * handler catching <code>T</code> is removed if no exception the Stmt might throw can be caught as
 * <code>T</code>. Afterwards trap handlers which became unreachable are removed.
 *
 * <p>In Soot, the TrapTightener shrinks the protected range of each Trap so that it begins at the
 * first and ends after the last unit which might throw an exception caught by the Trap. In SootUp
 * the Traps are derived from the exceptional flows of the {@link ControlFlowGraph}, so the
 * exceptional flows are tightened per Stmt instead, which can split a Trap into several ones.
 *
 * <p>The exceptions a Stmt might throw are inferred intraprocedurally by the {@link
 * StmtExceptionAnalyzer}. As every Stmt might throw an asynchronous {@link VirtualMachineError},
 * flows to catch-all handlers (e.g. the handlers releasing a monitor of a synchronized block) and
 * handlers of {@link Error} are always kept.
 *
 * @author Zun Wang
 */
public class TrapTightener implements BodyInterceptor {

  @Override
  public void interceptBody(Body.@NonNull BodyBuilder builder, @NonNull View view) {
    MutableControlFlowGraph graph = builder.getControlFlowGraph();
    TypeHierarchy hierarchy = view.getTypeHierarchy();
    StmtExceptionAnalyzer analyzer = new StmtExceptionAnalyzer(hierarchy);

    // collect first, as removing exceptional flows restructures the blocks of the graph
    Map<Stmt, List<ClassType>> unrealizableFlows = new LinkedHashMap<>();
    for (Stmt stmt : graph.getStmts()) {
      Set<ClassType> caughtTypes = graph.exceptionalSuccessors(stmt).keySet();
      if (caughtTypes.isEmpty()) {
        continue;
      }
      Set<ClassType> thrownTypes = mightThrow(stmt, graph, analyzer);
      for (ClassType caughtType : caughtTypes) {
        if (!mightBeCaughtAs(thrownTypes, caughtType, hierarchy)) {
          unrealizableFlows.computeIfAbsent(stmt, k -> new ArrayList<>()).add(caughtType);
        }
      }
    }

    if (unrealizableFlows.isEmpty()) {
      return;
    }
    unrealizableFlows.forEach(
        (stmt, caughtTypes) ->
            caughtTypes.forEach(caughtType -> graph.removeExceptionalEdge(stmt, caughtType)));

    // remove trap handlers that are no longer reachable
    new UnreachableCodeEliminator().interceptBody(builder, view);
  }

  /**
   * @return the exceptions the given stmt might throw, or {@link ExceptionType#THROWABLE} if it
   *     might throw any exception.
   */
  @NonNull
  private static Set<ClassType> mightThrow(
      @NonNull Stmt stmt,
      @NonNull ControlFlowGraph<?> graph,
      @NonNull StmtExceptionAnalyzer analyzer) {
    if (stmt.isInvokableStmt() && stmt.asInvokableStmt().getInvokeExpr().isPresent()) {
      // the analyzer only covers the exceptions of the invoke instruction itself (e.g. resolution
      // errors) - the invoked method can throw anything.
      return Collections.singleton(ExceptionType.THROWABLE);
    }

    Set<ClassType> thrownTypes = new HashSet<>(analyzer.mightThrowImplicitly(stmt).getExceptions());
    if (stmt instanceof JThrowStmt) {
      // athrow itself raises a NullPointerException for a null reference and an
      // IllegalMonitorStateException for unbalanced monitors.
      thrownTypes.add(ExceptionType.NUll_POINTER_EXCEPTION);
      thrownTypes.add(ExceptionType.ILLEGAL_MONITOR_STATE_EXCEPTION);
      JThrowStmt throwStmt = (JThrowStmt) stmt;
      if (throwStmt.getOp() instanceof Local) {
        try {
          thrownTypes.addAll(analyzer.mightThrowExplicitly(throwStmt, graph).getExceptions());
        } catch (IllegalArgumentException | IllegalStateException e) {
          // the type of the thrown exception is unknown to the view -> be conservative
          return Collections.singleton(ExceptionType.THROWABLE);
        }
      }
    }
    return thrownTypes;
  }

  /**
   * An exception of a thrown type might be caught by a handler of caughtType if the thrown type is
   * a subtype of caughtType or - as the runtime type of the exception might be more specific than
   * the inferred type - if caughtType is a subtype of the thrown type.
   */
  private static boolean mightBeCaughtAs(
      @NonNull Set<ClassType> thrownTypes,
      @NonNull ClassType caughtType,
      @NonNull TypeHierarchy hierarchy) {
    if (!hierarchy.contains(caughtType)) {
      // be conservative if the type hierarchy is incomplete
      return true;
    }
    for (ClassType thrownType : thrownTypes) {
      if (thrownType.equals(caughtType)
          || !hierarchy.contains(thrownType)
          || hierarchy.isSubtype(caughtType, thrownType)
          || hierarchy.isSubtype(thrownType, caughtType)) {
        return true;
      }
    }
    return false;
  }
}
