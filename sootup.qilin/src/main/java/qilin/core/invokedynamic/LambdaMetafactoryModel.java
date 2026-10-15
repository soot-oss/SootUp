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
import sootup.callgraph.invokedynamic.FunctionalObject;
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.signatures.FieldSignature;
import sootup.core.types.ReferenceType;
import sootup.core.views.View;

/**
 * Models the objects {@code LambdaMetafactory} invokedynamic call sites create (lambdas and method
 * references, see {@link FunctionalObject}). Before each such call site {@code r = indy(c_0, ...)},
 * splices in a synthetic allocation of the functional-interface type (registered with {@link
 * PAG#registerLambdaTarget} so it becomes a {@link qilin.core.pag.LambdaAllocNode}) and stores the
 * captured values into it: {@code r = new FI; r.<capture field 0> = c_0; ...}. The original
 * statement is left untouched - mirroring how {@link sootup.callgraph.reflection.ReflectionModel}
 * augments rather than replaces the original call.
 *
 * <p>First {@link DynamicInvokeResolver#desugar desugars} the body (string concatenation, record
 * methods).
 *
 * <p>Calls on the object dispatch to the implementation ({@code CallGraphBuilder}), loading the
 * captured values back from the receiver - so they stay with their object under context
 * sensitivity.
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
   * The synthetic field holding the {@code i}-th value captured for {@code fo}: declared by the
   * functional interface, named after the implementation so different lambdas keep their captures
   * apart.
   */
  public static FieldSignature captureField(View view, FunctionalObject fo, int i) {
    JDynamicInvokeExpr expr = (JDynamicInvokeExpr) ((JAssignStmt) fo.site()).getRightOp();
    return view.getIdentifierFactory()
        .getFieldSignature(
            "capture$" + i + "$" + fo.implementationMethod().getName(),
            fo.functionalInterface(),
            expr.getArg(i).getType());
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
    View view = ptaScene.getView();
    Body original = pag.getMethodBody(m);
    Body body = resolver.desugar(m, original, view);
    Body.BodyBuilder builder = null;
    for (Stmt u : body.getStmts()) {
      FunctionalObject fo = FunctionalObject.of(u, resolver, view).orElse(null);
      if (fo == null || !(((JAssignStmt) u).getLeftOp() instanceof Local lhs)) {
        continue;
      }
      JNewExpr syntheticAlloc = new JNewExpr(fo.functionalInterface());
      pag.registerLambdaTarget(syntheticAlloc, fo);
      if (builder == null) {
        builder = Body.builder(body, Collections.emptySet());
      }
      MutableControlFlowGraph cfg = builder.getControlFlowGraph();
      StmtPositionInfo pos = StmtPositionInfo.getNoStmtPositionInfo();
      cfg.insertBefore(u, new JAssignStmt(lhs, syntheticAlloc, pos));
      JDynamicInvokeExpr expr = (JDynamicInvokeExpr) ((JAssignStmt) u).getRightOp();
      for (int i = 0; i < expr.getArgCount(); i++) {
        Immediate arg = expr.getArg(i);
        if (arg.getType() instanceof ReferenceType) {
          JInstanceFieldRef field = new JInstanceFieldRef(lhs, captureField(view, fo, i));
          cfg.insertBefore(u, new JAssignStmt(field, arg, pos));
        }
      }
    }
    if (builder != null) {
      pag.updateMethodBody(m, builder.build());
    } else if (body != original) {
      pag.updateMethodBody(m, body);
    }
  }
}
