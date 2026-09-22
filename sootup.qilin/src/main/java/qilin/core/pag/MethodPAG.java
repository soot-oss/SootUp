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

import java.util.*;
import qilin.core.PTAScene;
import qilin.core.builder.MethodNodeFactory;
import qilin.core.config.PointerAnalysisConfig;
import qilin.util.FakeMainMethods;
import qilin.util.queue.ChunkedQueue;
import qilin.util.queue.QueueReader;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.JThrowStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;
import sootup.core.types.ClassType;

/**
 * Part of a pointer assignment graph for a single method.
 *
 * @author Ondrej Lhotak
 */
public class MethodPAG {
  private final ChunkedQueue<PagNode> internalEdges = new ChunkedQueue<>();
  private final QueueReader<PagNode> internalReader = internalEdges.reader();
  private final Set<SootMethod> clinits;
  private final Collection<InvokableStmt> invokeStmts;

  {
    clinits = new HashSet<>();
    invokeStmts = new HashSet<>();
  }

  public Body body;

  /**
   * Since now the exception analysis is handled on-the-fly, we should record the exception edges
   * explicitly for Eagle and Turner.
   */
  private final Map<PagNode, Set<PagNode>> exceptionEdges;

  protected MethodNodeFactory nodeFactory;
  protected final PTAScene ptaScene;
  protected final PointerAnalysisConfig config;
  SootMethod method;

  /**
   * Every statement of this method that may raise an exception which the pointer analysis tracks
   * (an explicit {@code throw}, or a call whose callee may let one escape), mapped to the handlers
   * that are in scope for it: caught exception type -&gt; the handler's first statement. A
   * statement with no enclosing {@code try} is still registered, with an empty map - membership in
   * this map is what makes a statement a throw site for {@code Solver.recordThrowStmts}, and an
   * uncaught throw still has to reach {@code MethodNodeFactory.caseMethodThrow()}. Only filled when
   * {@link PointerAnalysisConfig#isPreciseExceptions()} is set; see {@link #buildException()}.
   */
  public final Map<Stmt, Map<ClassType, Stmt>> stmt2Handlers;

  {
    exceptionEdges = new HashMap<>();
    stmt2Handlers = new HashMap<>();
  }

  public MethodPAG(PAG pag, SootMethod m, Body body) {
    this.ptaScene = pag.getPta().getScene();
    this.config = pag.getPta().getConfig();
    this.method = m;
    this.nodeFactory = new MethodNodeFactory(pag, this);
    this.body = body;
    build();
  }

  public SootMethod getMethod() {
    return method;
  }

  public MethodNodeFactory nodeFactory() {
    return nodeFactory;
  }

  public Collection<InvokableStmt> getInvokeStmts() {
    return invokeStmts;
  }

  public boolean addCallStmt(InvokableStmt unit) {
    return this.invokeStmts.add(unit);
  }

  protected void build() {
    // this method is invalid but exists in pmd-deps.jar
    if (method
        .getSignature()
        .toString()
        .equals(
            "<org.apache.xerces.parsers.XML11Configuration: boolean getFeature0(java.lang.String)>")) {
      return;
    }
    buildException();
    buildNormal();
    addMiscEdges();
  }

  protected void buildNormal() {
    if (method.isStatic()) {
      if (!FakeMainMethods.isFakeMainMethod(method)) {
        SootClass sc = ptaScene.getView().getClass(method.getDeclaringClassType()).get();
        nodeFactory.clinitsOf(sc).forEach(this::addTriggeredClinit);
      }
    }
    for (Stmt unit : body.getStmts()) {
      try {
        nodeFactory.handleStmt(unit);
      } catch (Exception e) {
        System.out.println("Warning:" + e + " in " + this.getClass());
      }
    }
  }

  /**
   * Collects this method's throw sites and the catch handlers in scope for each of them into {@link
   * #stmt2Handlers}, from which {@code Solver.recordThrowStmts()} creates the throw sites and
   * {@code ExceptionHandler.dispatch()} routes each thrown object to a handler.
   *
   * <p>We use the same logic as doop (library/exceptions/precise.logic): only explicit {@code
   * throw}s and exceptions propagated out of callees are modelled, never implicit ones (NPE,
   * ArrayIndexOutOfBounds, ...).
   *
   * <p>Unlike Soot, SootUp has no ordered exception table to read back: traps are stored in the
   * block graph as {@link ControlFlowGraph#exceptionalSuccessors(Stmt)}, an unordered map keyed by
   * caught type. Handler priority is therefore re-derived at dispatch time by preferring the most
   * specific matching type, which reproduces the bytecode order for handlers of one {@code try}
   * (Java rejects catching a subtype after its supertype there).
   */
  protected void buildException() {
    if (!config.isPreciseExceptions()) {
      return;
    }
    ControlFlowGraph<?> cfg = body.getControlFlowGraph();
    for (Stmt stmt : body.getStmts()) {
      PagNode src;
      if (stmt.isInvokableStmt() && stmt.asInvokableStmt().getInvokeExpr().isPresent()) {
        // note, method.getExceptions() does not return implicit exceptions.
        src = nodeFactory.makeInvokeStmtThrowVarNode(stmt, method);
      } else if (stmt instanceof JThrowStmt) {
        src = nodeFactory.getNode(((JThrowStmt) stmt).getOp());
      } else {
        continue;
      }
      if (src == null) {
        // `throw null`: no node can carry objects here, and registering it would make the Solver
        // cast a null node to VarNode.
        continue;
      }
      stmt2Handlers.put(stmt, new HashMap<>(cfg.exceptionalSuccessors(stmt)));
    }
  }

  protected void addMiscEdges() {
    if (method
        .getSignature()
        .toString()
        .equals(
            "<java.lang.ref.Reference: void <init>(java.lang.Object,java.lang.ref.ReferenceQueue)>")) {
      // Implements the special status of java.lang.ref.Reference just as in Doop
      // (library/reference.logic). "pending" is a JRE6/8-era Reference internal field - later
      // JDKs' reference-processing rewrite dropped/renamed it, so skip this modeling if absent
      // instead of crashing.
      SootClass sootClass =
          ptaScene.getSootClass(
              ptaScene.getView().getIdentifierFactory().getClassType("java.lang.ref.Reference"));
      Optional<? extends SootField> osf = sootClass.getField("pending");
      if (osf.isPresent()) {
        JStaticFieldRef sfr = Jimple.newStaticFieldRef(osf.get().getSignature());
        addInternalEdge(nodeFactory.caseThis(), nodeFactory.getNode(sfr));
      }
    }
  }

  public void addInternalEdge(PagNode src, PagNode dst) {
    if (src == null) {
      return;
    }
    internalEdges.add(src);
    internalEdges.add(dst);
  }

  public QueueReader<PagNode> getInternalReader() {
    return internalReader;
  }

  public void addTriggeredClinit(SootMethod clinit) {
    clinits.add(clinit);
  }

  public Iterator<SootMethod> triggeredClinits() {
    return clinits.iterator();
  }

  public void addExceptionEdge(PagNode from, PagNode to) {
    this.exceptionEdges.computeIfAbsent(from, k -> new HashSet<>()).add(to);
  }

  public Map<PagNode, Set<PagNode>> getExceptionEdges() {
    return this.exceptionEdges;
  }
}
