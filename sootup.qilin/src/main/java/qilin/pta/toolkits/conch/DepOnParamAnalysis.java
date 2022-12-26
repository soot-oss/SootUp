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
import qilin.core.builder.MethodNodeFactory;
import qilin.core.pag.*;
import qilin.util.PTAUtils;
import soot.SootMethod;
import soot.jimple.AssignStmt;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/*
 * It try to answer where the value of a variable comes from.
 * (1) comes from p.f*
 * (2) comes from O.f*
 * (3) comes from S.f* (S stands for a symbolic heap).
 * for the later two case, we are not sure whether the value of current variable comes from the out of its containing method or not.
 * We will rely on cs-dependent graph for further checking.
 * These two cases is not mentioned in the paper since it does not affect our results.
 * */

public class DepOnParamAnalysis extends AbstractPAG {
    private final Map<Node, Set<Node>> pathEdges = new ConcurrentHashMap<>();
    private final Set<Node> initialSeeds = ConcurrentHashMap.newKeySet();

    public DepOnParamAnalysis(PTA prePTA) {
        super(prePTA);
        build();
        solve();
    }

    protected void solve() {
        System.out.println("start analysis!");
        super.solve();
        System.out.println("finish PFG analysis!");
    }

    protected void addParamEdge(LocalVarNode param) {
        super.addParamEdge(param);
        initialSeeds.add(param);
    }

    protected void addNewEdge(AllocNode from, LocalVarNode to) {
        super.addNewEdge(from, to);
        initialSeeds.add(from);
    }

    protected void submitInitialSeeds() {
        for (Node node : initialSeeds) {
            propagate(node, node);
        }
    }

    private void propagate(Node srcParam, Node currNode) {
        Set<Node> fromParams = pathEdges.computeIfAbsent(currNode, k -> ConcurrentHashMap.newKeySet());
        if (!fromParams.contains(srcParam)) {
            executor.execute(new PathEdgeProcessingTask(srcParam, currNode));
        }
    }

    private class PathEdgeProcessingTask implements Runnable {
        private final Node sourceParam;
        private final Node currNode;

        public PathEdgeProcessingTask(Node param, Node node) {
            this.sourceParam = param;
            this.currNode = node;
        }

        @Override
        public void run() {
            pathEdges.computeIfAbsent(currNode, k -> ConcurrentHashMap.newKeySet()).add(sourceParam);
            for (TranEdge e : outAndSummaryEdges(currNode)) {
                Node nextNode = e.getTarget();
                DFA.TranCond tranCond = e.getTranCond();
                DFA.State nextState = DFA.nextState2(tranCond);
                if (nextState == DFA.State.ERROR) {
                    continue;
                }
                propagate(sourceParam, nextNode);
                if (nextState == DFA.State.E) {
                    // do something.
                    SootMethod containingMethod;
                    if (sourceParam instanceof LocalVarNode pj) {
                        containingMethod = pj.getMethod();
                    } else {
                        AllocNode heap = (AllocNode) sourceParam;
                        containingMethod = heap.getMethod();
                    }

                    Iterator<Edge> it = callGraph.edgesInto(containingMethod);
                    while (it.hasNext()) {
                        Edge edge = it.next();
                        SootMethod srcMethod = edge.src();
                        MethodPAG srcmpag = prePAG.getMethodPAG(srcMethod);
                        MethodNodeFactory srcnf = srcmpag.nodeFactory();
                        Stmt invokeStmt = (Stmt) edge.srcUnit();
                        if (invokeStmt instanceof AssignStmt assignStmt) {

                            VarNode r = (VarNode) srcnf.getNode(assignStmt.getLeftOp());
                            if (sourceParam instanceof LocalVarNode pj) {
                                VarNode aj = PTAUtils.paramToArg(prePAG, invokeStmt, srcmpag, pj);
                                if (aj != null) {
                                    addSummaryEdge(new TranEdge(aj, r, DFA.TranCond.INTER_ASSIGN));
                                }
                            } else {
                                AllocNode symbolHeap = getSymbolicHeapOf(srcMethod, invokeStmt);
                                addSummaryEdge(new TranEdge(symbolHeap, r, DFA.TranCond.NEW));
                                propagate(symbolHeap, symbolHeap);
                            }
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
        for (Node srcParam : pathEdges.getOrDefault(src, Collections.emptySet())) {
            DFA.State nextState = DFA.nextState2(tranCond);
            if (nextState == DFA.State.ERROR) {
                continue;
            }
            propagate(srcParam, tgt);
        }
    }

    public Set<Node> fetchReachableParamsOf(Node node) {
        return pathEdges.getOrDefault(node, Collections.emptySet());
    }
}
