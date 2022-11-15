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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphAlgorithm;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.views.View;
import sootup.java.core.views.JavaView;

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
  private MethodSignature mainMethodSignature;

  protected boolean includeReflectiveCalls = false;
  protected boolean includePhantomCallees = false;

  @DontSynchronize("readonly")
  protected final CallGraph cg;

  protected CacheLoader<Stmt, Collection<SootMethod>> loaderUnitToCallees =
      new CacheLoader<Stmt, Collection<SootMethod>>() {
        @Override
        public Collection<SootMethod> load(Stmt stmt) throws Exception {
          ArrayList<SootMethod> res = new ArrayList<>();
          MethodSignature methodSignature = stmt.getInvokeExpr().getMethodSignature();
          SootMethod sm = (SootMethod) view.getMethod(methodSignature).orElse(null);
          if (sm != null) {
            if (includePhantomCallees || sm.hasBody()) {
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
        @Override
        public Collection<Stmt> load(SootMethod m) throws Exception {
          ArrayList<Stmt> res = new ArrayList<>();
          // only retain callers that are explicit call sites or
          // Thread.start()
          Set<MethodSignature> callsToMethod = cg.callsTo(m.getSignature());
          for (MethodSignature ms : callsToMethod) {
            Stmt stmt = filterEdgeAndGetCallerStmt(ms);
            if (stmt != null) {
              res.add(stmt);
            }
          }
          res.trimToSize();
          return res;
        }

        /**
         * returns SootMethod if accepted, null otherwise
         *
         * @param methodSignature
         */
        private Stmt filterEdgeAndGetCallerStmt(MethodSignature methodSignature) {
          Set<Pair<MethodSignature, CalleeMethodSignature>> callEdges =
              CGEdgeUtil.getCallEdges(view, cg);
          for (Pair<MethodSignature, CalleeMethodSignature> callEdge : callEdges) {
            CalleeMethodSignature callee = callEdge.getValue();
            if (callee.getMethodSignature().equals(methodSignature)) {
              CGEdgeUtil.CallGraphEdgeType edgeType = callee.getEdgeType();
              if (edgeType.isExplicit()
                  || edgeType.isFake()
                  || edgeType.isClinit()
                  || (includeReflectiveCalls && edgeType.isReflection())) {
                return callee.getSourceStmt();
              }
            }
          }
          return null;
        }
      };

  @SynchronizedBy("by use of synchronized LoadingCache class")
  protected final LoadingCache<SootMethod, Collection<Stmt>> methodToCallers =
      IDESolver.DEFAULT_CACHE_BUILDER.build(loaderMethodToCallers);

  public JimpleBasedInterproceduralCFG(
      JavaView view,
      MethodSignature mainMethodSignature,
      boolean enableExceptions,
      boolean includeReflectiveCalls) {
    super(enableExceptions);
    this.includeReflectiveCalls = includeReflectiveCalls;
    this.view = view;
    this.mainMethodSignature = mainMethodSignature;
    cg = initCallGraph();
    initializeStmtToOwner();
  }

  private CallGraph initCallGraph() {
    final ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);
    CallGraphAlgorithm cga = new ClassHierarchyAnalysisAlgorithm(view, typeHierarchy);
    return cga.initialize(Collections.singletonList(mainMethodSignature));
  }

  protected void initializeStmtToOwner() {
    for (MethodSignature methodSignature : cg.getMethodSignatures()) {
      SootMethod sootMethod = (SootMethod) view.getMethod(methodSignature).orElse(null);
      initializeStmtToOwner(sootMethod);
    }
  }

  @Override
  public Collection<SootMethod> getCalleesOfCallAt(Stmt u) {
    return stmtToCallees.getUnchecked(u);
  }

  @Override
  public Collection<Stmt> getCallersOf(SootMethod m) {
    return methodToCallers.getUnchecked(m);
  }

  /**
   * Sets whether methods that operate on the callgraph shall also return phantom methods as
   * potential callees
   *
   * @param includePhantomCallees True if phantom methods shall be returned as potential callees,
   *     otherwise false
   */
  public void setIncludePhantomCallees(boolean includePhantomCallees) {
    this.includePhantomCallees = includePhantomCallees;
  }

  public static Set<Pair<MethodSignature, CalleeMethodSignature>> getCallEdges(
      View<? extends SootClass> view, CallGraph cg) {
    Set<MethodSignature> methodSigs = cg.getMethodSignatures();
    Set<Pair<MethodSignature, CalleeMethodSignature>> callEdges = new HashSet<>();
    for (MethodSignature caller : methodSigs) {
      SootMethod method = view.getMethod(caller).orElse(null);
      if (method != null && method.hasBody()) {
        for (Stmt s : method.getBody().getStmtGraph().nodes()) {
          if (s.containsInvokeExpr()) {
            CalleeMethodSignature callee =
                new CalleeMethodSignature(
                    s.getInvokeExpr().getMethodSignature(),
                    CGEdgeUtil.findCallGraphEdgeType(s.getInvokeExpr()),
                    s);
            callEdges.add(new ImmutablePair<>(caller, callee));
          }
        }
      }
    }
    return callEdges;
  }
}
