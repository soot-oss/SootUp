package de.upb.soot.callgraph;

import de.upb.soot.core.Method;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.common.expr.AbstractInvokeExpr;
import de.upb.soot.jimple.common.stmt.Stmt;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.views.View;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public abstract class AbstractCallGraphAlgorithm implements CallGraphAlgorithm {

  @Nonnull
  protected CallGraph constructCompleteCallGraph(View view, List<MethodSignature> entryPoints) {
    MutableCallGraph cg = new GraphBasedCallGraph();

    Deque<MethodSignature> workList = new ArrayDeque<>(entryPoints);
    Set<MethodSignature> processed = new HashSet<>();

    while (!workList.isEmpty()) {
      MethodSignature currentMethodSignature = workList.pop();
      if (processed.contains(currentMethodSignature)) continue;

      Stream<MethodSignature> invocationTargets =
          resolveAllCallsFromSourceMethod(view, currentMethodSignature);

      invocationTargets.forEach(
          t -> {
            if (!cg.hasNode(currentMethodSignature)) cg.addNode(currentMethodSignature);
            if (!cg.hasEdge(currentMethodSignature, t)) {
              if (!cg.hasNode(t)) cg.addNode(t);
              cg.addEdge(currentMethodSignature, t);
              workList.push(t);
            }
          });
      processed.add(currentMethodSignature);
    }
    return cg;
  }

  @Nonnull
  protected Stream<MethodSignature> resolveAllCallsFromSourceMethod(
      View view, MethodSignature sourceMethod) {
    Method currentMethodCandidate =
        view.getClass(sourceMethod.getDeclClassType())
            .flatMap(c -> c.getMethod(sourceMethod))
            .orElse(null);
    if (!(currentMethodCandidate instanceof SootMethod)) return Stream.empty();

    SootMethod currentMethod = (SootMethod) currentMethodCandidate;

    if (currentMethod.hasBody()) {
      return currentMethod.getBody().getStmts().stream()
          .filter(Stmt::containsInvokeExpr)
          .flatMap(s -> resolveCall(currentMethod, s.getInvokeExpr()));
    } else {
      return Stream.empty();
    }
  }

  @Nonnull
  protected abstract Stream<MethodSignature> resolveCall(
      SootMethod method, AbstractInvokeExpr invokeExpr);
}
