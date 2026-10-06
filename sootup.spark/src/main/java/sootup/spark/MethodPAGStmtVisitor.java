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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import sootup.callgraph.AbstractCallGraphAlgorithm;
import sootup.callgraph.CallGraph;
import sootup.callgraph.invokedynamic.DynamicInvokeResolver;
import sootup.callgraph.invokedynamic.DynamicInvokeTarget;
import sootup.callgraph.invokedynamic.FunctionalObject;
import sootup.callgraph.reflection.ReflectionModel;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;
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
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.views.View;
import sootup.spark.node.AllocationNode;
import sootup.spark.node.LambdaAllocationNode;

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

  /** Must be the model the call graph was built with, so callee bodies match its edges. */
  @Builder.Default ReflectionModel reflectionModel = ReflectionModel.none();

  /** Must be the resolver the call graph was built with, so invokedynamic edges match. */
  @Builder.Default
  DynamicInvokeResolver dynamicInvokeResolver = DynamicInvokeResolver.bootstrapMethodHandles();

  /**
   * CHA mode: the functional objects of the call graph's methods, by {@link
   * FunctionalObject#dispatchedImplementation}. A call edge to such a method from an interface call
   * the object {@link FunctionalObject#answers answers} is a lambda call.
   */
  @Builder.Default Map<MethodSignature, List<FunctionalObject>> functionalObjects = Map.of();

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
      FunctionalObject.of(stmt, dynamicInvokeResolver, view)
          .ifPresent(
              fo ->
                  nodeFactory
                      .createNode(stmt.getLeftOp(), methodSignature)
                      .ifPresent(lhsNode -> addPagEdge(lambdaAllocation(fo), lhsNode)));
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
    if (expr instanceof JDynamicInvokeExpr dynamicInvokeExpr) {
      for (DynamicInvokeTarget target : dynamicInvokeResolver.resolve(dynamicInvokeExpr)) {
        MethodSignature targetSig = dispatch(target);
        // a lambda body is called elsewhere (or at the creation site, if so configured)
        if (target.lambdaImplementation()
            ? callGraph.containsMethod(targetSig)
            : callGraph.containsCall(methodSignature, targetSig, stmt)) {
          installCaptureEdges(dynamicInvokeExpr, target, targetSig);
        }
      }
      return;
    }
    Set<MethodSignature> lambdaTargets = new HashSet<>();
    if (expr instanceof AbstractInstanceInvokeExpr instanceExpr) {
      for (CallGraph.Call call : callGraph.callsFrom(methodSignature)) {
        if (!call.invokableStmt().equals(stmt)) continue;
        MethodSignature targetSig = call.targetMethodSignature();
        for (FunctionalObject fo : functionalObjects.getOrDefault(targetSig, List.of())) {
          if (fo.answers(expr.getMethodSignature(), view.getTypeHierarchy())) {
            installLambdaCallEdges(instanceExpr, lhs, stmt, fo, targetSig);
            lambdaTargets.add(targetSig);
          }
        }
      }
    }
    callGraph.callTargetsFrom(methodSignature).stream()
        .filter(targetMethodSig -> !lambdaTargets.contains(targetMethodSig))
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
    if (expr instanceof JDynamicInvokeExpr dynamicInvokeExpr) {
      // invokedynamic call sites have no dispatch receiver to defer resolution on, so they must
      // not be queued as a PendingVirtualCall (Solver.solveOnTheFly casts every pending call's
      // expr to AbstractInstanceInvokeExpr). Their targets are fixed by the bootstrap arguments,
      // resolved exactly as CHA mode does. Lambda bodies are called where the object is called
      // (Solver.solveOnTheFly), captures still bind here.
      for (DynamicInvokeTarget target : dynamicInvokeResolver.resolve(dynamicInvokeExpr)) {
        installCaptureEdges(dynamicInvokeExpr, target, dispatch(target));
      }
      for (DynamicInvokeTarget target :
          dynamicInvokeResolver.creationSiteTargets(dynamicInvokeExpr)) {
        addOtfCallEdge(dispatch(target), stmt);
      }
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
          body(sootMethod).getStmts().stream()
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
          body(sootMethod).getStmts().stream()
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
                body(sootMethod).getStmts().stream()
                    .filter(s -> s instanceof JReturnStmt)
                    .map(s -> (JReturnStmt) s)
                    .forEach(
                        returnStmt ->
                            nodeFactory
                                .createNode(returnStmt.getOp(), targetMethodSig)
                                .ifPresent(retOpNode -> addPagEdge(retOpNode, lhsNode))));
  }

  /**
   * Installs the PAG edges of a call that reaches the body of {@code fo} ({@code targetMethodSig}):
   * the call's arguments go to the parameters / receiver {@link FunctionalObject#samArgumentIndex}
   * names; the result flows to {@code lhs}: the body's return value or, for a constructor
   * reference, a fresh object (also bound to the constructor's receiver). Captured values are bound
   * at the creation site ({@link #installCaptureEdges}).
   */
  public void installLambdaCallEdges(
      AbstractInstanceInvokeExpr expr,
      Optional<Value> lhs,
      InvokableStmt stmt,
      FunctionalObject fo,
      MethodSignature targetMethodSig) {
    Optional<? extends SootMethod> sootMethodOpt =
        view.getMethod(targetMethodSig).filter(SootMethod::hasBody);
    if (sootMethodOpt.isEmpty()) return;
    Body targetBody = body(sootMethodOpt.get());
    for (int j = 0; j < expr.getArgCount(); j++) {
      val argNode = nodeFactory.createNode(expr.getArg(j), methodSignature);
      val targetNode =
          parameterLocal(targetBody, fo.samArgumentIndex(j))
              .flatMap(local -> nodeFactory.createNode(local, targetMethodSig));
      if (argNode.isPresent() && targetNode.isPresent()) {
        addPagEdge(argNode.get(), targetNode.get());
      }
    }
    val lhsNode = lhs.flatMap(l -> nodeFactory.createNode(l, methodSignature));
    if (fo.isConstructorReference()) {
      AllocationNode created =
          AllocationNode.builder()
              .type(targetMethodSig.getDeclClassType())
              .containingMethodSig(methodSignature)
              .allocationSite(stmt)
              .build();
      lhsNode.ifPresent(node -> addPagEdge(created, node));
      parameterLocal(targetBody, DynamicInvokeTarget.RECEIVER)
          .flatMap(local -> nodeFactory.createNode(local, targetMethodSig))
          .ifPresent(node -> addPagEdge(created, node));
      return;
    }
    lhsNode.ifPresent(
        node ->
            targetBody.getStmts().stream()
                .filter(s -> s instanceof JReturnStmt)
                .map(s -> (JReturnStmt) s)
                .forEach(
                    returnStmt ->
                        nodeFactory
                            .createNode(returnStmt.getOp(), targetMethodSig)
                            .ifPresent(retOpNode -> addPagEdge(retOpNode, node))));
  }

  /** The object {@code fo} creates, one per creation site. */
  private LambdaAllocationNode lambdaAllocation(FunctionalObject fo) {
    return LambdaAllocationNode.builder()
        .type(fo.functionalInterface())
        .containingMethodSig(methodSignature)
        .allocationSite(fo)
        .functionalObject(fo)
        .build();
  }

  /** The concrete implementation of {@code target}, matching CHA's invokedynamic edges. */
  private MethodSignature dispatch(DynamicInvokeTarget target) {
    return AbstractCallGraphAlgorithm.resolveConcreteDispatch(view, target.method())
        .orElse(target.method());
  }

  /**
   * Binds the values an invokedynamic call site captures to the receiver/parameters of its target
   * (see {@link DynamicInvokeTarget#captureParameterIndex(int)}). No return edge: the call site
   * yields the functional object, not the target's result.
   */
  private void installCaptureEdges(
      JDynamicInvokeExpr expr, DynamicInvokeTarget target, MethodSignature targetMethodSig) {
    Optional<? extends SootMethod> sootMethodOpt =
        view.getMethod(targetMethodSig).filter(SootMethod::hasBody);
    if (sootMethodOpt.isEmpty()) return;
    Body targetBody = body(sootMethodOpt.get());
    for (int i = 0; i < expr.getArgCount(); i++) {
      Optional<Local> targetLocal = parameterLocal(targetBody, target.captureParameterIndex(i));
      val argNode = nodeFactory.createNode(expr.getArg(i), methodSignature);
      val targetNode = targetLocal.flatMap(local -> nodeFactory.createNode(local, targetMethodSig));
      if (argNode.isPresent() && targetNode.isPresent()) {
        addPagEdge(argNode.get(), targetNode.get());
      }
    }
  }

  /** The local holding parameter {@code index}, or the receiver for {@code RECEIVER}. */
  private static Optional<Local> parameterLocal(Body body, int index) {
    return index == DynamicInvokeTarget.RECEIVER
        ? identityLocal(body, ref -> ref instanceof JThisRef)
        : identityLocal(body, ref -> ref instanceof JParameterRef p && p.getIndex() == index);
  }

  private static Optional<Local> identityLocal(Body body, Predicate<Value> rightOp) {
    return body.getStmts().stream()
        .filter(s -> s instanceof JIdentityStmt)
        .map(s -> (JIdentityStmt) s)
        .filter(s -> rightOp.test(s.getRightOp()))
        .map(JIdentityStmt::getLeftOp)
        .findFirst();
  }

  private Body body(SootMethod method) {
    return reflectionModel.resolve(method, method.getBody());
  }
}
