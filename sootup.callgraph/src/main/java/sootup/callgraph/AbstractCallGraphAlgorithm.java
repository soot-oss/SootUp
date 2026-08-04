package sootup.callgraph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2022 Christian Brüggemann, Ben Hermann, Markus Schmidt, Jonas Klauke and others
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

import static sootup.core.jimple.basic.StmtPositionInfo.getNoStmtPositionInfo;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.callgraph.CallGraph.Call;
import sootup.callgraph.scope.CallResolver;
import sootup.callgraph.scope.DefaultCallResolver;
import sootup.callgraph.scope.ExplorationVerdict;
import sootup.callgraph.scope.VirtualCallResolver;
import sootup.core.IdentifierFactory;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JStaticInvokeExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Method;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;
import sootup.core.types.VoidType;
import sootup.core.views.View;

/**
 * The AbstractCallGraphAlgorithm class is the super class of all call graph algorithm. It provides
 * basic methods used in all call graph algorithm. It is abstract since it has no implemented
 * functionality to resolve method calls because it is decided by the applied call graph algorithm
 */
public abstract class AbstractCallGraphAlgorithm implements CallGraphAlgorithm {

  private static final Logger logger = LoggerFactory.getLogger(AbstractCallGraphAlgorithm.class);

  /** The view providing access to all classes and type hierarchy. */
  @NonNull protected final View view;

  /** The type hierarchy derived from the view. */
  @NonNull protected final TypeHierarchy typeHierarchy;

  /** The class type for java.lang.Thread, used for thread start call edge handling. */
  @NonNull protected final ClassType threadType;

  /** Controls which statements' calls are resolved and expanded at all (pre-dispatch). */
  @NonNull private final CallResolver callResolver;

  /** Controls which resolved dynamic-dispatch candidates are admitted/expanded (post-dispatch). */
  @NonNull private final VirtualCallResolver virtualCallResolver;

  /** Creates a new call graph algorithm using the given view. */
  protected AbstractCallGraphAlgorithm(@NonNull View view) {
    this(view, new DefaultCallResolver(view), VirtualCallResolver.all());
  }

  /**
   * Creates a new call graph algorithm using the given view and a custom {@link CallResolver} to
   * control which statements' calls are excluded from call graph expansion.
   */
  protected AbstractCallGraphAlgorithm(@NonNull View view, @NonNull CallResolver callResolver) {
    this(view, callResolver, VirtualCallResolver.all());
  }

  /**
   * Creates a new call graph algorithm using the given view and a custom {@link
   * VirtualCallResolver} to control which resolved dynamic-dispatch candidates are admitted and
   * expanded.
   */
  protected AbstractCallGraphAlgorithm(
      @NonNull View view, @NonNull VirtualCallResolver virtualCallResolver) {
    this(view, new DefaultCallResolver(view), virtualCallResolver);
  }

  /**
   * Creates a new call graph algorithm using the given view and custom {@link CallResolver} and
   * {@link VirtualCallResolver} to control which classes/methods are excluded from call graph
   * expansion.
   */
  protected AbstractCallGraphAlgorithm(
      @NonNull View view,
      @NonNull CallResolver callResolver,
      @NonNull VirtualCallResolver virtualCallResolver) {
    this.view = view;
    this.typeHierarchy = view.getTypeHierarchy();
    this.threadType = view.getIdentifierFactory().getClassType("java.lang.Thread");
    this.callResolver = callResolver;
    this.virtualCallResolver = virtualCallResolver;
  }

  /**
   * This method starts the construction of the call graph algorithm. It initializes the needed
   * objects for the call graph generation and calls processWorkList method.
   *
   * @param entryPoints a list of method signatures that will be added to the work list in the call
   *     graph generation.
   * @return the complete constructed call graph starting from the entry methods.
   */
  @NonNull
  final CallGraph constructCompleteCallGraph(List<MethodSignature> entryPoints) {
    Deque<MethodSignature> workList = new ArrayDeque<>(entryPoints);
    Set<MethodSignature> processed = new HashSet<>();

    // find additional entry points
    List<MethodSignature> clinits = getClinitFromEntryPoints(entryPoints);

    workList.addAll(clinits);
    MutableCallGraph cg = initializeCallGraph(entryPoints, clinits);

    processWorkList(new Frontier(workList, processed), cg);
    return cg;
  }

  /**
   * Bundles the mutable work-list state threaded through call graph construction: methods still to
   * process, methods already processed, and methods that have been added to the graph only as the
   * target of {@link ExplorationVerdict#STOP_AFTER_CALL} edges so far (discovered, but not yet
   * queued for expansion). A method is promoted from {@code notYetExpanded} into {@code workList}
   * the moment any edge admits it with {@link ExplorationVerdict#EXPLORE_METHOD}, so expansion is
   * the logical OR across all incoming edges seen so far, not "whichever edge discovers it first."
   */
  private static final class Frontier {
    @NonNull private final Deque<MethodSignature> workList;
    @NonNull private final Set<MethodSignature> processed;
    @NonNull private final Set<MethodSignature> notYetExpanded = new HashSet<>();

    private Frontier(
        @NonNull Deque<MethodSignature> workList, @NonNull Set<MethodSignature> processed) {
      this.workList = workList;
      this.processed = processed;
    }
  }

  /**
   * Only {@link ExplorationVerdict#EXPLORE_METHOD} causes a statement's call(s) to be resolved;
   * {@link ExplorationVerdict#STOP_AFTER_CALL} and {@link ExplorationVerdict#STOP} are equivalent
   * at the pre-dispatch checkpoint, since no specific callee is known yet.
   */
  private static boolean explores(ExplorationVerdict verdict) {
    return verdict == ExplorationVerdict.EXPLORE_METHOD;
  }

  /**
   * This method creates the mutable call graph which is used in the call graph algorithm. Overwrite
   * it to change the used mutable call graph
   *
   * @param entryPoints the initial entry point method signatures
   * @param clinits the static initializer signatures to include as roots
   * @return the initialized call graph used in the call graph algorithm
   */
  protected MutableCallGraph initializeCallGraph(
      List<MethodSignature> entryPoints, List<MethodSignature> clinits) {
    ArrayList<MethodSignature> rootSignatures = new ArrayList<>(entryPoints);
    rootSignatures.addAll(clinits);
    return new GraphBasedCallGraph(rootSignatures);
  }

  /**
   * This method returns a list of static initializers that should be considered by the given entry
   * points
   *
   * @param entryPoints the entry points of the call graph algorithm
   * @return the list of static initializer signatures for the entry point classes
   */
  protected List<MethodSignature> getClinitFromEntryPoints(List<MethodSignature> entryPoints) {
    return entryPoints.stream()
        .map(
            methodSignature ->
                getSignatureOfImplementedStaticInitializer(methodSignature.getDeclClassType()))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  private Optional<MethodSignature> getSignatureOfImplementedStaticInitializer(
      ClassType classType) {
    return view.getMethod(view.getIdentifierFactory().getStaticInitializerSignature(classType))
        .map(SootMethod::getSignature);
  }

  /**
   * Processes all entries in the <code>workList</code>, skipping those present in <code>processed
   * </code>, adding call edges to the graph. Newly discovered methods are added to the <code>
   * workList</code> and processed as well. <code>cg</code> is updated accordingly. The method
   * postProcessingMethod is called after a method is processed in the <code>workList</code>.
   *
   * @param frontier bundles the work list of methods still to process, the set of already processed
   *     methods, and the set of methods discovered only via non-expanding edges so far.
   * @param cg the call graph object that is filled with the found methods and call edges.
   */
  final void processWorkList(Frontier frontier, MutableCallGraph cg) {
    while (!frontier.workList.isEmpty()) {
      MethodSignature currentMethodSignature = frontier.workList.pop();
      // skip if already processed
      if (frontier.processed.contains(currentMethodSignature)) {
        continue;
      }

      SootClass currentClass =
          view.getClass(currentMethodSignature.getDeclClassType()).orElse(null);
      if (currentClass == null) {
        continue;
      }

      // perform pre-processing if needed
      preProcessingMethod(currentMethodSignature, frontier.workList, cg);

      // process the method
      if (!cg.containsMethod(currentMethodSignature)) {
        cg.addMethod(currentMethodSignature);
      }

      // transform the method signature to the actual SootMethod
      currentClass
          .getMethod(currentMethodSignature.getSubSignature())
          .ifPresent(
              currentMethod -> {
                if (!currentMethod.hasBody()) {
                  return;
                }
                // get all call targets of invocations in the method body
                resolveAllCallsFromSourceMethod(currentMethod, cg, frontier);

                // get all call targets of implicit edges in the method body
                resolveAllImplicitCallsFromSourceMethod(currentMethod, cg, frontier);
              });

      // set method as processed
      frontier.processed.add(currentMethodSignature);

      // perform post-processing if needed
      postProcessingMethod(currentMethodSignature, frontier.workList, cg);
    }
  }

  /**
   * Adds the defined call to the given call graph, always expanding the target if newly discovered.
   * If the source or target method was added as vertex to the call graph, they will be added to the
   * worklist.
   *
   * @param source the method signature of the caller
   * @param target the method signature of the callee
   * @param invokeStmt the stmt causing the call
   * @param cg the call graph that will be updated
   * @param frontier the work-list state that will be updated
   */
  protected void addCallToCG(
      @NonNull MethodSignature source,
      @NonNull MethodSignature target,
      @NonNull InvokableStmt invokeStmt,
      @NonNull MutableCallGraph cg,
      @NonNull Frontier frontier) {
    addCallToCG(source, target, invokeStmt, cg, frontier, true);
  }

  /**
   * Adds the defined call to the given call graph. If the source method was added as vertex to the
   * call graph, it will be added to the worklist. The target is added to the worklist for expansion
   * only if {@code expandTarget} is {@code true}; otherwise it is added to the graph as a node (and
   * the edge is added) but left unexpanded unless a later call promotes it (see {@link
   * Frontier#notYetExpanded}).
   *
   * @param source the method signature of the caller
   * @param target the method signature of the callee
   * @param invokeStmt the stmt causing the call
   * @param cg the call graph that will be updated
   * @param frontier the work-list state that will be updated
   * @param expandTarget whether the target should be queued for expansion
   */
  private void addCallToCG(
      @NonNull MethodSignature source,
      @NonNull MethodSignature target,
      @NonNull InvokableStmt invokeStmt,
      @NonNull MutableCallGraph cg,
      @NonNull Frontier frontier,
      boolean expandTarget) {
    if (!cg.containsMethod(source)) {
      cg.addMethod(source);
      frontier.workList.push(source);
    }
    if (!cg.containsMethod(target)) {
      cg.addMethod(target);
      if (expandTarget) {
        frontier.workList.push(target);
      } else {
        frontier.notYetExpanded.add(target);
      }
    } else if (expandTarget && frontier.notYetExpanded.remove(target)) {
      // promotion: an earlier edge left this target unexpanded, but this edge wants it expanded
      frontier.workList.push(target);
    }
    if (!cg.containsCall(source, target, invokeStmt)) {
      cg.addCall(source, target, invokeStmt);
    }
  }

  /**
   * Adds the defined call to the given call graph. If the source or target method was added as
   * vertex to the call graph, they will be added to the worklist
   *
   * @param call the call that should be added to the call graph
   * @param cg the call graph that will be updated
   * @param frontier the work-list state that will be updated
   */
  protected void addCallToCG(
      @NonNull Call call, @NonNull MutableCallGraph cg, @NonNull Frontier frontier) {
    addCallToCG(
        call.sourceMethodSignature(),
        call.targetMethodSignature(),
        call.invokableStmt(),
        cg,
        frontier);
  }

  /**
   * Adds the defined call to the given call graph, always expanding the target if newly discovered.
   * Overload for call sites that only have access to the raw worklist (e.g. {@code
   * preProcessingMethod}/{@code postProcessingMethod} overrides, whose signature is fixed by the
   * abstract contract) rather than the internal {@link Frontier}. Calls added this way bypass the
   * {@link Frontier#notYetExpanded} promotion bookkeeping, matching their prior behavior.
   *
   * @param call the call that should be added to the call graph
   * @param cg the call graph that will be updated
   * @param workList the worklist in which the method signature of newly added vertexes will be
   *     added
   */
  protected void addCallToCG(
      @NonNull Call call, @NonNull MutableCallGraph cg, @NonNull Deque<MethodSignature> workList) {
    MethodSignature source = call.sourceMethodSignature();
    MethodSignature target = call.targetMethodSignature();
    InvokableStmt invokeStmt = call.invokableStmt();
    if (!cg.containsMethod(source)) {
      cg.addMethod(source);
      workList.push(source);
    }
    if (!cg.containsMethod(target)) {
      cg.addMethod(target);
      workList.push(target);
    }
    if (!cg.containsCall(source, target, invokeStmt)) {
      cg.addCall(source, target, invokeStmt);
    }
  }

  /**
   * This method resolves all calls from a given source method. resolveCall is called for each
   * invokable statements in the body of the source method that is implemented in the corresponding
   * call graph algorithm. If new methods will be added as vertexes in the call graph, the work list
   * will be updated
   *
   * @param sourceMethod this signature is used to access the statements contained method body of
   *     the specified method
   * @param cg the call graph that will receive the found calls
   * @param frontier the work-list state that will be updated with found target methods
   */
  protected void resolveAllCallsFromSourceMethod(
      @NonNull SootMethod sourceMethod, @NonNull MutableCallGraph cg, @NonNull Frontier frontier) {
    sourceMethod.getBody().getStmts().stream()
        .filter(Stmt::isInvokableStmt)
        .map(Stmt::asInvokableStmt)
        .forEach(
            stmt -> {
              if (!explores(callResolver.tryAdvance(sourceMethod, stmt))) {
                return;
              }
              resolveCall(sourceMethod, stmt)
                  .forEach(
                      targetMethod ->
                          addResolvedCall(sourceMethod, targetMethod, stmt, cg, frontier));
            });
  }

  /**
   * Applies {@link VirtualCallResolver#tryAdvanceCall(SootMethod, MethodSignature, InvokableStmt)}
   * to a single dynamic-dispatch candidate resolved for the given statement, and adds the resulting
   * edge to the call graph unless the verdict is {@link ExplorationVerdict#STOP}.
   *
   * @param sourceMethod the caller method
   * @param targetMethod the resolved dynamic-dispatch candidate
   * @param stmt the invokable statement causing the call
   * @param cg the call graph that will receive the found call
   * @param frontier the work-list state that will be updated
   */
  private void addResolvedCall(
      @NonNull SootMethod sourceMethod,
      @NonNull MethodSignature targetMethod,
      @NonNull InvokableStmt stmt,
      @NonNull MutableCallGraph cg,
      @NonNull Frontier frontier) {
    ExplorationVerdict verdict =
        virtualCallResolver.tryAdvanceCall(sourceMethod, targetMethod, stmt);
    if (verdict == ExplorationVerdict.STOP) {
      return;
    }
    addCallToCG(
        sourceMethod.getSignature(),
        targetMethod,
        stmt,
        cg,
        frontier,
        verdict == ExplorationVerdict.EXPLORE_METHOD);
  }

  /**
   * It resolves the start-run implicit calls caused by the given source method
   *
   * @param sourceMethod the inspected source method
   * @param cg implicit start-run calls will be added to the call graph
   * @param frontier new run methods will be added to the work list
   */
  protected void implicitStartRunCall(
      @NonNull SootMethod sourceMethod, @NonNull MutableCallGraph cg, @NonNull Frontier frontier) {
    for (Stmt stmt : sourceMethod.getBody().getStmts()) {
      if (!stmt.isInvokableStmt()) {
        continue;
      }
      InvokableStmt invokableStmt = stmt.asInvokableStmt();
      if (!explores(callResolver.tryAdvance(sourceMethod, invokableStmt))) {
        continue;
      }
      AbstractInvokeExpr sourceMethodInvokeExpr = invokableStmt.getInvokeExpr().orElse(null);
      if (sourceMethodInvokeExpr == null || !sourceMethodInvokeExpr.isJVirtualInvokeExpr()) {
        continue;
      }
      MethodSignature methodSig = sourceMethodInvokeExpr.getMethodSignature();
      if (!methodSig.getType().equals(VoidType.getInstance())
          || !methodSig.getParameterTypes().isEmpty()
          || !methodSig.getName().equals("start")) {
        continue;
      }
      // check if java.lang.Thread is superClass of methodSig.classType()
      if (typeHierarchy
          .superClassesOf(methodSig.getDeclClassType())
          .noneMatch(classType -> classType.equals(threadType))) {
        continue;
      }
      MethodSignature implicitRunMethodSig =
          new MethodSignature(
              methodSig.getDeclClassType(),
              "run",
              methodSig.getParameterTypes(),
              methodSig.getType());
      JVirtualInvokeExpr runInvokeExpr =
          sourceMethodInvokeExpr.asJVirtualInvokeExpr().withMethodSignature(implicitRunMethodSig);
      InvokableStmt runInvokableStmt = new JInvokeStmt(runInvokeExpr, getNoStmtPositionInfo());
      resolveCall(sourceMethod, runInvokableStmt)
          .forEach(
              runMethodSignature -> {
                if (view.getMethod(runMethodSignature).isPresent()) {
                  addResolvedCall(sourceMethod, runMethodSignature, runInvokableStmt, cg, frontier);
                }
              });
    }
  }

  /**
   * Resolves all implicit calls caused by the given source method.
   *
   * @param sourceMethod the inspected source method
   * @param cg the mutable call graph where new calls will be added
   * @param frontier the work-list state in which the new target methods will be added
   */
  protected void resolveAllImplicitCallsFromSourceMethod(
      @NonNull SootMethod sourceMethod, @NonNull MutableCallGraph cg, @NonNull Frontier frontier) {
    implicitStartRunCall(sourceMethod, cg, frontier);
    ArrayListMultimap<ClassType, Call> potentialStaticInitializerCalls =
        resolveAllStaticInitializerCalls(sourceMethod);
    Stream<Call> staticInitializerCalls =
        postProcessingStaticInitializerCalls(potentialStaticInitializerCalls);
    addStaticInitializerCalls(staticInitializerCalls, cg, frontier);
  }

  /**
   * Resolves all potential static initializer calls caused by the given source method. This method
   * iterates over the sorted blocks of the source method, tracking whether each block contains a
   * static initializer ({@code <clinit>}) call for a specific class type. If all visited
   * predecessor blocks (meaning all statements in those blocks have been fully processed) already
   * contain a static initializer call for a given class type, the current block inherits this
   * state. Consequently, if a static initializer call is guaranteed to have occurred on all
   * incoming control flow paths, subsequent calls to the same initializer in the current block are
   * pruned to avoid creating duplicate edges.
   *
   * @param sourceMethod the inspected source method
   * @return a multimap containing the resolved potential static initializer calls
   */
  protected ArrayListMultimap<ClassType, Call> resolveAllStaticInitializerCalls(
      @NonNull SootMethod sourceMethod) {
    IdentifierFactory id = view.getIdentifierFactory();
    MethodSignature sourceMethodSignature = sourceMethod.getSignature();
    MethodSubSignature sourceMethodSubSignature = sourceMethodSignature.getSubSignature();
    ArrayListMultimap<ClassType, Call> potentialClinitCalls = ArrayListMultimap.create();
    InstantiateClassValueVisitor instantiateVisitor = new InstantiateClassValueVisitor();
    // row: classType of potential static initializer call
    // column: block of the invokeStmt
    // value: true, if the classType has a static initializer call in the block
    Table<ClassType, BasicBlock<?>, Boolean> table = HashBasedTable.create();
    Set<BasicBlock<?>> visitedBlocks = new HashSet<>();
    sourceMethod
        .getBody()
        .getControlFlowGraph()
        .getBlocksSorted()
        .forEach(
            basicBlock -> {
              // propagate the clinit flags from predecessors BEFORE evaluating statements
              List<?> preBlocks = basicBlock.getPredecessors();
              if (!preBlocks.isEmpty()) {
                BasicBlock<?> firstPre = findFirstVisitedPredBlock(preBlocks, visitedBlocks);
                if (firstPre != null) {
                  Set<ClassType> clinitFlagsInFirstPred = new HashSet<>();
                  Map<ClassType, Boolean> firstPreClasses = table.column(firstPre);
                  for (Map.Entry<ClassType, Boolean> entry : firstPreClasses.entrySet()) {
                    if (entry.getValue()) {
                      clinitFlagsInFirstPred.add(entry.getKey());
                    }
                  }
                  for (Object preObj : preBlocks) {
                    BasicBlock<?> preBlock = (BasicBlock<?>) preObj;
                    if (!preBlock.equals(basicBlock) && visitedBlocks.contains(preBlock)) {
                      clinitFlagsInFirstPred.removeIf(
                          classType -> !Boolean.TRUE.equals(table.get(classType, preBlock)));
                    }
                  }
                  // apply inherited state to the current block
                  for (ClassType type : clinitFlagsInFirstPred) {
                    table.put(type, basicBlock, Boolean.TRUE);
                  }
                }
              }
              // iterate over stmts
              for (Stmt stmt : basicBlock.getStmts()) {
                if (!stmt.isInvokableStmt()) {
                  continue;
                }
                InvokableStmt invokableStmt = stmt.asInvokableStmt();
                // static field usage
                ClassType targetClass = null;
                if (invokableStmt.containsFieldRef()
                    && invokableStmt.getFieldRef() instanceof JStaticFieldRef) {
                  targetClass = invokableStmt.getFieldRef().getFieldSignature().getDeclClassType();
                  if (!(targetClass
                          .getFullyQualifiedName()
                          .equals(sourceMethodSignature.getDeclClassType().getFullyQualifiedName())
                      && id.isStaticInitializerSubSignature(sourceMethodSubSignature))) {
                    potentialClinitCalls.putAll(
                        findStaticInitializerCalls(
                            sourceMethod, targetClass, invokableStmt, table));
                  }
                }
                // static method
                if (invokableStmt.getInvokeExpr().isPresent()) {
                  // static method call
                  AbstractInvokeExpr expr = invokableStmt.getInvokeExpr().get();
                  if (expr instanceof JStaticInvokeExpr) {
                    ClassType newTargetClass = expr.getMethodSignature().getDeclClassType();
                    // checks if the field points to the same clinit
                    if (!newTargetClass.equals(targetClass)) {
                      if (!(newTargetClass
                              .getFullyQualifiedName()
                              .equals(
                                  sourceMethodSignature.getDeclClassType().getFullyQualifiedName())
                          && id.isStaticInitializerSubSignature(sourceMethodSubSignature))) {
                        potentialClinitCalls.putAll(
                            findStaticInitializerCalls(
                                sourceMethod, newTargetClass, invokableStmt, table));
                      }
                    }
                  }
                } else {
                  if (invokableStmt instanceof JAssignStmt) {
                    Value rightOp = ((JAssignStmt) invokableStmt).getRightOp();
                    // extract class type out of new, new array and new multi array
                    instantiateVisitor.init();
                    rightOp.accept(instantiateVisitor);
                    ClassType newTargetClass = instantiateVisitor.getResult();
                    // check if class type is the same as in the field which could be on the left op
                    if (newTargetClass != null && !newTargetClass.equals(targetClass)) {
                      if (!(newTargetClass
                              .getFullyQualifiedName()
                              .equals(
                                  sourceMethodSignature.getDeclClassType().getFullyQualifiedName())
                          && id.isStaticInitializerSubSignature(sourceMethodSubSignature))) {
                        potentialClinitCalls.putAll(
                            findStaticInitializerCalls(
                                sourceMethod, newTargetClass, invokableStmt, table));
                      }
                    }
                  }
                }
              }
              visitedBlocks.add(basicBlock);
            });
    return potentialClinitCalls;
  }

  /**
   * Collects all potential static initializer calls of the given targetClass. An edge from the
   * sourceSig to all clinit methods of the targetClass and Superclasses will be added to the
   * MultiMap of potential static initializer calls.
   *
   * @param sourceMethod the source method causing the static initializer call
   * @param targetClass the class that is statically initialized
   * @param invokableStmt the statement causing the call
   * @param clinitCallTable a table tracking initialized classes per block. Rows represent class
   *     types, columns represent blocks, and a true value indicates the class has a static
   *     initializer call within or prior to that block
   * @return a multimap of newly discovered potential static initializer calls
   */
  protected ArrayListMultimap<ClassType, Call> findStaticInitializerCalls(
      SootMethod sourceMethod,
      ClassType targetClass,
      InvokableStmt invokableStmt,
      Table<ClassType, BasicBlock<?>, Boolean> clinitCallTable) {

    ArrayListMultimap<ClassType, Call> potentialClinitCalls = ArrayListMultimap.create();
    if (!explores(callResolver.tryAdvance(sourceMethod, invokableStmt))) {
      return potentialClinitCalls;
    }

    ControlFlowGraph<?> cfg = sourceMethod.getBody().getControlFlowGraph();
    BasicBlock<?> currentBlock = cfg.getBlockOf(invokableStmt);
    MethodSignature sourceSig = sourceMethod.getSignature();

    // static initializer call of class + all superclasses
    Stream.concat(Stream.of(targetClass), typeHierarchy.superClassesOf(targetClass))
        .map(
            classType ->
                view.getMethod(
                    view.getIdentifierFactory().getStaticInitializerSignature(classType)))
        .filter(Optional::isPresent)
        .map(Optional::get)
        // eliminates self-calls caused by superClasses
        .filter(targetMethod -> !targetMethod.getSignature().equals(sourceSig))
        .forEach(
            targetMethod -> {
              MethodSignature targetSig = targetMethod.getSignature();
              ClassType targetClassType = targetSig.getDeclClassType();

              if (clinitCallTable.get(targetClassType, currentBlock) == null
                  || clinitCallTable.get(targetClassType, currentBlock) == Boolean.FALSE) {
                clinitCallTable.put(targetClassType, currentBlock, Boolean.TRUE);
                Call callToAdd = new Call(sourceSig, targetSig, invokableStmt);
                potentialClinitCalls.put(targetMethod.getDeclaringClassType(), callToAdd);
              }
            });
    return potentialClinitCalls;
  }

  /**
   * Returns the first visited block from a list of predecessor blocks or null if none are found.
   *
   * @param preBlocks list of all predecessor blocks
   * @param visitedBlocks set of fully processed blocks (all stmts have been iterated)
   */
  private BasicBlock<?> findFirstVisitedPredBlock(
      List<?> preBlocks, Set<BasicBlock<?>> visitedBlocks) {
    BasicBlock<?> firstPreBlock;
    for (Object preBlock : preBlocks) {
      firstPreBlock = (BasicBlock<?>) preBlock;
      if (visitedBlocks.contains(firstPreBlock)) {
        return firstPreBlock;
      }
    }
    return null;
  }

  /**
   * This method enables optional post-processing of potential static initializer call edges.
   *
   * @param potentialStaticInitializerCalls all potential static initializer calls
   * @return a stream of valid static initializer calls
   */
  protected Stream<Call> postProcessingStaticInitializerCalls(
      @NonNull ArrayListMultimap<ClassType, Call> potentialStaticInitializerCalls) {
    return potentialStaticInitializerCalls.values().stream();
  }

  /**
   * After pruning unnecessary static initializer calls from the potential candidates, this method
   * adds all remaining calls to the call graph.
   *
   * @param staticInitializerCalls stream of pruned, valid static initializer calls
   * @param cg mutable call graph where calls will be added
   * @param frontier the work-list state in which newly static initializer call targets will be
   *     added
   */
  private void addStaticInitializerCalls(
      Stream<Call> staticInitializerCalls, MutableCallGraph cg, Frontier frontier) {
    staticInitializerCalls.forEach(call -> addCallToCG(call, cg, frontier));
  }

  /**
   * This method enables optional pre-processing of a method in the call graph algorithm
   *
   * @param sourceMethod the processed method
   * @param workList the current work list that might be extended
   * @param cg the current cg that might be extended
   */
  protected abstract void preProcessingMethod(
      @NonNull MethodSignature sourceMethod,
      @NonNull Deque<MethodSignature> workList,
      @NonNull MutableCallGraph cg);

  /**
   * This method enables optional post-processing of a method in the call graph algorithm
   *
   * @param sourceMethod the processed method
   * @param workList the current work list that might be extended
   * @param cg the current cg that might be extended
   */
  protected abstract void postProcessingMethod(
      @NonNull MethodSignature sourceMethod,
      @NonNull Deque<MethodSignature> workList,
      @NonNull MutableCallGraph cg);

  @NonNull
  @Override
  public CallGraph addClass(@NonNull CallGraph oldCallGraph, @NonNull ClassType classType) {
    SootClass clazz = view.getClassOrThrow(classType);
    Set<MethodSignature> newMethodSignatures =
        clazz.getMethods().stream()
            .map(Method::getSignature)
            .filter(methodSig -> !oldCallGraph.containsMethod(methodSig))
            .collect(Collectors.toSet());

    // were all the added method signatures already visited in the CallGraph? i.e. is there
    // something to add?
    if (newMethodSignatures.isEmpty()) {
      return oldCallGraph;
    }

    MutableCallGraph updated = oldCallGraph.copy();

    // Step 1: Add edges from the new methods to other methods
    Deque<MethodSignature> workList = new ArrayDeque<>(newMethodSignatures);
    Set<MethodSignature> processed = new HashSet<>(oldCallGraph.getMethodSignatures());
    processWorkList(new Frontier(workList, processed), updated);

    // Step 2: Add edges from old methods to methods overridden in the new class
    Stream<ClassType> superClasses = typeHierarchy.superClassesOf(classType);
    Stream<ClassType> implementedInterfaces = typeHierarchy.implementedInterfacesOf(classType);
    Stream<ClassType> superTypes = Stream.concat(superClasses, implementedInterfaces);

    Set<MethodSubSignature> newMethodSubSigs =
        newMethodSignatures.stream()
            .map(MethodSignature::getSubSignature)
            .collect(Collectors.toSet());

    superTypes
        .map(view::getClass)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .flatMap(superType -> superType.getMethods().stream())
        .map(Method::getSignature)
        .filter(
            superTypeMethodSig -> newMethodSubSigs.contains(superTypeMethodSig.getSubSignature()))
        .forEach(
            overriddenMethodSig -> {
              //noinspection OptionalGetWithoutIsPresent (We know this exists)
              MethodSignature overridingMethodSig =
                  clazz.getMethod(overriddenMethodSig.getSubSignature()).get().getSignature();

              if (updated.containsMethod(overriddenMethodSig)) {
                for (Call calls : updated.callsTo(overriddenMethodSig)) {
                  updated.addCall(
                      calls.sourceMethodSignature(), overridingMethodSig, calls.invokableStmt());
                }
              }
            });

    return updated;
  }

  /**
   * The method iterates over all classes present in view, and finds method with name main and
   * SourceType - Application. This method is used by initialize() method used for creating call
   * graph and the call graph is created by considering the main method as an entry point.
   *
   * <p>The method throws an exception if there is no main method in any of the classes or if there
   * are more than one main method.
   *
   * @return - MethodSignature of main method.
   */
  public MethodSignature findMainMethod() {
    Collection<SootMethod> mainMethods =
        view.getClasses()
            .filter(aClass -> !aClass.isLibraryClass())
            .flatMap(aClass -> aClass.getMethods().stream())
            .filter(method -> method.isStatic() && method.isMain(view.getIdentifierFactory()))
            .collect(Collectors.toSet());

    if (mainMethods.size() > 1) {
      throw new RuntimeException(
          "There are more than 1 main method present.\n Below main methods are found: \n"
              + mainMethods
              + "\n initialize() method can be used if only one main method exists. \n You can specify these main methods as entry points by passing them as parameter to initialize method.");
    } else if (mainMethods.isEmpty()) {
      throw new RuntimeException(
          "No main method is present in the input programs. initialize() method can be used if only one main method exists in the input program and that should be used as entry point for call graph. \n Please specify entry point as a parameter to initialize method.");
    }

    return mainMethods.stream().findFirst().get().getSignature();
  }

  /**
   * This method resolves the possible targets of a given invoke expression. The results are
   * dependable of the applied call graph algorithm. therefore, it is abstract.
   *
   * @param method the method object that contains the given invoke expression in the body.
   * @param invokableStmt it contains the call which is resolved.
   * @return a stream of all reachable method signatures defined by the applied call graph
   *     algorithm.
   */
  @NonNull
  protected abstract Stream<MethodSignature> resolveCall(
      SootMethod method, InvokableStmt invokableStmt);

  /**
   * Searches for the signature of the method that is the concrete implementation of <code>m</code>.
   * This is done by checking each superclass and the class itself for whether it contains the
   * concrete implementation.
   *
   * @param view the view providing access to all classes
   * @param m the method signature to resolve
   * @return the concrete implementing method signature, or empty if not found or abstract
   */
  @NonNull
  public static Optional<MethodSignature> resolveConcreteDispatch(View view, MethodSignature m) {
    Optional<? extends SootMethod> methodOp = findConcreteMethod(view, m);
    if (methodOp.isPresent()) {
      SootMethod method = methodOp.get();
      if (method.isAbstract()) {
        return Optional.empty();
      }
      return Optional.of(method.getSignature());
    }
    return Optional.empty();
  }

  /**
   * searches the method object in the given hierarchy
   *
   * @param view it contains all classes
   * @param sig the signature of the searched method
   * @return the found method object, or null if the method was not found.
   */
  protected static Optional<? extends SootMethod> findConcreteMethod(
      @NonNull View view, @NonNull MethodSignature sig) {
    SootClass startClass = view.getClass(sig.getDeclClassType()).orElse(null);

    if (startClass == null) {
      logger.warn(
          "Could not find \""
              + sig.getDeclClassType()
              + "\" of method"
              + sig
              + " to resolve the concrete method");
      return Optional.empty();
    }
    MethodSubSignature methodSig = sig.getSubSignature();

    // search method current class and in superclasses
    Optional<? extends SootMethod> method = findMethodInHierarchy(view, startClass, methodSig);
    if (method.isPresent()) {
      return method;
    }

    // search method in interfaces
    Optional<? extends SootMethod> defaultMethod = findDefaultMethod(view, startClass, methodSig);
    if (defaultMethod.isPresent()) {
      return defaultMethod;
    }

    logger.warn(
        "Could not find \""
            + sig.getSubSignature()
            + "\" in "
            + sig.getDeclClassType().getClassName()
            + " and in its superclasses and interfaces");
    return Optional.empty();
  }

  /**
   * Searches for a method matching the given sub-signature in the class hierarchy starting from the
   * given class and traversing superclasses.
   */
  protected static Optional<SootMethod> findMethodInHierarchy(
      @NonNull View view,
      @NonNull SootClass sootClass,
      @NonNull MethodSubSignature targetMethodSignature) {
    SootMethod target = sootClass.getMethod(targetMethodSignature).orElse(null);
    // check current class
    if (target != null) {
      return Optional.of(target);
    }

    ClassType superClassType = sootClass.getSuperclass().orElse(null);
    // does not have a superclass
    if (superClassType == null) {
      return Optional.empty();
    }
    SootClass superclass = view.getClass(superClassType).orElse(null);
    // superclass is mot in the view
    if (superclass == null) {
      return Optional.empty();
    }

    // method isn't found, continue with the superclass
    return findMethodInHierarchy(view, superclass, targetMethodSignature);
  }

  /**
   * Searches the default method that would be used as a target of the given SootClass and
   * MethodSubSignature. All interfaces are checked and it returns that SootMethod object of the
   * interface which is the subtype of all possible fitting default method.
   *
   * @param view The view contains the hierarchy information and the classes and methods
   * @param sootClass the sootClass which defines the start of the search
   * @param defaultSignature the method-subsignature which defines the target method
   * @return An Optional containing the default method or an empty Optional if there is no default
   *     method, or a superclass is not in the view.
   */
  protected static Optional<? extends SootMethod> findDefaultMethod(
      @NonNull View view,
      @NonNull SootClass sootClass,
      @NonNull MethodSubSignature defaultSignature) {
    TypeHierarchy typeHierarchy = view.getTypeHierarchy();
    return typeHierarchy
        .implementedInterfacesOf(sootClass.getType())
        .flatMap(
            classType ->
                view
                    .getMethod(
                        view.getIdentifierFactory().getMethodSignature(classType, defaultSignature))
                    .stream())
        .reduce(
            (currentLeastSubMethod, sootMethod) ->
                typeHierarchy.isSubtype(
                        currentLeastSubMethod.getDeclaringClassType(),
                        sootMethod.getDeclaringClassType())
                    ? sootMethod
                    : currentLeastSubMethod);
  }

  /** Returns true if the given class type represents an interface. */
  protected boolean isInterface(ClassType classType) {
    return typeHierarchy.isInterface(classType);
  }
}
