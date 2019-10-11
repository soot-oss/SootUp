package de.upb.swt.soot.callgraph;

import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Method;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.views.View;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public abstract class AbstractCallGraphAlgorithm implements CallGraphAlgorithm {

  @Nonnull
  final CallGraph constructCompleteCallGraph(View view, List<MethodSignature> entryPoints) {
    MutableCallGraph cg = new GraphBasedCallGraph();

    Deque<MethodSignature> workList = new ArrayDeque<>(entryPoints);
    Set<MethodSignature> processed = new HashSet<>();

    processWorkList(view, workList, processed, cg);
    return cg;
  }

  /**
   * Processes all entries in the <code>workList</code>, skipping those present in <code>processed
   * </code>, adding call edges to the graph. Newly discovered methods are added to the <code>
   * workList</code> and processed as well. <code>cg</code> is updated accordingly.
   */
  final void processWorkList(
      View view,
      Deque<MethodSignature> workList,
      Set<MethodSignature> processed,
      MutableCallGraph cg) {
    while (!workList.isEmpty()) {
      MethodSignature currentMethodSignature = workList.pop();
      if (processed.contains(currentMethodSignature)) continue;

      Stream<MethodSignature> invocationTargets =
          resolveAllCallsFromSourceMethod(view, currentMethodSignature);

      invocationTargets.forEach(
          t -> {
            if (!cg.containsMethod(currentMethodSignature)) cg.addMethod(currentMethodSignature);
            if (!cg.containsMethod(t)) cg.addMethod(t);
            if (!cg.containsCall(currentMethodSignature, t)) {
              cg.addCall(currentMethodSignature, t);
              workList.push(t);
            }
          });
      processed.add(currentMethodSignature);
    }
  }

  @Nonnull
  Stream<MethodSignature> resolveAllCallsFromSourceMethod(View view, MethodSignature sourceMethod) {
    Method currentMethodCandidate =
        view.getClass(sourceMethod.getDeclClassType())
            .flatMap(c -> c.getMethod(sourceMethod))
            .orElse(null);
    if (currentMethodCandidate == null) return Stream.empty();

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
  abstract Stream<MethodSignature> resolveCall(SootMethod method, AbstractInvokeExpr invokeExpr);
}
