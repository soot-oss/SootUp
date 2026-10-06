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
import sootup.core.jimple.common.constant.MethodHandle;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.core.views.View;

/**
 * Makes the calls other invokedynamic bootstraps perform implicitly explicit, as statements in
 * front of the invokedynamic (which stays):
 *
 * <ul>
 *   <li>{@code StringConcatFactory}: {@code a.toString()} for each object argument that is not a
 *       {@code String}.
 *   <li>{@code ObjectMethods} (a record's {@code toString}/{@code hashCode}/{@code equals}): the
 *       same method on each object component, for {@code equals} with the other record's component
 *       as argument.
 * </ul>
 *
 * Calls are declared by the argument's / component's static type, so class hierarchy based dispatch
 * finds the overriding methods.
 */
final class InvokeDynamicDesugaring {

  private static final String STRING_CONCAT_FACTORY = "java.lang.invoke.StringConcatFactory";
  private static final String OBJECT_METHODS = "java.lang.runtime.ObjectMethods";

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

  /** Whether {@link #desugar} changes a body containing {@code stmt}. */
  static boolean applies(@NonNull Stmt stmt) {
    JDynamicInvokeExpr expr = dynamicInvoke(stmt);
    if (expr == null) {
      return false;
    }
    String bootstrapClass =
        expr.getBootstrapMethodSignature().getDeclClassType().getFullyQualifiedName();
    return bootstrapClass.equals(STRING_CONCAT_FACTORY) || bootstrapClass.equals(OBJECT_METHODS);
  }

  /** {@code body} with the implicit calls made explicit; {@code body} itself if there are none. */
  @NonNull
  static Body desugar(@NonNull Body body, @NonNull View view) {
    return new InvokeDynamicDesugaring(view, body).run(body);
  }

  @Nullable
  private static JDynamicInvokeExpr dynamicInvoke(Stmt stmt) {
    if (stmt.isInvokableStmt()
        && stmt.asInvokableStmt().getInvokeExpr().orElse(null) instanceof JDynamicInvokeExpr e) {
      return e;
    }
    return null;
  }

  private Body run(Body body) {
    Map<Stmt, List<Stmt>> added = new LinkedHashMap<>();
    for (Stmt stmt : body.getStmts()) {
      if (!applies(stmt)) {
        continue;
      }
      JDynamicInvokeExpr expr = dynamicInvoke(stmt);
      StmtPositionInfo pos = stmt.getPositionInfo();
      String bootstrapClass =
          expr.getBootstrapMethodSignature().getDeclClassType().getFullyQualifiedName();
      List<Stmt> calls =
          bootstrapClass.equals(STRING_CONCAT_FACTORY)
              ? concatenation(expr, pos)
              : recordMethod(expr, pos);
      if (!calls.isEmpty()) {
        added.put(stmt, calls);
      }
    }
    if (added.isEmpty()) {
      return body;
    }
    MutableControlFlowGraph cfg = builder.getControlFlowGraph();
    added.forEach(
        (stmt, calls) -> calls.forEach(call -> cfg.insertBefore(stmt, (FallsThroughStmt) call)));
    return builder.build();
  }

  /** {@code a.toString()} for each non-{@code String} object argument {@code a}. */
  private List<Stmt> concatenation(JDynamicInvokeExpr expr, StmtPositionInfo pos) {
    List<Stmt> calls = new ArrayList<>();
    Type string = expr.getType();
    for (int i = 0; i < expr.getArgCount(); i++) {
      if (expr.getArg(i) instanceof Local arg
          && expr.getMethodSignature().getParameterType(i) instanceof ClassType type
          && !type.equals(string)) {
        calls.add(call(arg, type, "toString", string, List.of(), List.of(), pos));
      }
    }
    return calls;
  }

  /**
   * For each object component {@code f} of the record (the {@code REF_GET_FIELD} bootstrap
   * arguments): {@code this.f.toString()}, {@code this.f.hashCode()} or {@code this.f.equals(((R)
   * other).f)}, matching the invokedynamic's name.
   */
  private List<Stmt> recordMethod(JDynamicInvokeExpr expr, StmtPositionInfo pos) {
    String name = expr.getMethodSignature().getName();
    if (expr.getArgCount() == 0 || !(expr.getArg(0) instanceof Local self)) {
      return List.of();
    }
    boolean equals = name.equals("equals");
    if (!equals && !name.equals("toString") && !name.equals("hashCode")) {
      return List.of();
    }
    if (equals && (expr.getArgCount() < 2 || !(expr.getArg(1) instanceof Local))) {
      return List.of();
    }
    List<Stmt> stmts = new ArrayList<>();
    Local other = null;
    for (Immediate arg : expr.getBootstrapArgs()) {
      if (!(arg instanceof MethodHandle handle)
          || handle.getKind() != MethodHandle.Kind.REF_GET_FIELD
          || !(handle.getReferenceSignature() instanceof FieldSignature field)
          || !(field.getType() instanceof ClassType type)) {
        continue;
      }
      Local value = newLocal(type);
      stmts.add(Jimple.newAssignStmt(value, Jimple.newInstanceFieldRef(self, field), pos));
      if (!equals) {
        stmts.add(call(value, type, name, expr.getType(), List.of(), List.of(), pos));
        continue;
      }
      if (other == null) {
        ClassType record = field.getDeclClassType();
        other = newLocal(record);
        stmts.add(Jimple.newAssignStmt(other, Jimple.newCastExpr(expr.getArg(1), record), pos));
      }
      Local otherValue = newLocal(type);
      stmts.add(Jimple.newAssignStmt(otherValue, Jimple.newInstanceFieldRef(other, field), pos));
      Type object = expr.getMethodSignature().getParameterType(1);
      stmts.add(call(value, type, name, expr.getType(), List.of(object), List.of(otherValue), pos));
    }
    return stmts;
  }

  private Local newLocal(Type type) {
    Local local = locals.generateLocal(type);
    builder.addLocal(local);
    return local;
  }

  private Stmt call(
      Local base,
      ClassType declaringType,
      String name,
      Type returnType,
      List<Type> parameterTypes,
      List<Immediate> args,
      StmtPositionInfo pos) {
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
