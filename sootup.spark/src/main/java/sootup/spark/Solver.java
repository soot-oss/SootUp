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
import java.util.Collections;
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
import sootup.callgraph.AbstractCallGraphAlgorithm;
import sootup.callgraph.CallGraph;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.callgraph.GraphBasedCallGraph;
import sootup.callgraph.MutableCallGraph;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.core.types.VoidType;
import sootup.core.views.View;
import sootup.spark.node.AllocationNode;
import sootup.spark.node.Node;
import sootup.spark.node.ReflectiveClassToken;
import sootup.spark.node.ReflectiveMethodToken;
import sootup.spark.node.StringConstantNode;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
class Solver {

  private View view;
  private CallGraph callGraph;
  private PAG pag;
  private List<MethodSignature> entryPoints;
  private SparkOptions sparkOptions;

  /** Non-null only when {@link SparkOptions#isOnFlyCallGraph()} is set. */
  private IncrementalPointsToAnalysis incrementalAnalysis;

  @Builder
  Solver(
      View view,
      List<MethodSignature> entryPoints,
      SparkOptions sparkOptions,
      CallGraph callGraph) {
    this.view = view;
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
      this.callGraph = new ClassHierarchyAnalysisAlgorithm(view).initialize(entryPoints);
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
    List<MethodPAGStmtVisitor.PendingReflectiveForName> pendingReflectiveForName =
        new ArrayList<>();
    Set<ResolvedVirtualCall> resolved = new HashSet<>();
    Set<ResolvedReflectiveCall> resolvedReflective = new HashSet<>();
    Set<ResolvedReflectiveForName> resolvedReflectiveForName = new HashSet<>();
    Set<ResolvedReflectiveMethodCall> resolvedReflectiveMethod = new HashSet<>();

    boolean changed = true;
    int iteration = 0;
    while (changed) {
      changed = false;
      iteration++;

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
                        incrementalAnalysis, worklist, pending, pendingReflectiveForName))
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
          if (o instanceof ReflectiveClassToken token) {
            ResolvedReflectiveCall key =
                new ResolvedReflectiveCall(call.srcSig(), call.stmt(), token.getRepresented());
            if (!resolvedReflective.add(key)) continue;
            if (resolveReflectiveCall(token, call, cg, worklist, nodeFactory)) changed = true;
            continue;
          }
          if (o instanceof ReflectiveMethodToken methodToken) {
            ResolvedReflectiveMethodCall key =
                new ResolvedReflectiveMethodCall(
                    call.srcSig(),
                    call.stmt(),
                    methodToken.getDeclaringClass(),
                    methodToken.getMethodName());
            if (!resolvedReflectiveMethod.add(key)) continue;
            if (resolveReflectiveMethodInvoke(methodToken, call, cg, worklist, nodeFactory)) {
              changed = true;
            }
            continue;
          }
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

      // 4. Resolve pending Class.forName(arg) sites whose arg wasn't a literal at the call site,
      // in case a StringConstantNode carrying a resolvable literal now reaches it -- e.g. forwarded
      // as a parameter from some caller's literal argument, at any depth of indirection, since a
      // StringConstantNode rides the same PAG edges as any other allocation once created (see
      // ValueToNodeConversionVisitor.caseStringConstant).
      for (MethodPAGStmtVisitor.PendingReflectiveForName call : pendingReflectiveForName) {
        Optional<Node> argNode = nodeFactory.createNode(call.arg(), call.srcSig());
        if (argNode.isEmpty()) continue;
        Set<AllocationNode> reaching = incrementalAnalysis.reachingObjects(argNode.get());
        for (AllocationNode o : reaching) {
          if (!(o instanceof StringConstantNode stringNode)) continue;
          ResolvedReflectiveForName key =
              new ResolvedReflectiveForName(call.srcSig(), call.stmt(), stringNode.getValue());
          if (!resolvedReflectiveForName.add(key)) continue;

          MethodPAGStmtVisitor sourceVisitor = visitors.get(call.srcSig());
          if (sourceVisitor == null) continue;
          if (sourceVisitor.wireReflectiveClassToken(
              stringNode.getValue(), call.lhs(), call.callType())) {
            changed = true;
          }
        }
      }

      // Printed with System.err directly (not the SLF4J `log` field) since a *consuming*
      // project's classpath may have no SLF4J binding at all -- in that case `log.info`/`log.warn`
      // are silently a no-op, which would defeat the whole point of this: on a large, densely
      // interconnected program, this fixpoint can run for a long time (or exhaust the heap)
      // before converging, and printing progress every iteration -- flushed immediately, not
      // deferred to process exit -- means a crash mid-solve still leaves a visible trail of how
      // far the call graph and pending-call queues had grown, instead of losing every
      // intermediate number the way waiting for a final summary would.
      System.err.println(
          "[Spark OTF] iteration="
              + iteration
              + " methods="
              + cg.getMethodSignatures().size()
              + " pendingVirtualCalls="
              + pending.size()
              + " pendingReflectiveForName="
              + pendingReflectiveForName.size()
              + " resolved="
              + resolved.size()
              + " resolvedReflective="
              + resolvedReflective.size()
              + " resolvedReflectiveForName="
              + resolvedReflectiveForName.size()
              + " resolvedReflectiveMethod="
              + resolvedReflectiveMethod.size()
              + " changed="
              + changed);
    }
  }

  /** Resolves a virtual / interface call given the runtime type of the receiver. */
  private Optional<MethodSignature> dispatch(Type allocType, MethodSignature calledSig) {
    if (!(allocType instanceof ClassType ct)) return Optional.empty();
    MethodSignature lookupSig =
        view.getIdentifierFactory().getMethodSignature(ct, calledSig.getSubSignature());
    return AbstractCallGraphAlgorithm.resolveConcreteDispatch(view, lookupSig);
  }

  /**
   * Routes a call made on a {@link ReflectiveClassToken} receiver -- i.e. a call site whose
   * receiver traces back to a {@code Class.forName("literal")} -- to whichever reflective operation
   * it represents. Recognizes {@code newInstance()} (on either {@code Class} or {@code
   * Constructor}), {@code getConstructor()}/{@code getDeclaredConstructor()}, and {@code
   * getMethod()}/{@code getDeclaredMethod()}; anything else called on the token (e.g. {@code
   * getName()}) is left unresolved, same as before this feature existed.
   */
  private boolean resolveReflectiveCall(
      ReflectiveClassToken token,
      MethodPAGStmtVisitor.PendingVirtualCall call,
      MutableCallGraph cg,
      Deque<MethodSignature> worklist,
      NodeFactory nodeFactory) {
    String calledName = call.expr().getMethodSignature().getName();
    if (calledName.equals("newInstance")) {
      return instantiateReflectively(token, call, cg, worklist, nodeFactory);
    }
    if (calledName.equals("getConstructor") || calledName.equals("getDeclaredConstructor")) {
      return propagateReflectiveToken(token, call, nodeFactory);
    }
    if (calledName.equals("getMethod") || calledName.equals("getDeclaredMethod")) {
      return propagateReflectiveMethodToken(token, call, nodeFactory);
    }
    return false;
  }

  /**
   * Models {@code getMethod(name, ...)}/{@code getDeclaredMethod(name, ...)} on a reflective class
   * token as handing back a {@link ReflectiveMethodToken} for {@code name} -- only when {@code
   * name} is a compile-time string literal right at this call site, mirroring {@code
   * MethodPAGStmtVisitor#handleClassForName}'s eager literal case (no interprocedural propagation
   * for the method name is attempted; a name computed elsewhere and forwarded in is left
   * unresolved).
   */
  private boolean propagateReflectiveMethodToken(
      ReflectiveClassToken token,
      MethodPAGStmtVisitor.PendingVirtualCall call,
      NodeFactory nodeFactory) {
    if (call.lhs().isEmpty()) return false;
    if (call.expr().getArgCount() == 0) return false;
    Value nameArg = call.expr().getArg(0);
    if (!(nameArg instanceof StringConstant nameConst)) return false;

    Optional<Node> lhsNode = nodeFactory.createNode(call.lhs().get(), call.srcSig());
    if (lhsNode.isEmpty()) return false;

    ReflectiveMethodToken methodToken =
        ReflectiveMethodToken.builder()
            .type(call.expr().getType())
            .containingMethodSig(call.srcSig())
            .declaringClass(token.getRepresented())
            .methodName(nameConst.getValue())
            .allocationSite(Engine.incrementAndGetAllocCount())
            .build();
    incrementalAnalysis.addEdge(methodToken, lhsNode.get());
    return true;
  }

  /**
   * Models {@code Method#invoke(receiver, args)} on a {@link ReflectiveMethodToken} as a virtual
   * call to the represented method name, dispatched on {@code receiver}'s runtime type -- the
   * reflective analogue of ordinary virtual dispatch in {@code solveOnTheFly}'s main loop. The
   * {@code args} array's elements are not tracked (same scope limitation {@code
   * instantiateReflectively} documents for {@code newInstance(Object[])}): this makes the target
   * method reachable and wires its {@code this}, but not its formal parameters or (since arity may
   * mismatch a name-only match) its return value.
   */
  private boolean resolveReflectiveMethodInvoke(
      ReflectiveMethodToken methodToken,
      MethodPAGStmtVisitor.PendingVirtualCall call,
      MutableCallGraph cg,
      Deque<MethodSignature> worklist,
      NodeFactory nodeFactory) {
    if (!call.expr().getMethodSignature().getName().equals("invoke")) return false;
    if (call.expr().getArgCount() == 0) return false;

    Optional<Node> receiverNode = nodeFactory.createNode(call.expr().getArg(0), call.srcSig());
    if (receiverNode.isEmpty()) return false;

    boolean anyResolved = false;
    for (AllocationNode recv : incrementalAnalysis.reachingObjects(receiverNode.get())) {
      MethodSignature targetSig =
          findMethodByName(recv.getType(), methodToken.getMethodName(), call.expr().getArgCount())
              .orElse(null);
      if (targetSig == null) continue;

      if (!cg.containsMethod(targetSig)) {
        cg.addMethod(targetSig);
        worklist.add(targetSig);
      }
      if (!cg.containsCall(call.srcSig(), targetSig, call.stmt())) {
        cg.addCall(call.srcSig(), targetSig, call.stmt());
      }
      view.getMethod(targetSig)
          .filter(SootMethod::hasBody)
          .flatMap(MethodPAGStmtVisitor::findThisLocal)
          .flatMap(local -> nodeFactory.createNode(local, targetSig))
          .ifPresent(thisNode -> incrementalAnalysis.addEdge(recv, thisNode));
      anyResolved = true;
    }
    return anyResolved;
  }

  /**
   * Finds a declared, body-having method named {@code methodName} directly on {@code allocType} (no
   * superclass walk, no overload disambiguation) -- the same "exact when unambiguous, an
   * over-approximation otherwise" tradeoff {@link #resolveConstructor} makes, including bounding
   * candidates by {@code callSiteArgCount} ({@code invoke(Object, Object[])}'s own syntactic
   * argument count, always 2) for the same reason: the FAIR converter builds actual-argument lists
   * from this call site's own arguments, not the resolved callee's arity, so a candidate whose
   * parameter count exceeds what the call site can supply must be skipped rather than picked.
   */
  private Optional<MethodSignature> findMethodByName(
      Type allocType, String methodName, int callSiteArgCount) {
    if (!(allocType instanceof ClassType ct)) return Optional.empty();
    return view.getClass(ct)
        .flatMap(
            c ->
                c.getMethods().stream()
                    .filter(m -> m.getName().equals(methodName) && m.hasBody())
                    .filter(m -> m.getParameterCount() <= callSiteArgCount)
                    .map(SootMethod::getSignature)
                    .findFirst());
  }

  /**
   * Models {@code newInstance()} on a reflective token as if the source had written {@code new
   * Foo()} directly: makes {@code Foo}'s constructor (see {@link #resolveConstructor}) reachable,
   * and points both the call's {@code lhs} and the constructor's {@code this} at one freshly
   * allocated {@code Foo} object. {@code getConstructor(Class[])}'s parameter-type array and {@code
   * newInstance(Object[])}'s argument array are not tracked, so a specific overload picked
   * reflectively at runtime is not distinguished from any other constructor of the same class.
   */
  private boolean instantiateReflectively(
      ReflectiveClassToken token,
      MethodPAGStmtVisitor.PendingVirtualCall call,
      MutableCallGraph cg,
      Deque<MethodSignature> worklist,
      NodeFactory nodeFactory) {
    ClassType represented = token.getRepresented();
    Optional<? extends SootMethod> ctorOpt =
        resolveConstructor(represented, call.expr().getArgCount());
    if (ctorOpt.isEmpty()) return false;
    MethodSignature ctorSig = ctorOpt.get().getSignature();

    if (!cg.containsMethod(ctorSig)) {
      cg.addMethod(ctorSig);
      worklist.add(ctorSig);
    }
    if (!cg.containsCall(call.srcSig(), ctorSig, call.stmt())) {
      cg.addCall(call.srcSig(), ctorSig, call.stmt());
    }

    Node freshAlloc =
        AllocationNode.builder()
            .type(represented)
            .containingMethodSig(call.srcSig())
            .allocationSite(Engine.incrementAndGetAllocCount())
            .build();

    Optional<Local> thisLocal = MethodPAGStmtVisitor.findThisLocal(ctorOpt.get());
    thisLocal
        .flatMap(local -> nodeFactory.createNode(local, ctorSig))
        .ifPresent(thisNode -> incrementalAnalysis.addEdge(freshAlloc, thisNode));

    call.lhs()
        .flatMap(l -> nodeFactory.createNode(l, call.srcSig()))
        .ifPresent(lhsNode -> incrementalAnalysis.addEdge(freshAlloc, lhsNode));

    return true;
  }

  /**
   * Resolves the constructor to model for a reflective {@code newInstance()}. {@code
   * getConstructor(Class[])}'s parameter-type array isn't tracked (see {@link
   * #instantiateReflectively}'s doc), so the exact overload can't be picked deterministically:
   * prefers the no-arg constructor when {@code represented} has one (the common case for JAXP-style
   * factories), else falls back to any declared constructor with a body whose arity fits within
   * {@code callSiteArgCount}. Exact whenever the class has at most one reflectively-targeted
   * constructor -- true for every case this analysis has needed to resolve so far -- and an
   * over-approximation (picks one arbitrary overload) otherwise.
   *
   * <p>{@code callSiteArgCount} is {@code newInstance()}'s own syntactic argument count at this
   * call site -- 0 for {@code Class#newInstance()}, 1 for {@code Constructor#newInstance(Object[])}
   * (the argument array itself, never unpacked into individual actuals; see {@link
   * #instantiateReflectively}'s doc). The FAIR converter builds a resolved callee's actual-argument
   * list directly from these syntactic call-site arguments, not from the callee's own declared
   * arity, so a picked constructor whose {@code getParameterCount()} exceeds this bound doesn't
   * just lose precision -- {@code IFDSCallSemantics.buildSplitCall} indexes past the end of that
   * list and throws. A constructor within the bound must be preferred over one outside it; finding
   * none means this call site's real target constructor genuinely can't be represented here --
   * returning empty (no edge wired) rather than picking one that will crash.
   */
  private Optional<? extends SootMethod> resolveConstructor(
      ClassType represented, int callSiteArgCount) {
    MethodSignature noArgSig =
        view.getIdentifierFactory()
            .getMethodSignature(
                represented, "<init>", VoidType.getInstance(), Collections.emptyList());
    Optional<? extends SootMethod> noArg = view.getMethod(noArgSig).filter(SootMethod::hasBody);
    if (noArg.isPresent()) return noArg;

    return view.getClass(represented)
        .flatMap(
            c ->
                c.getMethods().stream()
                    .filter(m -> m.getName().equals("<init>") && m.hasBody())
                    .filter(m -> m.getParameterCount() <= callSiteArgCount)
                    .findFirst());
  }

  /**
   * Models {@code getConstructor()}/{@code getDeclaredConstructor()} as handing back another
   * reflective token for the same represented class -- so a later {@code newInstance()} on the
   * {@code Constructor} object resolves exactly like a direct {@code Class#newInstance()} would.
   */
  private boolean propagateReflectiveToken(
      ReflectiveClassToken token,
      MethodPAGStmtVisitor.PendingVirtualCall call,
      NodeFactory nodeFactory) {
    if (call.lhs().isEmpty()) return false;
    Optional<Node> lhsNode = nodeFactory.createNode(call.lhs().get(), call.srcSig());
    if (lhsNode.isEmpty()) return false;

    ReflectiveClassToken ctorToken =
        ReflectiveClassToken.builder()
            .type(call.expr().getType())
            .containingMethodSig(call.srcSig())
            .represented(token.getRepresented())
            .allocationSite(Engine.incrementAndGetAllocCount())
            .build();
    incrementalAnalysis.addEdge(ctorToken, lhsNode.get());
    return true;
  }

  private record ResolvedVirtualCall(
      MethodSignature srcSig,
      sootup.core.jimple.common.stmt.InvokableStmt stmt,
      MethodSignature targetSig) {}

  // Keyed by the represented ClassType, not the ReflectiveClassToken itself: distinct tokens can
  // (and, under context-insensitive merging at a shared dispatch site, routinely do) represent the
  // same class while carrying different allocationSite identities -- e.g. a fresh token minted by
  // propagateReflectiveToken() on every getConstructor()/getDeclaredConstructor() call, or one
  // ReflectiveClassToken per distinct Class.forName call site that all funnel into one shared
  // newInstance() receiver. Keying on the whole token treated every one of those as "new" forever,
  // so this set (and the redundant resolution work it was meant to bound) grew unboundedly across
  // OTF iterations instead of converging. This mirrors ResolvedVirtualCall, which already dedupes
  // by the resolved target signature rather than by the allocation node that produced it.
  private record ResolvedReflectiveCall(
      MethodSignature srcSig,
      sootup.core.jimple.common.stmt.InvokableStmt stmt,
      ClassType represented) {}

  private record ResolvedReflectiveForName(
      MethodSignature srcSig, sootup.core.jimple.common.stmt.InvokableStmt stmt, String value) {}

  // Keyed by (declaringClass, methodName), not the ReflectiveMethodToken itself -- same reasoning
  // as ResolvedReflectiveCall above: distinct tokens can represent the same (class, method name)
  // pair reaching a shared invoke() call site, and keying on token identity would reintroduce the
  // unbounded-growth bug already fixed for the constructor-token case.
  private record ResolvedReflectiveMethodCall(
      MethodSignature srcSig,
      sootup.core.jimple.common.stmt.InvokableStmt stmt,
      ClassType declaringClass,
      String methodName) {}
}
