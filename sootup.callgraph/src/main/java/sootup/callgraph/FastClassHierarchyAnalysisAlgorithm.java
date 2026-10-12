package sootup.callgraph;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import sootup.callgraph.invokedynamic.DynamicInvokeResolver;
import sootup.callgraph.reflection.ReflectionModel;
import sootup.callgraph.scope.CallResolver;
import sootup.callgraph.scope.VirtualCallResolver;
import sootup.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.core.views.View;

/**
 * A variant of {@link ClassHierarchyAnalysisAlgorithm} that resolves a virtual or interface call
 * over the subtypes of the static type of the receiver local, instead of the subtypes of the class
 * the invoked method reference names. This is how Soot's CHA resolves calls.
 *
 * <h2>How it differs from CHA</h2>
 *
 * <p>For {@code A a = new B(); a.m();} the bytecode refers to {@code A.m()}, but the Jimple local
 * holding the receiver is typed {@code B}, the type inferred from the assignments to it. CHA
 * resolves the call to {@code A.m()} and every override of {@code m()} in the subtypes of {@code
 * A}. This algorithm resolves it to the method that {@code B} and each subtype of {@code B}
 * declares or inherits - every subtype, whether the program instantiates it or not. When the
 * local's type is the referenced class itself, or not a subtype of it, both algorithms agree.
 *
 * <p>The difference matters most for the methods of {@code java.lang.Object}: dx and d8 encode
 * {@code toString()} on a local of an interface type as a call to {@code java.lang.Object}, which
 * CHA resolves to every {@code toString()} of the program. In Android apps that bundle large
 * libraries, e.g. Kotlin coroutines, this alone can make a call graph orders of magnitude larger.
 *
 * <h2>Caveats</h2>
 *
 * <ul>
 *   <li>The result depends on how the bodies were typed. The referenced class comes from the
 *       bytecode, and the JVM's verifier checks the receiver against it; the type of a local is
 *       inferred by the body interceptors. Without a {@code LocalSplitter} before typing, a local
 *       that holds a {@code B} and later a {@code C} is typed with their common supertype, so the
 *       same program gives a larger call graph. A {@code LocalPacker} that merges locals of
 *       different types widens them the same way.
 *   <li>A type that is too narrow makes the call graph unsound: calls whose receiver can be of
 *       another type lose their edges, without any error. Only use bodies whose typing you trust.
 *   <li>Locals without a class type, e.g. untyped locals of dex bodies before a {@code
 *       TypeAssigner} ran, are resolved like CHA does.
 *   <li>It is not class hierarchy analysis in the textbook sense, which uses the declared type of
 *       the source: the inferred type of a local carries information about the allocations assigned
 *       to it.
 * </ul>
 *
 * <h2>How to use it</h2>
 *
 * <p>Create the view's bodies with interceptors that split the locals and then assign their types,
 * in this order, e.g. {@code LocalSplitter} followed by {@code TypeAssigner}, and don't merge
 * locals of different types afterwards. This is what Soot's {@code jb} pack does for every body,
 * which is why Soot's CHA can rely on the types of locals.
 */
public class FastClassHierarchyAnalysisAlgorithm extends ClassHierarchyAnalysisAlgorithm {

  /**
   * The dispatch targets for a receiver type and a resolved method. Like CHA's own cache, it does
   * not depend on the call site.
   */
  private final Map<ReceiverCall, List<MethodSignature>> receiverDispatchTargetsCache =
      new HashMap<>();

  /** The {@link sootup.core.typehierarchy.TypeHierarchy#getModificationCount()} of the cache. */
  private long receiverDispatchTargetsCacheModCount = -1;

  public FastClassHierarchyAnalysisAlgorithm(@NonNull View view) {
    super(view);
  }

  public FastClassHierarchyAnalysisAlgorithm(
      @NonNull View view, @NonNull CallResolver callResolver) {
    super(view, callResolver);
  }

  public FastClassHierarchyAnalysisAlgorithm(
      @NonNull View view, @NonNull VirtualCallResolver virtualCallResolver) {
    super(view, virtualCallResolver);
  }

  public FastClassHierarchyAnalysisAlgorithm(
      @NonNull View view,
      @NonNull CallResolver callResolver,
      @NonNull VirtualCallResolver virtualCallResolver) {
    super(view, callResolver, virtualCallResolver);
  }

  public FastClassHierarchyAnalysisAlgorithm(
      @NonNull View view,
      @NonNull CallResolver callResolver,
      @NonNull VirtualCallResolver virtualCallResolver,
      boolean seedEntryPointClinits) {
    super(view, callResolver, virtualCallResolver, seedEntryPointClinits);
  }

  public FastClassHierarchyAnalysisAlgorithm(
      @NonNull View view,
      @NonNull CallResolver callResolver,
      @NonNull VirtualCallResolver virtualCallResolver,
      boolean seedEntryPointClinits,
      @NonNull ReflectionModel reflectionModel) {
    super(view, callResolver, virtualCallResolver, seedEntryPointClinits, reflectionModel);
  }

  public FastClassHierarchyAnalysisAlgorithm(
      @NonNull View view,
      @NonNull CallResolver callResolver,
      @NonNull VirtualCallResolver virtualCallResolver,
      boolean seedEntryPointClinits,
      @NonNull DynamicInvokeResolver dynamicInvokeResolver) {
    super(view, callResolver, virtualCallResolver, seedEntryPointClinits, dynamicInvokeResolver);
  }

  public FastClassHierarchyAnalysisAlgorithm(
      @NonNull View view,
      @NonNull CallResolver callResolver,
      @NonNull VirtualCallResolver virtualCallResolver,
      boolean seedEntryPointClinits,
      @NonNull ReflectionModel reflectionModel,
      @NonNull DynamicInvokeResolver dynamicInvokeResolver) {
    super(
        view,
        callResolver,
        virtualCallResolver,
        seedEntryPointClinits,
        reflectionModel,
        dynamicInvokeResolver);
  }

  @Override
  @NonNull
  protected Stream<MethodSignature> resolveCall(SootMethod method, InvokableStmt invokableStmt) {
    Optional<AbstractInvokeExpr> invokeExpr = invokableStmt.getInvokeExpr();
    if (invokeExpr.isPresent()) {
      Optional<ClassType> receiverType = narrowerReceiverType(invokeExpr.get());
      if (receiverType.isPresent()) {
        List<MethodSignature> targets =
            resolveReceiverDispatchTargets(
                receiverType.get(), invokeExpr.get().getMethodSignature());
        if (!targets.isEmpty()) {
          return targets.stream();
        }
      }
    }
    return super.resolveCall(method, invokableStmt);
  }

  /**
   * Returns the type of the receiver local of a virtual or interface call if it is a proper subtype
   * of the class the invoked method reference names, and is known to the type hierarchy
   */
  @NonNull
  private Optional<ClassType> narrowerReceiverType(@NonNull AbstractInvokeExpr invokeExpr) {
    if (!(invokeExpr instanceof AbstractInstanceInvokeExpr)
        || invokeExpr instanceof JSpecialInvokeExpr) {
      return Optional.empty();
    }
    Type baseType = ((AbstractInstanceInvokeExpr) invokeExpr).getBase().getType();
    if (!(baseType instanceof ClassType)) {
      return Optional.empty();
    }
    ClassType receiverType = (ClassType) baseType;
    ClassType referencedType = invokeExpr.getMethodSignature().getDeclClassType();
    if (receiverType.equals(referencedType)
        || !typeHierarchy.contains(receiverType)
        || !typeHierarchy.isSubtype(referencedType, receiverType)) {
      return Optional.empty();
    }
    return Optional.of(receiverType);
  }

  /**
   * Returns, for the receiver type and each of its subtypes that is a class, the non-abstract
   * method that a receiver of that class runs for a call to the method {@code reference} resolves
   * to; empty if the reference does not resolve
   */
  @NonNull
  private List<MethodSignature> resolveReceiverDispatchTargets(
      @NonNull ClassType receiverType, @NonNull MethodSignature reference) {
    long currentModCount = typeHierarchy.getModificationCount();
    if (currentModCount != receiverDispatchTargetsCacheModCount) {
      receiverDispatchTargetsCache.clear();
      receiverDispatchTargetsCacheModCount = currentModCount;
    }
    Optional<SootMethod> resolved = methodLookup.find(reference);
    if (resolved.isEmpty()) {
      return List.of();
    }
    ReceiverCall key = new ReceiverCall(receiverType, resolved.get().getSignature());
    List<MethodSignature> cached = receiverDispatchTargetsCache.get(key);
    if (cached != null) {
      return cached;
    }

    Set<MethodSignature> targets = new LinkedHashSet<>();
    Stream.concat(
            view.getClass(receiverType).stream(), typeHierarchy.subtypeClassesOf(receiverType))
        .filter(sootClass -> !sootClass.isInterface())
        .forEach(
            sootClass ->
                methodLookup
                    .select(sootClass.getType(), resolved.get())
                    .ifPresent(sootMethod -> targets.add(sootMethod.getSignature())));

    List<MethodSignature> computed = new ArrayList<>(targets);
    receiverDispatchTargetsCache.put(key, computed);
    return computed;
  }

  /** A receiver type and the method a call resolves to. */
  private record ReceiverCall(ClassType receiverType, MethodSignature resolved) {}
}
