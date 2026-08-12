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

package qilin.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import qilin.core.PTA;
import qilin.core.builder.MethodNodeFactory;
import qilin.core.builder.callgraph.Edge;
import qilin.core.pag.AllocNode;
import qilin.core.pag.LocalVarNode;
import qilin.core.pag.MethodPAG;
import qilin.core.pag.PAG;
import qilin.util.sets.PointsToSet;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JStaticInvokeExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.model.SootMethod;

/**
 * Propagates each static method's would-be {@code this} points-to set to every static method it
 * calls, so toolkits that reason about a "receiver" uniformly (including for static methods) have
 * one to look up.
 */
public final class StaticThisPointsTo {
  private StaticThisPointsTo() {}

  public static Map<LocalVarNode, Set<AllocNode>> calcStaticThisPTS(PTA pta) {
    Map<LocalVarNode, Set<AllocNode>> pts = new HashMap<>();
    Set<SootMethod> workList = new HashSet<>();
    PAG pag = pta.getPag();
    // add all instance methods which potentially contain static call
    for (SootMethod method : pta.getNakedReachableMethods()) {
      if (FakeMainMethods.isFakeMainMethod(method) || pag.hasBody(method) && !method.isStatic()) {
        MethodPAG srcmpag = pag.getMethodPAG(method);
        LocalVarNode thisRef = (LocalVarNode) srcmpag.nodeFactory().caseThis();
        final PointsToSet other = pta.reachingObjects(thisRef).toCIPointsToSet();

        for (final InvokableStmt s : srcmpag.getInvokeStmts()) {
          AbstractInvokeExpr ie = s.getInvokeExpr().get();
          if (ie instanceof JStaticInvokeExpr) {
            for (Iterator<Edge> it = pta.getCallGraph().edgesOutOf(s); it.hasNext(); ) {
              Edge e = it.next();
              SootMethod tgtmtd = e.tgt();
              MethodPAG tgtmpag = pag.getMethodPAG(tgtmtd);
              MethodNodeFactory tgtnf = tgtmpag.nodeFactory();
              LocalVarNode tgtThisRef = (LocalVarNode) tgtnf.caseThis();
              // create "THIS" ptr for static method
              Set<AllocNode> addTo = pts.computeIfAbsent(tgtThisRef, k -> new HashSet<>());
              boolean returnValue = false;
              for (Iterator<AllocNode> itx = other.iterator(); itx.hasNext(); ) {
                AllocNode node = itx.next();
                if (addTo.add(node)) {
                  returnValue = true;
                }
              }
              if (returnValue) {
                workList.add(tgtmtd);
              }
            }
          }
        }
      }
    }
    while (!workList.isEmpty()) {
      SootMethod method = workList.iterator().next();
      workList.remove(method);
      MethodPAG srcmpag = pag.getMethodPAG(method);
      LocalVarNode thisRef = (LocalVarNode) srcmpag.nodeFactory().caseThis();
      final Set<AllocNode> other = pts.computeIfAbsent(thisRef, k -> new HashSet<>());

      for (final InvokableStmt s : srcmpag.getInvokeStmts()) {
        AbstractInvokeExpr ie = s.getInvokeExpr().get();
        if (ie instanceof JStaticInvokeExpr) {
          for (Iterator<Edge> it = pta.getCallGraph().edgesOutOf(s); it.hasNext(); ) {
            Edge e = it.next();
            SootMethod tgtmtd = e.tgt();
            MethodPAG tgtmpag = pag.getMethodPAG(tgtmtd);
            MethodNodeFactory tgtnf = tgtmpag.nodeFactory();
            LocalVarNode tgtThisRef = (LocalVarNode) tgtnf.caseThis();
            // create "THIS" ptr for static method
            Set<AllocNode> addTo = pts.computeIfAbsent(tgtThisRef, k -> new HashSet<>());
            if (addTo.addAll(other)) {
              workList.add(tgtmtd);
            }
          }
        }
      }
    }
    return pts;
  }
}
