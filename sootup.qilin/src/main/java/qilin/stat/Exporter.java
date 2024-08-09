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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import qilin.CoreConfig;
import qilin.core.PTA;
import qilin.core.builder.callgraph.Edge;
import qilin.core.builder.callgraph.OnFlyCallGraph;
import qilin.core.pag.AllocNode;
import qilin.core.pag.LocalVarNode;
import qilin.core.pag.Parm;
import qilin.core.sets.PointsToSet;
import qilin.util.Util;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.types.Type;

public class Exporter {
  private final PTA pta;
  private final String metrics = "Metrics.csv";
  private final String staticFPTs = "StaticFieldPointsTo.csv";

  private static final int STATLENGTH = 50;
  private final StringBuffer report = new StringBuffer();

  public Exporter(PTA pta) {
    this.pta = pta;
  }

  public void addLine(String str) {
    report.append(str).append('\n');
  }

  private String makeUp(String string) {
    final String makeUpString = " ";
    String tmp = "";
    for (int i = 0; i < Math.max(0, STATLENGTH - string.length()); ++i) {
      tmp = tmp.concat(makeUpString);
    }
    //        StringBuilder ret = new StringBuilder();
    //        ret.append(makeUpString.repeat(Math.max(0, STATLENGTH - string.length())));
    return string + tmp;
  }

  public void collectMetric(String desc, String value) {
    addLine(makeUp(desc) + value);
  }

  private String getFilePath(String fileName) {
    String finalPath = CoreConfig.v().getOutConfig().outDir;
    finalPath =
        finalPath
            + File.separator
            + CoreConfig.v().getAppConfig().MAIN_CLASS
            + File.separator
            + CoreConfig.v().getPtaConfig().ptaName
            + File.separator;
    File file = new File(finalPath);
    if (!file.exists()) {
      file.mkdirs();
    }
    finalPath = finalPath + fileName;
    return finalPath;
  }

  private void dumpMethods(Collection<SootMethod> methods, String fileName) {
    StringBuilder builder = new StringBuilder();
    for (SootMethod sm : methods) {
      String sig = sm.getSignature().toString();
      sig = Util.stripQuotes(sig);
      builder.append(sig);
      builder.append("\n");
    }
    String finalPath = getFilePath(fileName);
    Util.writeToFile(finalPath, builder.toString());
  }

  public void dumpReachableMethods(Collection<SootMethod> reachables) {
    String reachMethods = "Reachable.csv";
    dumpMethods(reachables, reachMethods);
  }

  public void dumpAppReachableMethods(Collection<SootMethod> appReachables) {
    String appReachMethods = "AppReachable.csv";
    dumpMethods(appReachables, appReachMethods);
  }

  public void dumpSingleCallMethods(Collection<SootMethod> singleCallMs) {
    String singleCalls = "SingleCallMethods.csv";
    dumpMethods(singleCallMs, singleCalls);
  }

  public void dumpSingleReceiverMethods(Collection<SootMethod> singleReceiverMs) {
    String singleReceivers = "SingleReceiverMethods.csv";
    dumpMethods(singleReceiverMs, singleReceivers);
  }

  public void dumpSingleCallSingleReceiverMethods(
      Collection<SootMethod> singleCallSingleReceiverMs) {
    String singleCallSingleReceivers = "SingleCallSingleReceiverMethods.csv";
    dumpMethods(singleCallSingleReceiverMs, singleCallSingleReceivers);
  }

  public void dumpClassTypes(Collection<? extends SootClass> classes) {
    StringBuilder builder = new StringBuilder();
    for (SootClass sc : classes) {
      builder.append(sc.getName());
      builder.append("\n");
    }
    String classTypes = "ClassType.csv";
    String finalPath = getFilePath(classTypes);
    Util.writeToFile(finalPath, builder.toString());
  }

  public void dumpPolyCalls(Map<AbstractInvokeExpr, SootMethod> polys) {
    StringBuilder builder = new StringBuilder();
    for (AbstractInvokeExpr ie : polys.keySet()) {
      SootMethod tgt = pta.getView().getMethod(ie.getMethodSignature()).get();
      String polySig =
          polys.get(ie).getSignature()
              + "/"
              + tgt.getDeclaringClassType()
              + "."
              + tgt.getName()
              + "\n";
      builder.append(polySig);
    }
    String polyCalls = "PolyCalls.csv";
    String finalPath = getFilePath(polyCalls);
    Util.writeToFile(finalPath, builder.toString());
  }

  public void dumpMayFailCasts(Map<SootMethod, Set<Stmt>> casts) {
    StringBuilder builder = new StringBuilder();
    for (SootMethod sm : casts.keySet()) {
      for (Stmt stmt : casts.get(sm)) {
        JAssignStmt as = (JAssignStmt) stmt;
        JCastExpr ce = (JCastExpr) as.getRightOp();
        final Type targetType = ce.getType();
        builder.append(sm.toString());
        builder.append("\t");
        builder.append(targetType.toString());
        builder.append("\t");
        builder.append(sm).append("/").append(ce.getOp().toString());
        builder.append("\t");
        builder.append(sm).append("/").append(as.getLeftOp().toString());
        builder.append("\n");
      }
    }
    String mayFailCasts = "MayFailCasts.csv";
    String finalPath = getFilePath(mayFailCasts);
    Util.writeToFile(finalPath, builder.toString());
  }

  public void dumpMethodThrowPointsto(Map<SootMethod, PointsToSet> m2pts) {
    String methodThrowPts = "MethodThrowPointsTo.csv";
    String finalPath = getFilePath(methodThrowPts);
    try {
      File mfile = new File(finalPath);
      mfile.delete();
      mfile.createNewFile();
      BufferedWriter writer = new BufferedWriter(new FileWriter(mfile, true));
      for (SootMethod sm : m2pts.keySet()) {
        PointsToSet pts = m2pts.get(sm).toCIPointsToSet();
        for (Iterator<AllocNode> it = pts.iterator(); it.hasNext(); ) {
          AllocNode n = it.next();
          StringBuilder builder = new StringBuilder();
          builder.append(n.toString());
          builder.append("\t");
          String sig = Util.stripQuotes(sm.getSignature().toString());
          builder.append(sig);
          builder.append("\n");
          try {
            writer.write(builder.toString());
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
      writer.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void dumpInsensCallGraph(OnFlyCallGraph ciCallGraph) {
    String insensCallGraphEdges = "InsensCallGraphEdges.csv";
    String finalPath = getFilePath(insensCallGraphEdges);
    try {
      File mfile = new File(finalPath);
      mfile.delete();
      mfile.createNewFile();
      BufferedWriter writer = new BufferedWriter(new FileWriter(mfile, true));
      try {
        for (Edge edge : ciCallGraph) {
          String srcSig = Util.stripQuotes(edge.src().getSignature().toString());
          String dstSig = Util.stripQuotes(edge.tgt().getSignature().toString());
          String str = edge.srcStmt() + " in method " + srcSig + "\t" + dstSig + "\n";
          writer.write(str);
        }
      }finally{
        writer.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void dumpReachableLocalVars(Collection<LocalVarNode> lvns) {
    StringBuilder builder = new StringBuilder();
    for (LocalVarNode lvn : lvns) {
      String varName = getDoopVarName(lvn);
      builder.append(varName).append("\n");
    }
    String insensReachVars = "InsensReachVars.csv";
    String finalPath = getFilePath(insensReachVars);
    Util.writeToFile(finalPath, builder.toString());
  }

  public void dumpReachableLocalVarsNoNative(Collection<LocalVarNode> lvns) {
    StringBuilder builder = new StringBuilder();
    for (LocalVarNode lvn : lvns) {
      String varName = getDoopVarName(lvn);
      builder.append(varName).append("\n");
    }
    String insensReachVars = "InsensReachVarsNoNatives.csv";
    String finalPath = getFilePath(insensReachVars);
    Util.writeToFile(finalPath, builder.toString());
  }

  private String getDoopVarName(LocalVarNode lvn) {
    SootMethod m = lvn.getMethod();
    Object v = lvn.getVariable();
    String varName = v.toString();
    if (v instanceof Parm) {
      Parm parm = (Parm) v;
      if (parm.isThis()) {
        varName = "@this";
      } else if (parm.isReturn()) {

      } else if (parm.isThrowRet()) {

      } else {
        varName = "@parameter" + parm.getIndex();
      }
    }
    return m.getSignature() + "/" + varName;
  }

  public void dumpInsensPointsTo(Collection<LocalVarNode> lvns, PTA pta) {
    String insensVarPTs = "InsensVarPointsTo.csv";
    String finalPath = getFilePath(insensVarPTs);
    try {
      File mfile = new File(finalPath);
      mfile.delete();
      mfile.createNewFile();
      BufferedWriter writer = new BufferedWriter(new FileWriter(mfile, true));
      try {
        for (LocalVarNode lvn : lvns) {
          String varName = getDoopVarName(lvn);
          final Set<AllocNode> callocSites = new HashSet<>();
          PointsToSet cpts = pta.reachingObjects(lvn).toCIPointsToSet();
          for (Iterator<AllocNode> it = cpts.iterator(); it.hasNext(); ) {
            AllocNode heap = it.next();
            callocSites.add(heap);
          }
          for (AllocNode heap : callocSites) {
            String str = heap.getNewExpr() + "\t" + varName + "\n";
            if (heap.getMethod() != null) {
              str = heap.getMethod() + "/" + heap.getNewExpr() + "\t" + varName + "\n";
            }
            writer.write(str);
          }
        }
      }finally {
        writer.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public String report() {
    String tmp = report.toString();
    if (CoreConfig.v().getOutConfig().dumpStats) {
      String statistics = "Statistics.txt";
      String finalPath = getFilePath(statistics);
      Util.writeToFile(finalPath, tmp);
    }
    return tmp;
  }
}
