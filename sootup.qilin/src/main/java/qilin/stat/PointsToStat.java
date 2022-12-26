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

import qilin.CoreConfig;
import qilin.core.PTA;
import qilin.core.builder.MethodNodeFactory;
import qilin.core.pag.*;
import qilin.core.sets.PointsToSet;
import qilin.util.PTAUtils;
import soot.*;

import java.util.*;

public class PointsToStat implements AbstractStat {
    private final PTA pta;
    private final PAG pag;
    private int contextCnt = 0;
    private double avgCtxPerMthd = 0.0;

    private int ciAllocs = 0;
    private int csAllocs = 0;
    private int totalGlobalPointers = 0;
    private int totalGlobalPointsToCi = 0;
    private int totalGlobalPointsToCs = 0;
    private int appGlobalPointers = 0;
    private int appGlobalPointsToCi = 0;
    private int appGlobalPointsToCs = 0;
    private int totalLocalPointersCi = 0;
    private int totalLocalPointersCs = 0;
    private int totalLocalCiToCi = 0;
    private int totalLocalCiToCs = 0;
    private int totalLocalCsToCi = 0;
    private int totalLocalCsToCs = 0;
    private int appLocalPointersCi = 0;
    private int appLocalPointersCs = 0;
    private int appLocalCiToCi = 0;
    private int appLocalCiToCs = 0;
    private int appLocalCsToCi = 0;
    private int appLocalCsToCs = 0;
    private int totalFieldPointsToCs = 0;
    private int methodThrowCnt = 0;

    private final Map<SootMethod, PointsToSet> methodThrowPts;
    private final Set<LocalVarNode> mLocalVarNodes = new HashSet<>();
    private int ptsCnt = 0;
    private int varCnt = 0;
    private final Set<LocalVarNode> mLocalVarNodesNoNative = new HashSet<>();
    private int ptsCntNoNative = 0;
    private int varCntNoNative = 0;

    public PointsToStat(PTA pta) {
        this.pta = pta;
        this.pag = pta.getPag();
        methodThrowPts = new HashMap<>();
        init();
    }

    private final Set<String> handledNatives = Set.of(
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
            "<java.security.AccessController: java.lang.Object doPrivileged(java.security.PrivilegedExceptionAction,java.security.AccessControlContext)>"
    );

    protected Set<Object> getPointsToNewExpr(PointsToSet pts) {
        final Set<Object> allocSites = new HashSet<>();
        for (Iterator<AllocNode> it = pts.iterator(); it.hasNext(); ) {
            AllocNode n = it.next();
            allocSites.add(n.getNewExpr());
        }
        return allocSites;
    }

    private void init() {
        ciAllocs = pag.getAllocNodes().size();
        csAllocs = pag.getAlloc().keySet().size();
        // globals
        for (SootField global : pag.getGlobalPointers()) {
            try {
                if (!global.isStatic())
                    continue;
                GlobalVarNode gvn = pag.findGlobalVarNode(global);
                boolean app = gvn.getDeclaringClass().isApplicationClass();

                totalGlobalPointers++;
                if (app) {
                    appGlobalPointers++;
                }

                PointsToSet pts = pta.reachingObjects(gvn);
                final Set<Object> allocSites = getPointsToNewExpr(pts);

                totalGlobalPointsToCi += allocSites.size();
                totalGlobalPointsToCs += pts.size();
                if (app) {
                    appGlobalPointsToCi += allocSites.size();
                    appGlobalPointsToCs += pts.size();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // locals exclude Exceptions
        for (Local local : pag.getLocalPointers()) {
            try {
                Collection<VarNode> varNodes = pag.getVarNodes(local);
                LocalVarNode lvn = pag.findLocalVarNode(local);
                if (local.toString().contains("intermediate/")) {
                    continue;
                }
                mLocalVarNodes.add(lvn);
                if (!handledNatives.contains(lvn.getMethod().toString())) {
                    mLocalVarNodesNoNative.add(lvn);
                }
                boolean app = lvn.getMethod().getDeclaringClass().isApplicationClass();
                totalLocalPointersCi++;
                if (app) {
                    appLocalPointersCi++;
                }

                totalLocalPointersCs += varNodes.size();
                if (app) {
                    appLocalPointersCs += varNodes.size();
                }

                PointsToSet pts = pta.reachingObjects(local);
                final Set<Object> allocSites = getPointsToNewExpr(pts);

                totalLocalCiToCi += allocSites.size();
                totalLocalCiToCs += pts.size();
                if (app) {
                    appLocalCiToCi += allocSites.size();
                    appLocalCiToCs += pts.size();
                }

                for (VarNode cvn : varNodes) {
                    PointsToSet cpts = pta.reachingObjects(cvn);
                    final Set<Object> callocSites = getPointsToNewExpr(cpts);
                    totalLocalCsToCi += callocSites.size();
                    totalLocalCsToCs += cpts.size();
                    if (app) {
                        appLocalCsToCi += callocSites.size();
                        appLocalCsToCs += cpts.size();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // field points-to
        for (ContextField cfvn : pag.getContextFields()) {
            totalFieldPointsToCs += cfvn.getP2Set().size();
        }

        // stat context.
        Map<MethodPAG, Set<Context>> mpag2contexts = pag.getMethod2ContextsMap();
        int[] cnts = new int[2];
        mpag2contexts.forEach((k, v) -> {
            cnts[0]++;
            cnts[1] += v.size();
        });
        contextCnt = cnts[1];
        avgCtxPerMthd = cnts[1] * 1.0 / cnts[0];

        // stat method throw points-to.
        for (SootMethod sm : pta.getNakedReachableMethods()) {
            Node mThrow = pag.getMethodPAG(sm).nodeFactory().caseMethodThrow();
            PointsToSet pts = pta.reachingObjects(mThrow);
            if (!pts.isEmpty()) {
                methodThrowCnt++;
                methodThrowPts.put(sm, pts);
            }
        }

        // stat avg pts.
        for (SootMethod sm : pta.getNakedReachableMethods()) {
            MethodPAG mpag = pag.getMethodPAG(sm);
            MethodNodeFactory mnf = mpag.nodeFactory();
            if (!sm.isStatic()) {
                mLocalVarNodes.add((LocalVarNode) mnf.caseThis());
                if (!handledNatives.contains(sm.toString())) {
                    mLocalVarNodesNoNative.add((LocalVarNode) mnf.caseThis());
                }
            }
            for (int i = 0; i < sm.getParameterCount(); ++i) {
                Type mType = sm.getParameterType(i);
                if (mType instanceof RefLikeType) {
                    mLocalVarNodes.add((LocalVarNode) mnf.caseParm(i));
                    if (!handledNatives.contains(sm.toString())) {
                        mLocalVarNodesNoNative.add((LocalVarNode) mnf.caseParm(i));
                    }
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
            PointsToSet cpts = pta.reachingObjects(lvn);
            final Set<Object> callocSites = getPointsToNewExpr(cpts);
            if (callocSites.size() > 0) {
                ptsCnt += callocSites.size();
                varCnt++;
                if (!handledNatives.contains(sm.toString())) {
                    ptsCntNoNative += callocSites.size();
                    varCntNoNative++;
                }
            } else {
                tmp.add(lvn);
            }
        }
        mLocalVarNodes.removeAll(tmp);
        mLocalVarNodesNoNative.removeAll(tmp);
        System.out.println("PTS relation:" + ptsCnt);
        System.out.println("VAR CNT:" + varCnt);
        System.out.println("AVG PTS: " + (ptsCnt * 1.0 / varCnt));
        System.out.println("PTS relation (no native):" + ptsCntNoNative);
        System.out.println("VAR CNT (no native):" + varCntNoNative);
        System.out.println("AVG PTS (no native): " + (ptsCntNoNative * 1.0 / varCntNoNative));
    }

    @Override
    public void export(Exporter exporter) {
        exporter.collectMetric("#Context:", String.valueOf(contextCnt));
        exporter.collectMetric("#Avg Context per Method:", String.valueOf(avgCtxPerMthd));
        exporter.collectMetric("#Method with Throw Pointer-to:", String.valueOf(methodThrowCnt));

        exporter.collectMetric("#Alloc Node(CI): ", String.valueOf(ciAllocs));
        exporter.collectMetric("#Alloc Node(CS): ", String.valueOf(csAllocs));

        exporter.collectMetric("#Global CS Pointer-to Relation:", String.valueOf(totalGlobalPointsToCs));
        exporter.collectMetric("#Local CS Pointer-to Relation:", String.valueOf(totalLocalCsToCs));
        exporter.collectMetric("#Field CS Pointer-to Relation:", String.valueOf(totalFieldPointsToCs));

        exporter.collectMetric("#Global Pointer (lib + app):", String.valueOf(totalGlobalPointers));
        exporter.collectMetric("#Global Avg Points-To Target(CI):", String.valueOf(((double) totalGlobalPointsToCi) / ((double) totalGlobalPointers)));
        exporter.collectMetric("#Global Avg Points-To Target(CS):", String.valueOf(((double) totalGlobalPointsToCs) / ((double) totalGlobalPointers)));
        exporter.collectMetric("#App Global Pointer:", String.valueOf(appGlobalPointers));
        exporter.collectMetric("#App Global Avg Points-To Target(CI):", String.valueOf(((double) appGlobalPointsToCi) / ((double) appGlobalPointers)));
        exporter.collectMetric("#App Global Avg Points-To Target(CS):", String.valueOf(((double) appGlobalPointsToCs) / ((double) appGlobalPointers)));
        exporter.collectMetric("#Avg Points-to Target(CI):", String.valueOf(((double) ptsCnt) / (varCnt)));
        exporter.collectMetric("#Avg Points-to Target without Native Var(CI):", String.valueOf(((double) ptsCntNoNative) / (varCntNoNative)));
        exporter.collectMetric("#Local Pointer (lib + app):", String.valueOf(totalLocalPointersCi));
        exporter.collectMetric("#Local Avg Points-To Target(CI):", String.valueOf(((double) totalLocalCiToCi) / ((double) totalLocalPointersCi)));
        exporter.collectMetric("#Local Avg Points-To Target(CS):", String.valueOf(((double) totalLocalCiToCs) / ((double) totalLocalPointersCi)));
        exporter.collectMetric("#App Local Pointer:", String.valueOf(appLocalPointersCi));
        exporter.collectMetric("#App Local Avg Points-To Target(CI):", String.valueOf(((double) appLocalCiToCi) / ((double) appLocalPointersCi)));
        exporter.collectMetric("#App Local Avg Points-To Target(CS):", String.valueOf(((double) appLocalCiToCs) / ((double) appLocalPointersCi)));
        exporter.collectMetric("#Context Local Pointer (lib + app):", String.valueOf(totalLocalPointersCs));
        exporter.collectMetric("#Context Local Avg Points-To Target(CI):", String.valueOf(((double) totalLocalCsToCi) / ((double) totalLocalPointersCs)));
        exporter.collectMetric("#Context Local Avg Points-To Target(CS):", String.valueOf(((double) totalLocalCsToCs) / ((double) totalLocalPointersCs)));
        exporter.collectMetric("#App Context Local Pointer:", String.valueOf(appLocalPointersCs));
        exporter.collectMetric("#App Context Local Avg Points-To Target(CI):", String.valueOf(((double) appLocalCsToCi) / ((double) appLocalPointersCs)));
        exporter.collectMetric("#App Context Local Avg Points-To Target(CS):", String.valueOf(((double) appLocalCsToCs) / ((double) appLocalPointersCs)));
        if (CoreConfig.v().getOutConfig().dumpStats) {
            exporter.dumpMethodThrowPointsto(methodThrowPts);
            exporter.dumpReachableLocalVars(mLocalVarNodes);
            exporter.dumpReachableLocalVarsNoNative(mLocalVarNodesNoNative);
            exporter.dumpInsensPointsTo(mLocalVarNodes, pta);
        }
    }
}
