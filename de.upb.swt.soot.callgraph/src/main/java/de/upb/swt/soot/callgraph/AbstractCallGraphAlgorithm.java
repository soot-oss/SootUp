package de.upb.swt.soot.callgraph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Christian Brüggemann, Ben Hermann, Markus Schmidt and others
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

import de.upb.swt.soot.core.typehierarchy.TypeHierarchy;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Method;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.signatures.MethodSubSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCallGraphAlgorithm implements CallGraphAlgorithm {

  private static final Logger logger = LoggerFactory.getLogger(AbstractCallGraphAlgorithm.class);

  @Nonnull protected final View<? extends SootClass<?>> view;
  @Nonnull protected final TypeHierarchy typeHierarchy;

  protected AbstractCallGraphAlgorithm(
      @Nonnull View<? extends SootClass<?>> view, @Nonnull TypeHierarchy typeHierarchy) {
    this.view = view;
    this.typeHierarchy = typeHierarchy;
  }

  @Nonnull
  final CallGraph constructCompleteCallGraph(
      View<? extends SootClass<?>> view, List<MethodSignature> entryPoints) {
    MutableCallGraph cg = new GraphBasedCallGraph();

    Deque<MethodSignature> workList = new ArrayDeque<>(entryPoints);
    Set<MethodSignature> processed = new HashSet<>();

    processWorkList(view, workList, processed, cg);
    return cg;
  }

  /**
   * Processes all entries in the <code>workList</code>, skipping those present in <code>processed
   * </code>, adding call edges to the graph. Newly discovered methods are added to the <code>
   * workList</code> and processed as well. <code>cg</code> is updated accordingly.
   */
  final void processWorkList(
      View<? extends SootClass<?>> view,
      Deque<MethodSignature> workList,
      Set<MethodSignature> processed,
      MutableCallGraph cg) {
    while (!workList.isEmpty()) {
      MethodSignature currentMethodSignature = workList.pop();
      if (processed.contains(currentMethodSignature)) continue;

      if (!cg.containsMethod(currentMethodSignature)) cg.addMethod(currentMethodSignature);

      Stream<MethodSignature> invocationTargets =
          resolveAllCallsFromSourceMethod(view, currentMethodSignature);

      invocationTargets.forEach(
          t -> {
            if (!cg.containsMethod(t)) cg.addMethod(t);
            if (!cg.containsCall(currentMethodSignature, t)) {
              cg.addCall(currentMethodSignature, t);
              workList.push(t);
            }
          });
      processed.add(currentMethodSignature);

      postProcessingMethod(view, currentMethodSignature, workList, cg);
    }
  }

  @Nonnull
  Stream<MethodSignature> resolveAllCallsFromSourceMethod(
      View<? extends SootClass<?>> view, MethodSignature sourceMethod) {
    SootMethod currentMethodCandidate =
        view.getClass(sourceMethod.getDeclClassType())
            .flatMap(c -> c.getMethod(sourceMethod.getSubSignature()))
            .orElse(null);
    if (currentMethodCandidate == null) return Stream.empty();

    if (currentMethodCandidate.hasBody()) {
      return currentMethodCandidate.getBody().getStmtGraph().nodes().stream()
          .filter(Stmt::containsInvokeExpr)
          .flatMap(s -> resolveCall(currentMethodCandidate, s.getInvokeExpr()));
    } else {
      return Stream.empty();
    }
  }

  /** finds the given method signature in class's superclasses */
  final <T extends Method> T findMethodInHierarchy(
      @Nonnull View<? extends SootClass<?>> view, @Nonnull MethodSignature sig) {
    Optional<? extends SootClass> optSc = view.getClass(sig.getDeclClassType());

    if (optSc.isPresent()) {
      SootClass sc = optSc.get();

      List<ClassType> superClasses = typeHierarchy.superClassesOf(sc.getType());
      Set<ClassType> interfaces = typeHierarchy.implementedInterfacesOf(sc.getType());
      superClasses.addAll(interfaces);

      for (ClassType superClassType : superClasses) {
        Optional<? extends SootClass<?>> superClassOpt = view.getClass(superClassType);
        if (superClassOpt.isPresent()) {
          SootClass<?> superClass = superClassOpt.get();
          Optional<? extends SootMethod> methodOpt = superClass.getMethod(sig.getSubSignature());
          if (methodOpt.isPresent()) {
            return (T) methodOpt.get();
          }
        }
      }
      logger.warn(
          "Could not find \""
              + sig.getSubSignature()
              + "\" in "
              + sig.getDeclClassType().getClassName()
              + " and in its superclasses");
    } else {
      logger.trace("Could not find \"" + sig.getDeclClassType() + "\" in view");
    }
    return null;
  }

  /**
   * This method enables optional post processing of a method in the call graph algorithm
   *
   * @param view view
   * @param sourceMethod the processed method
   * @param workList the current worklist that might be extended
   * @param cg the current cg that might be extended
   */
  public void postProcessingMethod(
      View<? extends SootClass<?>> view,
      MethodSignature sourceMethod,
      @Nonnull Deque<MethodSignature> workList,
      @Nonnull MutableCallGraph cg) {
    // is only implemented if it is needed in the call graph algorithm
  }

  @Nonnull
  @Override
  public CallGraph addClass(@Nonnull CallGraph oldCallGraph, @Nonnull JavaClassType classType) {
    MutableCallGraph updated = oldCallGraph.copy();

    SootClass<?> clazz = view.getClassOrThrow(classType);
    Set<MethodSignature> newMethodSignatures =
        clazz.getMethods().stream().map(Method::getSignature).collect(Collectors.toSet());

    if (newMethodSignatures.stream().anyMatch(oldCallGraph::containsMethod)) {
      throw new IllegalArgumentException("CallGraph already contains methods from " + classType);
    }

    // Step 1: Add edges from the new methods to other methods
    Deque<MethodSignature> workList = new ArrayDeque<>(newMethodSignatures);
    Set<MethodSignature> processed = new HashSet<>(oldCallGraph.getMethodSignatures());
    processWorkList(view, workList, processed, updated);

    // Step 2: Add edges from old methods to methods overridden in the new class
    List<ClassType> superClasses = typeHierarchy.superClassesOf(classType);
    Set<ClassType> implementedInterfaces = typeHierarchy.implementedInterfacesOf(classType);
    Stream<ClassType> superTypes =
        Stream.concat(superClasses.stream(), implementedInterfaces.stream());

    Set<MethodSubSignature> newMethodSubSigs =
        newMethodSignatures.stream()
            .map(MethodSignature::getSubSignature)
            .collect(Collectors.toSet());

    superTypes
        .map(view::getClassOrThrow)
        .flatMap(superType -> superType.getMethods().stream())
        .map(Method::getSignature)
        .filter(
            superTypeMethodSig -> newMethodSubSigs.contains(superTypeMethodSig.getSubSignature()))
        .forEach(
            overriddenMethodSig -> {
              //noinspection OptionalGetWithoutIsPresent (We know this exists)
              MethodSignature overridingMethodSig =
                  clazz.getMethod(overriddenMethodSig.getSubSignature()).get().getSignature();

              for (MethodSignature callingMethodSig : updated.callsTo(overriddenMethodSig)) {
                updated.addCall(callingMethodSig, overridingMethodSig);
              }
            });

    return updated;
  }

  /**
   * The method iterates over all classes present in view, and finds method with name main &
   * SourceType - Library. This method is used by initialize() method used for creating call graph
   * and the call graph is created by considering the main method as an entry point.
   *
   * <p>The method throws an exception if there is no main method in any of the classes or if there
   * are more than one main method.
   *
   * @return - MethodSignature of main method.
   */
  public MethodSignature findMainMethod() {
    Set<SootClass<?>> classes = new HashSet<>(); /* Set to track the classes to check */
    for (SootClass<?> aClass : view.getClasses()) {
      if (!aClass.isLibraryClass()) {
        classes.add(aClass);
      }
    }

    Collection<SootMethod> mainMethods = new HashSet<>(); /* Set to store the methods */
    for (SootClass<?> aClass : classes) {
      for (SootMethod method : aClass.getMethods()) {
        if (method.isStatic()
            && method
                .getSignature()
                .equals(
                    JavaIdentifierFactory.getInstance()
                        .getMethodSignature(
                            "main",
                            aClass.getType(),
                            "void",
                            Collections.singletonList("java.lang.String[]")))) {
          mainMethods.add(method);
        }
      }
    }

    if (mainMethods.size() > 1) {
      throw new RuntimeException(
          "There are more than 1 main method present.\n Below main methods are found: \n"
              + mainMethods
              + "\n initialize() method can be used if only one main method exists. \n You can specify these main methods as entry points by passing them as parameter to initialize method.");
    } else if (mainMethods.size() == 0) {
      throw new RuntimeException(
          "No main method is present in the input programs. initialize() method can be used if only one main method exists in the input program and that should be used as entry point for call graph. \n Please specify entry point as a parameter to initialize method.");
    }

    return mainMethods.stream().findFirst().get().getSignature();
  }

  @Nonnull
  abstract Stream<MethodSignature> resolveCall(SootMethod method, AbstractInvokeExpr invokeExpr);
}
