package sootup.spark;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2002-2026 Ondrej Lhotak, Kadiray Karakaya and others
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

import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import sootup.callgraph.CallGraph;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.expr.JStaticInvokeExpr;
import sootup.core.jimple.common.ref.JParameterRef;
import sootup.core.jimple.common.ref.JThisRef;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.visitor.AbstractStmtVisitor;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.views.View;

@Slf4j
@Builder
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class MethodPAGStmtVisitor extends AbstractStmtVisitor {

  MethodSignature methodSignature;
  PAG PAG;
  CallGraph callGraph;
  View view;
  NodeFactory nodeFactory;

  /** Captured invoke site whose targets are decided by the OTF builder via points-to. */
  public record PendingVirtualCall(
      MethodSignature srcSig, AbstractInvokeExpr expr, Optional<Value> lhs, InvokableStmt stmt) {}

  /**
   * Optional state activated only when the OTF call graph builder runs. When non-null, virtual
   * invocations are recorded via {@link #otfPendingVirtualCalls} instead of being resolved against
   * the (still-empty) call graph; static and special invocations are still resolved at visit time
   * since their targets are determined by the static type of the call.
   */
  OtfContext otfContext;

  /** Bag of OTF-only collaborators; null in CHA mode. */
  public record OtfContext(
      IncrementalPointsToAnalysis propagator,
      java.util.Deque<MethodSignature> worklist,
      List<PendingVirtualCall> otfPendingVirtualCalls) {}

  @Override
  public void caseAssignStmt(JAssignStmt stmt) {
    if (stmt.isInvokableStmt() && stmt.asInvokableStmt().getInvokeExpr().isPresent()) {
      handleInvokeExpr(
          stmt.asInvokableStmt().getInvokeExpr().get(),
          Optional.of(stmt.getLeftOp()),
          stmt.asInvokableStmt());
    } else { // regular assignment
      val left = stmt.getLeftOp();
      val right = stmt.getRightOp();
      val leftNode = nodeFactory.createNode(left, methodSignature);
      val rightNode = nodeFactory.createNode(right, methodSignature);
      if (leftNode.isPresent() && rightNode.isPresent()) {
        addPagEdge(rightNode.get(), leftNode.get());
      } else {
        log.warn("Missing nodes for left: {} <- right: {}", left, right);
      }
    }
  }

  /**
   * At each call site, assignment edges are added from the nodes representing the actual arguments
   * to the nodes representing the corresponding parameters of all methods that may be targets of
   * the call site, and an assignment edge is added from the return node of each of these methods to
   * the node for the variable that receives the return value (if any) at the call site.
   */
  @Override
  public void caseInvokeStmt(JInvokeStmt stmt) {
    handleInvokeExpr(
        stmt.asInvokableStmt().getInvokeExpr().get(), Optional.empty(), stmt.asInvokableStmt());
  }

  /** Routes a PAG edge through the propagator when running OTF, plain PAG otherwise. */
  private void addPagEdge(sootup.spark.node.Node source, sootup.spark.node.Node target) {
    if (otfContext != null) {
      otfContext.propagator().addEdge(source, target);
    } else {
      PAG.addEdge(source, target);
    }
  }

  /**
   * The builder inserts edges into the pointer assignment graph to represent pointer flow through
   * method parameters and return values. In CHA mode, targets are taken from the pre-built call
   * graph. In OTF mode, static and special invokes are resolved here directly (their targets are
   * fixed by the static call type), while virtual / interface invokes are recorded as pending so
   * the OTF driver can resolve them once points-to information for the receiver is known.
   */
  private void handleInvokeExpr(AbstractInvokeExpr expr, Optional<Value> lhs, InvokableStmt stmt) {
    if (otfContext != null) {
      handleInvokeExprOtf(expr, lhs, stmt);
      return;
    }
    callGraph.callTargetsFrom(methodSignature).stream()
        .filter(
            targetMethodSig ->
                targetMethodSig
                    .getSubSignature()
                    .equals(expr.getMethodSignature().getSubSignature()))
        .forEach(targetMethodSig -> installCallEdges(expr, lhs, targetMethodSig));
  }

  private void handleInvokeExprOtf(
      AbstractInvokeExpr expr, Optional<Value> lhs, InvokableStmt stmt) {
    if (expr instanceof JStaticInvokeExpr || expr instanceof JSpecialInvokeExpr) {
      MethodSignature targetSig = expr.getMethodSignature();
      installCallEdges(expr, lhs, targetSig);
      // Record direct call edge in the OTF call graph and enqueue the target.
      addOtfCallEdge(targetSig, stmt);
      return;
    }
    // Virtual or interface invoke: defer until the receiver's points-to set is known.
    otfContext
        .otfPendingVirtualCalls()
        .add(new PendingVirtualCall(methodSignature, expr, lhs, stmt));
  }

  private void addOtfCallEdge(MethodSignature targetSig, InvokableStmt stmt) {
    if (!(callGraph instanceof sootup.callgraph.MutableCallGraph mcg)) return;
    if (!mcg.containsMethod(targetSig)) {
      mcg.addMethod(targetSig);
      otfContext.worklist().add(targetSig);
    }
    if (!mcg.containsCall(methodSignature, targetSig, stmt)) {
      mcg.addCall(methodSignature, targetSig, stmt);
    }
  }

  /**
   * Installs the receiver/parameter/return PAG edges connecting one call site to one resolved
   * target method. Public so the OTF driver can invoke it after virtual dispatch resolves a new
   * target on a previously-deferred call site.
   */
  public void installCallEdges(
      AbstractInvokeExpr expr, Optional<Value> lhs, MethodSignature targetMethodSig) {
    Optional<? extends SootMethod> sootMethodOpt =
        view.getMethod(targetMethodSig).filter(SootMethod::hasBody);
    if (sootMethodOpt.isEmpty()) return;
    SootMethod sootMethod = sootMethodOpt.get();

    if (expr instanceof AbstractInstanceInvokeExpr instanceExpr) {
      val baseNode = nodeFactory.createNode(instanceExpr.getBase(), methodSignature);
      val thisLocal =
          sootMethod.getBody().getStmts().stream()
              .filter(s -> s instanceof JIdentityStmt)
              .map(s -> (JIdentityStmt) s)
              .filter(s -> s.getRightOp() instanceof JThisRef)
              .map(JIdentityStmt::getLeftOp)
              .findFirst();
      if (baseNode.isPresent() && thisLocal.isPresent()) {
        val thisNode = nodeFactory.createNode(thisLocal.get(), targetMethodSig);
        thisNode.ifPresent(node -> addPagEdge(baseNode.get(), node));
      }
    }

    for (int i = 0; i < expr.getArgCount(); i++) {
      val argNode = nodeFactory.createNode(expr.getArg(i), methodSignature);
      final int index = i;
      val paramLocal =
          sootMethod.getBody().getStmts().stream()
              .filter(s -> s instanceof JIdentityStmt)
              .map(s -> (JIdentityStmt) s)
              .filter(s -> s.getRightOp() instanceof JParameterRef)
              .filter(s -> ((JParameterRef) s.getRightOp()).getIndex() == index)
              .map(JIdentityStmt::getLeftOp)
              .findFirst();
      if (argNode.isPresent() && paramLocal.isPresent()) {
        val paramNode = nodeFactory.createNode(paramLocal.get(), targetMethodSig);
        paramNode.ifPresent(node -> addPagEdge(argNode.get(), node));
      }
    }

    lhs.flatMap(l -> nodeFactory.createNode(l, methodSignature))
        .ifPresent(
            lhsNode ->
                sootMethod.getBody().getStmts().stream()
                    .filter(s -> s instanceof JReturnStmt)
                    .map(s -> (JReturnStmt) s)
                    .forEach(
                        returnStmt ->
                            nodeFactory
                                .createNode(returnStmt.getOp(), targetMethodSig)
                                .ifPresent(retOpNode -> addPagEdge(retOpNode, lhsNode))));
  }
}
