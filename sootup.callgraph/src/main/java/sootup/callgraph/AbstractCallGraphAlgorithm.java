package sootup.callgraph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2022 Christian Brüggemann, Ben Hermann, Markus Schmidt, Jonas Klauke and others
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.callgraph.CallGraph.Call;
import sootup.core.IdentifierFactory;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JStaticInvokeExpr;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Method;
import sootup.core.model.SootClass;
import sootup.core.model.SootClassMember;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.typehierarchy.HierarchyComparator;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * The AbstractCallGraphAlgorithm class is the super class of all call graph algorithm. It provides
 * basic methods used in all call graph algorithm. It is abstract since it has no implemented
 * functionality to resolve method calls because it is decided by the applied call graph algorithm
 */
public abstract class AbstractCallGraphAlgorithm implements CallGraphAlgorithm {

  private static final Logger logger = LoggerFactory.getLogger(AbstractCallGraphAlgorithm.class);

  @NonNull protected final View view;

  protected AbstractCallGraphAlgorithm(@NonNull View view) {
    this.view = view;
  }

  /**
   * This method starts the construction of the call graph algorithm. It initializes the needed
   * objects for the call graph generation and calls processWorkList method.
   *
   * @param entryPoints a list of method signatures that will be added to the work list in the call
   *     graph generation.
   * @return the complete constructed call graph starting from the entry methods.
   */
  @NonNull
  final CallGraph constructCompleteCallGraph(List<MethodSignature> entryPoints) {
    Deque<MethodSignature> workList = new ArrayDeque<>(entryPoints);
    Set<MethodSignature> processed = new HashSet<>();

    // find additional entry points
    List<MethodSignature> clinits = getClinitFromEntryPoints(entryPoints);

    workList.addAll(clinits);
    MutableCallGraph cg = initializeCallGraph(entryPoints, clinits);

    processWorkList(workList, processed, cg);
    return cg;
  }

  /**
   * This method creates the mutable call graph which is used in the call graph algorithm. Overwrite
   * it to change the used mutable call graph
   *
   * @return the initialized call graph used in the call graph algorithm
   */
  protected MutableCallGraph initializeCallGraph(
      List<MethodSignature> entryPoints, List<MethodSignature> clinits) {
    ArrayList<MethodSignature> rootSignatures = new ArrayList<>(entryPoints);
    rootSignatures.addAll(clinits);
    return new GraphBasedCallGraph(rootSignatures);
  }

  /**
   * This method returns a list of static initializers that should be considered by the given entry
   * points
   *
   * @param entryPoints the entry points of the call graph algorithm
   */
  protected List<MethodSignature> getClinitFromEntryPoints(List<MethodSignature> entryPoints) {
    return entryPoints.stream()
        .map(
            methodSignature ->
                getSignatureOfImplementedStaticInitializer(methodSignature.getDeclClassType()))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  private Optional<MethodSignature> getSignatureOfImplementedStaticInitializer(
      ClassType classType) {
    return view.getMethod(view.getIdentifierFactory().getStaticInitializerSignature(classType))
        .map(SootClassMember::getSignature);
  }

  /**
   * Processes all entries in the <code>workList</code>, skipping those present in <code>processed
   * </code>, adding call edges to the graph. Newly discovered methods are added to the <code>
   * workList</code> and processed as well. <code>cg</code> is updated accordingly. The method
   * postProcessingMethod is called after a method is processed in the <code>workList</code>.
   *
   * @param workList it contains all method that have to be processed in the call graph generation.
   *     This list is filled in the execution with found call targets in the call graph algorithm.
   * @param processed the list of processed method to only process the method once.
   * @param cg the call graph object that is filled with the found methods and call edges.
   */
  final void processWorkList(
      Deque<MethodSignature> workList, Set<MethodSignature> processed, MutableCallGraph cg) {
    while (!workList.isEmpty()) {
      MethodSignature currentMethodSignature = workList.pop();
      // skip if already processed
      if (processed.contains(currentMethodSignature)) {
        continue;
      }

      // skip if library class
      SootClass currentClass =
          view.getClass(currentMethodSignature.getDeclClassType()).orElse(null);
      if (currentClass == null || currentClass.isLibraryClass()) {
        continue;
      }

      // perform pre-processing if needed
      preProcessingMethod(currentMethodSignature, workList, cg);

      // process the method
      if (!cg.containsMethod(currentMethodSignature)) {
        cg.addMethod(currentMethodSignature);
      }

      // transform the method signature to the actual SootMethod
      SootMethod currentMethod =
          currentClass.getMethod(currentMethodSignature.getSubSignature()).orElse(null);

      // get all call targets of invocations in the method body
      resolveAllCallsFromSourceMethod(currentMethod, cg, workList);

      // get all call targets of implicit edges in the method body
      resolveAllImplicitCallsFromSourceMethod(currentMethod, cg, workList);

      // set method as processed
      processed.add(currentMethodSignature);

      // perform post-processing if needed
      postProcessingMethod(currentMethodSignature, workList, cg);
    }
  }

  /**
   * Adds the defined call to the given call graph. If the source or target method was added as
   * vertex to the call graph, they will be added to the worklist
   *
   * @param source the method signature of the caller
   * @param target the method signature of the callee
   * @param invokeStmt the stmt causing the call
   * @param cg the call graph that will be updated
   * @param workList the worklist in which the method signature of newly added vertexes will be
   *     added
   */
  protected void addCallToCG(
      @NonNull MethodSignature source,
      @NonNull MethodSignature target,
      @NonNull InvokableStmt invokeStmt,
      @NonNull MutableCallGraph cg,
      @NonNull Deque<MethodSignature> workList) {
    if (!cg.containsMethod(source)) {
      cg.addMethod(source);
      workList.push(source);
    }
    if (!cg.containsMethod(target)) {
      cg.addMethod(target);
      workList.push(target);
    }
    if (!cg.containsCall(source, target, invokeStmt)) {
      cg.addCall(source, target, invokeStmt);
    }
  }

  /**
   * This method resolves all calls from a given source method. resolveCall is called for each
   * invokable statements in the body of the source method that is implemented in the corresponding
   * call graph algorithm. If new methods will be added as vertexes in the call graph, the work list
   * will be updated
   *
   * @param sourceMethod this signature is used to access the statements contained method body of
   *     the specified method
   * @param cg the call graph that will receive the found calls
   * @param workList the work list that will be updated of found target methods
   */
  protected void resolveAllCallsFromSourceMethod(
      SootMethod sourceMethod, MutableCallGraph cg, Deque<MethodSignature> workList) {
    if (sourceMethod == null || !sourceMethod.hasBody()) {
      return;
    }

    sourceMethod.getBody().getStmts().stream()
        .filter(Stmt::isInvokableStmt)
        .map(Stmt::asInvokableStmt)
        .forEach(
            stmt ->
                resolveCall(sourceMethod, stmt)
                    .forEach(
                        targetMethod ->
                            addCallToCG(
                                sourceMethod.getSignature(), targetMethod, stmt, cg, workList)));
  }

  /**
   * It resolves all implicit calls caused by the given source method
   *
   * @param sourceMethod the inspected source method
   * @param cg new calls will be added to the call graph
   * @param workList new target methods will be added to the work list
   */
  protected void resolveAllImplicitCallsFromSourceMethod(
      SootMethod sourceMethod, MutableCallGraph cg, Deque<MethodSignature> workList) {
    if (sourceMethod == null || !sourceMethod.hasBody()) {
      return;
    }

    // collect all static initializer calls
    resolveAllStaticInitializerCalls(sourceMethod, cg, workList);
  }

  /**
   * It resolves all static initializer calls caused by the given source method
   *
   * @param sourceMethod the inspected source method
   * @param cg clinit calls will be added to the call graph
   * @param workList found clinit methods will be added to the work list
   */
  protected void resolveAllStaticInitializerCalls(
      SootMethod sourceMethod, MutableCallGraph cg, Deque<MethodSignature> workList) {
    if (sourceMethod == null || !sourceMethod.hasBody()) {
      return;
    }
    InstantiateClassValueVisitor instantiateVisitor = new InstantiateClassValueVisitor();
    sourceMethod.getBody().getStmts().stream()
        .filter(Stmt::isInvokableStmt)
        .map(Stmt::asInvokableStmt)
        .forEach(
            invokableStmt -> {
              // static field usage
              ClassType targetClass = null;
              if (invokableStmt.containsFieldRef()
                  && invokableStmt.getFieldRef() instanceof JStaticFieldRef) {
                targetClass = invokableStmt.getFieldRef().getFieldSignature().getDeclClassType();
                addStaticInitializerCalls(
                    sourceMethod.getSignature(), targetClass, invokableStmt, cg, workList);
              }
              // static method
              if (invokableStmt.containsInvokeExpr()) {
                // static method call
                Optional<AbstractInvokeExpr> exprOptional = invokableStmt.getInvokeExpr();
                if (exprOptional.isEmpty()) return;
                AbstractInvokeExpr expr = exprOptional.get();
                if (expr instanceof JStaticInvokeExpr) {
                  ClassType newTargetClass = expr.getMethodSignature().getDeclClassType();
                  // checks if the field points to the same clinit
                  if (!newTargetClass.equals(targetClass)) {
                    addStaticInitializerCalls(
                        sourceMethod.getSignature(), newTargetClass, invokableStmt, cg, workList);
                  }
                }
              } else {
                if (invokableStmt instanceof JAssignStmt) {
                  Value rightOp = ((JAssignStmt) invokableStmt).getRightOp();
                  // extract class type out of new, new array and new multi array
                  instantiateVisitor.init();
                  rightOp.accept(instantiateVisitor);
                  ClassType newTargetClass = instantiateVisitor.getResult();
                  // check if class type is the same as in the field which could be on the left op
                  if (newTargetClass != null && !newTargetClass.equals(targetClass)) {
                    addStaticInitializerCalls(
                        sourceMethod.getSignature(), newTargetClass, invokableStmt, cg, workList);
                  }
                }
              }
            });
  }

  /**
   * Adds all static initializer calls of the given targetClass. An edge from the sourceSig to all
   * clinit methods of the targetClass and Superclasses will be added to the call graph. If new
   * target methods will be found, the worklist will be updated.
   *
   * @param sourceSig the source method causing the static initilzer call
   * @param targetClass the class that is statically initialized
   * @param invokableStmt the statement causing the call
   * @param cg the call graph that will contain the found calls
   * @param workList the work list that will be updated with new target methods
   */
  private void addStaticInitializerCalls(
      MethodSignature sourceSig,
      ClassType targetClass,
      InvokableStmt invokableStmt,
      MutableCallGraph cg,
      Deque<MethodSignature> workList) {
    // static initializer call of class
    view.getMethod(view.getIdentifierFactory().getStaticInitializerSignature(targetClass))
        .ifPresent(
            targetSig ->
                addCallToCG(sourceSig, targetSig.getSignature(), invokableStmt, cg, workList));
    // static initializer calls of all superclasses
    view.getTypeHierarchy()
        .superClassesOf(targetClass)
        .map(
            classType ->
                view.getMethod(
                    view.getIdentifierFactory().getStaticInitializerSignature(classType)))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach(
            targetSig ->
                addCallToCG(sourceSig, targetSig.getSignature(), invokableStmt, cg, workList));
  }

  /**
   * This method enables optional pre-processing of a method in the call graph algorithm
   *
   * @param sourceMethod the processed method
   * @param workList the current work list that might be extended
   * @param cg the current cg that might be extended
   */
  protected abstract void preProcessingMethod(
      MethodSignature sourceMethod,
      @NonNull Deque<MethodSignature> workList,
      @NonNull MutableCallGraph cg);

  /**
   * This method enables optional post-processing of a method in the call graph algorithm
   *
   * @param sourceMethod the processed method
   * @param workList the current work list that might be extended
   * @param cg the current cg that might be extended
   */
  protected abstract void postProcessingMethod(
      MethodSignature sourceMethod,
      @NonNull Deque<MethodSignature> workList,
      @NonNull MutableCallGraph cg);

  @NonNull
  @Override
  public CallGraph addClass(@NonNull CallGraph oldCallGraph, @NonNull ClassType classType) {
    SootClass clazz = view.getClassOrThrow(classType);
    Set<MethodSignature> newMethodSignatures =
        clazz.getMethods().stream()
            .map(Method::getSignature)
            .filter(methodSig -> !oldCallGraph.containsMethod(methodSig))
            .collect(Collectors.toSet());

    // were all the added method signatures already visited in the CallGraph? i.e. is there
    // something to add?
    if (newMethodSignatures.isEmpty()) {
      return oldCallGraph;
    }

    MutableCallGraph updated = oldCallGraph.copy();

    // Step 1: Add edges from the new methods to other methods
    Deque<MethodSignature> workList = new ArrayDeque<>(newMethodSignatures);
    Set<MethodSignature> processed = new HashSet<>(oldCallGraph.getMethodSignatures());
    processWorkList(workList, processed, updated);

    // Step 2: Add edges from old methods to methods overridden in the new class
    Stream<ClassType> superClasses = view.getTypeHierarchy().superClassesOf(classType);
    Stream<ClassType> implementedInterfaces =
        view.getTypeHierarchy().implementedInterfacesOf(classType);
    Stream<ClassType> superTypes = Stream.concat(superClasses, implementedInterfaces);

    Set<MethodSubSignature> newMethodSubSigs =
        newMethodSignatures.stream()
            .map(MethodSignature::getSubSignature)
            .collect(Collectors.toSet());

    superTypes
        .map(view::getClass)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .flatMap(superType -> superType.getMethods().stream())
        .map(Method::getSignature)
        .filter(
            superTypeMethodSig -> newMethodSubSigs.contains(superTypeMethodSig.getSubSignature()))
        .forEach(
            overriddenMethodSig -> {
              //noinspection OptionalGetWithoutIsPresent (We know this exists)
              MethodSignature overridingMethodSig =
                  clazz.getMethod(overriddenMethodSig.getSubSignature()).get().getSignature();

              if (updated.containsMethod(overriddenMethodSig)) {
                for (Call calls : updated.callsTo(overriddenMethodSig)) {
                  updated.addCall(
                      calls.getSourceMethodSignature(),
                      overridingMethodSig,
                      calls.getInvokableStmt());
                }
              }
            });

    return updated;
  }

  /**
   * The method iterates over all classes present in view, and finds method with name main and
   * SourceType - Application. This method is used by initialize() method used for creating call
   * graph and the call graph is created by considering the main method as an entry point.
   *
   * <p>The method throws an exception if there is no main method in any of the classes or if there
   * are more than one main method.
   *
   * @return - MethodSignature of main method.
   */
  public MethodSignature findMainMethod() {
    Collection<SootMethod> mainMethods =
        view.getClasses()
            .filter(aClass -> !aClass.isLibraryClass())
            .flatMap(aClass -> aClass.getMethods().stream())
            .filter(method -> method.isStatic() && method.isMain(view.getIdentifierFactory()))
            .collect(Collectors.toSet());

    if (mainMethods.size() > 1) {
      throw new RuntimeException(
          "There are more than 1 main method present.\n Below main methods are found: \n"
              + mainMethods
              + "\n initialize() method can be used if only one main method exists. \n You can specify these main methods as entry points by passing them as parameter to initialize method.");
    } else if (mainMethods.isEmpty()) {
      throw new RuntimeException(
          "No main method is present in the input programs. initialize() method can be used if only one main method exists in the input program and that should be used as entry point for call graph. \n Please specify entry point as a parameter to initialize method.");
    }

    return mainMethods.stream().findFirst().get().getSignature();
  }

  /**
   * This method resolves the possible targets of a given invoke expression. The results are
   * dependable of the applied call graph algorithm. therefore, it is abstract.
   *
   * @param method the method object that contains the given invoke expression in the body.
   * @param invokableStmt it contains the call which is resolved.
   * @return a stream of all reachable method signatures defined by the applied call graph
   *     algorithm.
   */
  @NonNull
  protected abstract Stream<MethodSignature> resolveCall(
      SootMethod method, InvokableStmt invokableStmt);

  /**
   * Searches for the signature of the method that is the concrete implementation of <code>m</code>.
   * This is done by checking each superclass and the class itself for whether it contains the
   * concrete implementation.
   */
  @NonNull
  public static Optional<MethodSignature> resolveConcreteDispatch(View view, MethodSignature m) {
    Optional<? extends SootMethod> methodOp = findConcreteMethod(view, m);
    if (methodOp.isPresent()) {
      SootMethod method = methodOp.get();
      if (method.isAbstract()) {
        return Optional.empty();
      }
      return Optional.of(method.getSignature());
    }
    return Optional.empty();
  }

  /**
   * searches the method object in the given hierarchy
   *
   * @param view it contains all classes
   * @param sig the signature of the searched method
   * @return the found method object, or null if the method was not found.
   */
  public static Optional<SootMethod> findConcreteMethod(
      @NonNull View view, @NonNull MethodSignature sig) {
    IdentifierFactory identifierFactory = view.getIdentifierFactory();
    SootClass startclass = view.getClass(sig.getDeclClassType()).orElse(null);
    if (startclass == null) {
      logger.warn(
          "Could not find \""
              + sig.getDeclClassType()
              + "\" of method"
              + sig
              + " to resolve the concrete method");
      return Optional.empty();
    }
    Optional<SootMethod> startMethod =
        startclass.getMethod(sig.getSubSignature()).map(method -> (SootMethod) method);
    if (startMethod.isPresent()) {
      return startMethod;
    }
    TypeHierarchy typeHierarchy = view.getTypeHierarchy();

    Stream<ClassType> superClasses = typeHierarchy.superClassesOf(sig.getDeclClassType());
    Iterator<ClassType> iterator = superClasses.iterator();
    while (iterator.hasNext()) {
      ClassType superClassType = iterator.next();
      Optional<SootMethod> method =
          view.getMethod(
                  identifierFactory.getMethodSignature(superClassType, sig.getSubSignature()))
              .map(sm -> (SootMethod) sm);
      if (method.isPresent()) {
        return method;
      }
    }

    // interface1 is a sub-interface of interface2
    // interface1 is a super-interface of interface2
    // due to multiple inheritance in interfaces
    final HierarchyComparator hierarchyComparator =
        new HierarchyComparator(view.getTypeHierarchy());
    Optional<SootMethod> defaultMethod =
        typeHierarchy
            .implementedInterfacesOf(sig.getDeclClassType())
            .map(
                classType ->
                    view.getMethod(
                        identifierFactory.getMethodSignature(classType, sig.getSubSignature())))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .min(
                (m1, m2) ->
                    hierarchyComparator.compare(
                        m1.getDeclaringClassType(), m2.getDeclaringClassType()))
            .map(method -> (SootMethod) method);

    if (defaultMethod.isPresent()) {
      return defaultMethod;
    }
    logger.warn(
        "Could not find \""
            + sig.getSubSignature()
            + "\" in "
            + sig.getDeclClassType().getClassName()
            + " and in its superclasses and interfaces");
    return Optional.empty();
  }
}
