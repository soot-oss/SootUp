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

package qilin.pta.toolkits.selectx;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import qilin.core.PTA;
import qilin.core.PointsToAnalysis;
import qilin.core.builder.MethodNodeFactory;
import qilin.core.pag.*;
import soot.*;
import soot.jimple.*;
import soot.jimple.spark.pag.SparkField;
import soot.jimple.toolkits.callgraph.Edge;
import soot.util.queue.QueueReader;

public class Selectx {
    private final PTA prePTA;
    private final PAG prePAG;
    private final Set<SparkField> sparkFields = new HashSet<>();

    public Selectx(PTA pta) {
        this.prePTA = pta;
        this.prePAG = pta.getPag();
        buildGraph();
    }

    public void addNewEdge(AllocNode from, LocalVarNode to) {
        O fromE = O.v(from);
        L toE = L.v(to, true);
        fromE.addOutEdge(toE);
        L toEI = L.v(to, false);
        toEI.addOutEdge(fromE);
    }

    public void addAssignEdge(LocalVarNode from, LocalVarNode to) {
        L fromE = L.v(from, true), toE = L.v(to, true);
        fromE.addOutEdge(toE);
        L fromEI = L.v(from, false), toEI = L.v(to, false);
        toEI.addOutEdge(fromEI);
    }

    public void addEntryEdge(LocalVarNode from, LocalVarNode to, CallSite callSite) {
        int i = getCallSiteNumber(callSite);

        L fromE = L.v(from, true), toE = L.v(to, true);
        if (fromE.addOutEntryEdge(i, toE)) {
            toE.addInEntryEdge(i, fromE);
            L fromEI = L.v(from, false), toEI = L.v(to, false);
            toEI.addOutExitEdge(i, fromEI);
        }
    }

    public void addExitEdge(LocalVarNode from, LocalVarNode to, CallSite callSite) {
        int i = getCallSiteNumber(callSite);

        L fromE = L.v(from, true), toE = L.v(to, true);
        if (fromE.addOutExitEdge(i, toE)) {
            L fromEI = L.v(from, false), toEI = L.v(to, false);
            toEI.addOutEntryEdge(i, fromEI);
            fromEI.addInEntryEdge(i, toEI);
        }
    }

    public void addStoreEdge(LocalVarNode from, LocalVarNode base) {
        L fromE = L.v(from, true), baseE = L.v(base, true);
        L fromEI = L.v(from, false), baseEI = L.v(base, false);
        fromE.addOutEdge(baseEI);
        baseE.addOutEdge(fromEI);
    }

    public void addStaticStoreEdge(LocalVarNode from, GlobalVarNode to) {
        L fromE = L.v(from, true);
        G toE = G.v(to, true);
        fromE.addOutEdge(toE);
        L fromEI = L.v(from, false);
        G toEI = G.v(to, false);
        toEI.addOutEdge(fromEI);
    }

    public void addStaticLoadEdge(GlobalVarNode from, LocalVarNode to) {
        G fromE = G.v(from, true);
        L toE = L.v(to, true);
        fromE.addOutEdge(toE);
        G fromEI = G.v(from, false);
        L toEI = L.v(to, false);
        toEI.addOutEdge(fromEI);
    }

    private void propagate(Set<BNode> workList, Set<I> paraWorkList) {
        while (!workList.isEmpty() || !paraWorkList.isEmpty()) {
            while (!workList.isEmpty()) {
                BNode node = workList.iterator().next();
                workList.remove(node);
                node.forwardTargets().filter(BNode::setVisited).forEach(workList::add);
                if (node instanceof L l) {
                    l.getOutEntryEdges().stream().filter(tgt -> tgt.paras.add(tgt)).forEach(paraWorkList::add);
                }
            }
            while (!paraWorkList.isEmpty()) {
                I node = paraWorkList.iterator().next();
                paraWorkList.remove(node);
                // para propagation.
                node.getOutTargets().stream().filter(i -> i.update(node)).forEach(paraWorkList::add);
                if (node instanceof L l) {
                    l.getOutGs().filter(BNode::setVisited).forEach(workList::add);
                    l.getOutEntryEdges().stream().filter(tgt -> tgt.paras.add(tgt)).forEach(paraWorkList::add);
                    for (Map.Entry<Integer, Set<L>> entry : l.getOutExitEdges()) {
                        Integer i = entry.getKey();
                        Set<L> tgts = entry.getValue();
                        l.paras.stream().flatMap(para -> para.getInEntryEdges(i).stream()).forEach(arg -> {
                            tgts.forEach(tgt -> {
                                if (arg.addOutEdge(tgt)) { //add match edge
                                    if (arg.isVisited() && tgt.setVisited()) {
                                        workList.add(tgt);
                                    }
                                    if (tgt.update(arg)) {
                                        paraWorkList.add(tgt);
                                    }
                                }
                            });
                        });
                    }
                }
            } // para while
        } // outer while
    }

    private void resetNodes() {
        // reset nodes' visited state
        G.g2GN.values().forEach(BNode::reset);
        G.g2GP.values().forEach(BNode::reset);
        L.l2LN.values().forEach(BNode::reset);
        L.l2LP.values().forEach(BNode::reset);
        O.o2O.values().forEach(BNode::reset);
        // clear paras
        L.l2LN.values().forEach(I::clearParas);
        L.l2LP.values().forEach(I::clearParas);
        O.o2O.values().forEach(I::clearParas);
    }

    public Map<Object, Integer> process() {
        System.out.print("cs2 propogating ...");
        long time = System.currentTimeMillis();

        Set<BNode> workList = new HashSet<>();
        Set<I> paraWorkList = new HashSet<>();

        //forward processing...
        O.o2O.values().forEach(o -> {
            o.setVisited();
            workList.add(o);
        });
        propagate(workList, paraWorkList);

        //record and reset...
        Set<O> entryO = O.o2O.values().stream().filter(o -> !o.paras.isEmpty()).collect(Collectors.toSet());
        Set<L> entryL = Stream.concat(L.l2LP.values().stream(), L.l2LN.values().stream())
                .filter(l -> !l.paras.isEmpty()).collect(Collectors.toSet());
        resetNodes();

        //backward processing...
        L.l2LN.values().forEach(ln -> {
            ln.setVisited();
            workList.add(ln);
        });
        propagate(workList, paraWorkList);

        System.out.println((System.currentTimeMillis() - time) / 1000 + "s");

        Map<Object, Integer> ret = new HashMap<>();
        entryO.forEach(o -> {
            if (!o.paras.isEmpty()) {
                ret.put(o.sparkNode, 1);
            } else {
                ret.put(o.sparkNode, 0);
            }
        });
        entryL.forEach(l -> {
            if (!l.inv().paras.isEmpty()) {
                ret.put(l.sparkNode, 1);
            } else {
                ret.put(l.sparkNode, 0);
            }
        });
        this.sparkFields.forEach(f -> {
            ret.put(f, 1);
        });
        return ret;
    }


    Map<CallSite, Integer> call2Number = new HashMap<>();
    int totalCallsites = 0;

    int getCallSiteNumber(CallSite callsite) {
        Integer oldNumber = call2Number.get(callsite);
        if (oldNumber != null) {
            return oldNumber;
        }
        totalCallsites++;
        call2Number.put(callsite, totalCallsites);
        return totalCallsites;
    }


    private void buildGraph() {
        for (SootMethod method : prePTA.getNakedReachableMethods()) {
            if (method.isPhantom()) {
                continue;
            }
            MethodPAG srcmpag = prePAG.getMethodPAG(method);
            QueueReader<Node> reader = srcmpag.getInternalReader().clone();
            while (reader.hasNext()) {
                Node from = reader.next(), to = reader.next();
                if (from instanceof LocalVarNode) {
                    if (to instanceof LocalVarNode) {
                        this.addAssignEdge((LocalVarNode) from, (LocalVarNode) to);
                    } else if (to instanceof FieldRefNode fr) {
                        this.addStoreEdge((LocalVarNode) from, (LocalVarNode) fr.getBase());
                        this.sparkFields.add(fr.getField());
                    } else { // local-global
                        assert to instanceof GlobalVarNode;
                        this.addStaticStoreEdge((LocalVarNode) from, (GlobalVarNode) to);
                    }
                } else if (from instanceof AllocNode) {
                    if (to instanceof LocalVarNode) {
                        this.addNewEdge((AllocNode) from, (LocalVarNode) to);
                    } // GlobalVarNode
                } else if (from instanceof FieldRefNode fr) {
                    // load edge is treated as assign.
                    this.addAssignEdge((LocalVarNode) fr.getBase(), (LocalVarNode) to);
                    this.sparkFields.add(fr.getField());
                } else {
                    assert (from instanceof GlobalVarNode);
                    this.addStaticLoadEdge((GlobalVarNode) from, (LocalVarNode) to);
                }
            }

            // add exception edges that added dynamically during the pre-analysis.
            srcmpag.getExceptionEdges().forEach((k, vs) -> {
                for (Node v : vs) {
                    this.addAssignEdge((LocalVarNode) k, (LocalVarNode) v);
                }
            });

            // add invoke edges
            MethodNodeFactory srcnf = srcmpag.nodeFactory();
            for (final Unit u : srcmpag.getInvokeStmts()) {
                final Stmt s = (Stmt) u;

                CallSite callSite = new CallSite(u);
                InvokeExpr ie = s.getInvokeExpr();
                int numArgs = ie.getArgCount();
                Value[] args = new Value[numArgs];
                for (int i = 0; i < numArgs; i++) {
                    Value arg = ie.getArg(i);
                    if (!(arg.getType() instanceof RefLikeType) || arg instanceof NullConstant) {
                        continue;
                    }
                    args[i] = arg;
                }
                LocalVarNode retDest = null;
                if (s instanceof AssignStmt) {
                    Value dest = ((AssignStmt) s).getLeftOp();
                    if (dest.getType() instanceof RefLikeType) {
                        retDest = prePAG.findLocalVarNode(dest);
                    }
                }
                LocalVarNode receiver = null;
                if (ie instanceof InstanceInvokeExpr iie) {
                    receiver = prePAG.findLocalVarNode(iie.getBase());
                }
                for (Iterator<Edge> it = prePTA.getCallGraph().edgesOutOf(u); it.hasNext(); ) {
                    Edge e = it.next();
                    SootMethod tgtmtd = e.tgt();
                    MethodPAG tgtmpag = prePAG.getMethodPAG(tgtmtd);
                    MethodNodeFactory tgtnf = tgtmpag.nodeFactory();
                    for (int i = 0; i < numArgs; i++) {
                        if (args[i] == null || !(tgtmtd.getParameterType(i) instanceof RefLikeType)) {
                            continue;
                        }
                        LocalVarNode parm = (LocalVarNode) tgtnf.caseParm(i);
                        this.addEntryEdge((LocalVarNode) srcnf.getNode(args[i]), parm, callSite);
                    }
                    if (retDest != null && tgtmtd.getReturnType() instanceof RefLikeType) {
                        LocalVarNode ret = (LocalVarNode) tgtnf.caseRet();
                        this.addExitEdge(ret, retDest, callSite);
                    }
                    LocalVarNode stmtThrowNode = srcnf.makeInvokeStmtThrowVarNode(s, method);
                    LocalVarNode throwFinal = prePAG.findLocalVarNode(new Parm(tgtmtd, PointsToAnalysis.THROW_NODE));
                    if (throwFinal != null) {
                        this.addExitEdge(throwFinal, stmtThrowNode, callSite);
                    }
                    if (receiver != null) {
                        LocalVarNode thisRef = (LocalVarNode) tgtnf.caseThis();
                        this.addEntryEdge(receiver, thisRef, callSite);
                    }
                }
            }
        } // reachable methods.
    }
}