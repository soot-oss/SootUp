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

package qilin.pta.toolkits.eagle;

import qilin.core.PTA;
import qilin.core.PointsToAnalysis;
import qilin.core.builder.MethodNodeFactory;
import qilin.core.pag.*;
import qilin.core.sets.PointsToSet;
import qilin.util.PTAUtils;
import qilin.util.Util;
import qilin.util.queue.UniqueQueue;
import soot.RefLikeType;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.*;
import soot.jimple.spark.pag.SparkField;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.util.queue.QueueReader;

import java.util.*;
import java.util.stream.Collectors;

// implementation of Eagle (OOPSLA'19).
public class Eagle {
    protected Map<Object, Map<Boolean, BNode>> sparkNode2BNode = new HashMap<>();
    // fields for storage
    public Set<BNode> allocs = new HashSet<>();
    public Set<BNode> allocIs = new HashSet<>();
    public Map<BNode, Set<BNode>> outEdges = new HashMap<>();
    protected Map<BNode, Set<BNode>> balancedOutEdges = new HashMap<>();

    private int new_count = 0;
    private int assign_count = 0;
    protected int store_count = 0;
    private int load_count = 0;
    private int balance_count = 0;
    private int hstore_count = 0;
    private int hload_count = 0;
    private int total_nodes_count = 0;
    private int total_edges_count = 0;

    public void dumpCount() {
        System.out.println("#NEW:" + new_count);
        System.out.println("#ASSIGN:" + assign_count);
        System.out.println("#STORE:" + store_count);
        System.out.println("#LOAD:" + load_count);
        System.out.println("#HSTORE:" + hstore_count);
        System.out.println("#HLOAD:" + hload_count);
        System.out.println("#BALANCE:" + balance_count);
    }

    public Collection<? extends BNode> getNodes() {
        return sparkNode2BNode.values().stream().flatMap(subMap -> subMap.values().stream())
                .collect(Collectors.toSet());
    }

    public Collection<Object> getSparkNodes() {
        return sparkNode2BNode.values().stream().flatMap(subMap -> subMap.values().stream()).map(gn -> gn.sparkNode)
                .collect(Collectors.toSet());
    }

    public BNode getBNode(Object origin, Boolean forward) {
        Map<Boolean, BNode> subMap = sparkNode2BNode.computeIfAbsent(origin, k -> new HashMap<>());
        if (subMap.containsKey(forward)) {
            return subMap.get(forward);
        } else {
            BNode ret = new BNode(origin, forward);
            subMap.put(forward, ret);
            total_nodes_count++;
            return ret;
        }
    }

    protected void addNormalEdge(BNode from, BNode to) {
        Set<BNode> m = outEdges.computeIfAbsent(from, k -> new HashSet<>());
        m.add(to);
        total_edges_count++;
    }

    public boolean addBalancedEdge(BNode from, BNode to) {
        boolean ret = Util.addToMap(balancedOutEdges, from, to);
        balance_count++;
        total_edges_count++;
        return ret;
    }

    public void addNewEdge(AllocNode from, LocalVarNode to) {
        BNode fromE = getBNode(from, true), toE = getBNode(to, true);
        addNormalEdge(fromE, toE);
        BNode toEI = getBNode(to, false), fromEI = getBNode(from, false);
        addNormalEdge(toEI, fromEI);

        new_count++;
        allocs.add(fromE);
        allocIs.add(fromEI);
    }

    public void addAssignEdge(LocalVarNode from, LocalVarNode to) {
        BNode fromE = getBNode(from, true), toE = getBNode(to, true);
        addNormalEdge(fromE, toE);
        BNode toEI = getBNode(to, false), fromEI = getBNode(from, false);
        addNormalEdge(toEI, fromEI);

        assign_count++;
    }

    public void addStoreEdge(LocalVarNode from, LocalVarNode base) {
        BNode fromE = getBNode(from, true), baseEI = getBNode(base, false);
        addNormalEdge(fromE, baseEI);
        BNode baseE = getBNode(base, true), fromEI = getBNode(from, false);
        addNormalEdge(baseE, fromEI);

        store_count++;
    }

    public void addLoadEdge(LocalVarNode base, LocalVarNode to) {
        BNode baseE = getBNode(base, true), toE = getBNode(to, true);
        addNormalEdge(baseE, toE);
        BNode toEI = getBNode(to, false), baseEI = getBNode(base, false);
        addNormalEdge(toEI, baseEI);

        load_count++;
    }

    public void addHstoreEdge(Object from, AllocNode baseObj) {
        BNode fromE = getBNode(from, true), baseObjE = getBNode(baseObj, true);
        addNormalEdge(fromE, baseObjE);
        BNode baseObjEI = getBNode(baseObj, false), fromEI = getBNode(from, false);
        addNormalEdge(baseObjEI, fromEI);
        allocIs.add(baseObjEI);
        allocs.add(baseObjE);
        hstore_count++;
    }

    public void addHloadEdge(AllocNode baseObj, Object to) {
        BNode baseObjEI = getBNode(baseObj, false), toE = getBNode(to, true);
        addNormalEdge(baseObjEI, toE);
        BNode toEI = getBNode(to, false), baseObjE = getBNode(baseObj, true);
        addNormalEdge(toEI, baseObjE);
        allocIs.add(baseObjEI);
        allocs.add(baseObjE);
        hload_count++;
    }

    public int totalEdgesCount() {
        return total_edges_count;
    }

    public int totalNodesCount() {
        return total_nodes_count;
    }

    public Set<BNode> getAllOutEdges(BNode node) {
        Set<BNode> ret = new HashSet<>(getOutEdges(node));
        if (balancedOutEdges.containsKey(node)) {
            ret.addAll(balancedOutEdges.get(node));
        }
        return ret;
    }

    public Collection<BNode> getOutEdges(BNode node) {
        return outEdges.getOrDefault(node, Collections.emptySet());
    }

    public boolean reachValidReceiverObject(BNode from, BNode to) {
        BNode fromEI = getBNode(to.sparkNode, false);
        if (from.sparkNode instanceof Field || from.sparkNode instanceof ArrayElement) {
            return getOutEdges(fromEI).contains(from);
        }
        return true;
    }

    // eagle propagate
    protected boolean enterCS(BNode node) {
        return node.entryCS();
    }

    public Map<Object, Integer> contxtLengthAnalysis() {
        Queue<BNode> workList = new UniqueQueue<>();
        Set<Object> matchedObjects = new HashSet<>();
        // start from all "parameter/field" node
        for (BNode heapNode : allocIs) {
            for (BNode dst : getOutEdges(heapNode)) {
                dst.cs = true;
                workList.add(dst);
            }
        }
        while (!workList.isEmpty()) {
            BNode node = workList.poll();
            for (BNode dst : getAllOutEdges(node)) {
                if (dst.isHeapPlus() && !node.isHeapMinus()) {
                    if (reachValidReceiverObject(node, dst)) {
                        if (matchedObjects.add(dst.sparkNode)) { // add balanced edges
                            BNode fromEI = getBNode(dst.sparkNode, false);
                            addBalancedEdge(fromEI, dst, workList);
                        }
                    }
                } else {
                    if (enterCS(dst)) {
                        workList.add(dst);
                    }
                }
            }
        }
        Map<Object, Integer> ret = new HashMap<>();
        getSparkNodes().forEach(sparkNode -> {
            BNode node = getBNode(sparkNode, true);
            BNode nodeInv = getBNode(sparkNode, false);
            ret.put(sparkNode, node.cs && nodeInv.cs ? 1 : 0);
        });
        return ret;
    }

    protected void addBalancedEdge(BNode from, BNode to, Queue<BNode> workList) {
        if (addBalancedEdge(from, to)) {
            if (from.cs) {
                workList.add(from);// add src of the balanced edges to worklist
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////

    protected void addParamEdges(AllocNode o, LocalVarNode thisRef, LocalVarNode[] parms, LocalVarNode mret, LocalVarNode mThrow) {
        this.addHloadEdge(o, thisRef);
        for (VarNode parm : parms) {
            if (parm != null) {
                this.addHloadEdge(o, parm);
            }
        }
        if (mret != null) {
            this.addHstoreEdge(mret, o);
        }
        if (mThrow != null) {
            this.addHstoreEdge(mThrow, o);
        }
    }

    public void buildGraph(PTA prePTA) {
        PAG prePAG = prePTA.getPag();
        // calculate points-to set for "This" pointer in each static method.
        Map<LocalVarNode, Set<AllocNode>> pts = PTAUtils.calcStaticThisPTS(prePTA);

        CallGraph callGraph = prePTA.getCallGraph();
        for (SootMethod method : prePTA.getNakedReachableMethods()) {
            if (method.isPhantom()) {
                continue;
            }
            MethodPAG srcmpag = prePAG.getMethodPAG(method);
            MethodNodeFactory srcnf = srcmpag.nodeFactory();
            LocalVarNode thisRef = (LocalVarNode) srcnf.caseThis();
            // add local edges
            if (PTAUtils.isFakeMainMethod(method)) {
                // special treatment for fake main
                this.addNewEdge(prePTA.getRootNode(), thisRef);
            }
            QueueReader<Node> reader = srcmpag.getInternalReader().clone();
            while (reader.hasNext()) {
                Node from = reader.next(), to = reader.next();
                if (from instanceof LocalVarNode) {
                    if (to instanceof LocalVarNode)
                        this.addAssignEdge((LocalVarNode) from, (LocalVarNode) to);
                    else if (to instanceof FieldRefNode fr) {
                        this.addStoreEdge((LocalVarNode) from, (LocalVarNode) fr.getBase());
                    }  // local-global

                } else if (from instanceof AllocNode) {
                    if (to instanceof LocalVarNode) {
                        this.addNewEdge((AllocNode) from, (LocalVarNode) to);
                    } // GlobalVarNode
                } else if (from instanceof FieldRefNode fr) {
                    this.addLoadEdge((LocalVarNode) fr.getBase(), (LocalVarNode) to);
                }  // global-local
            }
            // add exception edges that added dynamically during the pre-analysis.
            srcmpag.getExceptionEdges().forEach((k, vs) -> {
                for (Node v : vs) {
                    this.addAssignEdge((LocalVarNode) k, (LocalVarNode) v);
                }
            });
            // add para edges
            int numParms = method.getParameterCount();
            LocalVarNode[] parms = new LocalVarNode[numParms];
            for (int i = 0; i < numParms; i++) {
                if (method.getParameterType(i) instanceof RefLikeType) {
                    parms[i] = (LocalVarNode) srcnf.caseParm(i);
                }
            }
            LocalVarNode mret = method.getReturnType() instanceof RefLikeType ? (LocalVarNode) srcnf.caseRet() : null;
            LocalVarNode throwFinal = prePAG.findLocalVarNode(new Parm(method, PointsToAnalysis.THROW_NODE));
            if (method.isStatic()) {
                pts.getOrDefault(thisRef, Collections.emptySet()).forEach(a -> {
                    addParamEdges(a, thisRef, parms, mret, throwFinal);
                });
            } else {
                PointsToSet thisPts = prePTA.reachingObjects(thisRef).toCIPointsToSet();
                for (Iterator<AllocNode> it = thisPts.iterator(); it.hasNext(); ) {
                    AllocNode n = it.next();
                    addParamEdges(n, thisRef, parms, mret, throwFinal);
                }
            }

            // add invoke edges
            for (final Unit u : srcmpag.getInvokeStmts()) {
                final Stmt s = (Stmt) u;
                InvokeExpr ie = s.getInvokeExpr();
                int numArgs = ie.getArgCount();
                Value[] args = new Value[numArgs];
                for (int i = 0; i < numArgs; i++) {
                    Value arg = ie.getArg(i);
                    if (!(arg.getType() instanceof RefLikeType) || arg instanceof NullConstant)
                        continue;
                    args[i] = arg;
                }
                LocalVarNode retDest = null;
                if (s instanceof AssignStmt) {
                    Value dest = ((AssignStmt) s).getLeftOp();
                    if (dest.getType() instanceof RefLikeType) {
                        retDest = prePAG.findLocalVarNode(dest);
                    }
                }
                LocalVarNode receiver;
                if (ie instanceof InstanceInvokeExpr iie) {
                    receiver = prePAG.findLocalVarNode(iie.getBase());
                } else {
                    // static call
                    receiver = thisRef;
                }
                for (Iterator<Edge> it = callGraph.edgesOutOf(u); it.hasNext(); ) {
                    Edge e = it.next();
                    SootMethod tgtmtd = e.tgt();
                    for (int i = 0; i < numArgs; i++) {
                        if (args[i] == null || !(tgtmtd.getParameterType(i) instanceof RefLikeType))
                            continue;
                        ValNode argNode = prePAG.findValNode(args[i]);
                        if (argNode instanceof LocalVarNode) {
                            this.addStoreEdge((LocalVarNode) argNode, receiver);
                        }
                    }
                    if (retDest != null && tgtmtd.getReturnType() instanceof RefLikeType) {
                        this.addLoadEdge(receiver, retDest);
                    }
                    LocalVarNode stmtThrowNode = srcnf.makeInvokeStmtThrowVarNode(s, method);
                    this.addLoadEdge(receiver, stmtThrowNode);
                    this.addStoreEdge(receiver, receiver);// do not move this out of loop
                }
            }
        }

        // add field edges
        prePAG.getContextFields().forEach(contextField -> {
            AllocNode base = contextField.getBase();
            SparkField field = contextField.getField();
            if (!prePAG.simpleInvLookup(contextField).isEmpty()) {
                this.addHloadEdge(base, field);
            }
            if (!prePAG.simpleLookup(contextField).isEmpty()) {
                this.addHstoreEdge(field, base);
            }
        });
    }
}
