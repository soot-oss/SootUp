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
import org.jspecify.annotations.NonNull;
import sootup.core.IdentifierFactory;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;
import sootup.core.jimple.common.expr.JInterfaceInvokeExpr;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.model.MethodModifier;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
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
  public ClassHierarchyAnalysisAlgorithm(@NonNull View view) {
    super(view);
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
    if (!optInvokeExpr.isPresent()) {
      return Stream.empty();
    }
    AbstractInvokeExpr invokeExpr = optInvokeExpr.get();
    MethodSignature targetMethodSignature = invokeExpr.getMethodSignature();
    if ((invokeExpr instanceof JDynamicInvokeExpr)) {
      return Stream.empty();
    }

    SootMethod targetMethod = findConcreteMethod(view, targetMethodSignature).orElse(null);

    if (targetMethod == null
        || MethodModifier.isStatic(targetMethod.getModifiers())
        || (invokeExpr instanceof JSpecialInvokeExpr)) {
      return Stream.of(targetMethodSignature);
    } else {
      ArrayList<ClassType> noImplementedMethod = new ArrayList<>();
      List<MethodSignature> targets =
          resolveAllCallTargets(targetMethodSignature, noImplementedMethod);
      if (!targetMethod.isAbstract()) {
        targets.add(targetMethod.getSignature());
      }
      if (invokeExpr instanceof JInterfaceInvokeExpr) {
        IdentifierFactory factory = view.getIdentifierFactory();
        noImplementedMethod.stream()
            .map(
                classType ->
                    resolveConcreteDispatch(
                        view,
                        factory.getMethodSignature(
                            classType, targetMethodSignature.getSubSignature())))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(targets::add);
      }
      return targets.stream();
    }
  }

  private List<MethodSignature> resolveAllCallTargets(
      MethodSignature targetMethodSignature, ArrayList<ClassType> noImplementedMethod) {
    ArrayList<MethodSignature> targets = new ArrayList<>();
    view.getTypeHierarchy()
        .subtypesOf(targetMethodSignature.getDeclClassType())
        .forEach(
            classType -> {
              SootClass clazz = view.getClass(classType).orElse(null);
              if (clazz == null) {
                return;
              }
              // check if method is implemented
              SootMethod method =
                  clazz.getMethod(targetMethodSignature.getSubSignature()).orElse(null);
              if (method != null && !method.isAbstract()) {
                targets.add(method.getSignature());
              }
              // save classes with no implementation of the searched method
              if (method == null && !clazz.isInterface()) {
                noImplementedMethod.add(classType);
              }
              // collect all default methods
              clazz
                  .getInterfaces()
                  .forEach(
                      interfaceType -> {
                        SootMethod defaultMethod =
                            view.getMethod(
                                    view.getIdentifierFactory()
                                        .getMethodSignature(
                                            interfaceType, targetMethodSignature.getSubSignature()))
                                .orElse(null);
                        // contains an implemented default method
                        if (defaultMethod != null && !defaultMethod.isAbstract()) {
                          targets.add(defaultMethod.getSignature());
                        }
                      });
            });
    return targets;
  }

  @Override
  protected void postProcessingMethod(
      MethodSignature sourceMethod,
      @NonNull Deque<MethodSignature> workList,
      @NonNull MutableCallGraph cg) {
    // do nothing
  }

  @Override
  protected void preProcessingMethod(
      MethodSignature sourceMethod,
      @NonNull Deque<MethodSignature> workList,
      @NonNull MutableCallGraph cg) {
    // do nothing
  }
}
