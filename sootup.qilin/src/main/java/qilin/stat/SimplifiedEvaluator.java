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
import qilin.core.PTA;
import qilin.core.builder.FakeMainFactory;
import qilin.core.builder.MethodNodeFactory;
import qilin.core.pag.*;
import qilin.core.sets.PointsToSet;
import qilin.util.PTAUtils;
import qilin.util.Stopwatch;
import qilin.util.Triple;
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
import sootup.core.model.SootMethod;
import sootup.core.types.ReferenceType;
import sootup.core.types.Type;

public class SimplifiedEvaluator implements IEvaluator {
  protected final PTA pta;
  protected final Exporter exporter;
  protected Stopwatch stopwatch;

  public SimplifiedEvaluator(PTA pta) {
    this.pta = pta;
    exporter = new Exporter();
  }

  @Override
  public void begin() {
    stopwatch = Stopwatch.newAndStart("PTA evaluator");
  }

  @Override
  public void end() {
    stopwatch.stop();
    exporter.collectMetric("Time (sec):", String.valueOf(((double) stopwatch.elapsed())));
    exporter.collectMetric(
        "#Reachable Method (CI):", String.valueOf(pta.getNakedReachableMethods().size() - 1));
    CallGraph ciCallGraph = pta.getCallGraph();
    exporter.collectMetric(
        "#Call Edge(CI):", String.valueOf(ciCallGraph.size() - FakeMainFactory.implicitCallEdges));

    CallGraph callGraph = pta.getCallGraph();

    // loop over all reachable method's statement to find casts, local
    // references, virtual call sites
    Set<SootMethod> reachableMethods = new HashSet<>();
    for (MethodOrMethodContext momc : pta.getCgb().getReachableMethods()) {
      final SootMethod sm = momc.method();
      reachableMethods.add(sm);
    }
    int totalPolyCalls = 0;
    int totalCastsMayFail = 0;
    for (SootMethod sm : reachableMethods) {
      // All the statements in the method
      for (Stmt st : PTAUtils.getMethodBody(sm).getStmts()) {
        // virtual calls
        if (st.containsInvokeExpr()) {
          AbstractInvokeExpr ie = st.getInvokeExpr();
          if (!(ie instanceof JStaticInvokeExpr)) {
            // Virtual, Special or Instance
            // have to check target soot method, cannot just
            // count edges
            Set<SootMethod> targets = new HashSet<>();
            for (Iterator<Edge> it = callGraph.edgesOutOf(st); it.hasNext(); )
              targets.add(it.next().tgt());
            if (targets.size() > 1) {
              totalPolyCalls++;
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
            boolean fails = false;
            Collection<AllocNode> pts = pta.reachingObjects(sm, (Local) v).toCollection();
            for (Node n : pts) {
              if (fails) {
                break;
              }
              fails = !PTAUtils.castNeverFails(n.getType(), targetType);
            }
            if (fails) {
              totalCastsMayFail++;
            }
          }
        }
      }
    }
    AliasStat aliasStat = new AliasStat(pta);
    aliasStat.aliasesProcessing();
    exporter.collectMetric("#May Fail Cast (Total):", String.valueOf(totalCastsMayFail));
    exporter.collectMetric("#Virtual Call Site(Polymorphic):", String.valueOf(totalPolyCalls));
    exporter.collectMetric(
        "#globalAlias_incstst:", String.valueOf(aliasStat.getGlobalAliasesIncludingStSt()));
    ptsStat();
  }

  private void ptsStat() {
    int ptsCntNoNative = 0;
    int varCntNoNative = 0;
    PAG pag = pta.getPag();
    // locals exclude Exceptions
    for (Triple<SootMethod, Local, Type> localTriple : pag.getLocalPointers()) {
      try {
        SootMethod method = localTriple.getFirst();
        Local local = localTriple.getSecond();
        Type type = localTriple.getThird();
        LocalVarNode lvn = pag.findLocalVarNode(method, local, type);
        if (local.toString().contains("intermediate/")) {
          continue;
        }
        mLocalVarNodes.add(lvn);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    // stat avg pts.
    for (SootMethod sm : pta.getNakedReachableMethods()) {
      MethodPAG mpag = pag.getMethodPAG(sm);
      MethodNodeFactory mnf = mpag.nodeFactory();
      if (!sm.isStatic()) {
        mLocalVarNodes.add((LocalVarNode) mnf.caseThis());
      }
      for (int i = 0; i < sm.getParameterCount(); ++i) {
        Type mType = sm.getParameterType(i);
        if (mType instanceof ReferenceType) {
          mLocalVarNodes.add((LocalVarNode) mnf.caseParm(i));
        }
      }
    }
    Set<LocalVarNode> tmp = new HashSet<>();
    for (LocalVarNode lvn : mLocalVarNodes) {
      SootMethod sm = lvn.getMethod();
      if (PTAUtils.isFakeMainMethod(sm)) {
        tmp.add(lvn);
        continue;
      }
      final Set<Object> callocSites = getPointsToNewExpr(pta.reachingObjects(lvn));
      if (callocSites.size() > 0) {
        if (!handledNatives.contains(sm.toString())) {
          ptsCntNoNative += callocSites.size();
          varCntNoNative++;
        }
      } else {
        tmp.add(lvn);
      }
    }
    mLocalVarNodes.removeAll(tmp);

    exporter.collectMetric(
        "#Avg Points-to Target without Native Var(CI):",
        String.valueOf(((double) ptsCntNoNative) / (varCntNoNative)));
  }

  private final Set<String> handledNatives =
      Set.of(
          "<org.apache.xerces.parsers.XML11Configuration: boolean getFeature0(java.lang.String)>",
          "<java.lang.ref.Finalizer: void invokeFinalizeMethod(java.lang.Object)>",
          "<java.lang.Thread: java.lang.Thread currentThread()>",
          "<java.lang.Thread: void start0()>",
          "<java.lang.Object: java.lang.Object clone()>",
          "<java.lang.System: void setIn0(java.io.InputStream)>",
          "<java.lang.System: void setOut0(java.io.PrintStream)>",
          "<java.lang.System: void setErr0(java.io.PrintStream)>",
          "<java.io.FileSystem: java.io.FileSystem getFileSystem()>",
          "<java.io.UnixFileSystem: java.lang.String[] list(java.io.File)>",
          "<java.security.AccessController: java.lang.Object doPrivileged(java.security.PrivilegedAction)>",
          "<java.security.AccessController: java.lang.Object doPrivileged(java.security.PrivilegedAction,java.security.AccessControlContext)>",
          "<java.security.AccessController: java.lang.Object doPrivileged(java.security.PrivilegedExceptionAction)>",
          "<java.security.AccessController: java.lang.Object doPrivileged(java.security.PrivilegedExceptionAction,java.security.AccessControlContext)>");

  private final Set<LocalVarNode> mLocalVarNodes = new HashSet<>();

  protected Set<Object> getPointsToNewExpr(PointsToSet pts) {
    final Set<Object> allocSites = new HashSet<>();
    for (AllocNode n : pts.toCollection()) {
      allocSites.add(n.getNewExpr());
    }
    return allocSites;
  }

  @Override
  public String toString() {
    return exporter.report();
  }
}
