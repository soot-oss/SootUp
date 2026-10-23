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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import sootup.callgraph.AbstractCallGraphAlgorithm;
import sootup.core.IdentifierFactory;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.constant.ClassConstant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.MethodHandle;
import sootup.core.jimple.common.constant.MethodType;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * The object a {@code LambdaMetafactory} call site creates (lambda / method reference): an
 * anonymous implementation of {@link #functionalInterface()} whose single abstract method {@link
 * #sam()} forwards to {@link #implementation()}. Algorithms dispatch calls on it like on any
 * object: a call that {@link #answers} it reaches the implementation, with arguments placed by
 * {@link #samArgumentIndex(int)}.
 *
 * @param site the invokedynamic statement creating the object
 * @param functionalInterface the type of the created object
 * @param markerInterfaces additional interfaces ({@code altMetafactory} {@code FLAG_MARKERS})
 * @param sam the functional method, plus {@code bridges} ({@code FLAG_BRIDGES})
 * @param implementation the lambda body / referenced method
 * @param captureCount number of values the call site captures (its argument count)
 */
public record FunctionalObject(
    @NonNull Stmt site,
    @NonNull ClassType functionalInterface,
    @NonNull Set<ClassType> markerInterfaces,
    @NonNull MethodSubSignature sam,
    @NonNull Set<MethodSubSignature> bridges,
    @NonNull DynamicInvokeTarget implementation,
    int captureCount) {

  private static final int FLAG_MARKERS = 1 << 1;
  private static final int FLAG_BRIDGES = 1 << 2;

  /**
   * The object {@code stmt} creates, if it assigns a {@code LambdaMetafactory} invokedynamic whose
   * implementation {@code resolver} reports.
   */
  @NonNull
  public static Optional<FunctionalObject> of(
      @NonNull Stmt stmt, @NonNull DynamicInvokeResolver resolver, @NonNull View view) {
    if (!(stmt instanceof JAssignStmt assign)
        || !(assign.getRightOp() instanceof JDynamicInvokeExpr expr)
        || !(expr.getType() instanceof ClassType functionalInterface)
        || !BootstrapMethodHandleResolver.isLambdaMetafactory(expr)) {
      return Optional.empty();
    }
    List<Immediate> args = expr.getBootstrapArgs();
    if (args.size() < 3 || !(args.get(0) instanceof MethodType samType)) {
      return Optional.empty();
    }
    Optional<DynamicInvokeTarget> implementation =
        resolver.resolve(expr).stream()
            .filter(DynamicInvokeTarget::lambdaImplementation)
            .findFirst();
    if (implementation.isEmpty()) {
      return Optional.empty();
    }
    IdentifierFactory factory = view.getIdentifierFactory();
    String name = expr.getMethodSignature().getName();
    Set<ClassType> markers = Set.of();
    Set<MethodSubSignature> bridges = Set.of();
    // altMetafactory: samType, impl, instantiatedType, flags, [n, markers...], [n, bridges...]
    if (args.size() > 3 && args.get(3) instanceof IntConstant flags) {
      int i = 4;
      if ((flags.getValue() & FLAG_MARKERS) != 0 && args.get(i) instanceof IntConstant n) {
        List<ClassType> list = new ArrayList<>();
        for (int k = 0; k < n.getValue(); k++) {
          if (args.get(i + 1 + k) instanceof ClassConstant c) {
            String desc = c.getValue(); // Lpkg/Name;
            String name0 = desc.startsWith("L") ? desc.substring(1, desc.length() - 1) : desc;
            list.add(factory.getClassType(name0.replace('/', '.')));
          }
        }
        markers = Set.copyOf(list);
        i += 1 + n.getValue();
      }
      if ((flags.getValue() & FLAG_BRIDGES) != 0 && args.get(i) instanceof IntConstant n) {
        List<MethodSubSignature> list = new ArrayList<>();
        for (int k = 0; k < n.getValue(); k++) {
          if (args.get(i + 1 + k) instanceof MethodType bridge) {
            list.add(subSignature(factory, name, bridge));
          }
        }
        bridges = Set.copyOf(list);
      }
    }
    return Optional.of(
        new FunctionalObject(
            stmt,
            functionalInterface,
            markers,
            subSignature(factory, name, samType),
            bridges,
            implementation.get(),
            expr.getArgCount()));
  }

  private static MethodSubSignature subSignature(
      IdentifierFactory factory, String name, MethodType type) {
    return factory.getMethodSubSignature(name, type.getReturnType(), type.getParameterTypes());
  }

  /**
   * Whether a call of {@code called} on this object reaches {@link #implementation()}: same
   * functional method (or bridge), declared by the functional interface, a marker, or a supertype.
   */
  public boolean answers(@NonNull MethodSignature called, @NonNull TypeHierarchy hierarchy) {
    MethodSubSignature subSig = called.getSubSignature();
    if (!subSig.equals(sam) && !bridges.contains(subSig)) {
      return false;
    }
    ClassType declaring = called.getDeclClassType();
    return implementsType(declaring, functionalInterface, hierarchy)
        || markerInterfaces.stream().anyMatch(m -> implementsType(declaring, m, hierarchy));
  }

  private static boolean implementsType(ClassType sup, ClassType sub, TypeHierarchy hierarchy) {
    if (sup.equals(sub)) {
      return true;
    }
    try {
      return hierarchy.isSubtype(sup, sub);
    } catch (RuntimeException e) {
      return false; // a type not in the view
    }
  }

  /** The implementation as concrete method signature (as written in the method handle). */
  @NonNull
  public MethodSignature implementationMethod() {
    return implementation.method();
  }

  /** The method a call reaches: {@link #implementationMethod()}, dispatched as declared. */
  @NonNull
  public MethodSignature dispatchedImplementation(@NonNull View view) {
    MethodSignature sig = implementationMethod();
    return AbstractCallGraphAlgorithm.resolveConcreteDispatch(view, sig).orElse(sig);
  }

  /**
   * Where the {@code j}-th argument of a {@link #answers answering} call lands in {@link
   * #implementation()}: {@link DynamicInvokeTarget#RECEIVER}, or a parameter index. Captured values
   * come first ({@link DynamicInvokeTarget#captureParameterIndex}); an unbound receiver reference
   * ({@code String::length}) takes its receiver from the first argument.
   */
  public int samArgumentIndex(int j) {
    if (!implementation.hasReceiver()) {
      return captureCount + j;
    }
    if (captureCount == 0) {
      return j == 0 ? DynamicInvokeTarget.RECEIVER : j - 1;
    }
    return captureCount - 1 + j;
  }

  /** Whether a call returns a fresh instance of the implementation's class ({@code Foo::new}). */
  public boolean isConstructorReference() {
    return implementation.kind() == MethodHandle.Kind.REF_INVOKE_CONSTRUCTOR;
  }
}
