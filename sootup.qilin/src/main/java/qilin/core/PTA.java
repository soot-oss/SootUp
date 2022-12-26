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

package qilin.core;

import qilin.core.builder.CallGraphBuilder;
import qilin.core.builder.ExceptionHandler;
import qilin.core.pag.*;
import qilin.core.sets.*;
import qilin.core.sets.PointsToSet;
import qilin.core.solver.Propagator;
import qilin.parm.ctxcons.CtxConstructor;
import qilin.parm.heapabst.HeapAbstractor;
import qilin.parm.select.CtxSelector;
import soot.*;
import soot.jimple.spark.pag.SparkField;
import soot.jimple.toolkits.callgraph.CallGraph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class PTA implements PointsToAnalysis {

    protected AllocNode rootNode;
    protected PAG pag;
    protected CallGraph callGraph;
    protected CallGraphBuilder cgb;
    protected ExceptionHandler eh;

    public PTA() {
        this.pag = createPAG();
        this.cgb = createCallGraphBuilder();
        this.eh = new ExceptionHandler(this);
        AllocNode rootBase = pag.makeAllocNode("ROOT", RefType.v("java.lang.Object"), null);
        this.rootNode = new ContextAllocNode(rootBase, CtxConstructor.emptyContext);
    }

    protected abstract PAG createPAG();

    protected abstract CallGraphBuilder createCallGraphBuilder();

    public void run() {
        pureRun();
    }

    public void pureRun() {
        getPropagator().propagate();
    }

    public PAG getPag() {
        return pag;
    }

    public CallGraphBuilder getCgb() {
        return cgb;
    }

    public ExceptionHandler getExceptionHandler() {
        return eh;
    }

    public CallGraph getCallGraph() {
        if (callGraph == null) {
            callGraph = cgb.getCICallGraph();
        }
        return callGraph;
    }

    public Collection<MethodOrMethodContext> getReachableMethods() {
        return cgb.getReachableMethods();
    }

    private Set<SootMethod> nakedReachables = null;

    public Collection<SootMethod> getNakedReachableMethods() {
        if (nakedReachables == null) {
            nakedReachables = new HashSet<>();
            cgb.getReachableMethods().forEach(momc -> nakedReachables.add(momc.method()));
        }
        return nakedReachables;
    }

    protected abstract Propagator getPropagator();

    public abstract Node parameterize(Node n, Context context);

    public abstract MethodOrMethodContext parameterize(SootMethod method, Context context);

    public abstract AllocNode getRootNode();

    public abstract Context emptyContext();

    public abstract Context createCalleeCtx(MethodOrMethodContext caller, AllocNode receiverNode, CallSite callSite, SootMethod target);

    public abstract HeapAbstractor heapAbstractor();

    public abstract CtxConstructor ctxConstructor();

    public abstract CtxSelector ctxSelector();

    /**
     * Returns the set of objects pointed to by variable l.
     */
    @Override
    public PointsToSet reachingObjects(Local l) {
        // find all context nodes, and collect their answers
        final PointsToSetInternal ret = new HybridPointsToSet();
        pag.getVarNodes(l).forEach(vn -> {
            ret.addAll(vn.getP2Set(), null);
        });
        return new UnmodifiablePointsToSet(this, ret);
    }

    /**
     * Returns the set of objects pointed by n:
     * case 1: n is an insensitive node, return objects pointed by n under every possible context.
     * case 2: n is a context-sensitive node, return objects pointed by n under the given context.
     */
    public PointsToSet reachingObjects(Node n) {
        final PointsToSetInternal ret;
        if (n instanceof ContextVarNode cvn) {
            ret = cvn.getP2Set();
        } else if (n instanceof ContextField cf) {
            ret = cf.getP2Set();
        } else {
            VarNode varNode = (VarNode) n;
            ret = new HybridPointsToSet();
            if (pag.getContextVarNodeMap().containsKey(varNode)) {
                pag.getContextVarNodeMap().get(varNode).values().forEach(vn -> {
                    ret.addAll(vn.getP2Set(), null);
                });
            }
        }
        return new UnmodifiablePointsToSet(this, ret);
    }

    /**
     * Returns the set of objects pointed to by elements of the arrays in the
     * PointsToSet s.
     */
    @Override
    public PointsToSet reachingObjectsOfArrayElement(PointsToSet s) {
        return reachingObjectsInternal(s, ArrayElement.v());
    }

    /**
     * Returns the set of objects pointed to by variable l in context c.
     */
    @Override
    public PointsToSet reachingObjects(Context c, Local l) {
        VarNode n = pag.findContextVarNode(l, c);
        PointsToSetInternal pts;
        if (n == null) {
            pts = HybridPointsToSet.getEmptySet();
        } else {
            pts = n.getP2Set();
        }
        return new UnmodifiablePointsToSet(this, pts);
    }

    /**
     * Returns the set of objects pointed to by instance field f of the objects
     * pointed to by l.
     */
    @Override
    public PointsToSet reachingObjects(Local l, SootField f) {
        return reachingObjects(reachingObjects(l), f);
    }

    /**
     * Returns the set of objects pointed to by instance field f of the objects in
     * the PointsToSet s.
     */
    @Override
    public PointsToSet reachingObjects(PointsToSet s, final SootField f) {
        if (f.isStatic()) {
            throw new RuntimeException("The parameter f must be an *instance* field.");
        }
        return reachingObjectsInternal(s, new Field(f));
    }

    /**
     * Returns the set of objects pointed to by instance field f of the objects
     * pointed to by l in context c.
     */
    @Override
    public PointsToSet reachingObjects(Context c, Local l, SootField f) {
        return reachingObjects(reachingObjects(c, l), f);
    }

    @Override
    public PointsToSet reachingObjects(SootField f) {
        PointsToSetInternal ret;
        if (f.isStatic()) {
            VarNode n = pag.findGlobalVarNode(f);
            if (n == null) {
                ret = HybridPointsToSet.getEmptySet();
            } else {
                ret = n.getP2Set();
            }
        } else {
            ret = new HybridPointsToSet();
            SparkField sparkField = new Field(f);
            pag.getContextFieldVarNodeMap().values().stream().filter(map -> map.containsKey(sparkField)).forEach(map -> {
                ContextField contextField = map.get(sparkField);
                ret.addAll(contextField.getP2Set(), null);
            });
        }
        return new UnmodifiablePointsToSet(this, ret);
    }

    public PointsToSet reachingObjectsInternal(PointsToSet bases, final SparkField f) {
        final PointsToSetInternal ret = new HybridPointsToSet();
        pag.getContextFieldVarNodeMap().values().stream().filter(map -> map.containsKey(f)).forEach(map -> {
            ContextField contextField = map.get(f);
            AllocNode base = contextField.getBase();
            if (bases.contains(base)) {
                ret.addAll(contextField.getP2Set(), null);
            }
        });
        return new UnmodifiablePointsToSet(this, ret);
    }

    public PointsToSet reachingObjectsInternal(AllocNode heap, final SparkField f) {
        final PointsToSetInternal ret = new HybridPointsToSet();
        pag.getContextFieldVarNodeMap().values().stream().filter(map -> map.containsKey(f)).forEach(map -> {
            ContextField contextField = map.get(f);
            AllocNode base = contextField.getBase();
            if (heap.equals(base)) {
                ret.addAll(contextField.getP2Set(), null);
            }
        });
        return new UnmodifiablePointsToSet(this, ret);
    }

    @Override
    public boolean mayAlias(Local l1, Local l2) {
        PointsToSet pts1 = reachingObjects(l1).toCIPointsToSet();
        PointsToSet pts2 = reachingObjects(l2).toCIPointsToSet();
        return pts1.hasNonEmptyIntersection(pts2);
    }
}
