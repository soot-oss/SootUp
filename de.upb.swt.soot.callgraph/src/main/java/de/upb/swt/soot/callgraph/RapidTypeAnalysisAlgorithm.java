package de.upb.swt.soot.callgraph;

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
import de.upb.swt.soot.callgraph.typehierarchy.MethodDispatchResolver;
import de.upb.swt.soot.callgraph.typehierarchy.TypeHierarchy;
import de.upb.swt.soot.core.jimple.common.expr.*;
import de.upb.swt.soot.core.model.*;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.views.View;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class RapidTypeAnalysisAlgorithm extends AbstractCallGraphAlgorithm {

  private static class Call {
    @Nonnull final MethodSignature source;
    @Nonnull final MethodSignature target;

    private Call(@Nonnull MethodSignature source, MethodSignature target) {
      this.source = source;
      this.target = target;
    }
  }

  @Nonnull private Set<ClassType> instantiatedClasses = new HashSet<>();
  @Nonnull private HashMap<ClassType, List<Call>> ignoredCalls = new HashMap<>();
  @Nonnull private CallGraph chaGraph;

  public RapidTypeAnalysisAlgorithm(@Nonnull View view, @Nonnull TypeHierarchy typeHierarchy) {
    super(view, typeHierarchy);
  }

  @Nonnull
  @Override
  public CallGraph initialize(@Nonnull List<MethodSignature> entryPoints) {
    ClassHierarchyAnalysisAlgorithm cha = new ClassHierarchyAnalysisAlgorithm(view, typeHierarchy);
    chaGraph = cha.initialize(entryPoints);
    return constructCompleteCallGraph(view, entryPoints);
  }

  private void collectInstantiatedClassesInMethod(SootMethod method) {
    Set<ClassType> instantiated =
        chaGraph.callsFrom(method.getSignature()).stream()
            .filter(s -> s.getSubSignature().getName().equals("<init>"))
            .map(s -> s.getDeclClassType())
            .collect(Collectors.toSet());
    instantiatedClasses.addAll(instantiated);

    // add also found classes' super classes
    instantiated.stream()
        .map(s -> view.getClass(s))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(s -> s.getSuperclass())
        .filter(s -> s.isPresent())
        .map(s -> s.get())
        .forEach(instantiatedClasses::add);
  }

  @Override
  @Nonnull
  protected Stream<MethodSignature> resolveCall(SootMethod method, AbstractInvokeExpr invokeExpr) {
    MethodSignature targetMethodSignature = invokeExpr.getMethodSignature();
    Stream<MethodSignature> result = Stream.of(targetMethodSignature);

    if (!chaGraph.containsMethod(method.getSignature())) {
      return result;
    }
    collectInstantiatedClassesInMethod(method);

    SootMethod targetMethod =
        view.getClass(targetMethodSignature.getDeclClassType())
            .flatMap(clazz -> clazz.getMethod(targetMethodSignature.getSubSignature()))
            .orElseGet(() -> findMethodInHierarchy(view, targetMethodSignature));

    if (targetMethod == null
        || Modifier.isStatic(targetMethod.getModifiers())
        || (invokeExpr instanceof JSpecialInvokeExpr)) {
      return result;
    } else {
      Set<MethodSignature> notInstantiatedCallTargets = Sets.newHashSet();
      Set<MethodSignature> implAndOverrides =
          MethodDispatchResolver.resolveAbstractDispatchInClasses(
              view, targetMethodSignature, instantiatedClasses, notInstantiatedCallTargets);

      notInstantiatedCallTargets.forEach(
          ignoredMethodSignature -> {
            List<Call> calls = ignoredCalls.get(ignoredMethodSignature.getDeclClassType());
            if (calls == null) {
              calls = new ArrayList<>();
              calls.add(new Call(method.getSignature(), ignoredMethodSignature));
              ignoredCalls.put(ignoredMethodSignature.getDeclClassType(), calls);
            } else {
              calls.add(new Call(method.getSignature(), ignoredMethodSignature));
            }
          });

      return Stream.concat(result, implAndOverrides.stream());
    }
  }

  /**
   * Post processing of a method in the RTA call graph algorithm
   *
   * <p>RTA has to add previously ignored calls since a later found instantiation of the class could
   * enables a call to the ignored method.
   *
   * @param view view
   * @param sourceMethod the processed method
   * @param workList the current worklist that is extended by methods that have to be analyzed.
   * @param cg the current cg is extended by new call targets and calls
   */
  @Override
  public void postProcessingMethod(
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
                  if (cg.containsMethod(call.target)) {
                    // method is already analyzed or is in the work list, simply add the call
                    cg.addCall(call.source, call.target);
                  } else {
                    // new target method found that has to be analyzed
                    cg.addMethod(call.target);
                    cg.addCall(call.source, call.target);
                    workList.push(call.target);
                  }
                });
            // can be removed because the instantiated class will be considered in future resolves
            ignoredCalls.remove(instantiatedClassType);
          }
        });
  }
}
