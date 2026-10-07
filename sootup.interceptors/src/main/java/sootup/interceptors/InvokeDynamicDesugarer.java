package sootup.interceptors;

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
import java.util.LinkedHashSet;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.jimple.basic.LocalGenerator;
import sootup.core.jimple.common.LValue;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.views.View;
import sootup.interceptors.invokedynamic.Fragment;
import sootup.interceptors.invokedynamic.InvokeDynamicDesugarizer;
import sootup.interceptors.invokedynamic.RecordMethodsDesugarizer;
import sootup.interceptors.invokedynamic.StringConcatDesugarizer;

/**
 * Replaces invokedynamic statements by equivalent plain Jimple, one {@link
 * InvokeDynamicDesugarizer} per kind of bootstrap - by default string concatenation and record
 * methods. Not part of the default interceptors: the body then no longer mirrors the bytecode.
 * Lambdas stay invokedynamics (they would need synthetic classes).
 */
public class InvokeDynamicDesugarer implements BodyInterceptor {

  /** {@link StringConcatDesugarizer} and {@link RecordMethodsDesugarizer}. */
  public static final List<InvokeDynamicDesugarizer> DEFAULT_DESUGARIZERS =
      List.of(new StringConcatDesugarizer(), new RecordMethodsDesugarizer());

  @NonNull private final List<InvokeDynamicDesugarizer> desugarizers;

  public InvokeDynamicDesugarer() {
    this(DEFAULT_DESUGARIZERS);
  }

  public InvokeDynamicDesugarer(@NonNull Collection<InvokeDynamicDesugarizer> desugarizers) {
    this.desugarizers = List.copyOf(desugarizers);
  }

  /** Whether {@link #interceptBody} would change a body with these statements. */
  public boolean appliesTo(@NonNull Collection<Stmt> stmts) {
    return stmts.stream().anyMatch(stmt -> desugarizer(stmt) != null);
  }

  @Override
  public void interceptBody(Body.@NonNull BodyBuilder builder, @NonNull View view) {
    MutableControlFlowGraph cfg = builder.getControlFlowGraph();
    LocalGenerator locals = new LocalGenerator(new LinkedHashSet<>(builder.getLocals()));
    for (Stmt stmt : new ArrayList<>(cfg.getStmts())) {
      InvokeDynamicDesugarizer desugarizer = desugarizer(stmt);
      if (desugarizer == null || cfg.successors(stmt).size() != 1) {
        continue;
      }
      JDynamicInvokeExpr expr = dynamicInvoke(stmt);
      LValue result = stmt instanceof JAssignStmt assign ? assign.getLeftOp() : null;
      Fragment code = Fragment.of(view, locals, stmt.getPositionInfo());
      if (desugarizer.desugar(expr, result, code)) {
        code.replace(builder, stmt);
      }
    }
  }

  @Nullable
  private InvokeDynamicDesugarizer desugarizer(Stmt stmt) {
    JDynamicInvokeExpr expr = dynamicInvoke(stmt);
    if (expr == null) {
      return null;
    }
    return desugarizers.stream().filter(d -> d.applies(expr)).findFirst().orElse(null);
  }

  @Nullable
  private static JDynamicInvokeExpr dynamicInvoke(Stmt stmt) {
    if (stmt.isInvokableStmt()
        && stmt.asInvokableStmt().getInvokeExpr().orElse(null) instanceof JDynamicInvokeExpr e) {
      return e;
    }
    return null;
  }
}
