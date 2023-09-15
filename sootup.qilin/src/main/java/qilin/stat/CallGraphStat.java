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
import qilin.core.PTAScene;
import qilin.core.builder.FakeMainFactory;
import qilin.core.pag.ContextVarNode;
import qilin.core.pag.LocalVarNode;
import soot.MethodOrMethodContext;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CallGraphStat implements AbstractStat {
    private final PTA pta;

    private final Set<SootMethod> reachableMethods = new HashSet<>();
    private int reachableStatic = 0;
    private final Set<MethodOrMethodContext> reachableParameterizedMethods = new HashSet<>();
    private final Set<MethodOrMethodContext> reachableAppParameterizedMethods = new HashSet<>();
    private final Set<SootMethod> reachableAppMethods = new HashSet<>();
    private int reachableAppStatic = 0;
    private int CSCallEdges = 0;
    private int CSStaticToStatic = 0;
    private int CSStaticToInstance = 0;
    private int CSInstanceToStatic = 0;
    private int CSInstancetoInstance = 0;
    private int CSApp2lib = 0;
    private int CSApp2app = 0;
    private int CSLib2lib = 0;
    private int CSLib2app = 0;
    private int CICallEdges = 0;
    private int CIApp2lib = 0;
    private int CIApp2app = 0;
    private int CILib2lib = 0;
    private int CILib2app = 0;
    private int CIStaticToStatic = 0;
    private int CIStaticToInstance = 0;
    private int CIInstanceToStatic = 0;
    private int CIInstancetoInstance = 0;

    public CallGraphStat(PTA pta) {
        this.pta = pta;
        init();
    }

    private void init() {
        CallGraph csCallGraph = pta.getCgb().getCallGraph();
        CSCallEdges = csCallGraph.size();
        for (final MethodOrMethodContext momc : pta.getCgb().getReachableMethods()) {
            final SootMethod m = momc.method();
            boolean toApp = m.getDeclaringClass().isApplicationClass();
            reachableParameterizedMethods.add(momc);
            reachableMethods.add(m);
//            if (m.toString().equals("<sun.security.provider.PolicyParser: void read(java.io.Reader)>")) {
//                System.out.println(PTAUtils.getMethodBody(m));
//            }

            if (toApp) {
                reachableAppParameterizedMethods.add(momc);
                reachableAppMethods.add(momc.method());
            }

            for (Iterator<Edge> iterator = csCallGraph.edgesInto(momc); iterator.hasNext(); ) {
                Edge e = iterator.next();
                final SootMethod srcm = e.getSrc().method();
                boolean fromApp = srcm.getDeclaringClass().isApplicationClass();
                if (fromApp && toApp) {
                    CSApp2app++;
                } else if (fromApp) {
                    CSApp2lib++;
                } else if (!toApp) {
                    CSLib2lib++;
                } else {
                    CSLib2app++;
                }
                if (e.src().isStatic()) {
                    if (e.isStatic()) {
                        CSStaticToStatic++;
                    } else {
                        CSStaticToInstance++;
                    }
                } else if (e.isStatic()) {
                    CSInstanceToStatic++;
                } else {
                    CSInstancetoInstance++;
                }
            }
        }
        CallGraph ciCallGraph = pta.getCallGraph();
        CICallEdges = ciCallGraph.size();
        for (SootMethod sm : reachableMethods) {
            boolean toApp = sm.getDeclaringClass().isApplicationClass();
            if (sm.isStatic()) {
                reachableStatic++;
                if (toApp) {
                    reachableAppStatic++;
                }
            }
            for (Iterator<Edge> iterator = ciCallGraph.edgesInto(sm); iterator.hasNext(); ) {
                Edge e = iterator.next();
                final SootMethod srcm = e.getSrc().method();
//                if (sm.toString().equals("<java.lang.ClassNotFoundException: java.lang.Throwable getCause()>")) {
//                    System.out.println("from:" + srcm);
//                }
                boolean fromApp = srcm.getDeclaringClass().isApplicationClass();
                if (fromApp && toApp) {
                    CIApp2app++;
                } else if (fromApp) {
                    CIApp2lib++;
                } else if (!toApp) {
                    CILib2lib++;
                } else {
                    CILib2app++;
                }

                if (e.src().isStatic()) {
                    if (e.isStatic()) {
                        CIStaticToStatic++;
                    } else {
                        CIStaticToInstance++;
                    }
                } else if (e.isStatic()) {
                    CIInstanceToStatic++;
                } else {
                    CIInstancetoInstance++;
                }
            }
        }
    }

    private long thisReceiverCount() {
        return pta.getCgb().getReceiverToSitesMap().entrySet().stream()
                .filter(e -> e.getKey() instanceof ContextVarNode)
                .filter(e -> e.getKey().base() instanceof LocalVarNode)
                .filter(e -> ((LocalVarNode) e.getKey().base()).isThis()).count();
    }

    @Override
    public void export(Exporter exporter) {
        exporter.collectMetric("#Method (Static):", String.valueOf(PTAScene.v().getMethodNumberer().size() - 1));// -fakeMain
        exporter.collectMetric("#Reachable Method (CI):", String.valueOf(reachableMethods.size() - 1));// -fakeMain
        exporter.collectMetric("\t#Reachable-Static Method (CI):", String.valueOf(reachableStatic - 1));// -fakeMain
        exporter.collectMetric("#Reachable Method (CS):", String.valueOf(reachableParameterizedMethods.size() - 1));// -fakeMain
        exporter.collectMetric("#Reachable App Method (CI):", String.valueOf(reachableAppMethods.size()));
        exporter.collectMetric("\t#Reachable-App-Static Method (CI):", String.valueOf(reachableAppStatic));
        exporter.collectMetric("#Reachable App Method (CS):", String.valueOf(reachableAppParameterizedMethods.size()));
        exporter.collectMetric("#Call Edge(CI):", String.valueOf(CICallEdges - FakeMainFactory.implicitCallEdges));
        exporter.collectMetric("\t#Static-Static Call Edge(CI):", String.valueOf(CIStaticToStatic - FakeMainFactory.implicitCallEdges));
        exporter.collectMetric("\t#Static-Instance Call Edge(CI):", String.valueOf(CIStaticToInstance));
        exporter.collectMetric("\t#Instance-Static Call Edge(CI):", String.valueOf(CIInstanceToStatic));
        exporter.collectMetric("\t#Instance-Instance Call Edge(CI):", String.valueOf(CIInstancetoInstance));
        exporter.collectMetric("\t#Application-Application Call Edge(CI):", String.valueOf(CIApp2app));
        exporter.collectMetric("\t#Application-Library Call Edge(CI):", String.valueOf(CIApp2lib));
        exporter.collectMetric("\t#Library-Application Call Edge(CI):", String.valueOf(CILib2app));
        exporter.collectMetric("\t#Library-Library Call Edge(CI):", String.valueOf(CILib2lib));
        exporter.collectMetric("#Call Edge(CS):", String.valueOf(CSCallEdges - FakeMainFactory.implicitCallEdges));
        exporter.collectMetric("\t#Static-Static Call Edge(CS):", String.valueOf(CSStaticToStatic - FakeMainFactory.implicitCallEdges));
        exporter.collectMetric("\t#Static-Instance Call Edge(CS):", String.valueOf(CSStaticToInstance));
        exporter.collectMetric("\t#Instance-Static Call Edge(CS):", String.valueOf(CSInstanceToStatic));
        exporter.collectMetric("\t#Instance-Instance Call Edge(CS):", String.valueOf(CSInstancetoInstance));
        exporter.collectMetric("\t#Application-Application Call Edge(CS):", String.valueOf(CSApp2app));
        exporter.collectMetric("\t#Application-Library Call Edge(CS):", String.valueOf(CSApp2lib));
        exporter.collectMetric("\t#Library-Application Call Edge(CS):", String.valueOf(CSLib2app));
        exporter.collectMetric("\t#Library-Library Call Edge(CS):", String.valueOf(CSLib2lib));
        exporter.collectMetric("#receivers:", String.valueOf(pta.getCgb().getReceiverToSitesMap().size()));
        exporter.collectMetric("\t#thisreceivers:", String.valueOf(thisReceiverCount()));
        exporter.collectMetric("#avg p2s size for virtualcalls:", String.valueOf((CSCallEdges - CSStaticToStatic - CSInstanceToStatic)
                * 1.0 / pta.getCgb().getReceiverToSitesMap().size()));
        if (CoreConfig.v().getOutConfig().dumpStats) {
            exporter.dumpReachableMethods(reachableMethods);
            exporter.dumpAppReachableMethods(reachableAppMethods);
            exporter.dumpInsensCallGraph(pta.getCallGraph());
        }
    }
}
