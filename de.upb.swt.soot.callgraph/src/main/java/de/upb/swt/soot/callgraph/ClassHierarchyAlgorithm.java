package de.upb.swt.soot.callgraph;

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

import de.upb.swt.soot.callgraph.typehierarchy.MethodDispatchResolver;
import de.upb.swt.soot.callgraph.typehierarchy.TypeHierarchy;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.model.*;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.signatures.MethodSubSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class ClassHierarchyAlgorithm extends AbstractCallGraphAlgorithm {
  @Nonnull private final View view;
  @Nonnull private final TypeHierarchy hierarchy;

  public ClassHierarchyAlgorithm(@Nonnull View view, @Nonnull TypeHierarchy hierarchy) {
    this.view = view;
    this.hierarchy = hierarchy;
  }

  @Nonnull
  @Override
  public CallGraph initialize(@Nonnull List<MethodSignature> entryPoints) {
    return constructCompleteCallGraph(view, entryPoints);
  }

  @Nonnull
  @Override
  public CallGraph addClass(@Nonnull CallGraph oldCallGraph, @Nonnull JavaClassType classType) {
    MutableCallGraph updated = oldCallGraph.copy();

    AbstractClass<? extends AbstractClassSource> clazz = view.getClassOrThrow(classType);
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
    List<ClassType> superClasses = hierarchy.superClassesOf(classType);
    Set<ClassType> implementedInterfaces = hierarchy.implementedInterfacesOf(classType);
    Stream<ClassType> superTypes =
        Stream.concat(superClasses.stream(), implementedInterfaces.stream());

    Set<MethodSubSignature> newMethodSubSigs =
        newMethodSignatures.stream()
            .map(methodSignature -> (MethodSubSignature) methodSignature.getSubSignature())
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
                  clazz
                      .getMethod((MethodSubSignature) overriddenMethodSig.getSubSignature())
                      .get()
                      .getSignature();

              for (MethodSignature callingMethodSig : oldCallGraph.callsTo(overriddenMethodSig)) {
                updated.addCall(callingMethodSig, overridingMethodSig);
              }
            });

    return updated;
  }

  @Override
  @Nonnull
  protected Stream<MethodSignature> resolveCall(SootMethod method, AbstractInvokeExpr invokeExpr) {
    MethodSignature targetMethodSignature = invokeExpr.getMethodSignature();
    Stream<MethodSignature> result = Stream.of(targetMethodSignature);

    //    if(((SootClass)
    // view.getClass(targetMethodSignature.getDeclClassType()).get()).isInterface()){
    //      return result;
    //    }

    Optional op =
        view.getClass(targetMethodSignature.getDeclClassType())
            .flatMap(clazz -> clazz.getMethod(targetMethodSignature));

    if (op.isPresent()) {
      SootMethod targetMethod = (SootMethod) op.get();
      if (Modifier.isStatic(targetMethod.getModifiers())) {
        return result;
      }
    }

    //    SootMethod targetMethod =
    //        (SootMethod)
    //                (view.getClass(targetMethodSignature.getDeclClassType()))
    //                .flatMap(clazz -> clazz.getMethod(targetMethodSignature)).get();
    //                .orElseThrow(
    //                    () ->
    //                        new ResolveException(
    //                            "Could not find " + targetMethodSignature + " in view"));

    return Stream.concat(
        result,
        MethodDispatchResolver.resolveAbstractDispatch(view, targetMethodSignature).stream());
  }
}
