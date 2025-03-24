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

import com.google.common.collect.ArrayListMultimap;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import sootup.callgraph.CallGraph.Call;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
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

  @NonNull protected Set<ClassType> instantiatedClasses = Collections.emptySet();
  @NonNull protected ArrayListMultimap<ClassType, Call> ignoredCalls = ArrayListMultimap.create();

  /**
   * The constructor of the RTA algorithm.
   *
   * @param view it contains the data of the classes and methods
   */
  public RapidTypeAnalysisAlgorithm(@NonNull View view) {
    super(view);
  }

  @NonNull
  @Override
  public CallGraph initialize() {
    List<MethodSignature> entryPoints = Collections.singletonList(findMainMethod());
    return initialize(entryPoints);
  }

  @NonNull
  @Override
  public CallGraph initialize(@NonNull List<MethodSignature> entryPoints) {
    // init helper data structures
    instantiatedClasses = new HashSet<>();
    ignoredCalls = ArrayListMultimap.create();

    CallGraph cg = constructCompleteCallGraph(entryPoints);

    // delete the data structures
    instantiatedClasses = Collections.emptySet();
    ignoredCalls = ArrayListMultimap.create();
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
   * @param invokableStmt the statement containing the call which is resolved.
   * @return a stream containing all reachable method signatures after applying the RTA call graph
   *     algorithm
   */
  @Override
  @NonNull
  protected Stream<MethodSignature> resolveCall(
      SootMethod sourceMethod, InvokableStmt invokableStmt) {
    Optional<AbstractInvokeExpr> optInvokeExpr = invokableStmt.getInvokeExpr();
    if (optInvokeExpr.isEmpty()) {
      return Stream.empty();
    }
    AbstractInvokeExpr invokeExpr = optInvokeExpr.get();
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
            resolveAllCallTargets(
                sourceMethod.getSignature(), resolveBaseMethodSignature, invokableStmt));
      } else {
        saveIgnoredCall(sourceMethod.getSignature(), resolveBaseMethodSignature, invokableStmt);
        return resolveAllCallTargets(
            sourceMethod.getSignature(), resolveBaseMethodSignature, invokableStmt);
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
   * @param invokableStmt the statement causing the call
   * @return a stream of all method signatures of instantiated classes that can be resolved as
   *     target from the given base method signature.
   */
  private Stream<MethodSignature> resolveAllCallTargets(
      MethodSignature source,
      MethodSignature resolveBaseMethodSignature,
      InvokableStmt invokableStmt) {
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
                saveIgnoredCall(source, method, invokableStmt);
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
   * @param invokableStmt the statement causing the call
   */
  private void saveIgnoredCall(
      MethodSignature source, MethodSignature target, InvokableStmt invokableStmt) {
    ClassType notInstantiatedClass = target.getDeclClassType();
    Call ignoredCall = new Call(source, target, invokableStmt);
    ignoredCalls.put(notInstantiatedClass, ignoredCall);
  }

  /**
   * Preprocessing of a method in the RTA call graph algorithm
   *
   * <p>Before processing the method, all instantiated types are collected inside the body of the
   * sourceMethod. If a new instantiated class has previously ignored calls to this class, they are
   * added to call graph
   *
   * @param sourceMethod the processed method
   * @param workList the current work list
   * @param cg the current cg
   */
  @Override
  protected void preProcessingMethod(
      MethodSignature sourceMethod,
      @NonNull Deque<MethodSignature> workList,
      @NonNull MutableCallGraph cg) {
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
    newEdges.forEach(
        call -> {
          MethodSignature concreteTarget =
              resolveConcreteDispatch(view, call.getTargetMethodSignature()).orElse(null);
          if (concreteTarget == null) {
            return;
          }
          addCallToCG(
              call.getSourceMethodSignature(),
              concreteTarget,
              call.getInvokableStmt(),
              cg,
              workList);
        });
    // can be removed because the instantiated class will be considered in future resolves
    ignoredCalls.removeAll(classType);
  }

  /**
   * Postprocessing is not needed in RTA
   *
   * @param sourceMethod the processed method
   * @param workList the current worklist that is extended by methods that have to be analyzed.
   * @param cg the current cg is extended by new call targets and calls
   */
  @Override
  protected void postProcessingMethod(
      MethodSignature sourceMethod,
      @NonNull Deque<MethodSignature> workList,
      @NonNull MutableCallGraph cg) {
    //    not needed
  }
}
