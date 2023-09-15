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

package qilin.pta.toolkits.conch;

import qilin.core.PTA;
import qilin.core.pag.*;
import qilin.util.PTAUtils;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/*
 * This structure is used to check whether an object could flow out of its containing method.
 * */
public class LeakAnalysis extends AbstractPAG {
    private final Map<Node, Set<PathEdge>> pathEdges = new ConcurrentHashMap<>();
    private final Set<PathEdge> initialSeeds = ConcurrentHashMap.newKeySet();
    private final Set<AllocNode> result = ConcurrentHashMap.newKeySet();

    public LeakAnalysis(PTA prePTA) {
        super(prePTA);
        build();
        solve();
    }

    protected void solve() {
        System.out.println("start analysis!");
        super.solve();
        System.out.println("finish MFG analysis!");
    }

    protected void submitInitialSeeds() {
        initialSeeds.forEach(pe -> executor.execute(new PathEdgeProcessingTask(pe)));
    }

    protected void addThrowEdge(Node throwNode) {
        super.addThrowEdge(throwNode);
        initialSeeds.add(new PathEdge(throwNode, DFA.State.B, throwNode, DFA.State.B));
    }

    protected void addParamEdge(LocalVarNode param) {
        super.addParamEdge(param);
        initialSeeds.add(new PathEdge(param, DFA.State.F, param, DFA.State.F));
    }

    protected void addReturnEdge(LocalVarNode mret) {
        super.addReturnEdge(mret);
        initialSeeds.add(new PathEdge(mret, DFA.State.B, mret, DFA.State.B));
    }

    protected void addNewEdge(AllocNode from, LocalVarNode to) {
        super.addNewEdge(from, to);
        initialSeeds.add(new PathEdge(from, DFA.State.O, from, DFA.State.O));
    }

    private void addPathEdge(PathEdge pe) {
        Node tgtNode = pe.getTgtNode();
        pathEdges.computeIfAbsent(tgtNode, k -> ConcurrentHashMap.newKeySet()).add(pe);
    }

    private boolean containPathEdge(PathEdge pe) {
        Node tgtNode = pe.getTgtNode();
        return pathEdges.getOrDefault(tgtNode, Collections.emptySet()).contains(pe);
    }

    private void propagate(PathEdge pe) {
        if (!containPathEdge(pe)) {
            executor.execute(new PathEdgeProcessingTask(pe));
        }
    }

    private class PathEdgeProcessingTask implements Runnable {
        PathEdge pe;

        public PathEdgeProcessingTask(PathEdge pe) {
            this.pe = pe;
        }

        @Override
        public void run() {
            addPathEdge(pe);
            DFA.State initState = pe.getSrcState();
            Node sourceNode = pe.getSrcNode();
            DFA.State targetState = pe.getTgtState();
            Node targetNode = pe.getTgtNode();

            for (TranEdge e : outAndSummaryEdges(targetNode)) {
                Node newTargetNode = e.getTarget();
                DFA.TranCond tranCond = e.getTranCond();
                DFA.State nextState = DFA.nextState(targetState, tranCond);
                if (nextState == DFA.State.ERROR) {
                    continue;
                }
                if (initState == DFA.State.B && nextState == DFA.State.O) {
                    // disallow such kinds of pathedge: <ret, B> --> <Heap, O>
                    continue;
                }
                PathEdge nPE = new PathEdge(sourceNode, initState, newTargetNode, nextState);
                propagate(nPE);
                if (nextState != DFA.State.E) {
                    continue;
                }
                // reach the end state.
                if (initState == DFA.State.O) {
                    // report a heap flows out of its containing method.
                    AllocNode sourceHeap = (AllocNode) sourceNode;
                    result.add(sourceHeap);
                    SootMethod containingMethod = sourceHeap.getMethod();
                    Iterator<Edge> it = callGraph.edgesInto(containingMethod);
                    while (it.hasNext()) {
                        Edge edge = it.next();
                        SootMethod srcMethod = edge.src();
                        MethodPAG srcmpag = prePAG.getMethodPAG(srcMethod);
                        Stmt invokeStmt = (Stmt) edge.srcUnit();
                        if (targetState == DFA.State.F) { // ret.f* = heap
                            // add S -new-> r summary edge for symbolic heaps.
                            VarNode ret = (VarNode) targetNode;
                            VarNode r = PTAUtils.paramToArg(prePAG, invokeStmt, srcmpag, ret);
                            if (r != null) {
                                AllocNode s = getSymbolicHeapOf(srcMethod, invokeStmt);
                                addSummaryEdge(new TranEdge(s, r, DFA.TranCond.NEW));
                                addSummaryEdge(new TranEdge(r, s, DFA.TranCond.I_NEW));
                            }
                        }
                    }
                } else if (initState == DFA.State.F) {
                    LocalVarNode pj = (LocalVarNode) sourceNode;
                    SootMethod containingMethod = pj.getMethod();
                    Iterator<Edge> it = callGraph.edgesInto(containingMethod);
                    while (it.hasNext()) {
                        Edge edge = it.next();
                        SootMethod srcMethod = edge.src();
                        MethodPAG srcmpag = prePAG.getMethodPAG(srcMethod);
                        Stmt invokeStmt = (Stmt) edge.srcUnit();
                        VarNode aj = PTAUtils.paramToArg(prePAG, invokeStmt, srcmpag, pj);
                        // a param reach end state.
                        if (targetState == DFA.State.B && sourceNode != targetNode) { // pi.f* = pj, pi != pj.
                            // add aj --> ai summary edge. inter_store.
                            VarNode pi = (VarNode) targetNode;
                            VarNode ai = PTAUtils.paramToArg(prePAG, invokeStmt, srcmpag, pi);
                            if (ai != null && aj != null && ai != aj) {
                                addSummaryEdge(new TranEdge(aj, ai, DFA.TranCond.INTER_STORE));
                            }
                        } else if (targetState == DFA.State.F) { // ret.f* = pj
                            // add aj --> r summary edge. inter_load.
                            VarNode ret = (VarNode) targetNode;
                            VarNode r = PTAUtils.paramToArg(prePAG, invokeStmt, srcmpag, ret);
                            if (r != null && aj != null) {
                                addSummaryEdge(new TranEdge(aj, r, DFA.TranCond.INTER_ASSIGN));
                            }
                        }
                    }
                } else if (initState == DFA.State.B && targetState == DFA.State.B) {
                    // ret = pi.f*
                    LocalVarNode retOrThrow = (LocalVarNode) sourceNode;
                    SootMethod containingMethod = retOrThrow.getMethod();
                    Iterator<Edge> it = callGraph.edgesInto(containingMethod);
                    VarNode pi = (VarNode) pe.getTgtNode();
                    // add r --> ai summary edge inverse_inter_load.
                    while (it.hasNext()) {
                        Edge edge = it.next();
                        SootMethod srcMethod = edge.src();
                        MethodPAG srcmpag = prePAG.getMethodPAG(srcMethod);
                        Stmt invokeStmt = (Stmt) edge.srcUnit();
                        VarNode ai = PTAUtils.paramToArg(prePAG, invokeStmt, srcmpag, pi);
                        VarNode r = PTAUtils.paramToArg(prePAG, invokeStmt, srcmpag, retOrThrow);
                        if (r != null && ai != null) {
                            addSummaryEdge(new TranEdge(r, ai, DFA.TranCond.I_INTER_LOAD));
                        }
                    }
                }
            }
        }
    }

    private void addSummaryEdge(TranEdge tranEdge) {
        Node src = tranEdge.getSource();
        Node tgt = tranEdge.getTarget();
        DFA.TranCond tranCond = tranEdge.getTranCond();
        sumEdges.computeIfAbsent(src, k -> ConcurrentHashMap.newKeySet()).add(tranEdge);
        for (PathEdge pe : pathEdges.getOrDefault(src, Collections.emptySet())) {
            DFA.State tgtState = pe.getTgtState();
            DFA.State nextState = DFA.nextState(tgtState, tranCond);
            if (nextState == DFA.State.ERROR) {
                continue;
            }
            PathEdge nPE = new PathEdge(pe.getSrcNode(), pe.getSrcState(), tgt, nextState);
            propagate(nPE);
        }
    }

    // Condition(A): heap that could flow out of its containing methods.
    public boolean isLeakObject(AllocNode heap) {
        return result.contains(heap);
    }
}
