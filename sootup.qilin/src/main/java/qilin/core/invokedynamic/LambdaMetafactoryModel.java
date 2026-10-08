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
import qilin.core.PTAScene;
import qilin.core.effect.MethodEffectModel;
import qilin.core.pag.PAG;
import sootup.callgraph.invokedynamic.DynamicInvokeResolver;
import sootup.callgraph.invokedynamic.DynamicInvokeTarget;
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.MethodHandle;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.types.ClassType;

/**
 * Precisely models invokedynamic call sites whose {@link DynamicInvokeResolver} target is a
 * non-capturing static {@code LambdaMetafactory} implementation - every non-capturing lambda body
 * and every static method reference. Splices in a synthetic allocation of the functional-interface
 * type (registered with {@link PAG#registerLambdaTarget} so it becomes a {@link
 * qilin.core.pag.LambdaAllocNode} instead of a plain one) before the original invokedynamic
 * statement, which is left untouched - mirroring how {@link
 * sootup.callgraph.reflection.ReflectionModel} augments rather than replaces the original call.
 * Calls on that object then dispatch straight to the target, with the functional interface's
 * arguments bound.
 *
 * <p>All other targets (captured values, instance/constructor refs) are called from the
 * invokedynamic statement itself, like CHA/RTA/Spark do - see {@code
 * CallGraphBuilder#addDynamicInvokeEdge}.
 */
public class LambdaMetafactoryModel implements MethodEffectModel {

  private final PTAScene ptaScene;
  private final PAG pag;
  private final DynamicInvokeResolver resolver;

  public LambdaMetafactoryModel(PTAScene ptaScene, PAG pag, DynamicInvokeResolver resolver) {
    this.ptaScene = ptaScene;
    this.pag = pag;
    this.resolver = resolver;
  }

  /**
   * Whether this model handles {@code target} of the invokedynamic {@code stmt}: a non-capturing
   * static lambda implementation whose functional object is assigned to a local.
   */
  public static boolean handles(InvokableStmt stmt, DynamicInvokeTarget target) {
    // Captured (closure) lambdas offset the target's parameters by the captured values, and
    // instance/constructor refs need a different edge shape (receiver from the SAM's own argument
    // or a fresh allocation) - those go through the invokedynamic-statement edge instead.
    return stmt instanceof JAssignStmt assign
        && assign.getLeftOp().getType() instanceof ClassType
        && assign.getRightOp() instanceof JDynamicInvokeExpr die
        && die.getArgCount() == 0
        && target.lambdaImplementation()
        && target.kind() == MethodHandle.Kind.REF_INVOKE_STATIC;
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
      if (!(u instanceof JAssignStmt assign)
          || !(assign.getRightOp() instanceof JDynamicInvokeExpr die)) {
        continue;
      }
      for (DynamicInvokeTarget target : resolver.resolve(die)) {
        if (!handles(assign, target)) {
          continue;
        }
        JNewExpr syntheticAlloc = new JNewExpr((ClassType) assign.getLeftOp().getType());
        pag.registerLambdaTarget(syntheticAlloc, target.method(), target.kind());
        if (builder == null) {
          builder = Body.builder(body, Collections.emptySet());
        }
        MutableControlFlowGraph cfg = builder.getControlFlowGraph();
        cfg.insertBefore(
            u,
            new JAssignStmt(
                assign.getLeftOp(), syntheticAlloc, StmtPositionInfo.getNoStmtPositionInfo()));
      }
    }
    if (builder != null) {
      pag.updateMethodBody(m, builder.build());
    }
  }
}
