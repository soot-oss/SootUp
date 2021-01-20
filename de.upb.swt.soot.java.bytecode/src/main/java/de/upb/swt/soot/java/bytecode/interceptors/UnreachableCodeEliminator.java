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
import org.checkerframework.checker.units.qual.A;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A BodyInterceptor that removes all unreachable stmts from the given Body.
 *
 * @author Zun Wang
 */

public class UnreachableCodeEliminator implements BodyInterceptor {

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder) {

    StmtGraph graph = builder.getStmtGraph();
    List<Stmt> stmtsInBody = builder.getStmts();

    List<Trap> traps = builder.getTraps();

    //store all stmts which has no predecessors in the graph
    //notice: it's possible, that a trap's HandlerStmt is not in Graph
    Deque<Stmt> startStmts = new ArrayDeque<>();
    startStmts.addLast(stmtsInBody.get(0));

    for(Trap trap : traps){
      startStmts.addFirst(trap.getHandlerStmt());
    }
    int trapPos = 0;
    boolean prunedTrap = false;

    //store all stmts which are reachable
    Set<Stmt> reachableStmts = new HashSet<>();
    while(!startStmts.isEmpty()){
      Stmt stmt = startStmts.removeFirst();
      if(stmt.toString().contains("@caughtexception")){
        if(!graph.containsNode(stmt)){
          traps.remove(traps.get(trapPos));
          prunedTrap = true;
          break;
        }
        trapPos++;
      }
      if(reachableStmts.add(stmt)){
        for(Stmt succ : graph.successors(stmt)){
          startStmts.addFirst(succ);
        }
      }
    }

    //get all stmts which are unreachable
    Set<Stmt> unreachableStmts = stmtsInBody.stream().filter(stmt ->!reachableStmts.contains(stmt)).collect(Collectors.toSet());

    if(prunedTrap){
      builder.setTraps(traps);
    }

    unreachableStmts.forEach(stmt -> builder.removeStmt(stmt));

  }
}
