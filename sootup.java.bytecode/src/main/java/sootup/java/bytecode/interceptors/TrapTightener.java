package sootup.java.bytecode.interceptors;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 John Jorgensen, Zun Wang
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
import javax.annotation.Nonnull;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Trap;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.javabytecode.stmt.JEnterMonitorStmt;
import sootup.core.jimple.javabytecode.stmt.JExitMonitorStmt;
import sootup.core.model.Body;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * @author Zun Wang
 *     <p>description from Soot: A BodyTransformer that shrinks the protected area covered by each
 *     Trap in the Body so that it begins at the first of the Body's Units which might throw an
 *     exception caught by the Trap and ends just after the last Unit which might throw an exception
 *     caught by the Trap. In the case where none of the Units protected by a Trap can throw the
 *     exception it catches, the Trap's protected area is left completely empty, which will likely
 *     cause the UnreachableCodeEliminator to remove the Trap(handler?) completely (if the
 *     traphandler does not cover another range). The TrapTightener is used to reduce the risk of
 *     unverifiable code which can result from the use of ExceptionalUnitGraphs from which
 *     unrealizable exceptional control flow edges have been removed.
 */
public class TrapTightener implements BodyInterceptor {

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder, @Nonnull View<?> view) {

    // FIXME: [ms] ThrowAnalysis is missing and in result mightThrow (...) makes no sense. Issue
    // #486
    if (true) {
      return;
    }

    MutableStmtGraph graph = builder.getStmtGraph();
    List<Stmt> stmtsInPrintOrder = builder.getStmts();

    // collect stmts
    Set<Stmt> monitoredStmts = monitoredStmts(graph);
    Map<Stmt, Collection<ClassType>> toRemove = new HashMap<>();
    for (BasicBlock<?> block : graph.getBlocks()) {
      for (Stmt stmt : block.getStmts()) {

        Collection<ClassType> removeForStmt = new ArrayList<>();
        for (Map.Entry<? extends ClassType, ?> exception :
            block.getExceptionalSuccessors().entrySet()) {

          // FIXME: check for java9 modules signature, too!
          boolean isCatchAll =
              exception.getKey().getFullyQualifiedName().equals("java.lang.Throwable");

          if (
          /* mightThrow(graph, stmt, trap) || */ (isCatchAll && monitoredStmts.contains(stmt))) {
            // if it might throw or if trap is a catch-all block and the current stmt has an active
            // monitor, we need to keep the block
            removeForStmt.add(exception.getKey());
            break;
          }
        }
        if (!removeForStmt.isEmpty()) {
          toRemove.put(stmt, removeForStmt);
        }
      }
    }

    // remove exceptions for stmts
    for (Map.Entry<Stmt, Collection<ClassType>> entry : toRemove.entrySet()) {
      for (ClassType classType : entry.getValue()) {
        graph.removeExceptionalEdge(entry.getKey(), classType);
      }
    }

    // FIXME: check if there are traphandlers that have no predecessor

  }

  /**
   * Find out all monitored stmts from a given exceptional graph, collect them into a list
   *
   * @param graph a given exceptionalStmtGraph
   * @return a list of monitored stmts
   */
  private Set<Stmt> monitoredStmts(@Nonnull StmtGraph<?> graph) {
    Set<Stmt> monitoredStmts = new HashSet<>();
    Deque<Stmt> queue = new ArrayDeque<>();
    queue.add(graph.getStartingStmt());
    Set<Stmt> visitedStmts = new HashSet<>();

    while (!queue.isEmpty()) {
      Stmt stmt = queue.removeFirst();
      visitedStmts.add(stmt);
      // enter a monitored block
      if (stmt instanceof JEnterMonitorStmt) {
        Deque<Stmt> monitoredQueue = new ArrayDeque<>();
        monitoredQueue.add(stmt);
        while (!monitoredQueue.isEmpty()) {
          Stmt monitoredStmt = monitoredQueue.removeFirst();
          monitoredStmts.add(monitoredStmt);
          visitedStmts.add(monitoredStmt);
          if (monitoredStmt instanceof JExitMonitorStmt) {
            queue.addAll(graph.getAllSuccessors(monitoredStmt));
          } else {
            for (Stmt succ : graph.getAllSuccessors(monitoredStmt)) {
              if (!visitedStmts.contains(succ)) {
                monitoredQueue.add(succ);
              }
            }
          }
        }
      } else {
        queue.addAll(graph.getAllSuccessors(stmt));
      }
    }
    return monitoredStmts;
  }

  /**
   * Check whether trap-destinations of the given stmt contain the given trap.
   *
   * @param graph is a exceptional StmtGraph
   * @param stmt is a stmt in the given graph
   * @param trap is a given trap
   * @return If trap-destinations of the given stmt contain the given trap, return true, otherwise
   *     return false
   */

  // FIXME: [ms] makes no sense in that Implementation! StmtGraph is not the legacy
  // ExceptionalUnitGraph
  private boolean mightThrow(@Nonnull StmtGraph<?> graph, @Nonnull Stmt stmt, @Nonnull Trap trap) {
    final BasicBlock<?> block = graph.getBlockOf(stmt);

    for (Map.Entry<? extends ClassType, ? extends BasicBlock<?>> dest :
        block.getExceptionalSuccessors().entrySet()) {
      final ClassType exceptionType = dest.getKey();
      final BasicBlock<?> traphandlerBlock = dest.getValue();
      if (exceptionType.equals(trap.getExceptionType())
          && traphandlerBlock.getHead().equals(trap.getHandlerStmt())) {
        return true;
      }
    }
    return false;
  }
}
