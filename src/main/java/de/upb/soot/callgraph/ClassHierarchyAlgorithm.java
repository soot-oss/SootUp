package de.upb.soot.callgraph;

import de.upb.soot.core.Modifier;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.common.expr.AbstractInvokeExpr;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.typehierarchy.MethodDispatchResolver;
import de.upb.soot.typehierarchy.TypeHierarchy;
import de.upb.soot.views.View;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

/**
 * This class implements CHA (Class Hierarchy Algorithm)
 *
 * @author Markus Schmidt
 * @author Christian Brüggemann
 * @author Ben Hermann
 */
public class ClassHierarchyAlgorithm extends AbstractCallGraphAlgorithm {
  @Nonnull private final View view;
  @Nonnull private final TypeHierarchy hierarchy;

  public ClassHierarchyAlgorithm(View view, TypeHierarchy hierarchy) {
    this.view = view;
    this.hierarchy = hierarchy;
  }

  @Override
  public CallGraph initialize(@Nonnull List<MethodSignature> entryPoints) {
    return constructCompleteCallGraph(view, entryPoints);
  }

  @Override
  @Nonnull
  protected Stream<MethodSignature> resolveCall(SootMethod method, AbstractInvokeExpr invokeExpr) {
    MethodSignature targetMethodSignature = invokeExpr.getMethodSignature();

    if (Modifier.isStatic(targetMethodSignature.getModifiers())) {
      return Stream.of(targetMethodSignature);
    } else {
      return MethodDispatchResolver.resolveAbstractDispatch(view, targetMethodSignature).stream();
    }
  }
}
