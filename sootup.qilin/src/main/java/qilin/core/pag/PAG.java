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

package qilin.core.pag;

import qilin.CoreConfig;
import qilin.core.PTA;
import qilin.core.PTAScene;
import qilin.core.PointsToAnalysis;
import qilin.core.natives.NativeMethodDriver;
import qilin.core.reflection.NopReflectionModel;
import qilin.core.reflection.ReflectionModel;
import qilin.core.reflection.TamiflexModel;
import qilin.parm.heapabst.HeapAbstractor;
import qilin.util.DataFactory;
import qilin.util.PTAUtils;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JArrayRef;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.spark.pag.SparkField;
import soot.util.ArrayNumberer;
import soot.util.queue.ChunkedQueue;
import soot.util.queue.QueueReader;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Pointer assignment graph.
 *
 * @author Ondrej Lhotak
 */
public class PAG {
    protected final NativeMethodDriver nativeDriver;
    protected final ReflectionModel reflectionModel;

    // ========================= context-sensitive nodes =================================
    protected final Map<VarNode, Map<Context, ContextVarNode>> contextVarNodeMap;
    protected final Map<AllocNode, Map<Context, ContextAllocNode>> contextAllocNodeMap;
    protected final Map<SootMethod, Map<Context, MethodOrMethodContext>> contextMethodMap;
    protected final Map<MethodPAG, Set<Context>> addedContexts;
    protected final Map<Context, Map<SparkField, ContextField>> contextFieldMap;

    // ==========================data=========================
    protected ArrayNumberer<AllocNode> allocNodeNumberer = new ArrayNumberer<>();
    protected ArrayNumberer<ValNode> valNodeNumberer = new ArrayNumberer<>();
    protected ArrayNumberer<FieldRefNode> fieldRefNodeNumberer = new ArrayNumberer<>();
    private static AtomicInteger maxFinishNumber = new AtomicInteger(0);

    // ========================= ir to Node ==============================================
    protected final Map<Object, AllocNode> valToAllocNode;
    protected final Map<Object, ValNode> valToValNode;
    protected final Map<SootMethod, MethodPAG> methodToPag;
    protected final Set<SootField> globals;
    protected final Set<Local> locals;
    // ==========================outer objects==============================
    protected ChunkedQueue<Node> edgeQueue;

    protected final Map<ValNode, Set<ValNode>> simple;
    protected final Map<ValNode, Set<ValNode>> simpleInv;
    protected final Map<FieldRefNode, Set<VarNode>> load;
    protected final Map<VarNode, Set<FieldRefNode>> loadInv;
    protected final Map<AllocNode, Set<VarNode>> alloc;
    protected final Map<VarNode, Set<AllocNode>> allocInv;
    protected final Map<VarNode, Set<FieldRefNode>> store;
    protected final Map<FieldRefNode, Set<VarNode>> storeInv;

    protected final PTA pta;

    public PAG(PTA pta) {
        this.pta = pta;
        this.simple = DataFactory.createMap();
        this.simpleInv = DataFactory.createMap();
        this.load = DataFactory.createMap();
        this.loadInv = DataFactory.createMap();
        this.alloc = DataFactory.createMap();
        this.allocInv = DataFactory.createMap();
        this.store = DataFactory.createMap();
        this.storeInv = DataFactory.createMap();
        this.nativeDriver = new NativeMethodDriver();
        this.reflectionModel = createReflectionModel();
        this.contextVarNodeMap = DataFactory.createMap(16000);
        this.contextAllocNodeMap = DataFactory.createMap(6000);
        this.contextMethodMap = DataFactory.createMap(6000);
        this.addedContexts = DataFactory.createMap();
        this.contextFieldMap = DataFactory.createMap(6000);
        this.valToAllocNode = DataFactory.createMap(10000);
        this.valToValNode = DataFactory.createMap(100000);
        this.methodToPag = DataFactory.createMap();
        this.globals = DataFactory.createSet(100000);
        this.locals = DataFactory.createSet(100000);
    }

    public void setEdgeQueue(ChunkedQueue<Node> edgeQueue) {
        this.edgeQueue = edgeQueue;
    }

    public Map<AllocNode, Set<VarNode>> getAlloc() {
        return alloc;
    }

    public Map<ValNode, Set<ValNode>> getSimple() {
        return simple;
    }

    public Map<ValNode, Set<ValNode>> getSimpleInv() {
        return simpleInv;
    }

    public Map<FieldRefNode, Set<VarNode>> getLoad() {
        return load;
    }

    public Map<FieldRefNode, Set<VarNode>> getStoreInv() {
        return storeInv;
    }

    public PTA getPta() {
        return this.pta;
    }

    public QueueReader<Node> edgeReader() {
        return edgeQueue.reader();
    }

    // =======================add edge===============================
    protected <K, V> boolean addToMap(Map<K, Set<V>> m, K key, V value) {
        Set<V> valueList = m.computeIfAbsent(key, k -> DataFactory.createSet(4));
        return valueList.add(value);
    }

    private boolean addAllocEdge(AllocNode from, VarNode to) {
        if (addToMap(alloc, from, to)) {
            addToMap(allocInv, to, from);
            return true;
        }
        return false;
    }

    private boolean addSimpleEdge(ValNode from, ValNode to) {
        if (addToMap(simple, from, to)) {
            addToMap(simpleInv, to, from);
            return true;
        }
        return false;
    }

    private boolean addStoreEdge(VarNode from, FieldRefNode to) {
        if (addToMap(storeInv, to, from)) {
            addToMap(store, from, to);
            return true;
        }
        return false;
    }

    private boolean addLoadEdge(FieldRefNode from, VarNode to) {
        if (addToMap(load, from, to)) {
            addToMap(loadInv, to, from);
            return true;
        }
        return false;
    }

    public void addGlobalPAGEdge(Node from, Node to) {
        from = pta.parameterize(from, pta.emptyContext());
        to = pta.parameterize(to, pta.emptyContext());
        addEdge(from, to);
    }

    /**
     * Adds an edge to the graph, returning false if it was already there.
     */
    public final void addEdge(Node from, Node to) {
        if (addEdgeIntenal(from, to)) {
            edgeQueue.add(from);
            edgeQueue.add(to);
        }
    }

    private boolean addEdgeIntenal(Node from, Node to) {
        if (from instanceof ValNode) {
            if (to instanceof ValNode) {
                return addSimpleEdge((ValNode) from, (ValNode) to);
            } else {
                return addStoreEdge((VarNode) from, (FieldRefNode) to);
            }
        } else if (from instanceof FieldRefNode) {
            return addLoadEdge((FieldRefNode) from, (VarNode) to);
        } else {
            AllocNode heap = (AllocNode) from;
            return addAllocEdge(heap, (VarNode) to);
        }
    }

    // ======================lookups===========================
    protected <K, V> Set<V> lookup(Map<K, Set<V>> m, K key) {
        return m.getOrDefault(key, Collections.emptySet());
    }

    public Set<VarNode> allocLookup(AllocNode key) {
        return lookup(alloc, key);
    }

    public Set<AllocNode> allocInvLookup(VarNode key) {
        return lookup(allocInv, key);
    }

    public Set<ValNode> simpleLookup(ValNode key) {
        return lookup(simple, key);
    }

    public Set<ValNode> simpleInvLookup(ValNode key) {
        return lookup(simpleInv, key);
    }

    public Set<FieldRefNode> loadInvLookup(VarNode key) {
        return lookup(loadInv, key);
    }

    public Set<VarNode> loadLookup(FieldRefNode key) {
        return lookup(load, key);
    }

    public Set<FieldRefNode> storeLookup(VarNode key) {
        return lookup(store, key);
    }

    public Set<VarNode> storeInvLookup(FieldRefNode key) {
        return lookup(storeInv, key);
    }

    public static int nextFinishNumber() {
        return maxFinishNumber.incrementAndGet();
    }

    public ArrayNumberer<AllocNode> getAllocNodeNumberer() {
        return allocNodeNumberer;
    }

    public ArrayNumberer<FieldRefNode> getFieldRefNodeNumberer() {
        return fieldRefNodeNumberer;
    }

    public ArrayNumberer<ValNode> getValNodeNumberer() {
        return valNodeNumberer;
    }

    public Collection<ValNode> getValNodes() {
        return valToValNode.values();
    }

    public Collection<AllocNode> getAllocNodes() {
        return valToAllocNode.values();
    }

    public Set<SootField> getGlobalPointers() {
        return globals;
    }

    public Set<Local> getLocalPointers() {
        return locals;
    }

    /**
     * Finds the ValNode for the variable value, or returns null.
     */
    public ValNode findValNode(Object value) {
        return valToValNode.get(value);
    }

    public AllocNode findAllocNode(Object obj) {
        return valToAllocNode.get(obj);
    }

    // ==========================create nodes==================================
    public AllocNode makeAllocNode(Object newExpr, Type type, SootMethod m) {
        AllocNode ret = valToAllocNode.get(newExpr);
        if (ret == null) {
            valToAllocNode.put(newExpr, ret = new AllocNode(newExpr, type, m));
            allocNodeNumberer.add(ret);
        } else if (!(ret.getType().equals(type))) {
            throw new RuntimeException("NewExpr " + newExpr + " of type " + type + " previously had type " + ret.getType());
        }
        return ret;
    }

    public AllocNode makeStringConstantNode(StringConstant sc) {
        StringConstant stringConstant = sc;
        if (!CoreConfig.v().getPtaConfig().stringConstants) {
            stringConstant = StringConstant.v(PointsToAnalysis.STRING_NODE);
        }
        AllocNode ret = valToAllocNode.get(stringConstant);
        if (ret == null) {
            valToAllocNode.put(stringConstant, ret = new StringConstantNode(stringConstant));
            allocNodeNumberer.add(ret);
        }
        return ret;
    }

    public AllocNode makeClassConstantNode(ClassConstant cc) {
        AllocNode ret = valToAllocNode.get(cc);
        if (ret == null) {
            valToAllocNode.put(cc, ret = new ClassConstantNode(cc));
            allocNodeNumberer.add(ret);
        }
        return ret;
    }

    /**
     * Finds or creates the GlobalVarNode for the variable value, of type type.
     */
    public GlobalVarNode makeGlobalVarNode(Object value, Type type) {
        GlobalVarNode ret = (GlobalVarNode) valToValNode.get(value);
        if (ret == null) {
            ret = (GlobalVarNode) valToValNode.computeIfAbsent(value, k -> new GlobalVarNode(value, type));
            valNodeNumberer.add(ret);
            if (value instanceof SootField) {
                globals.add((SootField) value);
            }
        } else if (!(ret.getType().equals(type))) {
            throw new RuntimeException("Value " + value + " of type " + type + " previously had type " + ret.getType());
        }
        return ret;
    }

    /**
     * Finds or creates the LocalVarNode for the variable value, of type type.
     */
    public LocalVarNode makeLocalVarNode(Object value, Type type, SootMethod method) {
        LocalVarNode ret = (LocalVarNode) valToValNode.get(value);
        if (ret == null) {
            valToValNode.put(value, ret = new LocalVarNode(value, type, method));
            valNodeNumberer.add(ret);
            if (value instanceof Local local) {
                if (local.getNumber() == 0) {
                    PTAScene.v().getLocalNumberer().add(local);
                }
                locals.add(local);
            }
        } else if (!(ret.getType().equals(type))) {
            throw new RuntimeException("Value " + value + " of type " + type + " previously had type " + ret.getType());
        }
        return ret;
    }

    /**
     * Finds or creates the FieldVarNode for the Java field or array element.
     * Treat Java field and array element as normal local variable.
     */
    public FieldValNode makeFieldValNode(SparkField field) {
        FieldValNode ret = (FieldValNode) valToValNode.get(field);
        if (ret == null) {
            valToValNode.put(field, ret = new FieldValNode(field));
            valNodeNumberer.add(ret);
        }
        return ret;
    }

    /**
     * Finds or creates the FieldRefNode for base variable base and field field, of
     * type type.
     */
    public FieldRefNode makeFieldRefNode(VarNode base, SparkField field) {
        FieldRefNode ret = base.dot(field);
        if (ret == null) {
            ret = new FieldRefNode(base, field);
            fieldRefNodeNumberer.add(ret);
        }
        return ret;
    }

    /**
     * Finds or creates the ContextVarNode for base variable base and context.
     */
    public ContextVarNode makeContextVarNode(VarNode base, Context context) {
        Map<Context, ContextVarNode> contextMap = contextVarNodeMap.computeIfAbsent(base, k1 -> DataFactory.createMap());
        ContextVarNode ret = contextMap.get(context);
        if (ret == null) {
            contextMap.put(context, ret = new ContextVarNode(base, context));
            valNodeNumberer.add(ret);
        }
        return ret;
    }

    /**
     * Finds or creates the ContextAllocNode for base alloc site and context.
     */
    public ContextAllocNode makeContextAllocNode(AllocNode allocNode, Context context) {
        Map<Context, ContextAllocNode> contextMap = contextAllocNodeMap.computeIfAbsent(allocNode, k1 -> DataFactory.createMap());
        ContextAllocNode ret = contextMap.get(context);
        if (ret == null) {
            contextMap.put(context, ret = new ContextAllocNode(allocNode, context));
            allocNodeNumberer.add(ret);
        }
        return ret;
    }

    /**
     * Finds or creates the ContextMethod for method and context.
     */
    public MethodOrMethodContext makeContextMethod(Context context, SootMethod method) {
        Map<Context, MethodOrMethodContext> contextMap = contextMethodMap.computeIfAbsent(method, k1 -> DataFactory.createMap());
        return contextMap.computeIfAbsent(context, k -> new ContextMethod(method, context));
    }

    public AllocNode getAllocNode(Object val) {
        return valToAllocNode.get(val);
    }

    public Map<MethodPAG, Set<Context>> getMethod2ContextsMap() {
        return addedContexts;
    }

    public boolean containsMethodPAG(SootMethod m) {
        return methodToPag.containsKey(m);
    }

    public Collection<ContextField> getContextFields() {
        return contextFieldMap.values().stream().flatMap(m -> m.values().stream()).collect(Collectors.toSet());
    }

    public Map<VarNode, Map<Context, ContextVarNode>> getContextVarNodeMap() {
        return contextVarNodeMap;
    }

    public Map<AllocNode, Map<Context, ContextAllocNode>> getContextAllocNodeMap() {
        return contextAllocNodeMap;
    }

    public Map<SootMethod, Map<Context, MethodOrMethodContext>> getContextMethodMap() {
        return contextMethodMap;
    }

    public Map<Context, Map<SparkField, ContextField>> getContextFieldVarNodeMap() {
        return contextFieldMap;
    }

    public ContextField makeContextField(Context context, FieldValNode fieldValNode) {
        SparkField field = fieldValNode.getField();
        Map<SparkField, ContextField> field2odotf = contextFieldMap.computeIfAbsent(context, k -> DataFactory.createMap());
        ContextField ret = field2odotf.get(field);
        if (ret == null) {
            field2odotf.put(field, ret = new ContextField(context, field));
            valNodeNumberer.add(ret);
        }
        return ret;
    }

    public Collection<VarNode> getVarNodes(Local local) {
        Map<?, ContextVarNode> subMap = contextVarNodeMap.get(findLocalVarNode(local));
        if (subMap == null) {
            return Collections.emptySet();
        }
        return new HashSet<>(subMap.values());
    }

    // ===================find nodes==============================

    /**
     * Finds the GlobalVarNode for the variable value, or returns null.
     */
    public GlobalVarNode findGlobalVarNode(Object value) {
        return (GlobalVarNode) findValNode(value);
    }

    /**
     * Finds the LocalVarNode for the variable value, or returns null.
     */
    public LocalVarNode findLocalVarNode(Object value) {
        ValNode ret = findValNode(value);
        if (ret instanceof LocalVarNode) {
            return (LocalVarNode) ret;
        }
        return null;
    }

    /**
     * Finds the ContextVarNode for base variable value and context context, or
     * returns null.
     */
    public ContextVarNode findContextVarNode(Local baseValue, Context context) {
        Map<Context, ContextVarNode> contextMap = contextVarNodeMap.get(findLocalVarNode(baseValue));
        return contextMap == null ? null : contextMap.get(context);
    }

    protected ReflectionModel createReflectionModel() {
        ReflectionModel model;
        if (CoreConfig.v().getAppConfig().REFLECTION_LOG != null && CoreConfig.v().getAppConfig().REFLECTION_LOG.length() > 0) {
            model = new TamiflexModel();
        } else {
            model = new NopReflectionModel();
        }
        return model;
    }

    public MethodPAG getMethodPAG(SootMethod m) {
        if (methodToPag.containsKey(m)) {
            return methodToPag.get(m);
        }
        Body body = PTAUtils.getMethodBody(m);
        synchronized (body) {
            // Some other thread may have created the MethodPAG for this method.
            if (methodToPag.containsKey(m)) {
                return methodToPag.get(m);
            }
            if (m.isConcrete()) {
                reflectionModel.buildReflection(m);
            }
            if (m.isNative()) {
                nativeDriver.buildNative(m);
            } else {
                // we will revert these back in the future.
                /*
                 * To keep same with Doop, we move the simulation of
                 * <java.lang.System: void arraycopy(java.lang.Object,int,java.lang.Object,int,int)>
                 * directly to its caller methods.
                 * */
                if (PTAScene.v().arraycopyBuilt.add(m)) {
                    handleArrayCopy(m);
                }
            }
        }
        return methodToPag.computeIfAbsent(m, k -> new MethodPAG(this, m, body));
    }

    private void handleArrayCopy(SootMethod method) {
        Map<Unit, Collection<Unit>> newUnits = DataFactory.createMap();
        Body body = PTAUtils.getMethodBody(method);
        for (Unit unit : body.getUnits()) {
            Stmt s = (Stmt) unit;
            if (s.containsInvokeExpr()) {
                InvokeExpr invokeExpr = s.getInvokeExpr();
                if (invokeExpr instanceof StaticInvokeExpr sie) {
                    SootMethod sm = sie.getMethod();
                    String sig = sm.getSignature();
                    if (sig.equals("<java.lang.System: void arraycopy(java.lang.Object,int,java.lang.Object,int,int)>")) {
                        Value srcArr = sie.getArg(0);
                        if (PTAUtils.isPrimitiveArrayType(srcArr.getType())) {
                            continue;
                        }
                        Type objType = RefType.v("java.lang.Object");
                        if (srcArr.getType() == objType) {
                            Local localSrc = new JimpleLocal("intermediate/" + body.getLocalCount(), ArrayType.v(objType, 1));
                            body.getLocals().add(localSrc);
                            newUnits.computeIfAbsent(unit, k -> new HashSet<>()).add(new JAssignStmt(localSrc, srcArr));
                            srcArr = localSrc;
                        }
                        Value dstArr = sie.getArg(2);
                        if (PTAUtils.isPrimitiveArrayType(dstArr.getType())) {
                            continue;
                        }
                        if (dstArr.getType() == objType) {
                            Local localDst = new JimpleLocal("intermediate/" + body.getLocalCount(), ArrayType.v(objType, 1));
                            body.getLocals().add(localDst);
                            newUnits.computeIfAbsent(unit, k -> new HashSet<>()).add(new JAssignStmt(localDst, dstArr));
                            dstArr = localDst;
                        }
                        Value src = new JArrayRef(srcArr, IntConstant.v(0));
                        Value dst = new JArrayRef(dstArr, IntConstant.v(0));
                        Local local = new JimpleLocal("nativeArrayCopy" + body.getLocalCount(), RefType.v("java.lang.Object"));
                        body.getLocals().add(local);
                        newUnits.computeIfAbsent(unit, k -> DataFactory.createSet()).add(new JAssignStmt(local, src));
                        newUnits.computeIfAbsent(unit, k -> DataFactory.createSet()).add(new JAssignStmt(dst, local));
                    }
                }
            }
        }
        for (Unit unit : newUnits.keySet()) {
            body.getUnits().insertAfter(newUnits.get(unit), unit);
        }
    }

    public LocalVarNode makeInvokeStmtThrowVarNode(Stmt invoke, SootMethod method) {
        return makeLocalVarNode(invoke, RefType.v("java.lang.Throwable"), method);
    }

    public HeapAbstractor heapAbstractor() {
        return pta.heapAbstractor();
    }


    public void resetPointsToSet() {
        this.addedContexts.clear();
        contextVarNodeMap.values().stream().flatMap(m -> m.values().stream()).forEach(ValNode::discardP2Set);
        contextFieldMap.values().stream().flatMap(m -> m.values().stream()).forEach(ValNode::discardP2Set);
        valToValNode.values().forEach(ValNode::discardP2Set);
        addedContexts.clear();
    }

}