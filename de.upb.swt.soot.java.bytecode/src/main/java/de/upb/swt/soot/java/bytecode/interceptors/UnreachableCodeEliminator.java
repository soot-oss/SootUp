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
import javax.annotation.Nonnull;

/**
 * A BodyInterceptor that removes all unreachable stmts from the given Body.
 *
 * @author Zun Wang
 */
public class UnreachableCodeEliminator implements BodyInterceptor {

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder) {

    StmtGraph<?> graph = builder.getStmtGraph();
    List<Trap> traps = builder.getTraps();

    // get all valid starting stmts: startingStmt and handlerStmts(if they in stmtGraph)
    Deque<Stmt> queue = new ArrayDeque<>();
    queue.add(graph.getStartingStmt());
    for (Trap trap : traps) {
      if (graph.containsNode(trap.getHandlerStmt())) {
        queue.addLast(trap.getHandlerStmt());
      }
    }

    // calculate all reachable stmts
    Set<Stmt> reachableStmts = new HashSet<>();
    while (!queue.isEmpty()) {
      Stmt stmt = queue.removeFirst();
      reachableStmts.add(stmt);
      for (Stmt succ : graph.successors(stmt)) {
        if (!reachableStmts.contains(succ)) {
          queue.addLast(succ);
        }
      }
    }

    // remove unreachable stmts from StmtGraph
    Queue<Stmt> removeQ = new ArrayDeque<>();
    for (Stmt stmt : graph.nodes()) {
      if (!reachableStmts.contains(stmt)) {
        removeQ.add(stmt);
      }
    }
    for (Stmt stmt : removeQ) {
      builder.removeStmt(stmt);
    }

    // cleanup invalid traps
    Iterator<Trap> trapIterator = traps.iterator();
    while (trapIterator.hasNext()) {
      Trap trap = trapIterator.next();
      // is the Traphandler Stmt (still) in the StmtGraph?
      if (!graph.containsNode(trap.getHandlerStmt())) {
        trapIterator.remove();
      } else {
        // has the trap a valid range? TODO: [ms] why don't we check that (i.e. trap range is empty)
        // in trap instantiation?
        /*      if (trap.getBeginStmt() == trap.getEndStmt()) {
               trapIterator.remove();
               // FIXME: [ms] do we really want to remove begin/end stmt if there is no trap..
               // FIXME: [ms] check that the handlerstmt is just reachable via an exceptional flow before removing it! (and remove successing stmts without another predecessor, too)
               builder.removeStmt(trap.getBeginStmt());
               builder.removeStmt(trap.getEndStmt());
               builder.removeStmt(trap.getHandlerStmt());
             }
        */
      }
    }
  }
}
