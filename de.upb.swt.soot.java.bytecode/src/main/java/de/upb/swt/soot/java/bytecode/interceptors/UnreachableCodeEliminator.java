package de.upb.swt.soot.java.bytecode.interceptors;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Christian Brüggemann, Zun Wang
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
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.ref.JCaughtExceptionRef;
import de.upb.swt.soot.core.jimple.common.stmt.JIdentityStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * A BodyInterceptor that removes all unreachable stmts from the given Body.
 *
 * @author Zun Wang
 */
public class UnreachableCodeEliminator implements BodyInterceptor {

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder) {

    StmtGraph graph = builder.getStmtGraph();
    Set<Stmt> stmtsInBody = graph.nodes();

    Set<Stmt> unreachableStmts = new HashSet<>();
    Set<Stmt> reachableStmts = new HashSet<>();

    // get all start stmts: startingStmt and handlerStmts
    Deque<Stmt> queue = new ArrayDeque<>();
    for (Stmt stmt : stmtsInBody) {
      if (isStartStmt(graph, stmt)) {
        queue.addLast(stmt);
      }
    }

    // get all reachable stmts
    while (!queue.isEmpty()) {
      Stmt stmt = queue.removeFirst();
      reachableStmts.add(stmt);
      for (Stmt succ : graph.successors(stmt)) {
        if (!reachableStmts.contains(succ)) {
          queue.addLast(succ);
        }
      }
    }

    unreachableStmts =
        stmtsInBody.stream()
            .filter(stmt -> !reachableStmts.contains(stmt))
            .collect(Collectors.toSet());

    // delete invalid traps
    List<Trap> traps = builder.getTraps();

    Iterator<Trap> trapIterator = traps.iterator();

    while (trapIterator.hasNext()) {
      Trap trap = trapIterator.next();
      if (!reachableStmts.contains(trap.getHandlerStmt())) {
        trapIterator.remove();

      } else if (trap.getBeginStmt() == trap.getEndStmt()) {
        trapIterator.remove();
        for (Stmt stmt : trap.getStmts()) {
          unreachableStmts.add(stmt);
        }
      }
    }

    unreachableStmts.forEach(stmt -> builder.removeStmt(stmt));
  }

  /**
   * Check whether the given stmt is a start stmt: startingStmt or handlerStmt of a trap
   *
   * @param graph
   * @param stmt a stmt in the given BodyBuilder
   * @return if the given stmt is a start stmt, then return true, otherwise return false.
   */
  private boolean isStartStmt(StmtGraph graph, Stmt stmt) {
    if (stmt == graph.getStartingStmt()) {
      return true;
    }
    return stmt instanceof JIdentityStmt
        && ((JIdentityStmt) stmt).getRightOp() instanceof JCaughtExceptionRef;
  }
}
