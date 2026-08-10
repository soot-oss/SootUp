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
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.constant.MethodHandle;
import sootup.core.jimple.common.constant.StringConstant;
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
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.core.views.View;
import sootup.spark.node.ReflectiveClassToken;

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
   * Captured {@code Class.forName(arg)} site whose {@code arg} isn't a literal at this call site --
   * resolved once {@code arg}'s points-to set is known, in case it contains a {@code
   * StringConstantNode} that reached here from a literal at some caller's call site (e.g. forwarded
   * through one or more levels of parameter passing, as happens with JAXP-style factory lookups:
   * {@code TransformerFactory.newInstance("literal", ...)} forwards the literal as a parameter into
   * an internal {@code Class.forName(param)} call). See {@code Solver.solveOnTheFly}'s resolution
   * pass for these.
   */
  public record PendingReflectiveForName(
      MethodSignature srcSig, Value arg, Type callType, Optional<Value> lhs, InvokableStmt stmt) {}

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
      List<PendingVirtualCall> otfPendingVirtualCalls,
      List<PendingReflectiveForName> otfPendingReflectiveForNameCalls) {}

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
      // No-op unless this is exactly Class.forName(...) -- see method doc.
      handleClassForName(expr, targetSig, lhs, stmt);
      return;
    }
    if (expr instanceof JDynamicInvokeExpr dynamicInvokeExpr) {
      // invokedynamic call sites (lambdas, method references, string-concat factories) have no
      // dispatch receiver to defer resolution on -- they are not virtual calls in the
      // AbstractInstanceInvokeExpr sense, so they must not be queued as a PendingVirtualCall
      // (Solver.solveOnTheFly force-casts every pending call's expr to
      // AbstractInstanceInvokeExpr to read the receiver, which throws a ClassCastException for
      // these). For LambdaMetafactory-backed sites (ordinary lambdas and method references) we
      // resolve the real target below so its body isn't invisible dead code to SPARK; every
      // other bootstrap (string-concat, records' ObjectMethods, anything unrecognized) has no
      // single "impl method" to route to and is left with no edges, same as before. Note this
      // stays OTF-only: CHA-mode Spark and sootup.callgraph's CHA/RTA algorithms still resolve
      // zero targets for any invokedynamic site.
      handleLambdaInvokeDynamic(dynamicInvokeExpr, stmt);
      return;
    }
    // Virtual or interface invoke: defer until the receiver's points-to set is known.
    otfContext
        .otfPendingVirtualCalls()
        .add(new PendingVirtualCall(methodSignature, expr, lhs, stmt));
  }

  private static final String LAMBDA_METAFACTORY = "java.lang.invoke.LambdaMetafactory";

  /**
   * Resolves the real target of a lambda / method-reference invokedynamic site and makes it
   * reachable, wiring its captured arguments into the target's parameters (and receiver, for
   * bound/unbound instance method references).
   *
   * <p>Deliberately does not wire a return edge: at an indy site the call's {@code lhs} is bound to
   * the functional-interface instance the JVM manufactures at link time, not to the target method's
   * return value -- that only flows once something later invokes the interface's SAM method on that
   * instance (e.g. {@code supplier.get()}), which this pass does not model (doing so needs a
   * synthetic closure-object points-to node and custom dispatch, tracked separately).
   */
  private void handleLambdaInvokeDynamic(JDynamicInvokeExpr expr, InvokableStmt stmt) {
    if (!expr.getBootstrapMethodSignature()
        .getDeclClassType()
        .getFullyQualifiedName()
        .equals(LAMBDA_METAFACTORY)) {
      return;
    }

    Optional<MethodHandle> implHandle = findImplMethodHandle(expr);
    if (implHandle.isEmpty()) return;
    MethodSignature targetSig = (MethodSignature) implHandle.get().getReferenceSignature();

    addOtfCallEdge(targetSig, stmt);
    installLambdaCaptureEdges(expr, targetSig, implHandle.get().getKind());
  }

  private static Optional<MethodHandle> findImplMethodHandle(JDynamicInvokeExpr expr) {
    for (Value bsmArg : expr.getBootstrapArgs()) {
      if (bsmArg instanceof MethodHandle mh && mh.isMethodRef()) {
        return Optional.of(mh);
      }
    }
    return Optional.empty();
  }

  /**
   * Wires the indy site's own args (the lambda's captured locals) into the target method's
   * parameters. For a bound/unbound instance method reference (kind {@code REF_INVOKE_VIRTUAL} /
   * {@code _SPECIAL} / {@code _INTERFACE}), capture 0 is instead the receiver, mapping to the
   * target's {@code this} rather than a parameter -- static and constructor targets have no such
   * slot, since a constructor's own receiver is the object under construction, not one of the
   * captures.
   */
  private void installLambdaCaptureEdges(
      JDynamicInvokeExpr expr, MethodSignature targetSig, MethodHandle.Kind kind) {
    Optional<? extends SootMethod> targetOpt =
        view.getMethod(targetSig).filter(SootMethod::hasBody);
    if (targetOpt.isEmpty()) return;
    SootMethod target = targetOpt.get();

    int paramOffset = 0;
    if (isReceiverCapturing(kind) && expr.getArgCount() > 0) {
      val receiverNode = nodeFactory.createNode(expr.getArg(0), methodSignature);
      val thisLocal = findThisLocal(target);
      if (receiverNode.isPresent() && thisLocal.isPresent()) {
        val thisNode = nodeFactory.createNode(thisLocal.get(), targetSig);
        thisNode.ifPresent(node -> addPagEdge(receiverNode.get(), node));
      }
      paramOffset = 1;
    }

    for (int i = paramOffset; i < expr.getArgCount(); i++) {
      val argNode = nodeFactory.createNode(expr.getArg(i), methodSignature);
      val paramLocal = findParamLocal(target, i - paramOffset);
      if (argNode.isPresent() && paramLocal.isPresent()) {
        val paramNode = nodeFactory.createNode(paramLocal.get(), targetSig);
        paramNode.ifPresent(node -> addPagEdge(argNode.get(), node));
      }
    }
  }

  private static boolean isReceiverCapturing(MethodHandle.Kind kind) {
    return kind == MethodHandle.Kind.REF_INVOKE_VIRTUAL
        || kind == MethodHandle.Kind.REF_INVOKE_SPECIAL
        || kind == MethodHandle.Kind.REF_INVOKE_INTERFACE;
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

  private static final String JAVA_LANG_CLASS = "java.lang.Class";

  /**
   * Models {@code Class.forName(...)} as a reflective allocation, so that a follow-on {@code
   * newInstance()} / {@code getConstructor().newInstance()} chain (handled by {@code Solver}, which
   * has the points-to information needed to see it) can resolve to the target class instead of
   * vanishing like any other unresolvable reflective call.
   *
   * <p>When the argument is a compile-time string literal right here, resolves eagerly via {@link
   * #wireReflectiveClassToken}. Otherwise (the argument is a {@code Local} -- e.g. a parameter
   * forwarded from some caller, as with JAXP-style factory lookups), defers resolution as a {@link
   * PendingReflectiveForName}: {@code Solver.solveOnTheFly}'s resolution pass checks the argument's
   * points-to set on every fixpoint iteration, in case a {@code StringConstantNode} carrying a
   * resolvable literal reaches it from any caller, at any depth of parameter forwarding. Either
   * way, a computed name whose value can never be traced to a literal (e.g. read from a file, built
   * via string concatenation) is left exactly as unresolved as before this feature existed.
   */
  private void handleClassForName(
      AbstractInvokeExpr expr, MethodSignature targetSig, Optional<Value> lhs, InvokableStmt stmt) {
    if (lhs.isEmpty()) return;
    if (!targetSig.getDeclClassType().getFullyQualifiedName().equals(JAVA_LANG_CLASS)) return;
    if (!targetSig.getName().equals("forName")) return;
    if (expr.getArgCount() == 0) return;

    Value arg = expr.getArg(0);
    if (arg instanceof StringConstant nameConst) {
      wireReflectiveClassToken(nameConst.getValue(), lhs, expr.getType());
      return;
    }

    if (otfContext != null) {
      otfContext
          .otfPendingReflectiveForNameCalls()
          .add(new PendingReflectiveForName(methodSignature, arg, expr.getType(), lhs, stmt));
    }
  }

  /**
   * Builds+wires a {@link ReflectiveClassToken} for {@code className} to {@code lhs}, as if a
   * {@code Class.forName("literal")} call directly named this class -- shared by {@link
   * #handleClassForName}'s eager (literal-at-call-site) path and {@code Solver}'s deferred
   * (points-to-resolved-argument) resolution pass, so both produce identical PAG state. Public so
   * {@code Solver} can invoke it once a {@link PendingReflectiveForName} resolves.
   *
   * <p>Resolving {@code className} is deliberately defensive end-to-end, from parsing it into a
   * {@link ClassType} through resolving that type against the classpath: the deferred ({@link
   * PendingReflectiveForName}) path feeds this every {@code StringConstantNode} reaching a {@code
   * Class.forName} argument's points-to set, which -- under context-insensitive analysis -- can
   * include completely unrelated string literals merged in from other call sites of a shared method
   * (e.g. an empty string, or a namespace URI like {@code "http://www.w3.org/1999/XSL/Transform"},
   * happening to reach the same parameter as a real class name, if that parameter's containing
   * method is also called elsewhere with that value). Such strings are not valid class names, but
   * both parsing one (e.g. {@code org.apache.commons.lang3.ClassUtils.getShortClassName} indexing
   * into an empty string) and resolving one against the classpath (e.g. a platform-specific {@code
   * InvalidPathException} on Windows, for a name containing {@code ':'} or other reserved path
   * characters) can throw rather than simply reporting "not found" -- treated the same as a
   * genuinely absent class: no resolution, no crash.
   *
   * @return {@code false} (no-op) if {@code className} doesn't parse or resolve to a class present
   *     in the view, or {@code lhs} has no representable node -- {@code true} if a token was
   *     actually wired.
   */
  public boolean wireReflectiveClassToken(String className, Optional<Value> lhs, Type callType) {
    if (lhs.isEmpty()) return false;
    ClassType represented;
    try {
      represented = view.getIdentifierFactory().getClassType(className);
      if (view.getClass(represented).isEmpty()) return false;
    } catch (RuntimeException e) {
      return false;
    }

    Optional<sootup.spark.node.Node> lhsNode = nodeFactory.createNode(lhs.get(), methodSignature);
    if (lhsNode.isEmpty()) return false;

    ReflectiveClassToken.ReflectiveClassTokenBuilder<?, ?> builder =
        ReflectiveClassToken.builder()
            .type(callType)
            .containingMethodSig(methodSignature)
            .represented(represented);
    if (!PAG.getOptions().isTypesForSites()) {
      builder.allocationSite(Engine.incrementAndGetAllocCount());
    }
    addPagEdge(builder.build(), lhsNode.get());
    return true;
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
      val thisLocal = findThisLocal(sootMethod);
      if (baseNode.isPresent() && thisLocal.isPresent()) {
        val thisNode = nodeFactory.createNode(thisLocal.get(), targetMethodSig);
        thisNode.ifPresent(node -> addPagEdge(baseNode.get(), node));
      }
    }

    for (int i = 0; i < expr.getArgCount(); i++) {
      val argNode = nodeFactory.createNode(expr.getArg(i), methodSignature);
      val paramLocal = findParamLocal(sootMethod, i);
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

  static Optional<Local> findThisLocal(SootMethod sootMethod) {
    return sootMethod.getBody().getStmts().stream()
        .filter(s -> s instanceof JIdentityStmt)
        .map(s -> (JIdentityStmt) s)
        .filter(s -> s.getRightOp() instanceof JThisRef)
        .map(JIdentityStmt::getLeftOp)
        .findFirst();
  }

  private static Optional<Local> findParamLocal(SootMethod sootMethod, int index) {
    return sootMethod.getBody().getStmts().stream()
        .filter(s -> s instanceof JIdentityStmt)
        .map(s -> (JIdentityStmt) s)
        .filter(s -> s.getRightOp() instanceof JParameterRef)
        .filter(s -> ((JParameterRef) s.getRightOp()).getIndex() == index)
        .map(JIdentityStmt::getLeftOp)
        .findFirst();
  }
}
