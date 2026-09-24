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

package qilin.core.invokedynamic;

import java.util.Collections;
import java.util.Set;
import qilin.core.PTAScene;
import qilin.core.effect.MethodEffectModel;
import qilin.core.pag.PAG;
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.MethodHandle;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;

/**
 * Resolves invokedynamic call sites bootstrapped by {@code java.lang.invoke.LambdaMetafactory}
 * whose target is a plain static method - every non-capturing lambda body and every static method
 * reference - by reading the target directly out of the bootstrap's {@link MethodHandle} constant.
 * Unlike reflection this needs no external trace/log - the compiler emits the exact target as
 * constant bootstrap data - so the resolution is sound, not heuristic. Splices in a synthetic
 * allocation of the functional-interface type (registered with {@link PAG#registerLambdaTarget} so
 * it becomes a {@link qilin.core.pag.LambdaAllocNode} instead of a plain one) before the original
 * invokedynamic statement, which is left untouched - mirroring how {@link
 * qilin.core.reflection.ReflectionModel} augments rather than replaces the original call.
 */
public class LambdaMetafactoryModel implements MethodEffectModel {
  private static final String LAMBDA_METAFACTORY = "java.lang.invoke.LambdaMetafactory";
  private static final Set<String> BOOTSTRAP_METHOD_NAMES = Set.of("metafactory", "altMetafactory");

  private final PTAScene ptaScene;
  private final PAG pag;

  public LambdaMetafactoryModel(PTAScene ptaScene, PAG pag) {
    this.ptaScene = ptaScene;
    this.pag = pag;
  }

  @Override
  public boolean appliesTo(SootMethod m) {
    return m.isConcrete();
  }

  @Override
  public void apply(SootMethod m) {
    if (!ptaScene.dynamicInvokeBuilt.add(m)) {
      return;
    }
    Body body = pag.getMethodBody(m);
    Body.BodyBuilder builder = null;
    for (Stmt u : body.getStmts()) {
      if (!(u instanceof JAssignStmt assign)) {
        continue;
      }
      if (!(assign.getRightOp() instanceof JDynamicInvokeExpr die)) {
        continue;
      }
      if (!isLambdaMetafactoryBootstrap(die)) {
        continue;
      }
      if (!(assign.getLeftOp().getType() instanceof ClassType functionalInterfaceType)) {
        continue;
      }
      if (die.getArgCount() > 0) {
        // Captured (closure) lambda/method-ref: the target's parameter list is offset by the
        // captured values bound at invokedynamic time, which aren't modeled here yet - skip
        // rather than wire an edge with the wrong arity. Documented gap, not a correctness bug:
        // this call site simply keeps today's (empty) points-to result for it.
        continue;
      }
      MethodHandle implHandle = findImplMethodHandle(die);
      if (implHandle == null || implHandle.getKind() != MethodHandle.Kind.REF_INVOKE_STATIC) {
        // Only plain static targets are redirected here: this covers every non-capturing lambda
        // body (javac always compiles those to a private static synthetic method) and explicit
        // static method references. Instance/virtual/interface/special refs need the SAM's own
        // argument as the receiver (not this lambda object) and constructor refs need a fresh
        // allocation + <init> call - both are a different edge shape, not handled here yet.
        // Documented gap, not a correctness bug: these call sites keep today's (empty) result.
        continue;
      }
      MethodSignature target = (MethodSignature) implHandle.getReferenceSignature();
      JNewExpr syntheticAlloc = new JNewExpr(functionalInterfaceType);
      pag.registerLambdaTarget(syntheticAlloc, target, implHandle.getKind());
      if (builder == null) {
        builder = Body.builder(body, Collections.emptySet());
      }
      MutableControlFlowGraph cfg = builder.getControlFlowGraph();
      cfg.insertBefore(
          u,
          new JAssignStmt(
              assign.getLeftOp(), syntheticAlloc, StmtPositionInfo.getNoStmtPositionInfo()));
    }
    if (builder != null) {
      pag.updateMethodBody(m, builder.build());
    }
  }

  private boolean isLambdaMetafactoryBootstrap(JDynamicInvokeExpr die) {
    MethodSignature bsm = die.getBootstrapMethodSignature();
    return bsm.getDeclClassType().getFullyQualifiedName().equals(LAMBDA_METAFACTORY)
        && BOOTSTRAP_METHOD_NAMES.contains(bsm.getName());
  }

  /** Picks the first {@link MethodHandle} bootstrap arg - the lambda's implementation method. */
  private MethodHandle findImplMethodHandle(JDynamicInvokeExpr die) {
    for (int i = 0; i < die.getBootstrapArgCount(); i++) {
      if (die.getBootstrapArg(i) instanceof MethodHandle mh) {
        return mh;
      }
    }
    return null;
  }
}
