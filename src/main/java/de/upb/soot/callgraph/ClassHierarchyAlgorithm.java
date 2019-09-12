package de.upb.soot.callgraph;

import de.upb.soot.core.Method;
import de.upb.soot.core.Modifier;
import de.upb.soot.core.SootMethod;
import de.upb.soot.frontends.ResolveException;
import de.upb.soot.jimple.common.expr.AbstractInvokeExpr;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.typehierarchy.MethodDispatchResolver;
import de.upb.soot.typehierarchy.TypeHierarchy;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.views.View;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
  public CallGraph addClassToCallGraph(
      @Nonnull CallGraph oldCallGraph, @Nonnull JavaClassType classType) {
    MutableCallGraph updated = oldCallGraph.copy();

    Deque<MethodSignature> workList =
        view.getClass(classType)
            .orElseThrow(() -> new ResolveException("Could not find " + classType + " in view"))
            .getMethods().stream()
            .map(Method::getSignature)
            .collect(Collectors.toCollection(ArrayDeque::new));
    Set<MethodSignature> processed = new HashSet<>(oldCallGraph.getMethodSignatures());
    processWorkList(view, workList, processed, updated);

    return updated;
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
