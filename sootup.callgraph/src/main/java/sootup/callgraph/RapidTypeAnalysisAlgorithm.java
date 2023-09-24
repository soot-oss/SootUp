package sootup.callgraph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2022 Kadiray Karakaya, Jonas Klauke
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

import com.google.common.collect.Sets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.model.MethodModifier;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.typehierarchy.MethodDispatchResolver;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * This class implements the Rapid Type Analysis call graph algorithm. In this algorithm, every
 * virtual call is resolved to the all implemented overwritten methods of subclasses in the entire
 * class path which have been instantiated by a new expression.
 *
 * <p>Compared to the CHA algorithm, this algorithm is more precise because it only considers
 * instantiated subclasses as call targets and CHA considers all subclasses.
 */
public class RapidTypeAnalysisAlgorithm extends AbstractCallGraphAlgorithm {

  /**
   * This private class is used to save reachable calls. Because every method is only processed
   * once, ignored calls are saved to include them at a later time if their class is instantiated at
   * a later time.
   */
  private static class Call {
    @Nonnull final MethodSignature source;
    @Nonnull final MethodSignature target;

    private Call(@Nonnull MethodSignature source, @Nonnull MethodSignature target) {
      this.source = source;
      this.target = target;
    }
  }

  @Nonnull private Set<ClassType> instantiatedClasses = Collections.emptySet();
  @Nonnull private Map<ClassType, List<Call>> ignoredCalls = Collections.emptyMap();

  /**
   * The constructor of the RTA algorithm.
   *
   * @param view it contains the data of the classes and methods
   */
  public RapidTypeAnalysisAlgorithm(@Nonnull View<? extends SootClass<?>> view) {
    super(view);
  }

  @Nonnull
  @Override
  public CallGraph initialize() {
    List<MethodSignature> entryPoints = Collections.singletonList(findMainMethod());
    return initialize(entryPoints);
  }

  @Nonnull
  @Override
  public CallGraph initialize(@Nonnull List<MethodSignature> entryPoints) {
    instantiatedClasses = new HashSet<>();
    ignoredCalls = new HashMap<>();
    CallGraph cg = constructCompleteCallGraph(view, entryPoints);
    instantiatedClasses = Collections.emptySet();
    ignoredCalls = Collections.emptyMap();
    return cg;
  }

  /**
   * This method is called to collect all instantiation of classes in a given method body. This is
   * important since the RTA algorithm resolves virtual calls only to instantiated classes
   *
   * @param method this object contains the method body which is inspected.
   */
  protected void collectInstantiatedClassesInMethod(SootMethod method) {
    if (method == null || method.isAbstract() || method.isNative()) {
      return;
    }

    Set<ClassType> instantiated =
        method.getBody().getStmts().stream()
            .filter(stmt -> stmt instanceof JAssignStmt)
            .map(stmt -> ((JAssignStmt<?, ?>) stmt).getRightOp())
            .filter(value -> value instanceof JNewExpr)
            .map(value -> ((JNewExpr) value).getType())
            .collect(Collectors.toSet());
    instantiatedClasses.addAll(instantiated);
  }

  /**
   * In the RTA algorithm, every virtual call is resolved by using the hierarchy and a hashset
   * containing every instantiated class. Every subclass of the class is considered as target if it
   * is instantiated and if it contains an implementation of the methods called in the invoke
   * expression.
   *
   * @param method the method object that contains the given invoke expression in the body.
   * @param invokeExpr it contains the call which is resolved.
   * @return a stream containing all reachable method signatures after applying the RTA call graph
   *     algorithm
   */
  @Override
  @Nonnull
  protected Stream<MethodSignature> resolveCall(SootMethod method, AbstractInvokeExpr invokeExpr) {
    MethodSignature targetMethodSignature = invokeExpr.getMethodSignature();
    Stream<MethodSignature> result = Stream.of(targetMethodSignature);

    SootMethod targetMethod =
        view.getClass(targetMethodSignature.getDeclClassType())
            .flatMap(clazz -> clazz.getMethod(targetMethodSignature.getSubSignature()))
            .orElseGet(() -> findMethodInHierarchy(view, targetMethodSignature));

    if (targetMethod == null
        || MethodModifier.isStatic(targetMethod.getModifiers())
        || (invokeExpr instanceof JSpecialInvokeExpr)) {
      return result;
    } else {
      Set<MethodSignature> notInstantiatedCallTargets = Sets.newHashSet();
      Set<MethodSignature> implAndOverrides =
          MethodDispatchResolver.resolveAllDispatchesInClasses(
              view, targetMethodSignature, instantiatedClasses, notInstantiatedCallTargets);

      // the class of the actual method call is instantiated
      boolean targetMethodClassIsInstantiated =
          instantiatedClasses.contains(targetMethodSignature.getDeclClassType());

      // add the targetMethod to the ignoredCalls
      if (!targetMethodClassIsInstantiated) {
        notInstantiatedCallTargets.add(targetMethodSignature);
      }

      // save filtered calls to include them later when their class is instantiated
      notInstantiatedCallTargets.forEach(
          ignoredMethodSignature -> {
            ClassType notInstantiatedClass = ignoredMethodSignature.getDeclClassType();
            List<Call> calls = ignoredCalls.get(notInstantiatedClass);
            if (calls == null) {
              calls = new ArrayList<>();
              calls.add(new Call(method.getSignature(), ignoredMethodSignature));
              ignoredCalls.put(notInstantiatedClass, calls);
            } else {
              calls.add(new Call(method.getSignature(), ignoredMethodSignature));
            }
          });

      // find the concrete dispatch of all possible dispatches
      Set<MethodSignature> concreteCallTargets =
          implAndOverrides.stream()
              .map(
                  methodSignature ->
                      MethodDispatchResolver.resolveConcreteDispatch(view, methodSignature))
              .filter(Optional::isPresent)
              .map(Optional::get)
              .collect(Collectors.toSet());

      // add the concrete of the targetMethod if the class is instantiated
      if (targetMethodClassIsInstantiated) {
        MethodDispatchResolver.resolveConcreteDispatch(view, targetMethodSignature)
            .ifPresent(concreteCallTargets::add);
      }

      return concreteCallTargets.stream();
    }
  }

  /**
   * Preprocessing of a method in the RTA call graph algorithm
   *
   * <p>Before processing the method, all instantiated types are collected inside the body of the
   * sourceMethod.
   *
   * @param view view
   * @param sourceMethod the processed method
   * @param workList the current work list
   * @param cg the current cg
   */
  @Override
  protected void preProcessingMethod(
      View<? extends SootClass<?>> view,
      MethodSignature sourceMethod,
      @Nonnull Deque<MethodSignature> workList,
      @Nonnull MutableCallGraph cg) {
    SootMethod method =
        view.getClass(sourceMethod.getDeclClassType())
            .flatMap(c -> c.getMethod(sourceMethod.getSubSignature()))
            .orElse(null);
    if (method == null) return;

    collectInstantiatedClassesInMethod(method);
  }

  /**
   * Postprocessing of a method in the RTA call graph algorithm
   *
   * <p>RTA has to add previously ignored calls because a found instantiation of a class could
   * enable a call to a ignored method at a later time.
   *
   * @param view view
   * @param sourceMethod the processed method
   * @param workList the current worklist that is extended by methods that have to be analyzed.
   * @param cg the current cg is extended by new call targets and calls
   */
  @Override
  protected void postProcessingMethod(
      View<? extends SootClass<?>> view,
      MethodSignature sourceMethod,
      @Nonnull Deque<MethodSignature> workList,
      @Nonnull MutableCallGraph cg) {
    instantiatedClasses.forEach(
        instantiatedClassType -> {
          List<Call> newEdges = ignoredCalls.get(instantiatedClassType);
          if (newEdges != null) {
            newEdges.forEach(
                call -> {
                  MethodSignature concreteTarget =
                      MethodDispatchResolver.resolveConcreteDispatch(view, call.target)
                          .orElse(null);
                  if (concreteTarget == null) {
                    return;
                  }
                  if (cg.containsMethod(concreteTarget)) {
                    // method is already analyzed or is in the work list, simply add the call
                    cg.addCall(call.source, concreteTarget);
                  } else {
                    // new target method found that has to be analyzed
                    cg.addMethod(concreteTarget);
                    cg.addCall(call.source, concreteTarget);
                    workList.push(concreteTarget);
                  }
                });
            // can be removed because the instantiated class will be considered in future resolves
            ignoredCalls.remove(instantiatedClassType);
          }
        });
  }
}
