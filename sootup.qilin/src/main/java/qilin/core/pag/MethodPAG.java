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
import qilin.CoreConfig;
import qilin.core.PTAScene;
import qilin.core.builder.MethodNodeFactory;
import qilin.util.DataFactory;
import qilin.util.PTAUtils;
import qilin.util.queue.ChunkedQueue;
import qilin.util.queue.QueueReader;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Trap;
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
  private final ChunkedQueue<Node> internalEdges = new ChunkedQueue<>();
  private final QueueReader<Node> internalReader = internalEdges.reader();
  private final Set<SootMethod> clinits = DataFactory.createSet();
  private final Collection<InvokableStmt> invokeStmts = DataFactory.createSet();
  public Body body;

  /**
   * Since now the exception analysis is handled on-the-fly, we should record the exception edges
   * explicitly for Eagle and Turner.
   */
  private final Map<Node, Set<Node>> exceptionEdges = DataFactory.createMap();

  protected MethodNodeFactory nodeFactory;
  protected final PTAScene ptaScene;
  SootMethod method;
  /*
   * List[i-1] is wrappered in List[i].
   * We have to extend the following structure from Map<Node, List<Trap>> to
   * Map<Node, Map<Stmt, List<Trap>>> because there exists cases where the same
   * node are thrown more than once and lies in different catch blocks.
   * */
  public final Map<Stmt, List<Trap>> stmt2wrapperedTraps = DataFactory.createMap();
  public final Map<Node, Map<Stmt, List<Trap>>> node2wrapperedTraps = DataFactory.createMap();

  public MethodPAG(PAG pag, SootMethod m, Body body) {
    this.ptaScene = pag.getPta().getScene();
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
    if (!CoreConfig.v().getPtaConfig().preciseExceptions) {
      return;
    }
    // List<Trap> traps = body.getTraps();
    // //    List<Stmt> units = body.getStmts();
    // Set<Stmt> inTraps = DataFactory.createSet();
  }

  private void addStmtTrap(Node src, Stmt stmt, Trap trap) {
    Map<Stmt, List<Trap>> stmt2Traps =
        node2wrapperedTraps.computeIfAbsent(src, k -> DataFactory.createMap());
    List<Trap> trapList = stmt2Traps.computeIfAbsent(stmt, k -> DataFactory.createList());
    trapList.add(trap);
    stmt2wrapperedTraps.computeIfAbsent(stmt, k -> DataFactory.createList()).add(trap);
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

  public void addInternalEdge(Node src, Node dst) {
    if (src == null) {
      return;
    }
    internalEdges.add(src);
    internalEdges.add(dst);
  }

  public QueueReader<Node> getInternalReader() {
    return internalReader;
  }

  public void addTriggeredClinit(SootMethod clinit) {
    clinits.add(clinit);
  }

  public Iterator<SootMethod> triggeredClinits() {
    return clinits.iterator();
  }

  public void addExceptionEdge(Node from, Node to) {
    this.exceptionEdges.computeIfAbsent(from, k -> DataFactory.createSet()).add(to);
  }

  public Map<Node, Set<Node>> getExceptionEdges() {
    return this.exceptionEdges;
  }
}
