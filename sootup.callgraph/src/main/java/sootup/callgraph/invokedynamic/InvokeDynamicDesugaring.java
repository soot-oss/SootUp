package sootup.callgraph.invokedynamic;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Markus Schmidt
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.IdentifierFactory;
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.LocalGenerator;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.core.views.View;

/**
 * Rewrites a body with a collection of {@link InvokeDynamicDesugarizer}s: inserts the statements
 * each applying desugarizer returns in front of its invokedynamic.
 */
final class InvokeDynamicDesugaring implements InvokeDynamicDesugarizer.Context {

  /** String concatenation and record methods. */
  static final List<InvokeDynamicDesugarizer> DEFAULT =
      List.of(new StringConcatDesugarizer(), new RecordMethodsDesugarizer());

  private final View view;
  private final IdentifierFactory factory;
  private final Body.BodyBuilder builder;
  private final LocalGenerator locals;

  private InvokeDynamicDesugaring(View view, Body body) {
    this.view = view;
    this.factory = view.getIdentifierFactory();
    this.builder = Body.builder(body, Collections.emptySet());
    this.locals = new LocalGenerator(new LinkedHashSet<>(body.getLocals()));
  }

  /** Whether one of {@code desugarizers} applies to {@code stmt}. */
  static boolean applies(
      @NonNull Stmt stmt, @NonNull Collection<InvokeDynamicDesugarizer> desugarizers) {
    JDynamicInvokeExpr expr = dynamicInvoke(stmt);
    return expr != null && desugarizers.stream().anyMatch(d -> d.applies(expr));
  }

  /** {@code body} with the implicit calls made explicit; {@code body} itself if there are none. */
  @NonNull
  static Body desugar(
      @NonNull Body body,
      @NonNull View view,
      @NonNull Collection<InvokeDynamicDesugarizer> desugarizers) {
    return new InvokeDynamicDesugaring(view, body).run(body, desugarizers);
  }

  @Nullable
  private static JDynamicInvokeExpr dynamicInvoke(Stmt stmt) {
    if (stmt.isInvokableStmt()
        && stmt.asInvokableStmt().getInvokeExpr().orElse(null) instanceof JDynamicInvokeExpr e) {
      return e;
    }
    return null;
  }

  private Body run(Body body, Collection<InvokeDynamicDesugarizer> desugarizers) {
    Map<Stmt, List<Stmt>> added = new LinkedHashMap<>();
    for (Stmt stmt : body.getStmts()) {
      JDynamicInvokeExpr expr = dynamicInvoke(stmt);
      if (expr == null) {
        continue;
      }
      List<Stmt> stmts = new ArrayList<>();
      for (InvokeDynamicDesugarizer desugarizer : desugarizers) {
        if (desugarizer.applies(expr)) {
          stmts.addAll(desugarizer.desugar(expr, stmt.getPositionInfo(), this));
        }
      }
      if (!stmts.isEmpty()) {
        added.put(stmt, stmts);
      }
    }
    if (added.isEmpty()) {
      return body;
    }
    MutableControlFlowGraph cfg = builder.getControlFlowGraph();
    added.forEach(
        (stmt, stmts) -> stmts.forEach(s -> cfg.insertBefore(stmt, (FallsThroughStmt) s)));
    return builder.build();
  }

  @NonNull
  @Override
  public Local newLocal(@NonNull Type type) {
    Local local = locals.generateLocal(type);
    builder.addLocal(local);
    return local;
  }

  @NonNull
  @Override
  public Stmt call(
      @NonNull Local base,
      @NonNull ClassType declaringType,
      @NonNull String name,
      @NonNull Type returnType,
      @NonNull List<Type> parameterTypes,
      @NonNull List<Immediate> args,
      @NonNull StmtPositionInfo pos) {
    MethodSignature sig =
        factory.getMethodSignature(declaringType, name, returnType, parameterTypes);
    boolean isInterface = view.getClass(declaringType).map(SootClass::isInterface).orElse(false);
    AbstractInvokeExpr invoke =
        isInterface
            ? Jimple.newInterfaceInvokeExpr(base, sig, args)
            : Jimple.newVirtualInvokeExpr(base, sig, args);
    return Jimple.newInvokeStmt(invoke, pos);
  }
}
