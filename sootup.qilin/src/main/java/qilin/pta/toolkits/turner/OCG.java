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
import qilin.core.builder.MethodNodeFactory;
import qilin.core.pag.*;
import qilin.core.sets.PointsToSet;
import qilin.pta.PTAConfig;
import qilin.util.PTAUtils;
import soot.*;
import soot.jimple.spark.pag.SparkField;

import java.util.*;

// Object Containment Graph
public class OCG {
    public final PTA pta;
    protected final Map<LocalVarNode, Set<AllocNode>> pts;
    public Map<AllocNode, OCGNode> nodes;
    private int total_node_count = 0;
    private int total_edge_count = 0;

    public OCG(PTA pta) {
        this.pta = pta;
        this.pts = PTAUtils.calcStaticThisPTS(pta);
        this.nodes = new HashMap<>();
        buildGraph();
    }

    protected void buildGraph() {
        PAG pag = pta.getPag();
        pag.getAllocNodes().forEach(this::findOrCreate);
        pag.getContextFields().forEach(contextField -> {
            AllocNode base = contextField.getBase();
            if (base instanceof ConstantNode) {
                return;
            }

            SparkField f = contextField.getField();
            if (f.getType() instanceof ArrayType at) {
                if (at.baseType instanceof PrimType) {
                    return;
                }
            }
            PointsToSet pts = pta.reachingObjects(contextField).toCIPointsToSet();
            for (Iterator<AllocNode> it = pts.iterator(); it.hasNext(); ) {
                AllocNode n = it.next();
                if (n instanceof ConstantNode) {
                    continue;
                }
                addEdge(findOrCreate(base), findOrCreate(n));
            }
        });
    }

    public Collection<OCGNode> allNodes() {
        return nodes.values();
    }

    public int getTotalNodeCount() {
        return total_node_count;
    }

    public int getTotalEdgeCount() {
        return total_edge_count;
    }

    /**
     * (1) case1: objects on OCG have successors but does not have predecessors.
     * (1-1) factorys
     * (1-2) normal uses.
     * (2) case2: objects on OCG does not have successors.
     * (2-1) no predecessors.
     * (2-2) have predecessors.
     * (3) othercase: objects on OCG have successors and predecessors.
     */
    public void stat() {
        int case1 = 0;
        int total_factory = 0;
        int case1_factory = 0;
        int case1_normal = 0;
        int case2 = 0;
        int case2_noPred = 0;
        int case2_hasPred = 0;
        int otherCase = 0;
        for (OCGNode node : nodes.values()) {
            if (node.successors.size() == 0) {
                case2++;
                if (node.predecessors.size() == 0) {
                    case2_noPred++;
                } else {
                    case2_hasPred++;
                }
                if (isFactoryObject(node.ir)) {
                    total_factory++;
                }
            } else if (node.predecessors.size() == 0) {
                case1++;
                if (isFactoryObject(node.ir)) {
                    case1_factory++;
                    // System.out.println(((AllocNode) node.ir).toString2());
                } else {
                    case1_normal++;
                }
            } else {
                if (isFactoryObject(node.ir)) {
                    total_factory++;
                }
                otherCase++;
            }
        }

        System.out.println("#case1:" + case1);
        System.out.println("#total_factory:" + total_factory);
        System.out.println("#case1_factory:" + case1_factory);
        System.out.println("#case1_normal:" + case1_normal);
        System.out.println("#case2:" + case2);
        System.out.println("#case2_noPred:" + case2_noPred);
        System.out.println("#case2_hasPred:" + case2_hasPred);
        System.out.println("#othercase:" + otherCase);
    }

    private OCGNode findOrCreate(AllocNode ir) {
        if (nodes.containsKey(ir)) {
            return nodes.get(ir);
        } else {
            total_node_count++;
            OCGNode ret = new OCGNode(ir);
            nodes.put(ir, ret);
            return ret;
        }
    }

    private boolean isNewTop(OCGNode node) {
        return node.predecessors.isEmpty() && !isFactoryObject(node.ir);
    }

    private boolean isNewBottom(OCGNode node) {
        return node.successors.isEmpty();
    }

    public boolean isTop(AllocNode heap) {
        return !nodes.containsKey(heap) || isNewTop(findOrCreate(heap));
    }

    public boolean isBottom(AllocNode heap) {
        return !nodes.containsKey(heap) || isNewBottom(findOrCreate(heap));
    }

    private boolean isNotTopAndBottom(OCGNode node) {
        return !isNewBottom(node) && !isNewTop(node);
    }

    public boolean isCSLikely(AllocNode allocNode) {
        OCGNode node = this.nodes.getOrDefault(allocNode, null);
        if (node == null) {
            return false;
        } else {
            return node.cslikely;
        }
    }

    public void run() {
        int[] a = new int[2];
        System.out.println(PTAConfig.v().turnerConfig);
        for (OCGNode node : nodes.values()) {
            PTAConfig.TurnerConfig hgConfig = PTAConfig.v().turnerConfig;
            if (hgConfig == PTAConfig.TurnerConfig.PHASE_TWO) {
                node.cslikely = true;
            } else {
                node.cslikely = isNotTopAndBottom(node);
            }
            if (node.cslikely) {
                a[1]++;
            } else {
                a[0]++;
            }

        }
        for (int i = 0; i < 2; ++i) {
            System.out.println("#level " + i + ": " + a[i]);
        }
        stat();
    }

    protected void addEdge(OCGNode pre, OCGNode succ) {
        total_edge_count++;
        pre.addSucc(succ);
        succ.addPred(pre);
    }

    public static class OCGNode {
        public final AllocNode ir;
        public Set<OCGNode> successors;
        public Set<OCGNode> predecessors;
        public boolean cslikely;

        public OCGNode(AllocNode ir) {
            this.ir = ir;
            this.cslikely = false;
            this.successors = new HashSet<>();
            this.predecessors = new HashSet<>();
        }

        @Override
        public String toString() {
            return ir.toString();
        }

        public void addSucc(OCGNode node) {
            this.successors.add(node);
        }

        public void addPred(OCGNode node) {
            this.predecessors.add(node);
        }
    }

    /**
     * patterns in case1: (1) create one object and never return out of the method.
     * (2) create one object and then return out (factory).
     */
    boolean isFactoryObject(AllocNode heap) {
        SootMethod method = heap.getMethod();
        if (method == null) {
            return false;
        }
        Type retType = method.getReturnType();
        if (!(retType instanceof RefLikeType)) {
            return false;
        }
        if (retType instanceof ArrayType at) {
            if (at.baseType instanceof PrimType) {
                return false;
            }
        }
        MethodPAG methodPAG = pta.getPag().getMethodPAG(method);
        MethodNodeFactory factory = methodPAG.nodeFactory();
        Node retNode = factory.caseRet();
        PointsToSet pts = pta.reachingObjects(retNode).toCIPointsToSet();
        return pts.contains(heap);
    }

}
