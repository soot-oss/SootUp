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

import com.google.common.collect.Streams;
import heros.solver.CountingThreadPoolExecutor;
import qilin.core.PTA;
import qilin.core.PointsToAnalysis;
import qilin.core.builder.MethodNodeFactory;
import qilin.core.pag.*;
import soot.RefLikeType;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.util.queue.QueueReader;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/*
 * The abstract pointer assignment graph constructed by rules in Fig. 10 and Fig. 11 in our paper.
 * our IFDS analysis is running on PAG rather than the conventional control flow graph.
 * */
public abstract class AbstractPAG {
    /*
     * A symbolic object is introduced to abstract all the possible objects returned from a callsite.
     * */
    protected final Map<SootMethod, Map<Stmt, AllocNode>> symbolicHeaps = new ConcurrentHashMap<>();
    protected final Map<Node, Set<TranEdge>> outEdges = new ConcurrentHashMap<>();
    protected final Map<Node, Set<TranEdge>> sumEdges = new ConcurrentHashMap<>();

    protected CountingThreadPoolExecutor executor;

    protected final PTA prePTA;
    protected final PAG prePAG;
    protected final CallGraph callGraph;

    protected AbstractPAG(PTA prePTA) {
        this.prePTA = prePTA;
        this.prePAG = prePTA.getPag();
        this.callGraph = prePTA.getCallGraph();
        int threadNum = Runtime.getRuntime().availableProcessors();
        this.executor = new CountingThreadPoolExecutor(threadNum, Integer.MAX_VALUE, 30,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    protected void build() {
        prePTA.getNakedReachableMethods()
                .parallelStream().filter(m -> !m.isPhantom())
                .forEach(this::buildFG);
    }

    private void buildFG(SootMethod method) {
        MethodPAG srcmpag = prePAG.getMethodPAG(method);
        MethodNodeFactory srcnf = srcmpag.nodeFactory();
        LocalVarNode thisRef = (LocalVarNode) srcnf.caseThis();
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

        // add param and return edges
        addParamEdge(thisRef);

        int numParms = method.getParameterCount();
        for (int i = 0; i < numParms; i++) {
            if (method.getParameterType(i) instanceof RefLikeType) {
                LocalVarNode param = (LocalVarNode) srcnf.caseParm(i);
                addParamEdge(param);
            }
        }
        if (method.getReturnType() instanceof RefLikeType) {
            LocalVarNode mret = (LocalVarNode) srcnf.caseRet();
            addReturnEdge(mret);
        }
        Node throwNode = prePAG.findLocalVarNode(new Parm(method, PointsToAnalysis.THROW_NODE));
        if (throwNode != null) {
            addThrowEdge(throwNode);
        }
    }

    protected void addNormalEdge(TranEdge edge) {
        outEdges.computeIfAbsent(edge.getSource(), k -> ConcurrentHashMap.newKeySet()).add(edge);
    }

    protected void addThrowEdge(Node throwNode) {
        addNormalEdge(new TranEdge(throwNode, throwNode, DFA.TranCond.THROW));
        addNormalEdge(new TranEdge(throwNode, throwNode, DFA.TranCond.I_THROW));
    }

    protected void addParamEdge(LocalVarNode param) {
        addNormalEdge(new TranEdge(param, param, DFA.TranCond.PARAM));
        addNormalEdge(new TranEdge(param, param, DFA.TranCond.I_PARAM));
    }

    protected void addReturnEdge(LocalVarNode mret) {
        addNormalEdge(new TranEdge(mret, mret, DFA.TranCond.RETURN));
        addNormalEdge(new TranEdge(mret, mret, DFA.TranCond.I_RETURN));
    }

    protected void addNewEdge(AllocNode from, LocalVarNode to) {
        // skip merged heaps.
        if (from.getMethod() == null && !(from instanceof ConstantNode)) {
            return;
        }
        TranEdge newEdge = new TranEdge(from, to, DFA.TranCond.NEW);
        addNormalEdge(newEdge);
        TranEdge newInvEdge = new TranEdge(to, from, DFA.TranCond.I_NEW);
        addNormalEdge(newInvEdge);
    }

    protected void addAssignEdge(LocalVarNode from, LocalVarNode to) {
        TranEdge assignEdge = new TranEdge(from, to, DFA.TranCond.ASSIGN);
        addNormalEdge(assignEdge);
        TranEdge assignInvEdge = new TranEdge(to, from, DFA.TranCond.I_ASSIGN);
        addNormalEdge(assignInvEdge);
    }

    protected void addStoreEdge(LocalVarNode from, LocalVarNode base) {
        TranEdge storeEdge = new TranEdge(from, base, DFA.TranCond.STORE);
        addNormalEdge(storeEdge);
        TranEdge storeInvEdge = new TranEdge(base, from, DFA.TranCond.I_STORE);
        addNormalEdge(storeInvEdge);
    }

    protected void addLoadEdge(LocalVarNode base, LocalVarNode to) {
        TranEdge loadEdge = new TranEdge(base, to, DFA.TranCond.LOAD);
        addNormalEdge(loadEdge);
        TranEdge loadInvEdge = new TranEdge(to, base, DFA.TranCond.I_LOAD);
        addNormalEdge(loadInvEdge);
    }

    /*
     * The following code forms the bases of a multi-threaded IFDS solver.
     * The codes is modified from the implementation of the fast IFDS solver in FlowDroid.
     */
    protected void solve() {
        submitInitialSeeds();
        awaitCompletionComputeValuesAndShutdown();
    }

    protected Collection<TranEdge> outAndSummaryEdges(Node node) {
        return Streams.concat(outEdges.getOrDefault(node, Collections.emptySet()).stream(),
                sumEdges.getOrDefault(node, Collections.emptySet()).stream()).collect(Collectors.toSet());
    }

    protected abstract void submitInitialSeeds();

    protected void awaitCompletionComputeValuesAndShutdown() {
        {
            // run executor and await termination of tasks
            runExecutorAndAwaitCompletion();
        }
        // ask executor to shut down;
        // this will cause new submissions to the executor to be rejected,
        // but at this point all tasks should have completed anyway
        executor.shutdown();

        // Wait for the executor to be really gone
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // silently ignore the exception, it's not an issue if the
                // thread gets aborted
            }
        }
        executor = null;
    }

    private void runExecutorAndAwaitCompletion() {
        try {
            executor.awaitCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Throwable exception = executor.getException();
        if (exception != null) {
            throw new RuntimeException("There were exceptions during IFDS analysis. Exiting.", exception);
        }
    }

    protected AllocNode getSymbolicHeapOf(SootMethod method, Stmt invokeStmt) {
        return symbolicHeaps.computeIfAbsent(method, k -> new ConcurrentHashMap<>()).computeIfAbsent(invokeStmt, k -> new AllocNode(invokeStmt, null, method));
    }
}
