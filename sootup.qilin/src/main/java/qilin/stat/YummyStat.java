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
import qilin.core.sets.PointsToSet;
import soot.MethodOrMethodContext;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class YummyStat implements AbstractStat {
    private final PTA pta;
    private long singleCallCnt;
    private long singleReceiverCnt;
    private long singleCallSingleReceiverCnt;
    private final Set<SootMethod> singleCalls = new HashSet<>();
    private final Set<SootMethod> singleReceivers = new HashSet<>();
    private final Set<SootMethod> singleCallSingleReceivers = new HashSet<>();

    public YummyStat(PTA pta) {
        this.pta = pta;
        init();
    }

    private void init() {
        Map<SootMethod, Set<Unit>> target2callsites = new HashMap<>();
        CallGraph ciCallGraph = pta.getCallGraph();
        for (Edge edge : ciCallGraph) {
            Unit callUnit = edge.srcUnit();
            SootMethod target = edge.tgt();
            if (callUnit != null && target != null) {
                target2callsites.computeIfAbsent(target, k -> new HashSet<>()).add(callUnit);
            }
        }

        target2callsites.entrySet().stream()
                .filter(e -> e.getValue().size() == 1)
                .map(Map.Entry::getKey).filter(m -> !m.isStatic())
                .forEach(singleCalls::add);

        singleCallCnt = singleCalls.size();


        Set<SootMethod> instanceReachables = new HashSet<>();
        for (final MethodOrMethodContext momc : pta.getReachableMethods()) {
            SootMethod method = momc.method();
            if (!method.isPhantom() && !method.isStatic()) {
                instanceReachables.add(method);
            }
        }

        for (SootMethod method : instanceReachables) {
            MethodNodeFactory nf = pta.getPag().getMethodPAG(method).nodeFactory();
            PointsToSet pts = pta.reachingObjects(nf.caseThis()).toCIPointsToSet();
            int ptSize = pts.size();
            if (ptSize == 1) {
                singleReceivers.add(method);
            }
        }

        singleReceiverCnt = singleReceivers.size();

        singleCalls.stream().filter(singleReceivers::contains).forEach(singleCallSingleReceivers::add);
        singleCallSingleReceiverCnt = singleCallSingleReceivers.size();
    }


    @Override
    public void export(Exporter exporter) {
        exporter.collectMetric("#Single-Call Methods:", String.valueOf(singleCallCnt));
        exporter.collectMetric("#Single-Receiver Methods:", String.valueOf(singleReceiverCnt));
        exporter.collectMetric("#Single-Call-Single-Receiver Methods:", String.valueOf(singleCallSingleReceiverCnt));
        if (CoreConfig.v().getOutConfig().dumpStats) {
            exporter.dumpSingleCallMethods(singleCalls);
            exporter.dumpSingleReceiverMethods(singleReceivers);
            exporter.dumpSingleCallSingleReceiverMethods(singleCallSingleReceivers);
        }
    }
}
