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

package qilin.pta.tools;

import qilin.core.pag.*;
import qilin.core.sets.PointsToSet;
import qilin.core.solver.Propagator;
import qilin.parm.select.CtxSelector;
import qilin.parm.select.DebloatingSelector;
import qilin.parm.select.PipelineSelector;
import qilin.pta.PTAConfig;
import qilin.pta.toolkits.common.DebloatedOAG;
import qilin.pta.toolkits.common.OAG;
import qilin.pta.toolkits.debloaterx.CollectionHeuristic;
import qilin.pta.toolkits.debloaterx.DebloaterX;
import qilin.pta.toolkits.conch.Conch;
import qilin.stat.IEvaluator;
import qilin.util.Stopwatch;
import soot.*;

import java.util.*;

/*
 * refer to "Context Debloating for Object-Sensitive Pointer Analysis" (ASE'21)
 * */
public class DebloatedPTA extends StagedPTA {
    public enum DebloatApproach {
        CONCH, DEBLOATERX, COLLECTION
    }

    protected BasePTA basePTA;
    protected Set<Object> ctxDepHeaps = new HashSet<>();
    protected DebloatApproach debloatApproach = DebloatApproach.CONCH;

    /*
     * The debloating approach is currently for object-sensitive PTA only.
     * Thus the base PTA should be k-OBJ, Zipper-OBJ or Eagle-OBJ.
     * */
    public DebloatedPTA(BasePTA basePTA) {
        this.basePTA = basePTA;
        CtxSelector debloatingSelector = new DebloatingSelector(ctxDepHeaps);
        basePTA.setContextSelector(new PipelineSelector(basePTA.ctxSelector(), debloatingSelector));
        if (basePTA instanceof StagedPTA stagedPTA) {
            this.prePTA = stagedPTA.getPrePTA();
        } else {
            this.prePTA = new Spark();
        }
        System.out.println("debloating ....");
    }

    /* this constructor is used to specify the debloating approach. */
    public DebloatedPTA(BasePTA basePTA, DebloatApproach approach) {
        this(basePTA);
        this.debloatApproach = approach;
    }

    @Override
    protected void preAnalysis() {
        Stopwatch sparkTimer = Stopwatch.newAndStart("Spark");
        prePTA.pureRun();
        sparkTimer.stop();
        System.out.println(sparkTimer);
        if (debloatApproach == DebloatApproach.CONCH) {
            Stopwatch conchTimer = Stopwatch.newAndStart("Conch");
            Conch hc = new Conch(prePTA);
            hc.runClassifier();
            this.ctxDepHeaps.addAll(hc.ctxDependentHeaps());
            System.out.println();
            conchTimer.stop();
            System.out.println(conchTimer);
        } else if (debloatApproach == DebloatApproach.DEBLOATERX) {
            Stopwatch debloaterXTimer = Stopwatch.newAndStart("DebloaterX");
            DebloaterX debloaterX = new DebloaterX(prePTA);
            debloaterX.run();
            Set<AllocNode> mCtxDepHeaps = debloaterX.getCtxDepHeaps();
            for (AllocNode obj : mCtxDepHeaps) {
                this.ctxDepHeaps.add(obj.getNewExpr());
            }
            System.out.println();
            debloaterXTimer.stop();
            System.out.println(debloaterXTimer);
            // stat OAG reductions
            OAG oag = new OAG(prePTA);
            oag.build();
            OAG doag1 = new DebloatedOAG(prePTA, mCtxDepHeaps);
            doag1.build();
            System.out.println("OAG #node:" + oag.nodeSize() + "; #edge:" + oag.edgeSize());
            System.out.println("DebloaterX OAG #node:" + doag1.nodeSize() + "; #edge:" + doag1.edgeSize());
        } else {
            assert (debloatApproach == DebloatApproach.COLLECTION);
            Stopwatch collectionHeuristic = Stopwatch.newAndStart("COLLECTION");
            CollectionHeuristic ch = new CollectionHeuristic(prePTA);
            ch.run();
            collectionHeuristic.stop();
            System.out.println(collectionHeuristic);
            for (AllocNode obj : ch.getCtxDepHeaps()) {
                this.ctxDepHeaps.add(obj.getNewExpr());
            }
        }
    }

    @Override
    protected void mainAnalysis() {
        if (!PTAConfig.v().getPtaConfig().preAnalysisOnly) {
            System.out.println("selective pta starts!");
            basePTA.run();
        }
    }

    @Override
    public Propagator getPropagator() {
        return basePTA.getPropagator();
    }

    @Override
    public Node parameterize(Node n, Context context) {
        return basePTA.parameterize(n, context);
    }

    @Override
    public MethodOrMethodContext parameterize(SootMethod method, Context context) {
        return basePTA.parameterize(method, context);
    }

    @Override
    public AllocNode getRootNode() {
        return basePTA.getRootNode();
    }

    @Override
    public IEvaluator evaluator() {
        return basePTA.evaluator();
    }

    @Override
    public Context emptyContext() {
        return basePTA.emptyContext();
    }

    @Override
    public Context createCalleeCtx(MethodOrMethodContext caller, AllocNode receiverNode, CallSite callSite, SootMethod target) {
        return basePTA.createCalleeCtx(caller, receiverNode, callSite, target);
    }

    @Override
    public PointsToSet reachingObjects(Local l) {
        return basePTA.reachingObjects(l);
    }

    @Override
    public PointsToSet reachingObjects(Node n) {
        return basePTA.reachingObjects(n);
    }

    @Override
    public PointsToSet reachingObjects(Context c, Local l) {
        return basePTA.reachingObjects(c, l);
    }

    @Override
    public PointsToSet reachingObjects(SootField f) {
        return basePTA.reachingObjects(f);
    }

    @Override
    public PointsToSet reachingObjects(PointsToSet s, SootField f) {
        return basePTA.reachingObjects(s, f);
    }

    @Override
    public PointsToSet reachingObjects(Local l, SootField f) {
        return basePTA.reachingObjects(l, f);
    }

    @Override
    public PointsToSet reachingObjects(Context c, Local l, SootField f) {
        return basePTA.reachingObjects(c, l, f);
    }

    @Override
    public PointsToSet reachingObjectsOfArrayElement(PointsToSet s) {
        return basePTA.reachingObjectsOfArrayElement(s);
    }
}
