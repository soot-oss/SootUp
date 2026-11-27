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
import lombok.extern.slf4j.Slf4j;
import sootup.callgraph.CallGraph;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.callgraph.GraphBasedCallGraph;
import sootup.callgraph.MethodLookup;
import sootup.callgraph.MutableCallGraph;
import sootup.callgraph.implicit.ImplicitCallContext;
import sootup.callgraph.implicit.ImplicitCallModel;
import sootup.callgraph.invokedynamic.DynamicInvokeResolver;
import sootup.callgraph.invokedynamic.FunctionalObject;
import sootup.callgraph.reflection.ReflectionModel;
import sootup.callgraph.scope.DefaultCallResolver;
import sootup.callgraph.scope.VirtualCallResolver;
import sootup.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import sootup.core.jimple.common.expr.JInterfaceInvokeExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.core.views.View;
import sootup.spark.node.AllocationNode;
import sootup.spark.node.LambdaAllocationNode;
import sootup.spark.node.Node;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
class Solver {

  private View view;
  private final MethodLookup methodLookup;
  private CallGraph callGraph;
  private PAG pag;
  private List<MethodSignature> entryPoints;
  private SparkOptions sparkOptions;
  private ReflectionModel reflectionModel;
  private DynamicInvokeResolver dynamicInvokeResolver;
  private ImplicitCallModel implicitCallModel;

  /** CHA mode: the implicit invocations of each method's call statements, see {@link #solve()}. */
  private Map<MethodSignature, Map<InvokableStmt, List<AbstractInstanceInvokeExpr>>>
      implicitInvokes = new HashMap<>();

  /** Non-null only when {@link SparkOptions#isOnFlyCallGraph()} is set. */
  private IncrementalPointsToAnalysis incrementalAnalysis;

  @Builder
  Solver(
      View view,
      List<MethodSignature> entryPoints,
      SparkOptions sparkOptions,
      CallGraph callGraph,
      ReflectionModel reflectionModel,
      DynamicInvokeResolver dynamicInvokeResolver,
      ImplicitCallModel implicitCallModel) {
    this.view = view;
    this.methodLookup = new MethodLookup(view);
    this.reflectionModel = reflectionModel != null ? reflectionModel : ReflectionModel.none();
    this.dynamicInvokeResolver =
        dynamicInvokeResolver != null
            ? dynamicInvokeResolver
            : DynamicInvokeResolver.bootstrapMethodHandles();
    this.implicitCallModel =
        implicitCallModel != null ? implicitCallModel : ImplicitCallModel.threadStart();
    this.entryPoints = entryPoints;
    this.sparkOptions = sparkOptions != null ? sparkOptions : SparkOptions.defaultOptions();
    this.pag = new PAG(this.sparkOptions);
    if (this.sparkOptions.isOnFlyCallGraph()) {
      if (callGraph != null) {
        log.warn(
            "a pre-built call graph was supplied but onFlyCallGraph is enabled; discarding it "
                + "since OTF mode grows its own call graph incrementally from the entry points");
      }
      requireEntryPoints(entryPoints);
      GraphBasedCallGraph cg = new GraphBasedCallGraph(entryPoints);
      for (MethodSignature ep : entryPoints) {
        cg.addMethod(ep);
      }
      this.callGraph = cg;
      this.incrementalAnalysis = new IncrementalPointsToAnalysis(this.pag);
    } else if (callGraph != null) {
      this.callGraph = callGraph;
      this.incrementalAnalysis = null;
    } else {
      requireEntryPoints(entryPoints);
      this.callGraph =
          new ClassHierarchyAnalysisAlgorithm(
                  view,
                  new DefaultCallResolver(view),
                  VirtualCallResolver.all(),
                  true,
                  this.reflectionModel,
                  this.dynamicInvokeResolver,
                  this.implicitCallModel)
              .initialize(entryPoints);
      this.incrementalAnalysis = null;
    }
  }

  private static void requireEntryPoints(List<MethodSignature> entryPoints) {
    if (entryPoints == null || entryPoints.isEmpty()) {
      throw new IllegalArgumentException(
          "entryPoints is required unless a pre-built callGraph is supplied");
    }
  }

  void solve() {
    if (sparkOptions.isOnFlyCallGraph()) {
      solveOnTheFly();
    } else {
      List<SootMethod> methods =
          callGraph.getMethodSignatures().stream()
              .flatMap(sig -> view.getMethod(sig).stream())
              .filter(SootMethod::hasBody)
              .map(SootMethod.class::cast)
              .toList();
      Map<MethodSignature, List<FunctionalObject>> functionalObjects = new HashMap<>();
      for (SootMethod method : methods) {
        body(method).getStmts().stream()
            .flatMap(stmt -> FunctionalObject.of(stmt, dynamicInvokeResolver, view).stream())
            .forEach(
                fo ->
                    functionalObjects
                        .computeIfAbsent(fo.dispatchedImplementation(view), k -> new ArrayList<>())
                        .add(fo));
      }
      for (SootMethod method : methods) {
        Map<InvokableStmt, List<AbstractInstanceInvokeExpr>> invokes =
            implicitInvokes(body(method));
        if (!invokes.isEmpty()) {
          implicitInvokes.put(method.getSignature(), invokes);
        }
      }
      methods.forEach(method -> buildMethodPAG(method, functionalObjects));
    }
  }

  /**
   * The implicit invocations of the call statements in {@code body}, resolved against the call
   * graph the PAG is built on, as the algorithm that built it resolved them.
   */
  private Map<InvokableStmt, List<AbstractInstanceInvokeExpr>> implicitInvokes(Body body) {
    ImplicitCallContext context =
        ImplicitCallContext.of(
            methodLookup,
            SootMethod::isConcrete,
            (caller, stmt) -> Optional.of(callTargetsAt(caller, stmt)));
    Map<InvokableStmt, List<AbstractInstanceInvokeExpr>> result = new HashMap<>();
    for (Stmt stmt : body.getStmts()) {
      if (!stmt.isInvokableStmt() || stmt.asInvokableStmt().getInvokeExpr().isEmpty()) continue;
      InvokableStmt invokableStmt = stmt.asInvokableStmt();
      List<AbstractInstanceInvokeExpr> invokes =
          implicitCallModel.resolve(context, body, invokableStmt);
      if (!invokes.isEmpty()) {
        result.put(invokableStmt, invokes);
      }
    }
    return result;
  }

  /** The targets of the call graph at {@code stmt} with the subsignature of its invocation. */
  private Set<MethodSignature> callTargetsAt(MethodSignature caller, InvokableStmt stmt) {
    if (!callGraph.containsMethod(caller)) return Set.of();
    MethodSubSignature subSignature =
        stmt.getInvokeExpr().orElseThrow().getMethodSignature().getSubSignature();
    Set<MethodSignature> targets = new HashSet<>();
    for (CallGraph.Call call : callGraph.callsFrom(caller)) {
      if (call.invokableStmt() == stmt
          && call.targetMethodSignature().getSubSignature().equals(subSignature)) {
        targets.add(call.targetMethodSignature());
      }
    }
    return targets;
  }

  private List<AbstractInstanceInvokeExpr> implicitInvokesAt(
      MethodSignature method, InvokableStmt stmt) {
    return implicitInvokes.getOrDefault(method, Map.of()).getOrDefault(stmt, List.of());
  }

  private Body body(SootMethod method) {
    return dynamicInvokeResolver.desugar(
        method, reflectionModel.resolve(method, method.getBody()), view);
  }

  private void buildMethodPAG(
      SootMethod method, Map<MethodSignature, List<FunctionalObject>> functionalObjects) {
    if (!method.hasBody()) return;
    MethodPAGStmtVisitor stmtVisitor =
        MethodPAGStmtVisitor.builder()
            .methodSignature(method.getSignature())
            .PAG(pag)
            .callGraph(callGraph)
            .view(view)
            .nodeFactory(new NodeFactory(sparkOptions, view))
            .reflectionModel(reflectionModel)
            .dynamicInvokeResolver(dynamicInvokeResolver)
            .functionalObjects(functionalObjects)
            .implicitInvokes(implicitInvokes.getOrDefault(method.getSignature(), Map.of()))
            .build();
    body(method).getStmts().forEach(stmt -> stmt.accept(stmtVisitor));
  }

  /**
   * On-the-fly call graph construction. The call graph starts empty (with just the entry points)
   * and grows as the points-to analysis discovers receivers at virtual call sites. Static and
   * special invocations are resolved at PAG-build time since their targets are determined by the
   * static type of the call.
   */
  private void solveOnTheFly() {
    MutableCallGraph cg = (MutableCallGraph) callGraph;
    NodeFactory nodeFactory = new NodeFactory(sparkOptions, view);
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
                    new MethodPAGStmtVisitor.OtfContext(
                        incrementalAnalysis, worklist, pending, methodLookup))
                .reflectionModel(reflectionModel)
                .dynamicInvokeResolver(dynamicInvokeResolver)
                .build();
        visitors.put(sig, visitor);
        body(method).getStmts().forEach(stmt -> stmt.accept(visitor));
        changed = true;
      }

      // 2. Bring points-to up to date with the edges added so far.
      incrementalAnalysis.propagate();

      // 3. Resolve pending virtual call sites against current points-to results.
      for (MethodPAGStmtVisitor.PendingVirtualCall call : pending) {
        AbstractInstanceInvokeExpr expr = (AbstractInstanceInvokeExpr) call.expr();
        Optional<Node> receiver = nodeFactory.createNode(expr.getBase(), call.srcSig());
        if (receiver.isEmpty()) continue;
        Set<AllocationNode> reaching = incrementalAnalysis.reachingObjects(receiver.get());
        for (AllocationNode o : reaching) {
          FunctionalObject fo = answeringFunctionalObject(o, expr);
          MethodSignature targetSig = target(o, expr).orElse(null);
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
          if (sourceVisitor != null && fo != null) {
            sourceVisitor.installLambdaCallEdges(expr, call.lhs(), call.stmt(), fo, targetSig);
          } else if (sourceVisitor != null) {
            sourceVisitor.installCallEdges(expr, call.lhs(), targetSig);
          }
          changed = true;
        }
      }
    }
  }

  /** The functional object of {@code o} if it answers the call {@code expr}, else null. */
  private FunctionalObject answeringFunctionalObject(
      AllocationNode o, AbstractInstanceInvokeExpr expr) {
    return o instanceof LambdaAllocationNode lambda
            && lambda
                .getFunctionalObject()
                .answers(expr.getMethodSignature(), view.getTypeHierarchy())
        ? lambda.getFunctionalObject()
        : null;
  }

  /** The method that the virtual or interface call {@code expr} runs on the object {@code o}. */
  private Optional<MethodSignature> target(AllocationNode o, AbstractInstanceInvokeExpr expr) {
    FunctionalObject fo = answeringFunctionalObject(o, expr);
    return fo != null
        ? Optional.of(fo.dispatchedImplementation(view))
        : dispatch(o.getType(), expr.getMethodSignature());
  }

  /**
   * The call graph that the solved points-to sets give, like Soot builds after a SPARK run without
   * an on-the-fly call graph: the calls of the call graph the PAG was built on, without the virtual
   * and interface calls that no object the receiver points to dispatches to, and only the methods
   * that stay reachable from the entry methods. The other calls, e.g. static calls and calls to
   * static initializers, are kept.
   */
  CallGraph pointsToCallGraph(PointsToAnalysis pta) {
    List<MethodSignature> entryMethods = callGraph.getEntryMethods();
    GraphBasedCallGraph result = new GraphBasedCallGraph(new ArrayList<>(entryMethods));
    Deque<MethodSignature> worklist = new ArrayDeque<>();
    for (MethodSignature entryMethod : entryMethods) {
      if (callGraph.containsMethod(entryMethod) && !result.containsMethod(entryMethod)) {
        result.addMethod(entryMethod);
        worklist.add(entryMethod);
      }
    }
    Map<CallSite, Set<MethodSignature>> virtualTargets = new HashMap<>();
    Map<CallSite, Set<MethodSignature>> implicitTargets = new HashMap<>();
    while (!worklist.isEmpty()) {
      MethodSignature source = worklist.poll();
      for (CallGraph.Call call : callGraph.callsFrom(source)) {
        MethodSignature target = call.targetMethodSignature();
        InvokableStmt stmt = call.invokableStmt();
        CallSite callSite = new CallSite(source, stmt);
        List<AbstractInstanceInvokeExpr> implicit = implicitInvokesAt(source, stmt);
        // an implicit call is kept by the objects its own receiver points to
        if (implicit.stream()
            .anyMatch(
                e -> e.getMethodSignature().getSubSignature().equals(target.getSubSignature()))) {
          if (implicitTargets
              .computeIfAbsent(callSite, site -> implicitTargets(pta, source, implicit))
              .contains(target)) {
            addCall(result, worklist, source, target, stmt);
            continue;
          }
          if (!stmt.getInvokeExpr()
              .orElseThrow()
              .getMethodSignature()
              .getSubSignature()
              .equals(target.getSubSignature())) {
            continue;
          }
        }
        Optional<AbstractInstanceInvokeExpr> virtualCall = virtualCall(stmt);
        if (virtualCall.isPresent()
            && !virtualTargets
                .computeIfAbsent(callSite, site -> virtualTargets(pta, source, virtualCall.get()))
                .contains(target)) {
          continue;
        }
        addCall(result, worklist, source, target, stmt);
      }
    }
    return result;
  }

  private static void addCall(
      GraphBasedCallGraph result,
      Deque<MethodSignature> worklist,
      MethodSignature source,
      MethodSignature target,
      InvokableStmt stmt) {
    if (!result.containsMethod(target)) {
      result.addMethod(target);
      worklist.add(target);
    }
    result.addCallIfAbsent(source, target, stmt);
  }

  private Set<MethodSignature> implicitTargets(
      PointsToAnalysis pta, MethodSignature source, List<AbstractInstanceInvokeExpr> invokes) {
    Set<MethodSignature> targets = new HashSet<>();
    for (AbstractInstanceInvokeExpr invoke : invokes) {
      targets.addAll(virtualTargets(pta, source, invoke));
    }
    return targets;
  }

  private static Optional<AbstractInstanceInvokeExpr> virtualCall(InvokableStmt stmt) {
    return stmt.getInvokeExpr()
        .filter(e -> e instanceof JVirtualInvokeExpr || e instanceof JInterfaceInvokeExpr)
        .map(AbstractInstanceInvokeExpr.class::cast);
  }

  private Set<MethodSignature> virtualTargets(
      PointsToAnalysis pta, MethodSignature source, AbstractInstanceInvokeExpr expr) {
    Set<MethodSignature> targets = new HashSet<>();
    for (AllocationNode o : pta.reachingObjects(expr.getBase(), source)) {
      target(o, expr).ifPresent(targets::add);
    }
    return targets;
  }

  private record CallSite(MethodSignature source, InvokableStmt stmt) {}

  /** Resolves a virtual / interface call given the runtime type of the receiver. */
  private Optional<MethodSignature> dispatch(Type allocType, MethodSignature calledSig) {
    if (allocType instanceof ArrayType) {
      // an array runs the methods of java.lang.Object
      allocType = view.getIdentifierFactory().getClassType("java.lang.Object");
    }
    if (!(allocType instanceof ClassType ct)) return Optional.empty();
    // resolves calledSig, then selects the method the receiver class runs; cached
    return methodLookup.dispatch(ct, calledSig).map(SootMethod::getSignature);
  }

  private record ResolvedVirtualCall(
      MethodSignature srcSig,
      sootup.core.jimple.common.stmt.InvokableStmt stmt,
      MethodSignature targetSig) {}
}
