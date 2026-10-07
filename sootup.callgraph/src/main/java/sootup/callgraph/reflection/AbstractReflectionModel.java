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

package sootup.callgraph.reflection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.NonNull;
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.views.View;

/**
 * Base for {@link ReflectionModel}s: dispatches each reflective call site to a per-API hook and
 * splices the returned stmts in front of it (the original call stays). Rewritten bodies are cached
 * per method signature, so one instance must only be used with one {@link View}; the first body
 * passed for a method wins.
 */
public abstract class AbstractReflectionModel implements ReflectionModel {
  public static final String SIG_FOR_NAME =
      "<java.lang.Class: java.lang.Class forName(java.lang.String)>";
  public static final String SIG_FOR_NAME2 =
      "<java.lang.Class: java.lang.Class forName(java.lang.String,boolean,java.lang.ClassLoader)>";
  public static final String SIG_CLASS_NEW_INSTANCE =
      "<java.lang.Class: java.lang.Object newInstance()>";
  public static final String SIG_CONSTRUCTOR_NEW_INSTANCE =
      "<java.lang.reflect.Constructor: java.lang.Object newInstance(java.lang.Object[])>";
  public static final String SIG_METHOD_INVOKE =
      "<java.lang.reflect.Method: java.lang.Object invoke(java.lang.Object,java.lang.Object[])>";
  public static final String SIG_FIELD_SET =
      "<java.lang.reflect.Field: void set(java.lang.Object,java.lang.Object)>";
  public static final String SIG_FIELD_GET =
      "<java.lang.reflect.Field: java.lang.Object get(java.lang.Object)>";
  public static final String SIG_ARRAY_NEW_INSTANCE =
      "<java.lang.reflect.Array: java.lang.Object newInstance(java.lang.Class,int)>";
  public static final String SIG_ARRAY_GET =
      "<java.lang.reflect.Array: java.lang.Object get(java.lang.Object,int)>";
  public static final String SIG_ARRAY_SET =
      "<java.lang.reflect.Array: void set(java.lang.Object,int,java.lang.Object)>";

  @NonNull protected final View view;
  private final Map<MethodSignature, Body> resolved = new ConcurrentHashMap<>();

  protected AbstractReflectionModel(@NonNull View view) {
    this.view = view;
  }

  /** The method being rewritten, its original body, and the builder for the rewritten one. */
  protected record MethodContext(SootMethod method, Body body, Body.BodyBuilder builder) {}

  @NonNull
  @Override
  public Body resolve(@NonNull SootMethod method, @NonNull Body body) {
    return resolved.computeIfAbsent(method.getSignature(), k -> transformBody(method, body));
  }

  private Body transformBody(SootMethod method, Body body) {
    if (body.getStmts().stream().noneMatch(AbstractReflectionModel::isReflectiveCall)) {
      return body; // common case: skip copying the CFG
    }
    Body.BodyBuilder builder = Body.builder(body, Collections.emptySet());
    MethodContext ctx = new MethodContext(method, body, builder);
    Map<Stmt, List<Stmt>> newUnits = new LinkedHashMap<>();
    for (Stmt u : body.getStmts()) {
      if (u.isInvokableStmt() && u.asInvokableStmt().getInvokeExpr().isPresent()) {
        List<Stmt> added = transform(ctx, u.asInvokableStmt());
        if (!added.isEmpty()) {
          newUnits.put(u, added);
        }
      }
    }
    if (newUnits.isEmpty()) {
      return body;
    }
    MutableControlFlowGraph cfg = builder.getControlFlowGraph();
    newUnits.forEach(
        (unit, added) -> {
          for (Stmt s : added) {
            cfg.insertBefore(unit, (FallsThroughStmt) s);
          }
        });
    return builder.build();
  }

  private static boolean isReflectiveCall(Stmt s) {
    return s.isInvokableStmt()
        && s.asInvokableStmt()
            .getInvokeExpr()
            .map(ie -> kindOf(ie.getMethodSignature().toString()) != null)
            .orElse(false);
  }

  /** Kind of reflective API {@code methodSig} (as {@code toString()}) is, or {@code null}. */
  protected static ReflectionKind kindOf(String methodSig) {
    return switch (methodSig) {
      case SIG_FOR_NAME, SIG_FOR_NAME2 -> ReflectionKind.ClassForName;
      case SIG_CLASS_NEW_INSTANCE -> ReflectionKind.ClassNewInstance;
      case SIG_CONSTRUCTOR_NEW_INSTANCE -> ReflectionKind.ConstructorNewInstance;
      case SIG_METHOD_INVOKE -> ReflectionKind.MethodInvoke;
      case SIG_FIELD_SET -> ReflectionKind.FieldSet;
      case SIG_FIELD_GET -> ReflectionKind.FieldGet;
      case SIG_ARRAY_NEW_INSTANCE -> ReflectionKind.ArrayNewInstance;
      case SIG_ARRAY_GET -> ReflectionKind.ArrayGet;
      case SIG_ARRAY_SET -> ReflectionKind.ArraySet;
      default -> null;
    };
  }

  private List<Stmt> transform(MethodContext ctx, InvokableStmt s) {
    AbstractInvokeExpr ie = s.getInvokeExpr().get();
    ReflectionKind kind = kindOf(ie.getMethodSignature().toString());
    if (kind == null) {
      return Collections.emptyList();
    }
    return switch (kind) {
      case ClassForName -> transformClassForName(ctx, s);
      case ClassNewInstance -> transformClassNewInstance(ctx, s);
      case ConstructorNewInstance -> transformConstructorNewInstance(ctx, s);
      case MethodInvoke -> transformMethodInvoke(ctx, s);
      case FieldSet -> transformFieldSet(ctx, s);
      case FieldGet -> transformFieldGet(ctx, s);
      case ArrayNewInstance -> transformArrayNewInstance(ctx, s);
      case ArrayGet -> transformArrayGet(ctx, s);
      case ArraySet -> transformArraySet(ctx, s);
      default -> Collections.emptyList();
    };
  }

  // each hook returns the stmts (JAssignStmt/JInvokeStmt) to insert before s, in order

  protected abstract List<Stmt> transformClassForName(MethodContext ctx, InvokableStmt s);

  protected abstract List<Stmt> transformClassNewInstance(MethodContext ctx, InvokableStmt s);

  protected abstract List<Stmt> transformConstructorNewInstance(MethodContext ctx, InvokableStmt s);

  protected abstract List<Stmt> transformMethodInvoke(MethodContext ctx, InvokableStmt s);

  protected abstract List<Stmt> transformFieldSet(MethodContext ctx, InvokableStmt s);

  protected abstract List<Stmt> transformFieldGet(MethodContext ctx, InvokableStmt s);

  protected abstract List<Stmt> transformArrayNewInstance(MethodContext ctx, InvokableStmt s);

  protected abstract List<Stmt> transformArrayGet(MethodContext ctx, InvokableStmt s);

  protected abstract List<Stmt> transformArraySet(MethodContext ctx, InvokableStmt s);
}
