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

package qilin.pta.toolkits.turner;

import qilin.core.PTA;
import qilin.core.pag.AllocNode;
import qilin.core.pag.PAG;
import qilin.pta.PTAConfig;
import qilin.util.graph.MergedNode;
import qilin.util.graph.SCCMergedGraph;
import qilin.util.graph.TopologicalSorter;
import soot.SootMethod;
import soot.jimple.spark.pag.SparkField;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Turner {
    protected OCG ocg;
    private int total_node_count = 0;
    private int total_edge_count = 0;
    private final PTA prePTA;
    private final int k;
    private final int hk;
    public static boolean isModular = false;

    public Turner(int k, PTA pta) {
        this.k = k;
        this.hk = k - 1;
        this.prePTA = pta;
        if (isModular) {
            System.out.println("Turner with modularization ...");
        } else {
            System.out.println("Turner ...");
        }
    }

    ////////////////////////////////////////////////////////////////////////
    public Map<Object, Integer> contxtLengthAnalysis() {
        this.ocg = new OCG(prePTA);
        ocg.run();
        mergeNodeAndEdgeCount(ocg.getTotalNodeCount(), ocg.getTotalEdgeCount());
        // compute level for variables in methods
        Set<Object> nodes = ConcurrentHashMap.newKeySet();
        Collection<SootMethod> reachables = prePTA.getNakedReachableMethods();
        if (isModular) {
            MethodLevelCallGraph mcg = new MethodLevelCallGraph(prePTA.getCallGraph());
            final SCCMergedGraph<SootMethod> mg = new SCCMergedGraph<>(mcg);
            mystat(mg);
            final TopologicalSorter<MergedNode<SootMethod>> topoSorter = new TopologicalSorter<>();
            topoSorter.sort(mg, true).forEach(node -> {
                for (SootMethod method : node.getContent()) {
                    nodes.addAll(computeCtxLevelForVariables(method, node));
                }
            });
        } else {
            reachables.forEach(method -> {
                nodes.addAll(computeCtxLevelForVariables(method));
            });
        }
        // collect nodes and their level
        Map<Object, Integer> ret1 = new HashMap<>();
        // compute level for variables/objects
        reachables.forEach(method -> {
            AbstractMVFG mvfg = MethodVFG.findMethodVFG(method);
            if (mvfg != null) {
                for (Object obj : mvfg.getAllNodes()) {
                    if (nodes.contains(obj)) {
                        ret1.put(obj, obj instanceof AllocNode ? hk : k);
                    } else {
                        ret1.put(obj, 0);
                    }
                }
            }
        });
        // compute level for fields, In our paper, we do not mention this part
        // as it does not hurt too much efficiency.
        PAG pag = prePTA.getPag();
        Set<SparkField> fields = new HashSet<>();
        Set<SparkField> readSet = new HashSet<>();
        Set<SparkField> writeSet = new HashSet<>();

        pag.getContextFields().forEach(contextField -> {
            SparkField field = contextField.getField();
            fields.add(field);
            if (!pag.simpleInvLookup(contextField).isEmpty()) {
                writeSet.add(field);
            }
            if (!pag.simpleLookup(contextField).isEmpty()) {
                readSet.add(field);
            }
        });

        for (SparkField f : fields) {
            int x = 0;
            this.total_node_count += 1;
            if (writeSet.contains(f) && readSet.contains(f)) {
                x = k;
            }
            ret1.put(f, x);
        }
        Map<Object, Integer> ret = new HashMap<>(ret1);
        if (PTAConfig.v().turnerConfig == PTAConfig.TurnerConfig.PHASE_ONE) {
            Map<Object, Integer> ret2 = new HashMap<>();
            ret1.forEach((w, v) -> {
                if (w instanceof AllocNode) {
                    ret2.put(w, ocg.isCSLikely((AllocNode) w) ? hk : 0);
                } else {
                    ret2.put(w, k);
                }
            });
            ret = ret2;
        }
        System.out.println("#Node:" + this.total_node_count);
        System.out.println("#Edge:" + this.total_edge_count);
        statObjInOCG(ret);
        return ret;
    }

    private void statObjInOCG(Map<Object, Integer> ret) {
        int cibyocg = 0;
        int cibydfa = 0;
        int csobj = 0;
        int tops = 0;
        int bottoms = 0;
        int topandbottoms = 0;
        for (AllocNode o : prePTA.getPag().getAllocNodes()) {
            if (!this.ocg.isCSLikely(o)) {
                cibyocg++;
            } else if (ret.containsKey(o) && ret.get(o) > 0) {
                csobj++;
            } else {
                cibydfa++;
            }
            if (this.ocg.isTop(o)) {
                tops++;
            }
            if (this.ocg.isBottom(o)) {
                bottoms++;
            }
            if (this.ocg.isTop(o) && this.ocg.isBottom(o)) {
                topandbottoms++;
            }
        }
        System.out.println("#CIByOCG:" + cibyocg);
        System.out.println("#CIByDFA:" + cibydfa);
        System.out.println("#CSOBJ:" + csobj);
        System.out.println("#CITOP:" + tops);
        System.out.println("#CIBOT:" + bottoms);
        System.out.println("#CITOPBOT:" + topandbottoms);
    }

    private Collection<Object> computeCtxLevelForVariables(SootMethod method) {
        if (method.isPhantom()) {
            return Collections.emptySet();
        } else {
            AbstractMVFG mvfg = MethodVFG.findOrCreateMethodVFG(prePTA, method, ocg);
            mvfg.computeNodesInPrecisionLossPatterns();
            mergeNodeAndEdgeCount(mvfg.getTotalNodeCount(), mvfg.getTotalEdgeCount());
            return mvfg.getCSNodes();
        }
    }

    private Collection<Object> computeCtxLevelForVariables(SootMethod method, MergedNode<SootMethod> sccNode) {
        if (method.isPhantom()) {
            return Collections.emptySet();
        } else {
            AbstractMVFG mvfg = ModularMVFG.findOrCreateMethodVFG(prePTA, method, ocg, sccNode);
            mvfg.computeNodesInPrecisionLossPatterns();
            mergeNodeAndEdgeCount(mvfg.getTotalNodeCount(), mvfg.getTotalEdgeCount());
            return mvfg.getCSNodes();
        }
    }

    private void mergeNodeAndEdgeCount(int nodeCnt, int edgeCnt) {
        this.total_node_count += nodeCnt;
        this.total_edge_count += edgeCnt;
    }

    private void mystat(SCCMergedGraph<SootMethod> scccg) {
        int sccCnt = scccg.allNodes().size();
        int sccCntGtW = 0;
        int maxScc = 0;
        double avgScc = 0.0;
        double avgSccGtW = 0.0;
        for (MergedNode<SootMethod> scc : scccg.allNodes()) {
            int sccSize = scc.getContent().size();
            if (sccSize > maxScc) {
                maxScc = sccSize;
            }
            avgScc += sccSize;
            if (sccSize > 1) {
                ++sccCntGtW;
                avgSccGtW += sccSize;
            }
        }
        avgScc /= sccCnt;
        avgSccGtW /= sccCntGtW;
        int[] dist = new int[maxScc + 1];
        for (MergedNode<SootMethod> scc : scccg.allNodes()) {
            int sccSize = scc.getContent().size();
            dist[sccSize]++;
        }
        System.out.println("#scc count:" + sccCnt);
        System.out.println("#scc count (exclude singleton):" + sccCntGtW);
        System.out.println("Average scc size:" + avgScc);
        System.out.println("Average scc size(exclude singleton):" + avgSccGtW);
        System.out.println("Maximum scc size:" + maxScc);
        System.out.println("Scc size distribution (size, count):");
        for (int i = 0; i <= maxScc; ++i) {
            if (dist[i] > 0) {
                System.out.println(i + "," + dist[i]);
            }
        }
    }
}
