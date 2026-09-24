package sootup.analysis.interprocedural.icfg;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2022 Kadiray Karakaya and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import heros.DontSynchronize;
import heros.InterproceduralCFG;
import heros.SynchronizedBy;
import heros.ThreadSafe;
import heros.solver.IDESolver;
import java.util.*;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraph.Call;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.views.View;

/**
 * Default implementation for the {@link InterproceduralCFG} interface. Includes all statements
 * reachable from entrypoints through explicit call statements or through calls to {@link
 * Thread#start()}.
 *
 * <p>This class is designed to be thread safe, and subclasses of this class must be designed in a
 * thread-safe way, too.
 */
@ThreadSafe
public class JimpleBasedInterproceduralCFG extends AbstractJimpleBasedICFG {

  protected static final Logger logger =
      LoggerFactory.getLogger(JimpleBasedInterproceduralCFG.class);

  protected boolean includeReflectiveCalls;

  @DontSynchronize("readonly")
  protected final CallGraph cg;

  protected CacheLoader<Stmt, Collection<SootMethod>> loaderUnitToCallees =
      new CacheLoader<>() {
        @NonNull
        @Override
        public Collection<SootMethod> load(Stmt stmt) {
          ArrayList<SootMethod> res = new ArrayList<>();
          if (!stmt.isInvokableStmt() && stmt.asInvokableStmt().getInvokeExpr().isEmpty()) {
            return res;
          }
          MethodSignature methodSignature =
              stmt.asInvokableStmt().getInvokeExpr().get().getMethodSignature();

          if (!cg.containsMethod(methodSignature)) {
            logger.warn(
                "Method {} is not reachable in the Call Graph!", methodSignature, new Exception());
            return res;
          }
          Optional<? extends SootMethod> smOpt = view.getMethod(methodSignature);
          if (smOpt.isPresent()) {
            SootMethod sm = smOpt.get();
            if (sm.hasBody()) {
              res.add(sm);
            } else {
              logger.error(
                  "Method {} is referenced but has no body!", sm.getSignature(), new Exception());
            }
          }
          res.trimToSize();
          return res;
        }
      };

  @SynchronizedBy("by use of synchronized LoadingCache class")
  protected final LoadingCache<Stmt, Collection<SootMethod>> stmtToCallees =
      IDESolver.DEFAULT_CACHE_BUILDER.build(loaderUnitToCallees);

  protected CacheLoader<SootMethod, Collection<Stmt>> loaderMethodToCallers =
      new CacheLoader<>() {
        @NonNull
        @Override
        public Collection<Stmt> load(SootMethod method) {
          Set<CallGraph.Call> calls = cg.callsTo(method.getSignature());
          return calls.stream().map(Call::invokableStmt).collect(Collectors.toSet());
        }
      };

  @SynchronizedBy("by use of synchronized LoadingCache class")
  protected final LoadingCache<SootMethod, Collection<Stmt>> methodToCallers =
      IDESolver.DEFAULT_CACHE_BUILDER.build(loaderMethodToCallers);

  public JimpleBasedInterproceduralCFG(
      View view,
      List<MethodSignature> cgEntryPoints,
      boolean enableExceptions,
      boolean includeReflectiveCalls) {
    this(
        new ClassHierarchyAnalysisAlgorithm(view).initialize(cgEntryPoints),
        view,
        enableExceptions,
        includeReflectiveCalls);
  }

  public JimpleBasedInterproceduralCFG(
      CallGraph cg, View view, boolean enableExceptions, boolean includeReflectiveCalls) {
    super(enableExceptions);
    this.includeReflectiveCalls = includeReflectiveCalls;
    this.view = view;
    this.cg = cg;
    initializeStmtToOwner();
  }

  public CallGraph getCg() {
    return cg;
  }

  public String buildICFGGraph(CallGraph callGraph) {
    Map<MethodSignature, ControlFlowGraph<?>> signatureToControlFlowGraph = new LinkedHashMap<>();
    computeAllCalls(callGraph.getEntryMethods(), signatureToControlFlowGraph, callGraph);
    return ICFGDotExporter.buildICFGGraph(signatureToControlFlowGraph, view, callGraph);
  }

  public void computeAllCalls(
      List<MethodSignature> entryPoints,
      Map<MethodSignature, ControlFlowGraph<?>> signatureToControlFlowGraph,
      CallGraph callGraph) {
    ArrayList<MethodSignature> visitedMethods = new ArrayList<>();
    computeAllCalls(entryPoints, signatureToControlFlowGraph, callGraph, visitedMethods);
  }

  private void computeAllCalls(
      List<MethodSignature> entryPoints,
      Map<MethodSignature, ControlFlowGraph<?>> signatureToControlFlowGraph,
      CallGraph callGraph,
      List<MethodSignature> visitedMethods) {
    visitedMethods.addAll(entryPoints);
    for (MethodSignature methodSignature : entryPoints) {
      final Optional<? extends SootMethod> methodOpt = view.getMethod(methodSignature);
      // return if the methodSignature is already added to the hashMap to avoid stackoverflow error.
      if (signatureToControlFlowGraph.containsKey(methodSignature)) {
        return;
      }
      if (methodOpt.isPresent()) {
        SootMethod sootMethod = methodOpt.get();
        if (sootMethod.hasBody()) {
          ControlFlowGraph<?> controlFlowGraph = sootMethod.getBody().getControlFlowGraph();
          signatureToControlFlowGraph.put(methodSignature, controlFlowGraph);
        }
      }
      callGraph.callTargetsFrom(methodSignature).stream()
          .filter(methodSignature1 -> !visitedMethods.contains(methodSignature1))
          .forEach(
              nextMethodSignature ->
                  computeAllCalls(
                      Collections.singletonList(nextMethodSignature),
                      signatureToControlFlowGraph,
                      callGraph,
                      visitedMethods));
    }
  }

  protected void initializeStmtToOwner() {
    for (MethodSignature methodSignature : cg.getMethodSignatures()) {
      final Optional<? extends SootMethod> methodOpt = view.getMethod(methodSignature);
      methodOpt.ifPresent(this::initializeStmtToOwner);
    }
  }

  @Override
  public Collection<SootMethod> getCalleesOfCallAt(@NonNull Stmt u) {
    return stmtToCallees.getUnchecked(u);
  }

  @Override
  public Collection<Stmt> getCallersOf(@NonNull SootMethod m) {
    return methodToCallers.getUnchecked(m);
  }
}
