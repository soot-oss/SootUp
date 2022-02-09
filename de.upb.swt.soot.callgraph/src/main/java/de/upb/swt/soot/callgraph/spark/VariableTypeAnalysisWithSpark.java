package de.upb.swt.soot.callgraph.spark;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2021 Kadiray Karakaya
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
import de.upb.swt.soot.callgraph.algorithm.AbstractCallGraphAlgorithm;
import de.upb.swt.soot.callgraph.algorithm.ClassHierarchyAnalysisAlgorithm;
import de.upb.swt.soot.callgraph.model.CallGraph;
import de.upb.swt.soot.callgraph.spark.pag.PointerAssignmentGraph;
import de.upb.swt.soot.callgraph.spark.pag.nodes.AllocationNode;
import de.upb.swt.soot.callgraph.typehierarchy.MethodDispatchResolver;
import de.upb.swt.soot.callgraph.typehierarchy.TypeHierarchy;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.common.expr.JNewExpr;
import de.upb.swt.soot.core.jimple.common.expr.JSpecialInvokeExpr;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class VariableTypeAnalysisWithSpark extends AbstractCallGraphAlgorithm {

  @Nonnull private Set<ClassType> instantiatedClasses = new HashSet<>();
  @Nonnull private CallGraph chaGraph;
  @Nonnull private PointerAssignmentGraph pag;

  public VariableTypeAnalysisWithSpark(
      @Nonnull View<? extends SootClass<?>> view,
      @Nonnull TypeHierarchy typeHierarchy,
      CallGraph chaGraph,
      PointerAssignmentGraph pag) {
    super(view, typeHierarchy);
    this.chaGraph = chaGraph;
    this.pag = pag;
  }

  @Nonnull
  @Override
  public CallGraph initialize(@Nonnull List<MethodSignature> entryPoints) {
    if (chaGraph.isEmpty()) {
      ClassHierarchyAnalysisAlgorithm cha = new ClassHierarchyAnalysisAlgorithm(view, typeHierarchy);
      chaGraph = cha.initialize(entryPoints);
    }
    return constructCompleteCallGraph(view, entryPoints);
  }

  @Override
  @Nonnull
  protected Stream<MethodSignature> resolveCall(SootMethod method, AbstractInvokeExpr invokeExpr) {
    MethodSignature targetMethodSignature = invokeExpr.getMethodSignature();
    Set<MethodSignature> result = Sets.newHashSet(targetMethodSignature);

    if (!chaGraph.containsMethod(method.getSignature())) {
      return result.stream();
    }
    collectInstantiatedAndAssignedClassesInMethod(method);

    SootMethod targetMethod =
        view.getClass(targetMethodSignature.getDeclClassType())
            .flatMap(clazz -> clazz.getMethod(targetMethodSignature.getSubSignature()))
            .orElseGet(() -> findMethodInHierarchy(view, targetMethodSignature));

    if (Modifier.isStatic(targetMethod.getModifiers())
        || (invokeExpr instanceof JSpecialInvokeExpr)) {
      return result.stream();
    } else {
      Set<MethodSignature> implAndOverrides =
          MethodDispatchResolver.resolveAbstractDispatchInClasses(
              view, targetMethodSignature, instantiatedClasses);
      result.addAll(implAndOverrides);
      return result.stream();
    }
  }

  private void collectInstantiatedAndAssignedClassesInMethod(SootMethod method) {
    Set<ClassType> instantiated = new HashSet<>();
    List<AllocationNode> allocationNodes = pag.getAssignedAllocationNodes(method);
    pag.getAllocationEdges();
    for (AllocationNode node : allocationNodes) {
      if (node.getNewExpr() instanceof JNewExpr) {
        JNewExpr newExpr = (JNewExpr) node.getNewExpr();
        if (newExpr.getType() instanceof ClassType) {
          instantiated.add((ClassType) newExpr.getType());
        }
      } else if (node.getNewExpr() instanceof JavaClassType) {
        JavaClassType newExpr = (JavaClassType) node.getNewExpr();
        instantiated.add(newExpr);
      }
    }
    instantiatedClasses.addAll(instantiated);
  }
}
