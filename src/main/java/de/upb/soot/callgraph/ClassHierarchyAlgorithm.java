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
public class ClassHierarchyAlgorithm implements CallGraphAlgorithm {

  TypeHierarchy hierarchy;
  CallGraph callGraph;
  private final View view;

  public ClassHierarchyAlgorithm(View view) {
    this.view = view;
  }

  @Override
  public CallGraph initialize(List<MethodSignature> entryPoints, TypeHierarchy hierarchy) {

    this.hierarchy = hierarchy;
    CallGraph cg = new AdjacencyList();

    Deque<MethodSignature> workList = new ArrayDeque<>(entryPoints);
    Set<MethodSignature> processed = new HashSet<>();

    while(!workList.isEmpty()) {
      MethodSignature currentMethodSignature = workList.pop();
      Optional<? extends Method> currentMethodCandidate =
              view.getClass(currentMethodSignature.getDeclClassType())
              .map(c -> c.getMethod(currentMethodSignature))
              .orElse(null);
      if (!currentMethodCandidate.isPresent() || !(currentMethodCandidate.get() instanceof SootMethod)) continue;
      SootMethod currentMethod = (SootMethod)currentMethodCandidate.get();

      if (processed.contains(currentMethodSignature)) continue;

      if (currentMethod.hasBody()) {
        Stream<MethodSignature> invocationTargets =
                currentMethod.getBody().getStmts().stream()
                .filter(s -> s.containsInvokeExpr())
                .flatMap(s -> resolveCall(currentMethod, s.getInvokeExpr()));
        invocationTargets.forEach(t -> {
          if (!cg.hasNode(currentMethodSignature)) cg.addNode(currentMethodSignature);
          if (!cg.hasEdge(currentMethodSignature, t)) {
            if (!cg.hasNode(t)) cg.addNode(t);
            cg.addEdge(currentMethodSignature, t);
            workList.push(t);
          }
        });
        processed.add(currentMethod.getSignature());
      }

    }
    return cg;
  }

  private Stream<MethodSignature> resolveCall(SootMethod method, AbstractInvokeExpr invokeExpr) {
    MethodSignature targetMethodSignature = invokeExpr.getMethodSignature();

    if (Modifier.isStatic(targetMethodSignature.getModifiers())) {
      return Stream.of(targetMethodSignature);
    } else {
      return MethodDispatchResolver.resolveAbstractDispatch(view, targetMethodSignature).stream();
    }
  }

}
