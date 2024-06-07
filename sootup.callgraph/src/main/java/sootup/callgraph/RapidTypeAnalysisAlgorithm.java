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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.model.MethodModifier;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
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
  protected static class Call {
    @Nonnull final MethodSignature source;
    @Nonnull final MethodSignature target;

    protected Call(@Nonnull MethodSignature source, @Nonnull MethodSignature target) {
      this.source = source;
      this.target = target;
    }
  }

  @Nonnull protected Set<ClassType> instantiatedClasses = Collections.emptySet();
  @Nonnull protected Map<ClassType, List<Call>> ignoredCalls = Collections.emptyMap();

  /**
   * The constructor of the RTA algorithm.
   *
   * @param view it contains the data of the classes and methods
   */
  public RapidTypeAnalysisAlgorithm(@Nonnull View view) {
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
    //init helper data structures
    instantiatedClasses = new HashSet<>();
    ignoredCalls = new HashMap<>();

    CallGraph cg = constructCompleteCallGraph(view, entryPoints);

    //delete the data structures
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
  protected List<ClassType> collectInstantiatedClassesInMethod(SootMethod method) {
    if (method == null || method.isAbstract() || method.isNative()) {
      return Collections.emptyList();
    }

    Set<ClassType> instantiated =
        method.getBody().getStmts().stream()
            .filter(stmt -> stmt instanceof JAssignStmt)
            .map(stmt -> ((JAssignStmt) stmt).getRightOp())
            .filter(value -> value instanceof JNewExpr)
            .map(value -> ((JNewExpr) value).getType())
            .collect(Collectors.toSet());
    List<ClassType> newInstantiatedClassTypes =
        instantiated.stream()
            .filter(classType -> !instantiatedClasses.contains(classType))
            .collect(Collectors.toList());
    instantiatedClasses.addAll(instantiated);
    return newInstantiatedClassTypes;
  }

  /**
   * In the RTA algorithm, every virtual call is resolved by using the hierarchy and a hashset
   * containing every instantiated class. Every subclass of the class is considered as target if it
   * is instantiated and if it contains an implementation of the methods called in the invoke
   * expression.
   *
   * @param sourceMethod the method object that contains the given invoke expression in the body.
   * @param invokeExpr it contains the call which is resolved.
   * @return a stream containing all reachable method signatures after applying the RTA call graph
   *     algorithm
   */
  @Override
  @Nonnull
  protected Stream<MethodSignature> resolveCall(
      SootMethod sourceMethod, AbstractInvokeExpr invokeExpr) {
    MethodSignature resolveBaseMethodSignature = invokeExpr.getMethodSignature();
    Stream<MethodSignature> result = Stream.of(resolveBaseMethodSignature);

    SootMethod concreteBaseMethod =
        findConcreteMethod(view, resolveBaseMethodSignature).orElse(null);

    if (concreteBaseMethod == null
        || MethodModifier.isStatic(concreteBaseMethod.getModifiers())
        || (invokeExpr instanceof JSpecialInvokeExpr)) {
      return result;
    } else {
      // the class of the actual method call is instantiated
      if (instantiatedClasses.contains(resolveBaseMethodSignature.getDeclClassType())) {
        return Stream.concat(
            Stream.of(concreteBaseMethod.getSignature()),
            resolveAllCallTargets(sourceMethod.getSignature(), resolveBaseMethodSignature));
      } else {
        saveIgnoredCall(sourceMethod.getSignature(), resolveBaseMethodSignature);
        return resolveAllCallTargets(sourceMethod.getSignature(), resolveBaseMethodSignature);
      }
    }
  }

  /**
   * Resolves all targets of the given signature of the call. Only instantiated classes are
   * considered as target. All possible class of non instantiated classes are saved to the
   * ignoredCall Hashmap, because the classes can be instantiated at a later time
   *
   * @param source the method which contains call
   * @param resolveBaseMethodSignature the base of the resolving. All subtypes of the declaring
   *     class are analyzed as potential targets
   * @return a stream of all method signatures of instantiated classes that can be resolved as
   *     target from the given base method signature.
   */
  private Stream<MethodSignature> resolveAllCallTargets(
      MethodSignature source, MethodSignature resolveBaseMethodSignature) {
    return view.getTypeHierarchy()
        .subtypesOf(resolveBaseMethodSignature.getDeclClassType())
        .map(
            classType -> {
              MethodSignature method =
                  view.getIdentifierFactory()
                      .getMethodSignature(classType, resolveBaseMethodSignature.getSubSignature());
              if (instantiatedClasses.contains(classType)) {
                return resolveConcreteDispatch(view, method);
              } else {
                saveIgnoredCall(source, method);
                return Optional.<MethodSignature>empty();
              }
            })
        .filter(Optional::isPresent)
        .map(Optional::get);
  }

  /**
   * This method saves an ignored call If this is the first ignored call of the class type in the
   * target method, an entry for the class type is created in the ignoredCalls Hashmap
   *
   * @param source the source method of the call
   * @param target the target method of the call
   */
  private void saveIgnoredCall(MethodSignature source, MethodSignature target) {
    ClassType notInstantiatedClass = target.getDeclClassType();
    List<Call> calls = ignoredCalls.get(notInstantiatedClass);
    Call ignoredCall = new Call(source, target);
    if (calls == null) {
      calls = new ArrayList<>();
      ignoredCalls.put(notInstantiatedClass, calls);
    }
    calls.add(ignoredCall);
  }

  /**
   * Preprocessing of a method in the RTA call graph algorithm
   *
   * <p>Before processing the method, all instantiated types are collected inside the body of the
   * sourceMethod. If a new instantiated class has previously ignored calls to this class, they are
   * added to call graph
   *
   * @param view view
   * @param sourceMethod the processed method
   * @param workList the current work list
   * @param cg the current cg
   */
  @Override
  protected void preProcessingMethod(
      View view,
      MethodSignature sourceMethod,
      @Nonnull Deque<MethodSignature> workList,
      @Nonnull MutableCallGraph cg) {
    SootMethod method =
        view.getClass(sourceMethod.getDeclClassType())
            .flatMap(c -> c.getMethod(sourceMethod.getSubSignature()))
            .orElse(null);
    if (method == null) {
      return;
    }

    List<ClassType> newInstantiatedClasses = collectInstantiatedClassesInMethod(method);
    newInstantiatedClasses.forEach(
        classType -> includeIgnoredCallsToClass(classType, cg, workList));
  }

  /**
   * This method will add all saved ignored calls from a given class to the call graph. All new
   * targets will be added to the worklist
   *
   * @param classType the class type which is the target of all ignored calls
   * @param cg the call graph that will be extended by the ignored calls
   * @param workList the work list that will be extended by the new targets of ignored calls.
   */
  protected void includeIgnoredCallsToClass(
      ClassType classType, MutableCallGraph cg, Deque<MethodSignature> workList) {
    List<Call> newEdges = ignoredCalls.get(classType);
    if (newEdges != null) {
      newEdges.forEach(
          call -> {
            MethodSignature concreteTarget =
                resolveConcreteDispatch(view, call.target).orElse(null);
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
      ignoredCalls.remove(classType);
    }
  }

  /**
   * Postprocessing is not needed in RTA
   *
   * @param view view
   * @param sourceMethod the processed method
   * @param workList the current worklist that is extended by methods that have to be analyzed.
   * @param cg the current cg is extended by new call targets and calls
   */
  @Override
  protected void postProcessingMethod(
      View view,
      MethodSignature sourceMethod,
      @Nonnull Deque<MethodSignature> workList,
      @Nonnull MutableCallGraph cg) {
    //    not needed
  }
}
