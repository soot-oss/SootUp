/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.stat;

import java.util.*;
import qilin.CoreConfig;
import qilin.core.PTA;
import qilin.core.PTAScene;
import qilin.core.builder.FakeMainFactory;
import qilin.core.pag.AllocNode;
import qilin.util.PTAUtils;
import soot.MethodOrMethodContext;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.expr.JStaticInvokeExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.types.ReferenceType;
import sootup.core.types.Type;
import sootup.core.views.View;

public class TypeClientStat implements AbstractStat {
  private final PTA pta;

  private int totalCasts = 0;
  private int appCasts = 0;
  private int totalCastsMayFail = 0;
  private int appCastsMayFail = 0;
  private int totalVirtualCalls = 0;
  private int appVirtualCalls = 0;
  private int totalPolyCalls = 0;
  private int appPolyCalls = 0;
  private int totalStaticCalls = 0;
  private int totalPolyCallTargets = 0;
  private int unreachable = 0;
  private final Map<AbstractInvokeExpr, SootMethod> polyCalls = new HashMap<>();
  private final Map<SootMethod, Set<Stmt>> mayFailCasts = new HashMap<>();

  public TypeClientStat(PTA pta) {
    this.pta = pta;
    init();
  }

  private void init() {
    CallGraph callGraph = pta.getCallGraph();

    // loop over all reachable method's statement to find casts, local
    // references, virtual call sites
    Set<SootMethod> reachableMethods = new HashSet<>();
    for (MethodOrMethodContext momc : pta.getCgb().getReachableMethods()) {
      final SootMethod sm = momc.method();
      reachableMethods.add(sm);
    }

    for (SootMethod sm : reachableMethods) {
      View view = PTAScene.v().getView();
      SootClass sc = (SootClass) view.getClass(sm.getDeclaringClassType()).get();
      boolean app = sc.isApplicationClass();

      // All the statements in the method
      for (Stmt st : PTAUtils.getMethodBody(sm).getStmts()) {
        // virtual calls
        if (st.containsInvokeExpr()) {
          AbstractInvokeExpr ie = st.getInvokeExpr();
          if (ie instanceof JStaticInvokeExpr) {
            totalStaticCalls++;
          } else { // Virtual, Special or Instance
            totalVirtualCalls++;
            if (app) {
              appVirtualCalls++;
            }
            // have to check target soot method, cannot just
            // count edges
            Set<SootMethod> targets = new HashSet<>();

            for (Iterator<Edge> it = callGraph.edgesOutOf(st); it.hasNext(); )
              targets.add(it.next().tgt());
            if (targets.size() == 0) {
              unreachable++;
            }
            if (targets.size() > 1) {
              totalPolyCallTargets += targets.size();
              totalPolyCalls++;
              polyCalls.put(ie, sm);
              if (app) {
                appPolyCalls++;
              }
            }
          }
        } else if (st instanceof JAssignStmt assignStmt) {
          Value rhs = assignStmt.getRightOp();
          Value lhs = assignStmt.getLeftOp();
          if (rhs instanceof JCastExpr && lhs.getType() instanceof ReferenceType) {
            final Type targetType = rhs.getType();
            Value v = ((JCastExpr) rhs).getOp();
            if (!(v instanceof Local)) {
              continue;
            }
            totalCasts++;
            if (app) {
              appCasts++;
            }
            boolean fails = false;
            Collection<AllocNode> pts = pta.reachingObjects((Local) v).toCollection();
            for (AllocNode n : pts) {
              if (fails) {
                break;
              }
              fails = !PTAUtils.castNeverFails(n.getType(), targetType);
            }

            if (fails) {
              totalCastsMayFail++;
              mayFailCasts.computeIfAbsent(sm, k -> new HashSet<>()).add(st);
              if (app) {
                appCastsMayFail++;
              }
            }
          }
        }
      }
    }
  }

  @Override
  public void export(Exporter exporter) {
    exporter.collectMetric("#Cast (Total):", String.valueOf(totalCasts));
    exporter.collectMetric("#Cast (AppOnly):", String.valueOf(appCasts));
    exporter.collectMetric("#May Fail Cast (Total):", String.valueOf(totalCastsMayFail));
    exporter.collectMetric("#May Fail Cast (AppOnly):", String.valueOf(appCastsMayFail));
    exporter.collectMetric(
        "#Static Call Site(Total):",
        String.valueOf(totalStaticCalls - FakeMainFactory.implicitCallEdges));
    exporter.collectMetric("#Virtual Call Site(Total):", String.valueOf(totalVirtualCalls));
    exporter.collectMetric("#Virtual Call Site(AppOnly):", String.valueOf(appVirtualCalls));
    exporter.collectMetric("#Virtual Call Site(Polymorphic):", String.valueOf(totalPolyCalls));
    exporter.collectMetric(
        "#Virtual Call Site(Polymorphic AppOnly):", String.valueOf(appPolyCalls));
    exporter.collectMetric("#Virtual Call Site(Unreachable):", String.valueOf(unreachable));
    exporter.collectMetric(
        "#Avg Poly Call Targets:", String.valueOf(1.0 * totalPolyCallTargets / totalPolyCalls));

    if (CoreConfig.v().getOutConfig().dumpStats) {
      exporter.dumpPolyCalls(polyCalls);
      exporter.dumpMayFailCasts(mayFailCasts);
    }
  }
}
