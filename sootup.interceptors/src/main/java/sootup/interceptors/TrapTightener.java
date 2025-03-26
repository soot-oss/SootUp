package sootup.interceptors;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2025 John Jorgensen, Zun Wang
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
import sootup.core.graph.*;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.javabytecode.stmt.JEnterMonitorStmt;
import sootup.core.jimple.javabytecode.stmt.JExitMonitorStmt;
import sootup.core.model.Body;
import sootup.core.transform.BodyInterceptor;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.core.exceptions.StmtExceptionAnalyzer;

/**
 * @author Zun Wang
 *     <p>description from Soot: A BodyTransformer that shrinks the protected area covered by each
 *     Trap in the Body so that it begins at the first of the Body's Units which might throw an
 *     exception caught by the Trap and ends just after the last Unit which might throw an exception
 *     caught by the Trap. In the case where none of the Units protected by a Trap can throw the
 *     exception it catches, the Trap's protected area is left completely empty, which will likely
 *     cause the UnreachableCodeEliminator to remove the Trap completely. The TrapTightener is used
 *     to reduce the risk of unverifiable code which can result from the use of
 *     ExceptionalUnitGraphs from which unrealizable exceptional control flow edges have been
 *     removed.
 */
public class TrapTightener implements BodyInterceptor {

  TypeHierarchy hierarchy;
  StmtExceptionAnalyzer exceptionAnalyzer;

  @Override
  public void interceptBody(Body.@NonNull BodyBuilder builder, @NonNull View view) {

    this.hierarchy = view.getTypeHierarchy();
    this.exceptionAnalyzer = new StmtExceptionAnalyzer(hierarchy);
    MutableBlockStmtGraph blockGraph = (MutableBlockStmtGraph) builder.getStmtGraph();

    Set<Stmt> monitoredStmts = monitoredStmts(blockGraph);

    Map<Stmt, Collection<ClassType>> toRemove = new HashMap<>();
    for (Stmt stmt : builder.getStmts()) {
      Map<ClassType, Stmt> exceptionalMap = blockGraph.exceptionalSuccessors(stmt);
      for (ClassType exceptionType : exceptionalMap.keySet()) {
        if (!isCatchAll(exceptionType) || !monitoredStmts.contains(stmt)) {
          if (!canThrowExceptionInGraph(blockGraph, stmt, exceptionType)) {
            if (!toRemove.containsKey(stmt)) {
              toRemove.put(stmt, new HashSet<>());
            }
            toRemove.get(stmt).add(exceptionType);
          }
        }
      }
    }

    // remove exceptions for stmts
    Set<MutableBasicBlock> mutatedBlocks = new HashSet<>();
    for (Map.Entry<Stmt, Collection<ClassType>> entry : toRemove.entrySet()) {
      Stmt stmt = entry.getKey();
      MutableBasicBlock block = (MutableBasicBlock) blockGraph.getBlockOf(stmt);
      blockGraph.splitAndExcludeStmtFromBlock(stmt, block);
      for (ClassType classType : entry.getValue()) {
        MutableBasicBlock blockWithUnthrowableException =
            (MutableBasicBlock) blockGraph.getBlockOf(entry.getKey());
        blockWithUnthrowableException.removeExceptionalSuccessorBlock(classType);
        mutatedBlocks.add(blockWithUnthrowableException);
      }
    }
    mutatedBlocks.stream().forEach(block -> blockGraph.tryMergeIntoSurroundingBlocks(block));

    // delete the unused traps
    UnreachableCodeEliminator codeEliminator = new UnreachableCodeEliminator();
    codeEliminator.interceptBody(builder, view);
  }

  /**
   * Find out all monitored stmts from a given StmtGraph, collect them into a list
   *
   * @param graph a given StmtGraph
   * @return a set of monitored stmts
   */

  public Set<Stmt> monitoredStmts(@NonNull StmtGraph<?> graph) {
    Map<Stmt, Set<Value>> monitored = new HashMap<>();
    
    Deque<Stmt> queue = new ArrayDeque<>();
    queue.add(graph.getStartingStmt());
    Set<Stmt> visitedStmts = new HashSet<>();
    Value exitValue = null;

    while (!queue.isEmpty()) {
      boolean hasChanged = false;
      Stmt currStmt = queue.removeFirst();
      if (currStmt instanceof JEnterMonitorStmt) {
        Value monitoredValue = ((JEnterMonitorStmt) currStmt).getOp();
        if (!monitored.containsKey(currStmt)) {
          monitored.put(currStmt, new HashSet<>());
        }
        hasChanged = monitored.get(currStmt).add(monitoredValue);
      } else if (currStmt instanceof JExitMonitorStmt) {
        exitValue = ((JExitMonitorStmt) currStmt).getOp();
      }
      for (Stmt pred : graph.predecessors(currStmt)) {
        if (monitored.containsKey(pred)) {
          for (Value value : monitored.get(pred)) {
            if (value != exitValue) {
              if (!monitored.containsKey(currStmt)) {
                monitored.put(currStmt, new HashSet<>());
              }
              hasChanged = monitored.get(currStmt).add(value);
            }
          }
        }
      }
      if (visitedStmts.add(currStmt) || hasChanged) {
        queue.addAll(graph.getAllSuccessors(currStmt));
      }
    }
    return monitored.keySet();
  }

  /**
   * Check whether the given stmt could throw the exception interpreted by a given stmtGraph
   *
   * @param graph is a StmtGraph
   * @param stmt is a stmt in the given graph
   * @return true, if the given stmt can throw the exception stored in the given stmtGraph
   */
  private boolean canThrowExceptionInGraph(
      @NonNull StmtGraph<?> graph, @NonNull Stmt stmt, @NonNull ClassType exceptionType) {
    Set<ClassType> inferredExceptions = exceptionAnalyzer.mightThrow(stmt, graph).getExceptions();
    boolean isThrowable =
        inferredExceptions.stream()
            .anyMatch(
                inferredException ->
                    (inferredException.equals(exceptionType)
                        || hierarchy.isSubtype(inferredException, exceptionType)));
    return isThrowable;
  }

  private boolean isCatchAll(ClassType exceptionType) {
    return exceptionType.getFullyQualifiedName().equals("java.lang.Throwable")
        || exceptionType.getFullyQualifiedName().equals("java.base/java.lang.Throwable");
  }
}
