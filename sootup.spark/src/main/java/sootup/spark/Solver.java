package sootup.spark;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2002-2026 Ondrej Lhotak, Kadiray Karakaya and others
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import sootup.callgraph.AbstractCallGraphAlgorithm;
import sootup.callgraph.CallGraph;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.callgraph.GraphBasedCallGraph;
import sootup.callgraph.MutableCallGraph;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.core.views.View;
import sootup.spark.node.AllocationNode;
import sootup.spark.node.Node;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
class Solver {

  private View view;
  private CallGraph callGraph;
  private PAG pag;
  private List<MethodSignature> entryPoints;
  private SparkOptions sparkOptions;

  /** Non-null only when {@link SparkOptions#isOnFlyCallGraph()} is set. */
  private IncrementalPointsToAnalysis incrementalAnalysis;

  @Builder
  Solver(View view, List<MethodSignature> entryPoints, SparkOptions sparkOptions) {
    this.view = view;
    this.entryPoints = entryPoints;
    this.sparkOptions = sparkOptions != null ? sparkOptions : SparkOptions.defaultOptions();
    this.pag = new PAG(this.sparkOptions);
    if (this.sparkOptions.isOnFlyCallGraph()) {
      GraphBasedCallGraph cg = new GraphBasedCallGraph(entryPoints);
      for (MethodSignature ep : entryPoints) {
        cg.addMethod(ep);
      }
      this.callGraph = cg;
      this.incrementalAnalysis = new IncrementalPointsToAnalysis(this.pag);
    } else {
      this.callGraph = new ClassHierarchyAnalysisAlgorithm(view).initialize(entryPoints);
      this.incrementalAnalysis = null;
    }
  }

  void solve() {
    if (sparkOptions.isOnFlyCallGraph()) {
      solveOnTheFly();
    } else {
      callGraph
          .getMethodSignatures()
          .forEach(
              methodSignature -> view.getMethod(methodSignature).ifPresent(this::buildMethodPAG));
    }
  }

  private void buildMethodPAG(SootMethod method) {
    if (!method.hasBody()) return;
    MethodPAGStmtVisitor stmtVisitor =
        MethodPAGStmtVisitor.builder()
            .methodSignature(method.getSignature())
            .PAG(pag)
            .callGraph(callGraph)
            .view(view)
            .nodeFactory(new NodeFactory(sparkOptions))
            .build();
    method.getBody().getStmts().forEach(stmt -> stmt.accept(stmtVisitor));
  }

  /**
   * On-the-fly call graph construction. The call graph starts empty (with just the entry points)
   * and grows as the points-to analysis discovers receivers at virtual call sites. Static and
   * special invocations are resolved at PAG-build time since their targets are determined by the
   * static type of the call.
   */
  private void solveOnTheFly() {
    MutableCallGraph cg = (MutableCallGraph) callGraph;
    NodeFactory nodeFactory = new NodeFactory(sparkOptions);
    Deque<MethodSignature> worklist = new ArrayDeque<>(entryPoints);
    Set<MethodSignature> processed = new HashSet<>();
    Map<MethodSignature, MethodPAGStmtVisitor> visitors = new HashMap<>();
    List<MethodPAGStmtVisitor.PendingVirtualCall> pending = new ArrayList<>();
    Set<ResolvedVirtualCall> resolved = new HashSet<>();

    boolean changed = true;
    while (changed) {
      changed = false;

      // 1. Build PAGs (and resolve direct invokes) for all newly-discovered methods.
      while (!worklist.isEmpty()) {
        MethodSignature sig = worklist.poll();
        if (!processed.add(sig)) continue;
        Optional<? extends SootMethod> methodOpt = view.getMethod(sig);
        if (methodOpt.isEmpty() || !methodOpt.get().hasBody()) continue;
        SootMethod method = methodOpt.get();
        MethodPAGStmtVisitor visitor =
            MethodPAGStmtVisitor.builder()
                .methodSignature(sig)
                .PAG(pag)
                .callGraph(cg)
                .view(view)
                .nodeFactory(nodeFactory)
                .otfContext(
                    new MethodPAGStmtVisitor.OtfContext(incrementalAnalysis, worklist, pending))
                .build();
        visitors.put(sig, visitor);
        method.getBody().getStmts().forEach(stmt -> stmt.accept(visitor));
        changed = true;
      }

      // 2. Bring points-to up to date with the edges added so far.
      incrementalAnalysis.propagate();

      // 3. Resolve pending virtual call sites against current points-to results.
      for (MethodPAGStmtVisitor.PendingVirtualCall call : pending) {
        Optional<Node> receiver =
            nodeFactory.createNode(
                ((sootup.core.jimple.common.expr.AbstractInstanceInvokeExpr) call.expr()).getBase(),
                call.srcSig());
        if (receiver.isEmpty()) continue;
        Set<AllocationNode> reaching = incrementalAnalysis.reachingObjects(receiver.get());
        for (AllocationNode o : reaching) {
          MethodSignature targetSig =
              dispatch(o.getType(), call.expr().getMethodSignature()).orElse(null);
          if (targetSig == null) continue;
          ResolvedVirtualCall key = new ResolvedVirtualCall(call.srcSig(), call.stmt(), targetSig);
          if (!resolved.add(key)) continue;

          // Add to call graph.
          if (!cg.containsMethod(targetSig)) {
            cg.addMethod(targetSig);
            worklist.add(targetSig);
          }
          if (!cg.containsCall(call.srcSig(), targetSig, call.stmt())) {
            cg.addCall(call.srcSig(), targetSig, call.stmt());
          }
          // Install this/param/return edges via the source method's visitor.
          MethodPAGStmtVisitor sourceVisitor = visitors.get(call.srcSig());
          if (sourceVisitor != null) {
            sourceVisitor.installCallEdges(call.expr(), call.lhs(), targetSig);
          }
          changed = true;
        }
      }
    }
  }

  /** Resolves a virtual / interface call given the runtime type of the receiver. */
  private Optional<MethodSignature> dispatch(Type allocType, MethodSignature calledSig) {
    if (!(allocType instanceof ClassType ct)) return Optional.empty();
    MethodSignature lookupSig =
        view.getIdentifierFactory().getMethodSignature(ct, calledSig.getSubSignature());
    return AbstractCallGraphAlgorithm.resolveConcreteDispatch(view, lookupSig);
  }

  private record ResolvedVirtualCall(
      MethodSignature srcSig,
      sootup.core.jimple.common.stmt.InvokableStmt stmt,
      MethodSignature targetSig) {}
}
