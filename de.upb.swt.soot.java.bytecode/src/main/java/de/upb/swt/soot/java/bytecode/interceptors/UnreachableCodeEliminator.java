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

    StmtGraph graph = builder.getStmtGraph();
    Set<Stmt> stmtsInBody = graph.nodes();

    Set<Stmt> unreachableStmts = new HashSet<>();
    // get all unreachable stmts
    for (Stmt stmt : stmtsInBody) {
      if (graph.predecessors(stmt).size() == 0) {
        if (!stmt.equals(graph.getStartingStmt())
            && !stmt.toString().contains("@caughtexception")) {
          unreachableStmts.add(stmt);
        }
      }
    }

    // if all predecessors of a stmt are unreachable, then this stmt is also unreachable
    int counter = 1;

    while (counter > 0) {
      counter = 0;

      for (Stmt stmt : stmtsInBody) {

        if (!unreachableStmts.contains(stmt)) {

          if (!stmt.equals(graph.getStartingStmt())
              && !stmt.toString().contains("@caughtexception")) {
            boolean unreachable = true;

            for (Stmt pred : graph.predecessors(stmt)) {
              if (!unreachableStmts.contains(pred)) {
                unreachable = false;
                break;
              }
            }

            if (unreachable) {
              unreachableStmts.add(stmt);
              counter++;
            }
          }
        }
      }
    }

    // delete unvalid traps
    List<Trap> traps = builder.getTraps();

    Iterator<Trap> trapIterator = traps.iterator();

    while (trapIterator.hasNext()) {

      Trap trap = trapIterator.next();
      if (!graph.nodes().contains(trap.getHandlerStmt())) {

        trapIterator.remove();

      } else if (trap.getBeginStmt() == trap.getEndStmt()) {

        trapIterator.remove();
        trap.getStmts().forEach(stmt -> unreachableStmts.add(stmt));
      }
    }

    unreachableStmts.forEach(stmt -> builder.removeStmt(stmt));
  }
}
