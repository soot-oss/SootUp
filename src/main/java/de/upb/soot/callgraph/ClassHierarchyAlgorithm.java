package de.upb.soot.callgraph;

import de.upb.soot.core.Method;
import de.upb.soot.core.Modifier;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.common.expr.AbstractInvokeExpr;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.typehierarchy.MethodDispatchResolver;
import de.upb.soot.typehierarchy.TypeHierarchy;
import de.upb.soot.views.View;

import java.util.*;
import java.util.stream.Stream;

/**
 * This class implements CHA (Class Hierarchy Algorithm)
 *
 * @author Markus Schmidt
 * @author Christian Brüggemann
 * @author Ben Hermann
 */
public class ClassHierarchyAlgorithm extends AbstractCallGraphAlgorithm {
  private final View view;
  private final TypeHierarchy hierarchy;

  public ClassHierarchyAlgorithm(View view, TypeHierarchy hierarchy) {
    this.view = view;
    this.hierarchy = hierarchy;
  }

  @Override
  public CallGraph initialize(List<MethodSignature> entryPoints) {
    return constructCompleteCallGraph(view, entryPoints);
  }

  @Override
  protected Stream<MethodSignature> resolveCall(SootMethod method, AbstractInvokeExpr invokeExpr) {
    MethodSignature targetMethodSignature = invokeExpr.getMethodSignature();

    if (Modifier.isStatic(targetMethodSignature.getModifiers())) {
      return Stream.of(targetMethodSignature);
    } else {
      return MethodDispatchResolver.resolveAbstractDispatch(view, targetMethodSignature).stream();
    }
  }

}
