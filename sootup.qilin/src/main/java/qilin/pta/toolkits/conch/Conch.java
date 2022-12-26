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
import qilin.core.sets.PointsToSet;
import qilin.util.PTAUtils;
import qilin.util.Pair;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.spark.pag.SparkField;

import java.util.*;
import java.util.stream.Collectors;

/*
 * This classifier will classify heaps into context-dependent and context-independent heaps.
 * */
public class Conch extends AbstractConch {

    private final LeakAnalysis mfg;
    private final DepOnParamAnalysis pfg;

    private final Set<AllocNode> csHeaps = new HashSet<>();
    private final Set<AllocNode> ciHeaps = new HashSet<>();

    public Set<Object> ctxDependentHeaps() {
        return csHeaps.stream().map(AllocNode::getNewExpr).collect(Collectors.toSet());
    }

    public Set<AllocNode> ctxIndenpendentHeaps() {
        return ciHeaps;
    }

    public Set<AllocNode> ctxDependentHeaps2() {
        return csHeaps;
    }

    public Conch(PTA pta) {
        super(pta);
        this.mfg = new LeakAnalysis(pta);
        this.pfg = new DepOnParamAnalysis(pta);
    }

    private SootMethod findInvokedConstructorOf(AllocNode heap) {
        SootMethod containingMethod = heap.getMethod();
        MethodPAG cmpag = pag.getMethodPAG(containingMethod);
        MethodNodeFactory nodeFactory = cmpag.nodeFactory();
        for (Unit unit : cmpag.getInvokeStmts()) {
            if (unit instanceof InvokeStmt invokeStmt) {
                InvokeExpr expr = invokeStmt.getInvokeExpr();
                if (expr instanceof SpecialInvokeExpr iie) {
                    Value base = iie.getBase();
                    VarNode baseNode = (VarNode) nodeFactory.getNode(base);
                    PointsToSet v1pts = pta.reachingObjects(baseNode);
                    SootMethod target = iie.getMethod();
                    if (v1pts.size() == 1 && v1pts.toCIPointsToSet().contains(heap) && target.isConstructor()) {
                        return target;
                    }
                }
            }
        }
        return null; // not found.
    }

    private SootMethod findInvokedConstructorOf(SootMethod outerInit) {
        MethodPAG cmpag = pag.getMethodPAG(outerInit);
        MethodNodeFactory nodeFactory = cmpag.nodeFactory();
        VarNode thisNode = nodeFactory.caseThis();
        for (Unit unit : cmpag.getInvokeStmts()) {
            if (unit instanceof InvokeStmt invokeStmt) {
                InvokeExpr expr = invokeStmt.getInvokeExpr();
                if (expr instanceof SpecialInvokeExpr iie) {
                    Value base = iie.getBase();
                    VarNode baseNode = (VarNode) nodeFactory.getNode(base);
                    SootMethod target = iie.getMethod();
                    if (PTAUtils.mustAlias(pta, thisNode, baseNode) && target.isConstructor()) {
                        return target;
                    }
                }
            }
        }
        return null; // not found.
    }

    private ArrayList<SootMethod> recoverConstructorChain(SootMethod sm, AllocNode heap) {
        ArrayList<SootMethod> ret = new ArrayList<>();
        SootMethod origInit = findInvokedConstructorOf(heap);
        if (origInit != null) {
            while (origInit != sm) {
                ret.add(0, origInit);
                origInit = findInvokedConstructorOf(origInit);
                if (origInit == null) {
                    break;
                }
            }
        }
        return ret;
    }

    private Set<Node> mappingtoCallerCommingParamsOrHeaps(Set<Node> params, SootMethod curr, SootMethod caller) {
        MethodPAG cmpag = pag.getMethodPAG(caller);
        Set<Node> ret = new HashSet<>();
        for (Unit unit : cmpag.getInvokeStmts()) {
            Stmt stmt = (Stmt) unit;
            if (!(stmt.getInvokeExpr() instanceof SpecialInvokeExpr)) {
                continue;
            }
            SootMethod target = stmt.getInvokeExpr().getMethod();
            if (target != null && target.equals(curr)) {
                for (Node n : params) {
                    if (n instanceof VarNode paramNode) {
                        LocalVarNode argNode = PTAUtils.paramToArg(pag, stmt, cmpag, paramNode);
                        if (argNode != null) {
                            ret.addAll(this.pfg.fetchReachableParamsOf(argNode));
                        }
                    }
                }
            }
        }
        return ret;
    }

    private boolean containHeaps(Set<Node> nodes) {
        boolean ret = false;
        for (Node n : nodes) {
            if (n instanceof AllocNode) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    private Trilean handleTransitiveConstructors(SootMethod sm, AllocNode heap, Set<Node> params) {
        SootMethod containingMethod = heap.getMethod();
        ArrayList<SootMethod> chain = recoverConstructorChain(sm, heap);
        SootMethod caller = sm;
        SootMethod curr;
        Set<Node> ret = params;
        boolean notSure = containHeaps(params);
        for (SootMethod method : chain) {
            curr = caller;
            caller = method;
            ret = mappingtoCallerCommingParamsOrHeaps(ret, curr, caller);
            notSure |= containHeaps(ret);
            Trilean res = checkResult(ret);
            if (res != Trilean.TRUE) {
                if (notSure) {
                    return Trilean.UNKNOWN;
                } else {
                    return Trilean.FALSE;
                }
            }
        }
        curr = caller;
        caller = containingMethod;
        ret = mappingtoCallerCommingParamsOrHeaps(ret, curr, caller);
        Trilean tmpRes2 = checkResult(ret);
        if (notSure) {
            tmpRes2 = Trilean.OR(tmpRes2, Trilean.UNKNOWN);
        }
        return tmpRes2;
    }

    private Trilean checkResult(Set<Node> res) {
        if (res.isEmpty()) {
            return Trilean.FALSE;
        }
        boolean hasParam = false;
        for (Node n : res) {
            if (!(n instanceof AllocNode)) {
                hasParam = true;
                break;
            }
        }
        if (hasParam) {
            return Trilean.TRUE;
        } else {
            return Trilean.UNKNOWN;
        }
    }

    private Trilean isCommingFromParams(LocalVarNode from, SootMethod method, AllocNode heap) {
        Set<Node> ret = this.pfg.fetchReachableParamsOf(from);
        if (method.isConstructor()) {
            return handleTransitiveConstructors(method, heap, ret);
        } else {
            return checkResult(ret);
        }
    }

    private final Map<AllocNode, Set<SparkField>> notSureFields = new HashMap<>();

    private Trilean checkHeap(AllocNode heap) {
        Set<SparkField> fields = o2fs.getOrDefault(heap, Collections.emptySet());
        Trilean ret = Trilean.FALSE;
        for (SparkField field : fields) {
            Trilean csorci = Trilean.FALSE;
            if (!hasLoadOn(heap, field) || !hasStoreOn(heap, field) || emptyFieldPts(heap, field)) {
                continue;
            }

            // check stores:
            Map<SparkField, Set<Pair<VarNode, VarNode>>> f2sts = o2nonThisFStores.getOrDefault(heap, Collections.emptyMap());
            Set<Pair<VarNode, VarNode>> pairs = f2sts.getOrDefault(field, Collections.emptySet());
            if (!pairs.isEmpty()) {
                for (Pair<VarNode, VarNode> pair : pairs) {
                    LocalVarNode storeBase = (LocalVarNode) pair.getFirst();
                    VarNode from = pair.getSecond();
                    if (storeBase.getMethod() != heap.getMethod()) {
                        csorci = Trilean.TRUE;
                    } else {
                        Trilean fromparam = isCommingFromParams((LocalVarNode) from, storeBase.getMethod(), heap);
                        csorci = Trilean.OR(csorci, fromparam);
                        if (fromparam == Trilean.UNKNOWN) {
                            notSureFields.computeIfAbsent(heap, k -> new HashSet<>()).add(field);
                        }
                    }
                }
            }
            Set<SootMethod> onMethods = invokedMethods.getOrDefault(heap, Collections.emptySet());
            for (SootMethod method : onMethods) {
                Map<SparkField, Set<Pair<VarNode, VarNode>>> f2stsX = m2thisFStores.getOrDefault(method, Collections.emptyMap());
                Set<Pair<VarNode, VarNode>> thisFStores = f2stsX.getOrDefault(field, Collections.emptySet());
                if (!thisFStores.isEmpty()) {
                    for (Pair<VarNode, VarNode> pair : thisFStores) {
                        VarNode from = pair.getSecond();
                        Trilean fromparam = isCommingFromParams((LocalVarNode) from, method, heap);
                        csorci = Trilean.OR(csorci, fromparam);
                    }
                }
            }

            ret = Trilean.OR(ret, csorci);
            if (csorci == Trilean.UNKNOWN) {
                notSureFields.computeIfAbsent(heap, k -> new HashSet<>()).add(field);
            }
        }
        return ret;
    }

    private boolean hasInstanceFieldWithStoreLoad(AllocNode heap) {
        Set<SparkField> fields = o2fs.getOrDefault(heap, Collections.emptySet());
        for (SparkField field : fields) {
            boolean hasLoads = hasLoadOn(heap, field);
            boolean hasStores = hasStoreOn(heap, field);
            boolean emptyFieldPts = emptyFieldPts(heap, field);
            if (!hasLoads || !hasStores || emptyFieldPts) {
                continue;
            }
            return true;
        }
        return false;
    }

    public void runClassifier() {
        Collection<AllocNode> allHeaps = pag.getAllocNodes();
        int heapCnt = allHeaps.size();
        int[] condACnt = new int[1];

        /*
         * pre-process.
         * Those heaps usually are assigned empty context in a tradition pointer analysis.
         * Classify them to be CS or CI does not affect the efficiency of pointer analysis.
         * Thus, we handle them in the pre-process.
         */
        Set<AllocNode> remainToSolve = new HashSet<>();
        allHeaps.forEach(heap -> {
            if (heap.getMethod() == null
                    || heap instanceof ConstantNode
                    || PTAUtils.isEmptyArray(heap)
                    || PTAUtils.isOfPrimitiveBaseType(heap)) {
                ciHeaps.add(heap);
            } else {
                SootMethod mthd = heap.getMethod();
                if (mthd.isStaticInitializer()) {
                    ciHeaps.add(heap);
                } else {
                    remainToSolve.add(heap);
                }
            }
        });

        // check by rules.
        Set<AllocNode> unknownyet = new HashSet<>();
        remainToSolve.forEach(heap -> {
            // Obs 2
            boolean condA = this.mfg.isLeakObject(heap);
            condACnt[0] += (condA ? 1 : 0);
            if (!condA) {
                ciHeaps.add(heap);
                return;
            }
            // Obs 1
            boolean condB = hasInstanceFieldWithStoreLoad(heap);
            if (!condB) {
                ciHeaps.add(heap);
                return;
            }
            // Obs 3 (a)
            Trilean condExtra = checkHeap(heap);
            if (condExtra == Trilean.TRUE) {
                csHeaps.add(heap);
            } else if (condExtra == Trilean.FALSE) {
                ciHeaps.add(heap);
            } else {
                unknownyet.add(heap);
            }
        });
        // Obs 3 (b)
        classifyForRemain(unknownyet);
        // stat
        System.out.println("#Heaps:" + heapCnt);
        System.out.println("#CondA:" + condACnt[0]);
        System.out.println("#CS:" + csHeaps.size());
        System.out.println("#CI:" + ciHeaps.size());
    }

    private void classifyForRemain(Set<AllocNode> unknownyet) {
        CSDG csdg = new CSDG();
        // build the dependency graph.
        for (AllocNode heap : unknownyet) {
            Set<SparkField> ifs = notSureFields.getOrDefault(heap, Collections.emptySet());
            boolean cs = false;
            boolean existUnknown = false;
            Set<AllocNode> tos = new HashSet<>();
            for (SparkField sf : ifs) {
                PointsToSet pts = pta.reachingObjectsInternal(heap, sf);
                for (AllocNode o : pts.toCIPointsToSet().toCollection()) {
                    // if o is cs, then heap is cs;
                    if (csHeaps.contains(o)) {
                        cs = true;
                        break;
                    }
                    if (!ciHeaps.contains(o)) {
                        // exist unknown.
                        tos.add(o);
                        existUnknown = true;
                    }
                }
                if (cs) {
                    break;
                }
            }
            if (cs) {
                csHeaps.add(heap);
            } else if (existUnknown) {
                for (AllocNode to : tos) {
                    csdg.addEdge(heap, to);
                }
            } else {
                ciHeaps.add(heap);
            }
        }
        // recursively classify heaps for these on CSDG.
        System.out.println("#InitOnCSDG:" + csdg.allNodes().size());
        while (true) {
            Set<AllocNode> noOutDegree = csdg.noOutDegreeNodes();
            if (noOutDegree.isEmpty()) {
                break;
            }
            for (AllocNode nod : noOutDegree) {
                if (csHeaps.contains(nod)) {
                    csHeaps.addAll(csdg.predsOf(nod));
                } else {
                    ciHeaps.add(nod);
                }
                csdg.removeNode(nod);
            }
        }
        System.out.println("#StillOnCSDG:" + csdg.allNodes().size());
        ciHeaps.addAll(csdg.allNodes());
    }
}
