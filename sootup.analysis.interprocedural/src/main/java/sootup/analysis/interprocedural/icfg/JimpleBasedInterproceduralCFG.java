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
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.callgraph.CallGraph;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.core.graph.StmtGraph;
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
      new CacheLoader<Stmt, Collection<SootMethod>>() {
        @Nonnull
        @Override
        public Collection<SootMethod> load(Stmt stmt) {
          ArrayList<SootMethod> res = new ArrayList<>();
          if (!stmt.isInvokableStmt() && !stmt.asInvokableStmt().containsInvokeExpr()) return res;
          MethodSignature methodSignature =
              stmt.asInvokableStmt().getInvokeExpr().get().getMethodSignature();
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
      new CacheLoader<SootMethod, Collection<Stmt>>() {
        @Nonnull
        @Override
        public Collection<Stmt> load(SootMethod method) {
          Set<CallGraph.Call> calls = cg.callsTo(method.getSignature());
          Set<Stmt> callerStmts =
              calls.stream().map(c -> c.getInvokableStmt()).collect(Collectors.toSet());
          return callerStmts;
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
    Map<MethodSignature, StmtGraph<?>> signatureToStmtGraph = new LinkedHashMap<>();
    computeAllCalls(callGraph.getEntryMethods(), signatureToStmtGraph, callGraph);
    return ICFGDotExporter.buildICFGGraph(signatureToStmtGraph, view, callGraph);
  }

  public void computeAllCalls(
      List<MethodSignature> entryPoints,
      Map<MethodSignature, StmtGraph<?>> signatureToStmtGraph,
      CallGraph callGraph) {
    ArrayList<MethodSignature> visitedMethods = new ArrayList<>();
    computeAllCalls(entryPoints, signatureToStmtGraph, callGraph, visitedMethods);
  }

  private void computeAllCalls(
      List<MethodSignature> entryPoints,
      Map<MethodSignature, StmtGraph<?>> signatureToStmtGraph,
      CallGraph callGraph,
      List<MethodSignature> visitedMethods) {
    visitedMethods.addAll(entryPoints);
    for (MethodSignature methodSignature : entryPoints) {
      final Optional<? extends SootMethod> methodOpt = view.getMethod(methodSignature);
      // return if the methodSignature is already added to the hashMap to avoid stackoverflow error.
      if (signatureToStmtGraph.containsKey(methodSignature)) {
        return;
      }
      if (methodOpt.isPresent()) {
        SootMethod sootMethod = methodOpt.get();
        if (sootMethod.hasBody()) {
          StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();
          signatureToStmtGraph.put(methodSignature, stmtGraph);
        }
      }
      callGraph.callTargetsFrom(methodSignature).stream()
          .filter(methodSignature1 -> !visitedMethods.contains(methodSignature1))
          .forEach(
              nextMethodSignature ->
                  computeAllCalls(
                      Collections.singletonList(nextMethodSignature),
                      signatureToStmtGraph,
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
  public Collection<SootMethod> getCalleesOfCallAt(@Nonnull Stmt u) {
    return stmtToCallees.getUnchecked(u);
  }

  @Override
  public Collection<Stmt> getCallersOf(@Nonnull SootMethod m) {
    return methodToCallers.getUnchecked(m);
  }
}
