package sootup.callgraph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Linghui Luo, Christian Brüggemann, Ben Hermann, Markus Schmidt
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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import sootup.callgraph.scope.CallResolver;
import sootup.callgraph.scope.VirtualCallResolver;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.model.MethodModifier;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * This class implements the Class Hierarchy Analysis call graph algorithm. In this algorithm, every
 * virtual call is resolved to the all implemented overwritten methods of subclasses in the entire
 * class path.
 */
public class ClassHierarchyAnalysisAlgorithm extends AbstractCallGraphAlgorithm {

  /**
   * Caches the result of virtual/interface dispatch resolution (everything in {@link #resolveCall}
   * from the point a call is established to be neither static nor a special invoke onward), keyed
   * by the target {@link MethodSignature}. That portion of {@link #resolveCall} is (unlike RTA's) a
   * pure function of the target signature and the current hierarchy state - it does not depend on
   * the source method, the invoke statement, or any other call-site-specific or
   * incrementally-mutable state. Many call sites across an app routinely target the same method
   * (e.g. a common interface method), so this avoids repeating the subclass/method resolution work
   * for each one.
   */
  private final Map<MethodSignature, List<MethodSignature>> virtualDispatchTargetsCache =
      new HashMap<>();

  /** The {@link TypeHierarchy#getModificationCount()} value as of the last cache population. */
  private long virtualDispatchTargetsCacheModCount = -1;

  /**
   * The constructor of the CHA algorithm.
   *
   * @param view it contains the data of the classes and methods
   */
  public ClassHierarchyAnalysisAlgorithm(@NonNull View view) {
    super(view);
  }

  /**
   * The constructor of the CHA algorithm that allows restricting which statements' calls are
   * resolved during call graph construction.
   *
   * @param view it contains the data of the classes and methods
   * @param callResolver decides which statements' calls are excluded from the call graph
   */
  public ClassHierarchyAnalysisAlgorithm(@NonNull View view, @NonNull CallResolver callResolver) {
    super(view, callResolver);
  }

  /**
   * The constructor of the CHA algorithm that allows restricting which resolved dynamic-dispatch
   * candidates are admitted/expanded during call graph construction.
   *
   * @param view it contains the data of the classes and methods
   * @param virtualCallResolver decides which resolved call candidates are excluded from the call
   *     graph
   */
  public ClassHierarchyAnalysisAlgorithm(
      @NonNull View view, @NonNull VirtualCallResolver virtualCallResolver) {
    super(view, virtualCallResolver);
  }

  /**
   * The constructor of the CHA algorithm that allows restricting which calls are expanded during
   * call graph construction.
   *
   * @param view it contains the data of the classes and methods
   * @param callResolver decides which statements' calls are excluded from the call graph
   * @param virtualCallResolver decides which resolved call candidates are excluded from the call
   *     graph
   */
  public ClassHierarchyAnalysisAlgorithm(
      @NonNull View view,
      @NonNull CallResolver callResolver,
      @NonNull VirtualCallResolver virtualCallResolver) {
    super(view, callResolver, virtualCallResolver);
  }

  /**
   * The constructor of the CHA algorithm that allows restricting which calls are expanded during
   * call graph construction, and whether entry points' declaring-class {@code <clinit>}s are
   * eagerly seeded as roots. See {@link AbstractCallGraphAlgorithm#AbstractCallGraphAlgorithm(View,
   * CallResolver, VirtualCallResolver, boolean)} for the four classic static-initializer handling
   * modes this enables.
   *
   * @param view it contains the data of the classes and methods
   * @param callResolver decides which statements' calls are excluded from the call graph
   * @param virtualCallResolver decides which resolved call candidates are excluded from the call
   *     graph
   * @param seedEntryPointClinits whether entry points' declaring-class {@code <clinit>}s are
   *     eagerly seeded as roots before traversal starts
   */
  public ClassHierarchyAnalysisAlgorithm(
      @NonNull View view,
      @NonNull CallResolver callResolver,
      @NonNull VirtualCallResolver virtualCallResolver,
      boolean seedEntryPointClinits) {
    super(view, callResolver, virtualCallResolver, seedEntryPointClinits);
  }

  @NonNull
  @Override
  public CallGraph initialize() {
    return constructCompleteCallGraph(Collections.singletonList(findMainMethod()));
  }

  @NonNull
  @Override
  public CallGraph initialize(@NonNull List<MethodSignature> entryPoints) {
    return constructCompleteCallGraph(entryPoints);
  }

  /**
   * In the CHA algorithm, every virtual call is resolved by only using the hierarchy. Every
   * subclass of the class is considered as target if it contains an implementation of the methods
   * called in the invoke expression.
   *
   * @param method the method object that contains the given invoke expression in the body.
   * @param invokableStmt it contains the call which is resolved.
   * @return a stream containing all reachable method signatures after applying the CHA call graph
   *     algorithm
   */
  @Override
  @NonNull
  protected Stream<MethodSignature> resolveCall(SootMethod method, InvokableStmt invokableStmt) {
    Optional<AbstractInvokeExpr> optInvokeExpr = invokableStmt.getInvokeExpr();
    if (optInvokeExpr.isEmpty()) {
      return Stream.empty();
    }
    AbstractInvokeExpr invokeExpr = optInvokeExpr.get();
    MethodSignature targetMethodSignature = invokeExpr.getMethodSignature();
    if ((invokeExpr instanceof JDynamicInvokeExpr)) {
      return Stream.empty();
    }

    SootMethod actualTargetMethod = view.getMethod(targetMethodSignature).orElse(null);
    if (actualTargetMethod == null) {
      // method not implemented, search for implementation in super classes or ínterfaces
      actualTargetMethod = findConcreteMethod(view, targetMethodSignature).orElse(null);
      // method implementation isn't contained in the view. return the called method as target
      if (actualTargetMethod == null) {
        return Stream.of(targetMethodSignature);
      }
    }

    // special invokes and static invokes can only have one specific target
    if (MethodModifier.isStatic(actualTargetMethod.getModifiers())
        || (invokeExpr instanceof JSpecialInvokeExpr)) {
      return Stream.of(actualTargetMethod.getSignature());
    }

    return resolveVirtualDispatchTargets(targetMethodSignature, actualTargetMethod).stream();
  }

  /**
   * Resolves the targets of a virtual/interface dispatch for {@code targetMethodSignature}, caching
   * the result. Safe to cache purely by {@code targetMethodSignature} because - unlike RTA - none
   * of this computation depends on the source method, the invoke statement, or any other state that
   * changes independently of the type hierarchy.
   */
  @NonNull
  private List<MethodSignature> resolveVirtualDispatchTargets(
      @NonNull MethodSignature targetMethodSignature, @NonNull SootMethod actualTargetMethod) {
    long currentModCount = typeHierarchy.getModificationCount();
    if (currentModCount != virtualDispatchTargetsCacheModCount) {
      virtualDispatchTargetsCache.clear();
      virtualDispatchTargetsCacheModCount = currentModCount;
    }

    List<MethodSignature> cached = virtualDispatchTargetsCache.get(targetMethodSignature);
    if (cached != null) {
      return cached;
    }

    // get all subclasses
    // the target method is used since this is the type of the invoke
    List<? extends SootClass> subclasses =
        typeHierarchy.subtypeClassesOf(targetMethodSignature.getDeclClassType()).toList();

    // get all targets of these subtypes
    Stream<MethodSignature> targets = resolveAllCallTargets(subclasses, targetMethodSignature);

    // if the current implementation of the method is a default method the algorithm changes
    // because superclasses can overwrite default methods which are not subtypes of the interface
    if (isInterface(actualTargetMethod.getDeclaringClassType())) {
      // get all default methods of sub-interfaces of the interface
      // which are interfaces of the subtypes
      targets =
          Stream.concat(
              targets,
              resolveAllDefaultTargets(
                  subclasses,
                  actualTargetMethod.getDeclClassType(),
                  targetMethodSignature.getSubSignature()));
      // all subtypes that do not have an implementation of the method
      // can have an implementation in the supertype
      targets =
          Stream.concat(targets, resolveAllOverwrittenTargets(subclasses, targetMethodSignature));
    }

    // if the actual base method is abstract it cannot be called by the invoke
    List<MethodSignature> computed =
        actualTargetMethod.isAbstract()
            ? targets.toList()
            : Stream.concat(Stream.of(actualTargetMethod.getSignature()), targets).toList();

    virtualDispatchTargetsCache.put(targetMethodSignature, computed);
    return computed;
  }

  private Stream<MethodSignature> resolveAllCallTargets(
      List<? extends SootClass> subclasses, MethodSignature targetMethodSignature) {
    return subclasses.stream()
        .flatMap(sootClass -> sootClass.getMethod(targetMethodSignature.getSubSignature()).stream())
        .filter(sootMethod -> !sootMethod.isAbstract())
        .map(SootMethod::getSignature);
  }

  private Stream<MethodSignature> resolveAllDefaultTargets(
      List<? extends SootClass> subclasses,
      ClassType interfaceClassType,
      MethodSubSignature targetSubSignature) {
    Set<? extends ClassType> interfaces =
        subclasses.stream()
            .flatMap(sootClass -> sootClass.getInterfaces().stream())
            .collect(Collectors.toSet());
    return typeHierarchy
        .subinterfacesOf(interfaceClassType)
        .flatMap(
            classType ->
                view
                    .getMethod(
                        view.getIdentifierFactory()
                            .getMethodSignature(classType, targetSubSignature))
                    .stream())
        .filter(sootMethod -> interfaces.contains(sootMethod.getDeclClassType()))
        .map(SootMethod::getSignature);
  }

  private Stream<MethodSignature> resolveAllOverwrittenTargets(
      List<? extends SootClass> subclasses, MethodSignature targetMethodSignature) {
    return subclasses.stream()
        .filter(sootClass -> sootClass.getMethod(targetMethodSignature.getSubSignature()).isEmpty())
        .flatMap(
            sootClass ->
                findMethodInHierarchy(view, sootClass, targetMethodSignature.getSubSignature())
                    .stream())
        .map(SootMethod::getSignature);
  }

  @Override
  protected void postProcessingMethod(
      @NonNull MethodSignature sourceMethod,
      @NonNull Deque<MethodSignature> workList,
      @NonNull MutableCallGraph cg) {
    // do nothing
  }

  @Override
  protected void preProcessingMethod(
      @NonNull MethodSignature sourceMethod,
      @NonNull Deque<MethodSignature> workList,
      @NonNull MutableCallGraph cg) {
    // do nothing
  }
}
