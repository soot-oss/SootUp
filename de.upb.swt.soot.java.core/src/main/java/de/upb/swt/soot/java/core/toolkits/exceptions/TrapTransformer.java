package de.upb.swt.soot.java.core.toolkits.exceptions;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2018 Raja Vallée-Rai and others
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
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JEnterMonitorStmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JExitMonitorStmt;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import soot.Unit;
import soot.jimple.ExitMonitorStmt;
import soot.util.HashMultiMap;
import soot.util.MultiMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Common abstract base class for all body transformers that change the trap list to, e.g., minimize the trap list
 * 
 * @author Steven Arzt
 *
 */
public abstract class TrapTransformer implements BodyInterceptor {

  public Set<Stmt> getStmtsWithMonitor(StmtGraph stmtGraph) {
    // Idea: Associate each unit with a set of monitors held at that
    // statement
    MultiMap<Stmt, Value> stmtMonitors = new HashMultiMap<>();

    // Start at the heads of the unit graph
    List<Stmt> workList = new ArrayList<>();
    Set<Stmt> doneSet = new HashSet<>();
    for (Stmt head : stmtGraph.getEntrypoints()) {
      workList.add(head);
    }

    while (!workList.isEmpty()) {
      Stmt curUnit = workList.remove(0);

      boolean hasChanged = false;
      Value exitValue = null;
      if (curUnit instanceof JEnterMonitorStmt) {
        // We enter a new monitor
        JEnterMonitorStmt ems = (JEnterMonitorStmt) curUnit;
        hasChanged = stmtMonitors.put(curUnit, ems.getOp());
      } else if (curUnit instanceof JExitMonitorStmt) {
        // We leave a monitor
        JExitMonitorStmt ems = (JExitMonitorStmt) curUnit;
        exitValue = ems.getOp();
      }

      // Copy over the monitors from the predecessors
      for (Stmt pred : stmtGraph.getPredsOf(curUnit)) {
        for (Value v : stmtMonitors.get(pred)) {
          if (v != exitValue) {
            if (stmtMonitors.put(curUnit, v)) {
              hasChanged = true;
            }
          }
        }
      }

      // Work on the successors
      if (doneSet.add(curUnit) || hasChanged) {
        workList.addAll(stmtGraph.getSuccsOf(curUnit));
      }
    }

    return stmtMonitors.keySet();
  }

}
