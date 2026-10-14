package sootup.analysis.interprocedural.icfg;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Hazem Gamal and others
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import org.jspecify.annotations.NonNull;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.JThrowStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.core.views.View;
import sootup.java.core.exceptions.StmtExceptionAnalyzer;

/**
 * Computes the exceptions that a call may throw from the bodies of its callees, like Soot's
 * interprocedural throw analysis (<code>UnitThrowAnalysis.interproc()</code>): the exceptions
 * thrown by the statements of a callee, including those of the methods it calls, minus those it
 * catches itself.
 *
 * <ul>
 *   <li><code>throw x</code> throws exactly the allocated type if <code>x</code> is only assigned
 *       <code>new T</code>, otherwise the static type of <code>x</code> or any of its subtypes.
 *   <li>Every statement may throw its implicit exceptions (see {@link StmtExceptionAnalyzer}), or
 *       any of their subtypes.
 *   <li>A callee without a body (e.g., a native method) throws the exceptions in its <code>throws
 *       </code> clause, or any of their subtypes. Soot assumes it throws nothing. This is not
 *       sound: a native method may also throw unchecked exceptions it does not declare, e.g. <code>
 *       System.arraycopy</code> throws <code>IndexOutOfBoundsException</code>. Assuming any
 *       unchecked exception instead would keep nearly every handler of an unchecked exception,
 *       since creating an exception already reaches the native <code>Throwable.fillInStackTrace
 *       </code>.
 *   <li>A call whose callee is unknown (not in the view) may throw anything.
 * </ul>
 *
 * <p>The results are cached per method. The analysis is thread-safe.
 */
public class CallExceptionAnalysis {

  private static final int MAX_DEPTH = 200;

  /** A set of thrown exception types. */
  public static final class ThrownTypes {

    private final Map<ClassType, Boolean> types;

    private ThrownTypes(Map<ClassType, Boolean> types) {
      this.types = types;
    }

    /**
     * Returns whether an exception in this set can be caught by a handler of the given exception
     * type.
     */
    public boolean catchableAs(@NonNull ClassType handlerType, @NonNull TypeHierarchy hierarchy) {
      for (Map.Entry<ClassType, Boolean> entry : types.entrySet()) {
        ClassType type = entry.getKey();
        if (isSubtypeOrSame(handlerType, type, hierarchy, true)) {
          return true;
        }
        boolean exact = entry.getValue();
        if (!exact && isSubtypeOrSame(type, handlerType, hierarchy, true)) {
          return true;
        }
      }
      return false;
    }

    public boolean isEmpty() {
      return types.isEmpty();
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("{");
      for (Map.Entry<ClassType, Boolean> entry : types.entrySet()) {
        if (sb.length() > 1) {
          sb.append(", ");
        }
        sb.append(entry.getValue() ? "" : "AnySubType(").append(entry.getKey());
        sb.append(entry.getValue() ? "" : ")");
      }
      return sb.append("}").toString();
    }
  }

  private final View view;
  private final TypeHierarchy hierarchy;
  private final StmtExceptionAnalyzer stmtAnalyzer;
  private final ClassType throwable;
  private final BiFunction<MethodSignature, Stmt, Collection<MethodSignature>> callTargets;
  private final Map<MethodSignature, Map<ClassType, Boolean>> cache = new ConcurrentHashMap<>();

  /**
   * @param view the view holding the callees
   * @param callTargets the possible targets of a call statement in the given method, e.g., from a
   *     call graph. An empty collection means that the target is unknown and the invoked method is
   *     used instead.
   */
  public CallExceptionAnalysis(
      @NonNull View view,
      @NonNull BiFunction<MethodSignature, Stmt, Collection<MethodSignature>> callTargets) {
    this.view = view;
    this.hierarchy = view.getTypeHierarchy();
    this.stmtAnalyzer = new StmtExceptionAnalyzer(hierarchy);
    this.throwable = view.getIdentifierFactory().getClassType("java.lang.Throwable");
    this.callTargets = callTargets;
  }

  /**
   * Returns the exceptions that the given call statement in the given method may throw: those of
   * the call itself, e.g. a {@code NullPointerException} on a null receiver, and those of its
   * callees.
   */
  public ThrownTypes mightThrow(@NonNull MethodSignature caller, @NonNull Stmt call) {
    Map<ClassType, Boolean> result = new HashMap<>();
    addImplicitExceptions(result, call);
    addCallExceptions(result, caller, call, new Context());
    return new ThrownTypes(result);
  }

  /** Adds the exceptions that the statement may throw implicitly, or any of their subtypes. */
  private void addImplicitExceptions(Map<ClassType, Boolean> result, Stmt stmt) {
    try {
      for (ClassType implicit : stmtAnalyzer.mightThrowImplicitly(stmt).getExceptions()) {
        add(result, implicit, false);
      }
    } catch (RuntimeException e) {
      // e.g. an exception type that is not in the view
      add(result, throwable, false);
    }
  }

  /** The methods on the current path of the depth-first search. */
  private static final class Context {
    final Map<MethodSignature, Integer> onStack = new HashMap<>();

    /** the lowest stack position that a cut cycle or the depth limit referred to */
    int lowestCut = Integer.MAX_VALUE;
  }

  private void addCallExceptions(
      Map<ClassType, Boolean> result, MethodSignature caller, Stmt call, Context context) {
    Collection<MethodSignature> targets = callTargets.apply(caller, call);
    if (targets.isEmpty()) {
      targets =
          Collections.singletonList(
              call.asInvokableStmt().getInvokeExpr().get().getMethodSignature());
    }
    for (MethodSignature target : targets) {
      addAll(result, methodExceptions(target, context));
    }
  }

  private Map<ClassType, Boolean> methodExceptions(MethodSignature method, Context context) {
    Map<ClassType, Boolean> cached = cache.get(method);
    if (cached != null) {
      return cached;
    }
    Integer position = context.onStack.get(method);
    if (position != null) {
      // a recursive call: the exceptions of the method are collected where it was entered
      context.lowestCut = Math.min(context.lowestCut, position);
      return Collections.emptyMap();
    }
    int myPosition = context.onStack.size();
    if (myPosition >= MAX_DEPTH) {
      context.lowestCut = -1;
      return anything();
    }

    context.onStack.put(method, myPosition);
    int outerCut = context.lowestCut;
    context.lowestCut = Integer.MAX_VALUE;
    Map<ClassType, Boolean> result;
    try {
      result = computeMethodExceptions(method, context);
    } finally {
      context.onStack.remove(method);
    }
    // only cache results that do not depend on a method further up the stack
    if (context.lowestCut >= myPosition) {
      cache.put(method, result);
    }
    context.lowestCut = Math.min(outerCut, context.lowestCut);
    return result;
  }

  private Map<ClassType, Boolean> computeMethodExceptions(
      MethodSignature signature, Context context) {
    Optional<? extends SootMethod> methodOpt = view.getMethod(signature);
    if (!methodOpt.isPresent()) {
      return anything();
    }
    SootMethod method = methodOpt.get();
    Map<ClassType, Boolean> result = new HashMap<>();
    if (!method.hasBody()) {
      for (ClassType declared : method.getExceptionSignatures()) {
        add(result, declared, false);
      }
      return result;
    }

    Body body = method.getBody();
    ControlFlowGraph<?> graph = body.getControlFlowGraph();
    for (Stmt stmt : graph.getStmts()) {
      Map<ClassType, Boolean> stmtResult = new HashMap<>();
      addImplicitExceptions(stmtResult, stmt);
      if (stmt instanceof JThrowStmt) {
        addThrown(stmtResult, (JThrowStmt) stmt, graph);
      } else if (stmt.isInvokableStmt() && stmt.asInvokableStmt().getInvokeExpr().isPresent()) {
        addCallExceptions(stmtResult, signature, stmt, context);
      }
      // the exceptions that the method catches itself
      for (ClassType caught : graph.exceptionalSuccessors(stmt).keySet()) {
        stmtResult.keySet().removeIf(type -> isSubtypeOrSame(caught, type, hierarchy, false));
      }
      addAll(result, stmtResult);
    }
    return result;
  }

  private void addThrown(
      Map<ClassType, Boolean> result, JThrowStmt throwStmt, ControlFlowGraph<?> graph) {
    if (!(throwStmt.getOp() instanceof Local)) {
      add(result, throwable, false);
      return;
    }
    Local local = (Local) throwStmt.getOp();
    // throw x, where x is only assigned new T, throws exactly T
    Set<Type> allocated = new HashSet<>();
    boolean onlyAllocations = true;
    for (Stmt stmt : graph.getStmts()) {
      if (stmt instanceof AbstractDefinitionStmt
          && ((AbstractDefinitionStmt) stmt).getLeftOp() == local) {
        if (((AbstractDefinitionStmt) stmt).getRightOp() instanceof JNewExpr) {
          allocated.add(((AbstractDefinitionStmt) stmt).getRightOp().getType());
        } else {
          onlyAllocations = false;
        }
      }
    }
    if (onlyAllocations && !allocated.isEmpty()) {
      for (Type type : allocated) {
        if (type instanceof ClassType) {
          add(result, (ClassType) type, true);
        } else {
          add(result, throwable, false);
        }
      }
      return;
    }
    Type type = local.getType();
    add(result, type instanceof ClassType ? (ClassType) type : throwable, false);
  }

  private Map<ClassType, Boolean> anything() {
    return Collections.singletonMap(throwable, false);
  }

  private static void add(Map<ClassType, Boolean> result, ClassType type, boolean exact) {
    // a type that is thrown exactly and as any subtype is thrown as any subtype
    result.merge(type, exact, Boolean::logicalAnd);
  }

  private static void addAll(Map<ClassType, Boolean> result, Map<ClassType, Boolean> other) {
    other.forEach((type, exact) -> add(result, type, exact));
  }

  /**
   * Returns whether subtype is the same type as supertype or one of its subtypes, or ifUnknown if a
   * type is not in the view.
   */
  private static boolean isSubtypeOrSame(
      ClassType supertype, ClassType subtype, TypeHierarchy hierarchy, boolean ifUnknown) {
    if (supertype.equals(subtype)) {
      return true;
    }
    try {
      return hierarchy.isSubtype(supertype, subtype);
    } catch (RuntimeException e) {
      return ifUnknown;
    }
  }
}
