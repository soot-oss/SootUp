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
import heros.SynchronizedBy;
import heros.solver.IDESolver;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.views.View;

public abstract class AbstractJimpleBasedICFG implements BiDiInterproceduralCFG<Stmt, SootMethod> {

  protected final boolean enableExceptions;

  protected View view;

  @DontSynchronize("written by single thread; read afterwards")
  private final Map<Stmt, Body> stmtToOwner = createStmtToOwnerMap();

  @SynchronizedBy("by use of synchronized LoadingCache class")
  protected LoadingCache<Body, StmtGraph<?>> bodyToStmtGraph =
      IDESolver.DEFAULT_CACHE_BUILDER.build(
          new CacheLoader<Body, StmtGraph<?>>() {
            @Nonnull
            @Override
            public StmtGraph<?> load(@Nonnull Body body) {
              return makeGraph(body);
            }
          });

  @SynchronizedBy("by use of synchronized LoadingCache class")
  protected LoadingCache<SootMethod, List<Value>> methodToParameterRefs =
      IDESolver.DEFAULT_CACHE_BUILDER.build(
          new CacheLoader<SootMethod, List<Value>>() {
            @Nonnull
            @Override
            public List<Value> load(@Nonnull SootMethod m) {
              return new ArrayList<>(m.getBody().getParameterLocals());
            }
          });

  @SynchronizedBy("by use of synchronized LoadingCache class")
  protected LoadingCache<SootMethod, Set<Stmt>> methodToCallsFromWithin =
      IDESolver.DEFAULT_CACHE_BUILDER.build(
          new CacheLoader<SootMethod, Set<Stmt>>() {
            @Nonnull
            @Override
            public Set<Stmt> load(@Nonnull SootMethod m) {
              return getCallsFromWithinMethod(m);
            }
          });

  protected AbstractJimpleBasedICFG() {
    this(true);
  }

  protected Map<Stmt, Body> createStmtToOwnerMap() {
    return new IdentityHashMap<>();
  }

  protected AbstractJimpleBasedICFG(boolean enableExceptions) {
    this.enableExceptions = enableExceptions;
  }

  public Body getBodyOf(Stmt stmt) {
    Body body = stmtToOwner.get(stmt);
    assert body != null : "Statement " + stmt + " not in Stmt-to-owner mapping";
    return body;
  }

  @Override
  public SootMethod getMethodOf(Stmt stmt) {
    Body b = getBodyOf(stmt);
    if (b == null) {
      return null;
    }
    return view.getMethod(b.getMethodSignature()).orElse(null);
  }

  @Override
  public List<Stmt> getSuccsOf(Stmt stmt) {
    Body body = getBodyOf(stmt);
    if (body == null) {
      return Collections.emptyList();
    }
    StmtGraph<?> stmtGraph = getOrCreateStmtGraph(body);
    return stmtGraph.successors(stmt);
  }

  @Override
  public StmtGraph<?> getOrCreateStmtGraph(SootMethod method) {
    return getOrCreateStmtGraph(method.getBody());
  }

  public StmtGraph<?> getOrCreateStmtGraph(Body body) {
    return bodyToStmtGraph.getUnchecked(body);
  }

  protected StmtGraph<?> makeGraph(Body body) {
    return body.getStmtGraph();
  }

  protected Set<Stmt> getCallsFromWithinMethod(SootMethod method) {
    return method.getBody().getStmts().stream()
        .filter(this::isCallStmt)
        .collect(Collectors.toSet());
  }

  @Override
  public boolean isExitStmt(Stmt stmt) {
    Body body = getBodyOf(stmt);
    StmtGraph<?> stmtGraph = getOrCreateStmtGraph(body);
    return stmtGraph.getTails().contains(stmt);
  }

  @Override
  public boolean isStartPoint(Stmt stmt) {
    Body body = getBodyOf(stmt);
    StmtGraph<?> stmtGraph = getOrCreateStmtGraph(body);
    return stmtGraph.getEntrypoints().contains(stmt);
  }

  @Override
  public boolean isFallThroughSuccessor(Stmt stmt, Stmt successorCandidate) {
    assert getSuccsOf(stmt).contains(successorCandidate);
    if (!stmt.fallsThrough()) {
      return false;
    }
    Body body = getBodyOf(stmt);
    return body.getStmtGraph().successors(stmt).get(0) == successorCandidate;
  }

  @Override
  public boolean isBranchTarget(Stmt u, Stmt succ) {
    assert getSuccsOf(u).contains(succ);
    return u.branches();
  }

  @Override
  public List<Value> getParameterRefs(SootMethod m) {
    return methodToParameterRefs.getUnchecked(m);
  }

  @Override
  public Collection<Stmt> getStartPointsOf(SootMethod m) {
    if (!m.hasBody()) {
      return Collections.emptySet();
    }
    Body body = m.getBody();
    StmtGraph<?> stmtGraph = getOrCreateStmtGraph(body);
    return stmtGraph.getEntrypoints();
  }

  public boolean setOwnerStatement(Stmt u, Body b) {
    return stmtToOwner.put(u, b) == null;
  }

  @Override
  public boolean isCallStmt(Stmt stmt) {
    return stmt.isInvokableStmt() && stmt.asInvokableStmt().containsInvokeExpr();
  }

  @Override
  public Set<Stmt> allNonCallStartNodes() {
    return stmtToOwner.keySet().stream()
        .filter(u -> !(isStartPoint(u) || isCallStmt(u)))
        .collect(Collectors.toSet());
  }

  @Override
  public Set<Stmt> allNonCallEndNodes() {
    return stmtToOwner.keySet().stream()
        .filter(u -> !(isExitStmt(u) || isCallStmt(u)))
        .collect(Collectors.toSet());
  }

  @Override
  public Collection<Stmt> getReturnSitesOfCallAt(Stmt u) {
    return getSuccsOf(u);
  }

  @Override
  public Set<Stmt> getCallsFromWithin(SootMethod m) {
    return methodToCallsFromWithin.getUnchecked(m);
  }

  public void initializeStmtToOwner(SootMethod m) {
    if (!m.hasBody()) {
      return;
    }
    Body b = m.getBody();
    b.getStmtGraph().getNodes().forEach(node -> stmtToOwner.put(node, b));
  }

  @Override
  public List<Stmt> getPredsOf(Stmt u) {
    assert u != null;
    Body body = getBodyOf(u);
    if (body == null) {
      return Collections.emptyList();
    }
    StmtGraph<?> stmtGraph = getOrCreateStmtGraph(body);
    return stmtGraph.predecessors(u);
  }

  @Override
  public Collection<Stmt> getEndPointsOf(SootMethod m) {
    if (!m.hasBody()) {
      return Collections.emptySet();
    }
    Body body = m.getBody();
    StmtGraph<?> stmtGraph = getOrCreateStmtGraph(body);
    return stmtGraph.getTails();
  }

  @Override
  public List<Stmt> getPredsOfCallAt(Stmt u) {
    return getPredsOf(u);
  }

  @Override
  public boolean isReturnSite(Stmt n) {
    return getPredsOf(n).stream().anyMatch(this::isCallStmt);
  }

  @Override
  public boolean isReachable(Stmt u) {
    return stmtToOwner.containsKey(u);
  }
}
