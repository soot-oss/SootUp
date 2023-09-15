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
import soot.ArrayType;
import soot.PrimType;
import soot.SootMethod;
import soot.jimple.spark.pag.SparkField;

import java.util.*;

public class AbstractConch {
    public final PTA pta;
    public final PAG pag;

    protected final Map<AllocNode, Set<SootMethod>> invokedMethods = new HashMap<>();
    // field |--> <store_base, from>
    protected final Map<SootMethod, Map<SparkField, Set<Pair<VarNode, VarNode>>>> m2thisFStores = new HashMap<>();
    protected final Map<AllocNode, Map<SparkField, Set<Pair<VarNode, VarNode>>>> o2nonThisFStores = new HashMap<>();
    protected final Map<SootMethod, Map<SparkField, Set<VarNode>>> m2thisFLoads = new HashMap<>();
    protected final Map<AllocNode, Map<SparkField, Set<VarNode>>> o2nonThisFLoads = new HashMap<>();
    protected final Map<AllocNode, Set<SparkField>> o2fs = new HashMap<>();

    public AbstractConch(PTA pta) {
        this.pta = pta;
        this.pag = pta.getPag();
        init();
    }

    private void init() {
        /*
         * Static methods are modeled as a special instance methods of its most recent instance methods' receciver objects.
         * thus, inherit its most recent instance methods' contexts (which is standard in the literature).
         * The following line computes the receiver objects for the this_ptr of static methods.
         * */
        Map<LocalVarNode, Set<AllocNode>> pts = PTAUtils.calcStaticThisPTS(pta);
        pta.getNakedReachableMethods().stream().filter(m -> !m.isPhantom()).forEach(method -> {
            collectStoresIn(method);
            collectLoadsIn(method);
            buildInvokedOnFor(method, pts);
        });

        buildHeap2AccessedFieldsMap();
    }

    /*
     * build a map, heap |--> {fields}.
     * */
    private void buildHeap2AccessedFieldsMap() {
        for (AllocNode heap : pag.getAllocNodes()) {
            Set<SparkField> tmp = new HashSet<>();
            tmp.addAll(o2nonThisFLoads.getOrDefault(heap, Collections.emptyMap()).keySet());
            tmp.addAll(o2nonThisFStores.getOrDefault(heap, Collections.emptyMap()).keySet());
            for (SootMethod sm : invokedMethods.getOrDefault(heap, Collections.emptySet())) {
                tmp.addAll(m2thisFLoads.getOrDefault(sm, Collections.emptyMap()).keySet());
                tmp.addAll(m2thisFStores.getOrDefault(sm, Collections.emptyMap()).keySet());
            }
            o2fs.put(heap, tmp);
        }
    }

    /*
     * give a method, we map its receiver objects to this method.
     * */
    private void buildInvokedOnFor(SootMethod m, Map<LocalVarNode, Set<AllocNode>> pts) {
        MethodPAG srcmpag = pag.getMethodPAG(m);
        MethodNodeFactory srcnf = srcmpag.nodeFactory();
        LocalVarNode thisRef = (LocalVarNode) srcnf.caseThis();

        if (m.isStatic()) {
            pts.getOrDefault(thisRef, Collections.emptySet()).forEach(a -> {
                invokedMethods.computeIfAbsent(a, k -> new HashSet<>()).add(m);
            });
        } else {
            PointsToSet thisPts = pta.reachingObjects(thisRef).toCIPointsToSet();
            for (Iterator<AllocNode> it = thisPts.iterator(); it.hasNext(); ) {
                AllocNode n = it.next();
                invokedMethods.computeIfAbsent(n, k -> new HashSet<>()).add(m);
            }
        }
    }

    private final Map<MethodPAG, SMPAG> methodSMPAGMap = new HashMap<>();

    public SMPAG getSMAPG(MethodPAG mpag) {
        return methodSMPAGMap.computeIfAbsent(mpag, k -> new SMPAG(mpag));
    }

    private void collectLoadsIn(SootMethod method) {
        MethodPAG srcmpag = pag.getMethodPAG(method);
        MethodNodeFactory srcnf = srcmpag.nodeFactory();
        LocalVarNode thisRef = (LocalVarNode) srcnf.caseThis();
        SMPAG smpag = getSMAPG(srcmpag);
        for (Pair<Node, Node> ld : smpag.getLoads()) {
            FieldRefNode fr = (FieldRefNode) ld.getSecond();
            LocalVarNode loadBase = (LocalVarNode) fr.getBase();
            SparkField field = fr.getField();
            if (primitiveField(field)) {
                continue;
            }
            if (PTAUtils.mustAlias(pta, thisRef, loadBase)) { // handle THIS LOAD, i.e., ... = this.f
                Map<SparkField, Set<VarNode>> f2bs = m2thisFLoads.computeIfAbsent(method, k -> new HashMap<>());
                f2bs.computeIfAbsent(field, k -> new HashSet<>()).add(loadBase);
            } else {
                for (AllocNode heap : pta.reachingObjects(loadBase).toCIPointsToSet().toCollection()) {
                    if (heap.getMethod() != method) {
                        /* we filter loads in the containing method,
                         * since this often not satisfy the second theoretical condition O.f*--)-->v.
                         */
                        Map<SparkField, Set<VarNode>> f2bs = o2nonThisFLoads.computeIfAbsent(heap, k -> new HashMap<>());
                        f2bs.computeIfAbsent(field, k -> new HashSet<>()).add(loadBase);
                    }
                }
            }
        }
    }

    private void collectStoresIn(SootMethod method) {
        MethodPAG srcmpag = pag.getMethodPAG(method);
        MethodNodeFactory srcnf = srcmpag.nodeFactory();
        LocalVarNode thisRef = (LocalVarNode) srcnf.caseThis();
        SMPAG smpag = getSMAPG(srcmpag);
        for (Pair<Node, Node> st : smpag.getStores()) {
            LocalVarNode from = (LocalVarNode) st.getSecond();
            FieldRefNode fr = (FieldRefNode) st.getFirst();
            LocalVarNode storeBase = (LocalVarNode) fr.getBase();
            SparkField field = fr.getField();
            if (primitiveField(field)) {
                continue;
            }
            if (PTAUtils.mustAlias(pta, thisRef, storeBase)) { // handle this STORE, i.e., this.f = ...
                Map<SparkField, Set<Pair<VarNode, VarNode>>> m2s = m2thisFStores.computeIfAbsent(method, k -> new HashMap<>());
                m2s.computeIfAbsent(field, k -> new HashSet<>()).add(new Pair<>(storeBase, from));
            } else {
                for (AllocNode heap : pta.reachingObjects(storeBase).toCIPointsToSet().toCollection()) {
                    if (!emptyFieldPts(heap, field)) {
                        Map<SparkField, Set<Pair<VarNode, VarNode>>> f2bs = o2nonThisFStores.computeIfAbsent(heap, k -> new HashMap<>());
                        f2bs.computeIfAbsent(field, k -> new HashSet<>()).add(new Pair<>(storeBase, from));
                    }
                }
            }
        }
    }

    private boolean primitiveField(SparkField f) {
        String s = "java.lang.String";
        if (f.getType() instanceof PrimType) {
            return true;
        } else if (f.getType() instanceof ArrayType at) {
            /*
             * here, we let primitive array as primitive type as that in Turner.
             * this wont hurt precision of clients.
             * */
            return at.baseType instanceof PrimType;
        } else return f.getType().toString().equals(s);
    }

    protected boolean emptyFieldPts(AllocNode heap, SparkField field) {
        PointsToSet pts = pta.reachingObjectsInternal(heap, field);
        Set<AllocNode> tmp = new HashSet<>();
        for (Iterator<AllocNode> it = pts.iterator(); it.hasNext(); ) {
            AllocNode n = it.next();
            // filter StringConstant.
            if (!(n instanceof StringConstantNode)) {
                tmp.add(n);
            }
        }
        return tmp.isEmpty();
    }

    protected boolean hasLoadOn(AllocNode heap, SparkField field) {
        Map<SparkField, Set<VarNode>> f2bs = o2nonThisFLoads.getOrDefault(heap, Collections.emptyMap());
        Set<VarNode> loadBases = f2bs.getOrDefault(field, Collections.emptySet());
        if (!loadBases.isEmpty()) {
            return true;
        }
        for (SootMethod method : invokedMethods.getOrDefault(heap, Collections.emptySet())) {
            Map<SparkField, Set<VarNode>> f2bsX = m2thisFLoads.getOrDefault(method, Collections.emptyMap());
            Set<VarNode> loadBasesX = f2bsX.getOrDefault(field, Collections.emptySet());
            if (!loadBasesX.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    protected boolean hasStoreOn(AllocNode heap, SparkField field) {
        Map<SparkField, Set<Pair<VarNode, VarNode>>> f2bs = o2nonThisFStores.getOrDefault(heap, Collections.emptyMap());
        Set<Pair<VarNode, VarNode>> storeBases = f2bs.getOrDefault(field, Collections.emptySet());
        if (!storeBases.isEmpty()) {
            return true;
        }
        for (SootMethod method : invokedMethods.getOrDefault(heap, Collections.emptySet())) {
            Map<SparkField, Set<Pair<VarNode, VarNode>>> f2bsX = m2thisFStores.getOrDefault(method, Collections.emptyMap());
            Set<Pair<VarNode, VarNode>> storeBasesX = f2bsX.getOrDefault(field, Collections.emptySet());
            if (!storeBasesX.isEmpty()) {
                return true;
            }
        }
        return false;
    }

}
