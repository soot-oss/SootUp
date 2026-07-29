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
import qilin.util.PTAUtils;
import qilin.util.queue.ChunkedQueue;
import qilin.util.queue.QueueReader;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.common.Trap;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;

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
  /*
   * List[i-1] is wrappered in List[i].
   * We have to extend the following structure from Map<Node, List<Trap>> to
   * Map<Node, Map<Stmt, List<Trap>>> because there exists cases where the same
   * node are thrown more than once and lies in different catch blocks.
   * */
  public final Map<Stmt, List<Trap>> stmt2wrapperedTraps;
  public final Map<PagNode, Map<Stmt, List<Trap>>> node2wrapperedTraps;

  {
    exceptionEdges = new HashMap<>();
    stmt2wrapperedTraps = new HashMap<>();
    node2wrapperedTraps = new HashMap<>();
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
      if (!PTAUtils.isFakeMainMethod(method)) {
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

  protected void buildException() {
    // we use the same logic as doop (library/exceptions/precise.logic).
    if (!config.isPreciseExceptions()) {
      return;
    }
    // List<Trap> traps = body.getTraps();
    // //    List<Stmt> units = body.getStmts();
    // Set<Stmt> inTraps = DataFactory.createSet();
  }

  private void addStmtTrap(PagNode src, Stmt stmt, Trap trap) {
    Map<Stmt, List<Trap>> stmt2Traps =
        node2wrapperedTraps.computeIfAbsent(src, k -> new HashMap<>());
    List<Trap> trapList = stmt2Traps.computeIfAbsent(stmt, k -> new ArrayList<>());
    trapList.add(trap);
    stmt2wrapperedTraps.computeIfAbsent(stmt, k -> new ArrayList<>()).add(trap);
  }

  protected void addMiscEdges() {
    if (method
        .getSignature()
        .toString()
        .equals(
            "<java.lang.ref.Reference: void <init>(java.lang.Object,java.lang.ref.ReferenceQueue)>")) {
      // Implements the special status of java.lang.ref.Reference just as in Doop
      // (library/reference.logic).
      SootClass sootClass = ptaScene.getSootClass("java.lang.ref.Reference");
      SootField sf = sootClass.getField("pending").get();
      JStaticFieldRef sfr = Jimple.newStaticFieldRef(sf.getSignature());
      addInternalEdge(nodeFactory.caseThis(), nodeFactory.getNode(sfr));
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
