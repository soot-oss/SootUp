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
    List<Trap> traps = builder.getTraps();

    Set<Stmt> reachableStmts = new HashSet<>();

    // get all start stmts: startingStmt and handlerStmts(if they in stmtGraph)
    Deque<Stmt> queue = new ArrayDeque<>();
    queue.add(graph.getStartingStmt());
    for (Trap trap : traps) {
      if (graph.containsNode(trap.getHandlerStmt())) {
        queue.addLast(trap.getHandlerStmt());
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

    // get all unreachable stmts
    Set<Stmt> unreachableStmts =
        stmtsInBody.stream()
            .filter(stmt -> !reachableStmts.contains(stmt))
            .collect(Collectors.toSet());

    // delete invalid traps
    Iterator<Trap> trapIterator = traps.iterator();
    while (trapIterator.hasNext()) {
      Trap trap = trapIterator.next();
      if (!reachableStmts.contains(trap.getHandlerStmt())) {
        trapIterator.remove();

      } else if (trap.getBeginStmt() == trap.getEndStmt()) {
        trapIterator.remove();
        unreachableStmts.add(trap.getBeginStmt());
      }
    }

    // delete all unreachable stmts from the current builder
    unreachableStmts.forEach(builder::removeStmt);
  }
}
