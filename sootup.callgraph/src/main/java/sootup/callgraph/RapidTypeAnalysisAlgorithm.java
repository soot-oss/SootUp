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
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import sootup.callgraph.CallGraph.Call;
import sootup.callgraph.scope.CallGraphScope;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.model.MethodModifier;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
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

  @NonNull protected Set<ClassType> instantiatedClasses;
  @NonNull protected ArrayListMultimap<ClassType, Call> ignoredCalls = ArrayListMultimap.create();

  /**
   * The constructor of the RTA algorithm.
   *
   * @param view it contains the data of the classes and methods
   */
  public RapidTypeAnalysisAlgorithm(@NonNull View view) {
    this(view, Collections.emptySet());
  }

  /**
   * Constructor of the RTA algorithm with a predefined set of classes that are considered
   * instantiated before the RTA call graph algorithm starts.
   *
   * @param view it contains the data of the classes and methods
   * @param preInstantiatedClasses predefined set of instantiated classes
   */
  public RapidTypeAnalysisAlgorithm(
      @NonNull View view, @NonNull Set<ClassType> preInstantiatedClasses) {
    super(view);
    this.instantiatedClasses = new HashSet<>(preInstantiatedClasses);
  }

  /**
   * The constructor of the RTA algorithm that allows restricting which classes/methods are expanded
   * during call graph construction.
   *
   * @param view it contains the data of the classes and methods
   * @param scope decides which classes/methods are excluded from the call graph
   */
  public RapidTypeAnalysisAlgorithm(@NonNull View view, @NonNull CallGraphScope scope) {
    super(view, scope);
    this.instantiatedClasses = new HashSet<>();
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
    instantiatedClasses = new HashSet<>(instantiatedClasses);
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
  protected Stream<ClassType> collectInstantiatedClassesInMethod(@NonNull SootMethod method) {
    if (method.isAbstract() || method.isNative()) {
      return Stream.empty();
    }
    return method.getBody().getStmts().stream()
        .filter(stmt -> stmt instanceof JAssignStmt)
        .map(stmt -> ((JAssignStmt) stmt).getRightOp())
        .filter(value -> value instanceof JNewExpr)
        .map(value -> ((JNewExpr) value).getType())
        .filter(classType -> !instantiatedClasses.contains(classType));
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

    // get all instantiated subclasses
    // the target method is used since this is the type of the invoke
    List<? extends SootClass> subclasses =
        typeHierarchy
            .subtypesOf(targetMethodSignature.getDeclClassType())
            .flatMap(classType -> view.getClass(classType).stream())
            .toList();

    // get all targets of these subtypes
    Stream<MethodSignature> targets =
        resolveAllCallTargets(
            subclasses,
            sourceMethod.getSignature(),
            actualTargetMethod.getSignature(),
            invokableStmt);

    // if the current implementation of the method is a default method the algorithm changes
    // because superclasses can overwrite default methods which are not subtypes of the interface
    boolean isDefaultMethod = isInterface(actualTargetMethod.getDeclaringClassType());
    if (isDefaultMethod) {
      // get all default methods of sub-interfaces of the interface
      // which are interfaces of the subtypes
      targets =
          Stream.concat(
              targets, resolveAllDefaultTargets(subclasses, actualTargetMethod.getSignature()));
      // all subtypes that do not have an implementation of the method
      // can have an implementation in the supertype
      targets =
          Stream.concat(
              targets,
              resolveAllOverwrittenTargets(subclasses, targetMethodSignature.getSubSignature()));
    }

    // if the actual base method is abstract it cannot be called by the invoke
    if (actualTargetMethod.isAbstract()) {
      return targets;
    }
    // check if the class of the base method is instantiated
    if (instantiatedClasses.contains(targetMethodSignature.getDeclClassType())
        || (isDefaultMethod && isInterface(targetMethodSignature.getDeclClassType()))) {
      // the call is created with the actual base target
      return Stream.concat(Stream.of(actualTargetMethod.getSignature()), targets);
    }
    // save the ignored call
    saveIgnoredCall(sourceMethod.getSignature(), actualTargetMethod.getSignature(), invokableStmt);
    return targets;
  }

  /**
   * Resolves all targets of the given signature of the call. Only instantiated classes are
   * considered as target. All possible class of non instantiated classes are saved to the
   * ignoredCall Hashmap, because the classes can be instantiated at a later time
   *
   * @param source the method which contains call
   * @param targetBase the base of the resolving. All subtypes of the declaring class are analyzed
   *     as potential targets
   * @param invokableStmt the statement causing the call
   * @return a stream of all method signatures of instantiated classes that can be resolved as
   *     target from the given base method signature.
   */
  private Stream<MethodSignature> resolveAllCallTargets(
      List<? extends SootClass> subclasses,
      MethodSignature source,
      MethodSignature targetBase,
      InvokableStmt invokableStmt) {
    return subclasses.stream()
        .flatMap(
            sootClass -> {
              if (!instantiatedClasses.contains(sootClass.getType())) {
                saveIgnoredCall(
                    source,
                    view.getIdentifierFactory()
                        .getMethodSignature(sootClass.getType(), targetBase.getSubSignature()),
                    invokableStmt);
                return Stream.empty();
              }
              SootMethod targetMethod =
                  findMethodInHierarchy(view, sootClass, targetBase.getSubSignature()).orElse(null);
              return Stream.ofNullable(targetMethod);
            })
        .map(SootMethod::getSignature);
  }

  private Stream<MethodSignature> resolveAllDefaultTargets(
      List<? extends SootClass> subclasses, MethodSignature targetSignature) {

    ArrayListMultimap<ClassType, ClassType> interfaces = ArrayListMultimap.create();
    // saves all interfaces and put the classes next to them
    subclasses.forEach(
        sootClass ->
            sootClass
                .getInterfaces()
                .forEach(interfaceClass -> interfaces.put(interfaceClass, sootClass.getType())));

    return typeHierarchy
        .subinterfacesOf(targetSignature.getDeclClassType())
        .flatMap(
            sootClass ->
                view
                    .getMethod(
                        view.getIdentifierFactory()
                            .getMethodSignature(sootClass, targetSignature.getSubSignature()))
                    .stream())
        .filter(sootMethod -> interfaces.containsKey(sootMethod.getDeclaringClassType()))
        .flatMap(
            interfaceMethod -> {
              // check if any of the classes which implement the interface are initialized
              List<ClassType> classes = interfaces.get(interfaceMethod.getDeclaringClassType());
              if (classes.stream().anyMatch(classType -> instantiatedClasses.contains(classType))) {
                // check if they have an default method
                return Stream.of(interfaceMethod.getSignature());
              }
              // does not need to be saved,
              // since it will be added later if this is a target of currently not instantiated
              // class
              return Stream.empty();
            });
  }

  private Stream<MethodSignature> resolveAllOverwrittenTargets(
      List<? extends SootClass> subclasses, MethodSubSignature targetMethodSignature) {
    return subclasses.stream()
        .filter(sootClass -> instantiatedClasses.contains(sootClass.getType()))
        .filter(sootClass -> sootClass.getMethod(targetMethodSignature).isEmpty())
        .flatMap(
            sootClass -> findMethodInHierarchy(view, sootClass, targetMethodSignature).stream())
        .map(SootMethod::getSignature);
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
    ignoredCalls.put(target.getDeclClassType(), new Call(source, target, invokableStmt));
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
      @NonNull MethodSignature sourceMethod,
      @NonNull Deque<MethodSignature> workList,
      @NonNull MutableCallGraph cg) {
    view.getClass(sourceMethod.getDeclClassType())
        .flatMap(c -> c.getMethod(sourceMethod.getSubSignature()))
        .ifPresent(
            sootMethod ->
                collectInstantiatedClassesInMethod(sootMethod)
                    .forEach(
                        classType -> {
                          instantiatedClasses.add(classType);
                          includeIgnoredCallsToClass(classType, cg, workList);
                        }));
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
      @NonNull ClassType classType,
      @NonNull MutableCallGraph cg,
      @NonNull Deque<MethodSignature> workList) {
    ignoredCalls.get(classType).stream()
        .filter(Objects::nonNull)
        .flatMap(
            call ->
                findConcreteMethod(view, call.targetMethodSignature()).stream()
                    .map(
                        sootMethod ->
                            new Call(
                                call.sourceMethodSignature(),
                                sootMethod.getSignature(),
                                call.invokableStmt())))
        .forEach(call -> addCallToCG(call, cg, workList));
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
      @NonNull MethodSignature sourceMethod,
      @NonNull Deque<MethodSignature> workList,
      @NonNull MutableCallGraph cg) {
    //    not needed
  }
}
