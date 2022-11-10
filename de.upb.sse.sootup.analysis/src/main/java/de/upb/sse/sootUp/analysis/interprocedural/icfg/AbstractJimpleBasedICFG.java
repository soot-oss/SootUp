package de.upb.sse.sootUp.analysis.interprocedural.icfg;

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
import de.upb.sse.sootup.core.graph.ExceptionalStmtGraph;
import de.upb.sse.sootup.core.graph.ForwardingStmtGraph;
import de.upb.sse.sootup.core.graph.MutableExceptionalStmtGraph;
import de.upb.sse.sootup.core.graph.StmtGraph;
import de.upb.sse.sootup.core.jimple.basic.Value;
import de.upb.sse.sootup.core.jimple.common.stmt.Stmt;
import de.upb.sse.sootup.core.model.Body;
import de.upb.sse.sootup.core.model.SootMethod;
import de.upb.sse.sootup.core.views.View;
import heros.DontSynchronize;
import heros.SynchronizedBy;
import heros.solver.IDESolver;
import java.util.*;

public abstract class AbstractJimpleBasedICFG implements BiDiInterproceduralCFG<Stmt, SootMethod> {

  protected final boolean enableExceptions;

  protected View view;

  @DontSynchronize("written by single thread; read afterwards")
  private final Map<Stmt, Body> stmtToOwner = createStmtToOwnerMap();

  @SynchronizedBy("by use of synchronized LoadingCache class")
  protected LoadingCache<Body, StmtGraph> bodyToStmtGraph =
      IDESolver.DEFAULT_CACHE_BUILDER.build(
          new CacheLoader<Body, StmtGraph>() {
            @Override
            public StmtGraph load(Body body) throws Exception {
              return makeGraph(body);
            }
          });

  @SynchronizedBy("by use of synchronized LoadingCache class")
  protected LoadingCache<SootMethod, List<Value>> methodToParameterRefs =
      IDESolver.DEFAULT_CACHE_BUILDER.build(
          new CacheLoader<SootMethod, List<Value>>() {
            @Override
            public List<Value> load(SootMethod m) throws Exception {
              return new ArrayList<>(m.getBody().getParameterLocals());
            }
          });

  @SynchronizedBy("by use of synchronized LoadingCache class")
  protected LoadingCache<SootMethod, Set<Stmt>> methodToCallsFromWithin =
      IDESolver.DEFAULT_CACHE_BUILDER.build(
          new CacheLoader<SootMethod, Set<Stmt>>() {
            @Override
            public Set<Stmt> load(SootMethod m) throws Exception {
              return getCallsFromWithinMethod(m);
            }
          });

  protected AbstractJimpleBasedICFG() {
    this(true);
  }

  protected Map<Stmt, Body> createStmtToOwnerMap() {
    return new LinkedHashMap<>();
  }

  protected AbstractJimpleBasedICFG(boolean enableExceptions) {
    this.enableExceptions = enableExceptions;
  }

  public Body getBodyOf(Stmt u) {
    assert stmtToOwner.containsKey(u) : "Statement " + u + " not in unit-to-owner mapping";
    return stmtToOwner.get(u);
  }

  @Override
  public SootMethod getMethodOf(Stmt u) {
    Body b = getBodyOf(u);
    return b == null ? null : (SootMethod) view.getMethod(b.getMethodSignature()).orElse(null);
  }

  @Override
  public List<Stmt> getSuccsOf(Stmt u) {
    Body body = getBodyOf(u);
    if (body == null) {
      return Collections.emptyList();
    }
    StmtGraph unitGraph = getOrCreateStmtGraph(body);
    return unitGraph.successors(u);
  }

  @Override
  public StmtGraph getOrCreateStmtGraph(SootMethod m) {
    return getOrCreateStmtGraph(m.getBody());
  }

  public StmtGraph getOrCreateStmtGraph(Body body) {
    return bodyToStmtGraph.getUnchecked(body);
  }

  protected StmtGraph makeGraph(Body body) {
    return enableExceptions
        ? new ExceptionalStmtGraph(new MutableExceptionalStmtGraph())
        : new ForwardingStmtGraph(body.getStmtGraph());
  }

  protected Set<Stmt> getCallsFromWithinMethod(SootMethod m) {
    Set<Stmt> res = null;
    for (Stmt u : m.getBody().getStmts()) {
      if (isCallStmt(u)) {
        if (res == null) {
          res = new LinkedHashSet<>();
        }
        res.add(u);
      }
    }
    return res == null ? Collections.emptySet() : res;
  }

  @Override
  public boolean isExitStmt(Stmt u) {
    Body body = getBodyOf(u);
    StmtGraph unitGraph = getOrCreateStmtGraph(body);
    return unitGraph.getTails().contains(u);
  }

  @Override
  public boolean isStartPoint(Stmt u) {
    Body body = getBodyOf(u);
    StmtGraph unitGraph = getOrCreateStmtGraph(body);
    return unitGraph.getEntrypoints().contains(u);
  }

  @Override
  public boolean isFallThroughSuccessor(Stmt u, Stmt succ) {
    assert getSuccsOf(u).contains(succ);
    if (!u.fallsThrough()) {
      return false;
    }
    Body body = getBodyOf(u);
    return body.getStmtGraph().successors(u) == succ;
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
    if (m.hasBody()) {
      Body body = m.getBody();
      StmtGraph unitGraph = getOrCreateStmtGraph(body);
      return unitGraph.getEntrypoints();
    }
    return Collections.emptySet();
  }

  public boolean setOwnerStatement(Stmt u, Body b) {
    return stmtToOwner.put(u, b) == null;
  }

  @Override
  public boolean isCallStmt(Stmt stmt) {
    return stmt.containsInvokeExpr();
  }

  @Override
  public Set<Stmt> allNonCallStartNodes() {
    Set<Stmt> res = new LinkedHashSet<>(stmtToOwner.keySet());
    for (Iterator<Stmt> iter = res.iterator(); iter.hasNext(); ) {
      Stmt u = iter.next();
      if (isStartPoint(u) || isCallStmt(u)) {
        iter.remove();
      }
    }
    return res;
  }

  @Override
  public Set<Stmt> allNonCallEndNodes() {
    Set<Stmt> res = new LinkedHashSet<>(stmtToOwner.keySet());
    for (Iterator<Stmt> iter = res.iterator(); iter.hasNext(); ) {
      Stmt u = iter.next();
      if (isExitStmt(u) || isCallStmt(u)) {
        iter.remove();
      }
    }
    return res;
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
    if (m.hasBody()) {
      Body b = m.getBody();
      for (Stmt node : b.getStmtGraph().nodes()) {
        stmtToOwner.put(node, b);
      }
    }
  }

  @Override
  public List<Stmt> getPredsOf(Stmt u) {
    assert u != null;
    Body body = getBodyOf(u);
    if (body == null) {
      return Collections.emptyList();
    }
    StmtGraph unitGraph = getOrCreateStmtGraph(body);
    return unitGraph.predecessors(u);
  }

  @Override
  public Collection<Stmt> getEndPointsOf(SootMethod m) {
    if (m.hasBody()) {
      Body body = m.getBody();
      StmtGraph unitGraph = getOrCreateStmtGraph(body);
      return unitGraph.getTails();
    }
    return Collections.emptySet();
  }

  @Override
  public List<Stmt> getPredsOfCallAt(Stmt u) {
    return getPredsOf(u);
  }

  @Override
  public boolean isReturnSite(Stmt n) {
    for (Stmt pred : getPredsOf(n)) {
      if (isCallStmt(pred)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isReachable(Stmt u) {
    return stmtToOwner.containsKey(u);
  }
}
