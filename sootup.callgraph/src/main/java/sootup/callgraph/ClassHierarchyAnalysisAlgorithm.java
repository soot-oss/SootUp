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
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.model.MethodModifier;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.typehierarchy.MethodDispatchResolver;
import sootup.core.views.View;

/**
 * This class implements the Class Hierarchy Analysis call graph algorithm. In this algorithm, every
 * virtual call is resolved to the all implemented overwritten methods of subclasses in the entire
 * class path.
 */
public class ClassHierarchyAnalysisAlgorithm extends AbstractCallGraphAlgorithm {

  /**
   * The constructor of the CHA algorithm.
   *
   * @param view it contains the data of the classes and methods
   */
  public ClassHierarchyAnalysisAlgorithm(@Nonnull View<? extends SootClass<?>> view) {
    super(view);
  }

  @Nonnull
  @Override
  public CallGraph initialize() {
    return constructCompleteCallGraph(view, Collections.singletonList(findMainMethod()));
  }

  @Nonnull
  @Override
  public CallGraph initialize(@Nonnull List<MethodSignature> entryPoints) {
    return constructCompleteCallGraph(view, entryPoints);
  }

  /**
   * In the CHA algorithm, every virtual call is resolved by only using the hierarchy. Every
   * subclass of the class is considered as target if it contains an implementation of the methods
   * called in the invoke expression.
   *
   * @param method the method object that contains the given invoke expression in the body.
   * @param invokeExpr it contains the call which is resolved.
   * @return a stream containing all reachable method signatures after applying the CHA call graph
   *     algorithm
   */
  @Override
  @Nonnull
  protected Stream<MethodSignature> resolveCall(SootMethod method, AbstractInvokeExpr invokeExpr) {
    MethodSignature targetMethodSignature = invokeExpr.getMethodSignature();
    if ((invokeExpr instanceof JDynamicInvokeExpr)) {
      return Stream.empty();
    }

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
      if (targetMethod.isAbstract()) {
        return resolveAllSubClassCallTargets(targetMethodSignature);
      }
      return MethodDispatchResolver.resolveConcreteDispatch(view, targetMethodSignature)
          .map(
              methodSignature ->
                  Stream.concat(
                      Stream.of(methodSignature),
                      resolveAllSubClassCallTargets(targetMethodSignature)))
          .orElseGet(() -> resolveAllSubClassCallTargets(targetMethodSignature));
    }
  }

  private Stream<MethodSignature> resolveAllSubClassCallTargets(
      MethodSignature targetMethodSignature) {
    return MethodDispatchResolver.resolveAllDispatches(view, targetMethodSignature).stream()
        .map(
            methodSignature ->
                MethodDispatchResolver.resolveConcreteDispatch(view, methodSignature))
        .filter(Optional::isPresent)
        .map(Optional::get);
  }

  @Override
  protected void postProcessingMethod(
      View<? extends SootClass<?>> view,
      MethodSignature sourceMethod,
      @Nonnull Deque<MethodSignature> workList,
      @Nonnull MutableCallGraph cg) {
    // do nothing
  }

  @Override
  protected void preProcessingMethod(
      View<? extends SootClass<?>> view,
      MethodSignature sourceMethod,
      @Nonnull Deque<MethodSignature> workList,
      @Nonnull MutableCallGraph cg) {
    // do nothing
  }
}
