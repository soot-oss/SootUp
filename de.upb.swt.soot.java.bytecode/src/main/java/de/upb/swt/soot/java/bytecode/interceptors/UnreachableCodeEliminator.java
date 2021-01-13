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
import javax.annotation.Nonnull;
import java.util.*;

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
    boolean trapsReduced = false;

    //store all stmts which has no predecessors in the graph
    Deque<Stmt> startStmts = new ArrayDeque<>();
    startStmts.addLast(stmtsInBody.get(0));
    for(Trap trap : builder.getTraps()){
      if(stmtsInBody.contains(trap.getBeginStmt())){
        startStmts.addLast(trap.getBeginStmt());
      }else{
        traps.remove(trap);
        trapsReduced = true;
      }
    }

    //store all stmts which are reachable
    Set<Stmt> reachableStmts = new HashSet<>();
    while(!startStmts.isEmpty()){
      Stmt stmt = startStmts.removeFirst();
      if(reachableStmts.add(stmt)){
        for(Stmt succ : graph.successors(stmt)){
          startStmts.addFirst(succ);
        }
      }
    }

    //get all stmts which are unreachable
    Set<Stmt> unreachableStmts = new HashSet<>();
    for(Stmt stmt : stmtsInBody){
      if(!reachableStmts.contains(stmt)){
        unreachableStmts.add(stmt);
      }
    }

    if(trapsReduced){
      builder.setTraps(traps);
    }


    //TODO: build body with new list of units??

  }
}
