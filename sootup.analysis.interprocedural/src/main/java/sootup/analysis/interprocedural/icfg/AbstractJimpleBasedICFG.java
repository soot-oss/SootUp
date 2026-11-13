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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.stmt.BranchingStmt;
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
  protected LoadingCache<Body, ControlFlowGraph<?>> bodyToControlFlowGraph =
      IDESolver.DEFAULT_CACHE_BUILDER.build(
          new CacheLoader<>() {
            @NonNull
            @Override
            public ControlFlowGraph<?> load(@NonNull Body body) {
              return makeGraph(body);
            }
          });

  @SynchronizedBy("by use of synchronized LoadingCache class")
  protected LoadingCache<SootMethod, List<Value>> methodToParameterRefs =
      IDESolver.DEFAULT_CACHE_BUILDER.build(
          new CacheLoader<>() {
            @NonNull
            @Override
            public List<Value> load(@NonNull SootMethod m) {
              return new ArrayList<>(m.getBody().getParameterLocals());
            }
          });

  @SynchronizedBy("by use of synchronized LoadingCache class")
  protected LoadingCache<SootMethod, Set<Stmt>> methodToCallsFromWithin =
      IDESolver.DEFAULT_CACHE_BUILDER.build(
          new CacheLoader<>() {
            @NonNull
            @Override
            public Set<Stmt> load(@NonNull SootMethod m) {
              return getCallsFromWithinMethod(m);
            }
          });

  protected AbstractJimpleBasedICFG() {
    this(true);
  }

  /**
   * Data flow solvers query the ICFG concurrently, while statements may be registered with {@link
   * #setOwnerStatement(Stmt, Body)}. Statements are compared by identity, so a concurrent hash map
   * keeps the identity semantics.
   */
  protected Map<Stmt, Body> createStmtToOwnerMap() {
    return new ConcurrentHashMap<>();
  }

  protected AbstractJimpleBasedICFG(boolean enableExceptions) {
    this.enableExceptions = enableExceptions;
  }

  public Body getBodyOf(Stmt stmt) {
    Body body = stmtToOwner.get(stmt);
    assert body != null : "Statement " + stmt + " not in Stmt-to-owner mapping";
    return body;
  }

  /** Returns the body owning the given statement, or null if the statement is not known. */
  protected Body findBodyOf(Stmt stmt) {
    return stmtToOwner.get(stmt);
  }

  @Override
  public SootMethod getMethodOf(Stmt stmt) {
    Body b = getBodyOf(stmt);
    if (b == null) {
      return null;
    }
    return view.getMethod(b.getMethodSignature()).orElse(null);
  }

  /**
   * Returns the successors of the given statement. If exceptions are enabled, this includes the
   * heads of the trap handlers that may catch an exception thrown at the given statement.
   */
  @Override
  public List<Stmt> getSuccsOf(Stmt stmt) {
    Body body = getBodyOf(stmt);
    if (body == null) {
      return Collections.emptyList();
    }
    ControlFlowGraph<?> controlFlowGraph = getOrCreateControlFlowGraph(body);
    List<Stmt> successors = controlFlowGraph.successors(stmt);
    if (!enableExceptions) {
      return successors;
    }
    return appendMissing(successors, controlFlowGraph.exceptionalSuccessors(stmt).values());
  }

  /** Appends the elements of additional that are not yet contained (by identity) in base. */
  private static List<Stmt> appendMissing(List<Stmt> base, Collection<Stmt> additional) {
    if (additional.isEmpty()) {
      return base;
    }
    List<Stmt> result = new ArrayList<>(base.size() + additional.size());
    result.addAll(base);
    Set<Stmt> seen = Collections.newSetFromMap(new IdentityHashMap<>());
    seen.addAll(base);
    for (Stmt stmt : additional) {
      if (seen.add(stmt)) {
        result.add(stmt);
      }
    }
    return result;
  }

  @Override
  public ControlFlowGraph<?> getOrCreateControlFlowGraph(SootMethod method) {
    return getOrCreateControlFlowGraph(method.getBody());
  }

  public ControlFlowGraph<?> getOrCreateControlFlowGraph(Body body) {
    return bodyToControlFlowGraph.getUnchecked(body);
  }

  protected ControlFlowGraph<?> makeGraph(Body body) {
    return body.getControlFlowGraph();
  }

  protected Set<Stmt> getCallsFromWithinMethod(SootMethod method) {
    return method.getBody().getStmts().stream()
        .filter(this::isCallStmt)
        .collect(Collectors.toSet());
  }

  @Override
  public boolean isExitStmt(Stmt stmt) {
    Body body = getBodyOf(stmt);
    ControlFlowGraph<?> controlFlowGraph = getOrCreateControlFlowGraph(body);
    return controlFlowGraph.getTails().contains(stmt);
  }

  @Override
  public boolean isStartPoint(Stmt stmt) {
    return getStartPoints(getBodyOf(stmt)).contains(stmt);
  }

  /**
   * Returns the heads of the graph exposed by this ICFG. Without exceptional edges, trap handlers
   * have no predecessor and are therefore heads as well (like Soot's BriefUnitGraph). With
   * exceptional edges, trap handlers are reached from the statements they protect and only the
   * statements without any predecessor remain heads (like Soot's ExceptionalUnitGraph).
   */
  protected Collection<Stmt> getStartPoints(Body body) {
    ControlFlowGraph<?> controlFlowGraph = getOrCreateControlFlowGraph(body);
    Collection<Stmt> entrypoints = controlFlowGraph.getEntrypoints();
    if (!enableExceptions) {
      return entrypoints;
    }
    Stmt startingStmt = controlFlowGraph.getStartingStmt();
    List<Stmt> heads = new ArrayList<>();
    heads.add(startingStmt);
    for (Stmt entrypoint : entrypoints) {
      if (entrypoint != startingStmt && getPredsOf(entrypoint).isEmpty()) {
        heads.add(entrypoint);
      }
    }
    return heads;
  }

  @Override
  public boolean isFallThroughSuccessor(Stmt stmt, Stmt successorCandidate) {
    assert getSuccsOf(stmt).contains(successorCandidate);
    if (!stmt.fallsThrough()) {
      return false;
    }
    Body body = getBodyOf(stmt);
    // the first successor of a statement that falls through is its fall-through successor
    return getOrCreateControlFlowGraph(body).successors(stmt).get(0) == successorCandidate;
  }

  @Override
  public boolean isBranchTarget(Stmt u, Stmt succ) {
    assert getSuccsOf(u).contains(succ);
    if (!(u instanceof BranchingStmt) || !u.branches()) {
      return false;
    }
    ControlFlowGraph<?> controlFlowGraph = getOrCreateControlFlowGraph(getBodyOf(u));
    return controlFlowGraph.getBranchTargetsOf((BranchingStmt) u).contains(succ);
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
    return getStartPoints(m.getBody());
  }

  public boolean setOwnerStatement(Stmt u, Body b) {
    return stmtToOwner.put(u, b) == null;
  }

  @Override
  public boolean isCallStmt(Stmt stmt) {
    return stmt.isInvokableStmt() && stmt.asInvokableStmt().getInvokeExpr().isPresent();
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
    b.getControlFlowGraph().getNodes().forEach(node -> stmtToOwner.put(node, b));
  }

  /**
   * Returns the predecessors of the given statement. If exceptions are enabled and the statement is
   * the head of a trap handler, this includes the statements protected by that handler.
   */
  @Override
  public List<Stmt> getPredsOf(Stmt u) {
    assert u != null;
    Body body = getBodyOf(u);
    if (body == null) {
      return Collections.emptyList();
    }
    ControlFlowGraph<?> controlFlowGraph = getOrCreateControlFlowGraph(body);
    // ControlFlowGraph.predecessors() of a trap handler also returns the last statement of each
    // block that can throw into the handler, while successors() only returns normal flow. Keep only
    // the normal predecessors so that predecessors and successors stay symmetric.
    List<Stmt> predecessors = new ArrayList<>();
    for (Stmt pred : controlFlowGraph.predecessors(u)) {
      if (controlFlowGraph.successors(pred).contains(u)) {
        predecessors.add(pred);
      }
    }
    if (!enableExceptions) {
      return predecessors;
    }
    return appendMissing(predecessors, controlFlowGraph.exceptionalPredecessors(u));
  }

  @Override
  public Collection<Stmt> getEndPointsOf(SootMethod m) {
    if (!m.hasBody()) {
      return Collections.emptySet();
    }
    Body body = m.getBody();
    ControlFlowGraph<?> controlFlowGraph = getOrCreateControlFlowGraph(body);
    return controlFlowGraph.getTails();
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
