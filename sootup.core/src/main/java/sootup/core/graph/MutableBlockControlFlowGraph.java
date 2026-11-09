package sootup.core.graph;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 1997 - 2024 Raja Vallée-Rai and others
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

import java.util.*;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.LocalGenerator;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Trap;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.stmt.*;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;

/**
 * Control flow graph that stores Stmts in maximal basic blocks.
 *
 * <p>Every mutation is built from three structural primitives on top of the edge primitives of
 * {@link MutableBasicBlockImpl}:
 *
 * <ul>
 *   <li>{@link #split}: cut a block so that a given stmt becomes the head of a block,
 *   <li>{@link #merge}: join a block with its only, fall-through successor,
 *   <li>{@link #normalize}: merge a block with its neighbours where possible.
 * </ul>
 *
 * Operations split what they need, change edges, then normalize, so blocks stay maximal.
 *
 * @author Markus Schmidt
 */
public class MutableBlockControlFlowGraph extends MutableControlFlowGraph {
  @Nullable private Stmt startingStmt = null;

  /**
   * The statements in {@link #iterator()} order, computed on demand; every change of the graph
   * resets it. Interceptors ask for the statements of a body many times between changes.
   */
  @Nullable private Stmt[] stmtsInOrder = null;

  @NonNull private final Map<Stmt, MutableBasicBlockImpl> stmtToBlock = new IdentityHashMap<>();

  @NonNull private final Set<MutableBasicBlockImpl> blocks = new LinkedHashSet<>();

  public MutableBlockControlFlowGraph() {}

  public MutableBlockControlFlowGraph(
      boolean isStatic, MethodSignature sig, LocalGenerator localgen) {
    final List<Stmt> stmts = new ArrayList<>(sig.getParameterTypes().size() + (isStatic ? 0 : 1));
    if (!isStatic) {
      ClassType thisType = sig.getDeclClassType();
      Local thisLocal = localgen.generateThisLocal(thisType);
      Stmt stmt =
          Jimple.newIdentityStmt(
              thisLocal, Jimple.newThisRef(thisType), StmtPositionInfo.getNoStmtPositionInfo());
      stmts.add(stmt);
    }
    int i = 0;
    for (Type parameterType : sig.getParameterTypes()) {
      Stmt stmt =
          Jimple.newIdentityStmt(
              localgen.generateParameterLocal(parameterType, i),
              Jimple.newParameterRef(parameterType, i++),
              StmtPositionInfo.getNoStmtPositionInfo());
      stmts.add(stmt);
    }
    if (!stmts.isEmpty()) {
      setStartingStmt(stmts.get(0));
      addBlock(stmts);
    }
  }

  /** copies a ControlFlowGraph into this Mutable instance, block by block */
  public MutableBlockControlFlowGraph(@NonNull ControlFlowGraph<? extends BasicBlock<?>> graph) {
    Collection<? extends BasicBlock<?>> sourceBlocks = graph.getBlocks();
    for (BasicBlock<?> b : sourceBlocks) {
      newBlock(b.getStmts());
    }
    for (BasicBlock<?> b : sourceBlocks) {
      final MutableBasicBlockImpl copy = blockOf(b.getHead());
      List<? extends BasicBlock<?>> successors = b.getSuccessors();
      for (int i = 0; i < successors.size(); i++) {
        copy.link(i, blockOf(successors.get(i).getHead()));
      }
      b.getExceptionalSuccessors()
          .forEach((type, handler) -> copy.linkExc(type, blockOf(handler.getHead())));
    }
    final Stmt startStmt = graph.getStartingStmt();
    if (startStmt != null) {
      setStartingStmt(startStmt);
    }
  }

  public static ControlFlowGraph<?> createUnmodifiableControlFlowGraph(
      ControlFlowGraph<?> controlFlowGraph) {
    if (controlFlowGraph instanceof MutableControlFlowGraph) {
      return ((MutableControlFlowGraph) controlFlowGraph).unmodifiableControlFlowGraph();
    }
    return controlFlowGraph;
  }

  /**
   * Creates a Graph representation from the 'legacy' representation i.e. a List of Stmts and Traps.
   */
  public void initializeWith(
      @NonNull List<List<Stmt>> blocks,
      @NonNull Map<BranchingStmt, List<Stmt>> successorMap,
      @NonNull List<Trap> traps) {
    modified();

    if (blocks.isEmpty()) {
      return;
    }

    HashMap<Stmt, Integer> trapstmtToIdx = new HashMap<>();
    PriorityQueue<Trap> trapStart =
        new PriorityQueue<>(
            Comparator.comparingInt((Trap t) -> trapstmtToIdx.get(t.getBeginStmt())));
    PriorityQueue<Trap> trapEnd =
        new PriorityQueue<>(Comparator.comparingInt((Trap t) -> trapstmtToIdx.get(t.getEndStmt())));

    Comparator<Trap> trapComparator;
    if (!traps.isEmpty()) {
      Map<Stmt, Integer> blockIdxMap = new HashMap<>();
      Set<Stmt> blockStmts = Collections.newSetFromMap(new IdentityHashMap<>());
      int i = 0;
      for (List<Stmt> block : blocks) {
        blockIdxMap.put(block.get(0), i++);
        blockStmts.addAll(block);
        /*
        for (int j = 0; j < block.size(); j++) {
          blockIdxMap.put(block.get(j), i++);
        }
         */
      }

      traps.forEach(
          trap -> {
            Integer beginIdx = blockIdxMap.get(trap.getBeginStmt());
            if (beginIdx == null) {
              throw new AssertionError();
            }
            trapstmtToIdx.put(trap.getBeginStmt(), beginIdx);
            Integer endIdx = blockIdxMap.get(trap.getEndStmt());
            if (endIdx == null) { // The end statement is not the head of any block
              if (blockStmts.contains(trap.getEndStmt())) {
                // a Trap can only end where a Block begins; inside a Block it would cover part of
                // it
                throw new IllegalArgumentException(
                    "The end of the Trap "
                        + trap
                        + " is in the middle of a Block instead of at the head of one.");
              }
              // the end is in no Block at all (e.g. removed as unreachable): the Trap reaches the
              // end of the body
              endIdx = blockIdxMap.size();
            }
            trapstmtToIdx.put(trap.getEndStmt(), endIdx);
            Integer handlerIdx = blockIdxMap.get(trap.getHandlerStmt());
            assert handlerIdx != null;
            trapstmtToIdx.put(trap.getHandlerStmt(), handlerIdx);
          });

      duplicateCatchAllTrapRemover(traps, trapstmtToIdx);
      trapComparator = (trapA, trapB) -> getTrapApplicationComparator(trapstmtToIdx, trapA, trapB);
      traps.forEach(
          trap -> {
            trapStart.add(trap);
            trapEnd.add(trap);
          });

      // traps.sort(getTrapComparator(trapstmtToIdx));
      /* debug print:
           traps.forEach(t ->  System.out.println(t.getExceptionType() + " "+ trapstmtToIdx.get(t.getBeginStmt()) + " " + trapstmtToIdx.get(t.getEndStmt()) + " -> " + trapstmtToIdx.get(t.getHandlerStmt()) + " " + t.getHandlerStmt()  ));
      */
    } else {
      trapComparator = null;
    }

    validateLegacyBlocks(blocks, successorMap);
    final MutableBasicBlockImpl[] newBlocks = new MutableBasicBlockImpl[blocks.size()];
    final Map<Stmt, MutableBasicBlockImpl> headToBlock = new IdentityHashMap<>();
    for (int b = 0; b < blocks.size(); b++) {
      newBlocks[b] = newBlock(blocks.get(b));
      headToBlock.put(blocks.get(b).get(0), newBlocks[b]);
    }
    startingStmt = blocks.get(0).get(0);
    Map<ClassType, Stmt> exceptionToHandlerMap = new HashMap<>();
    Map<ClassType, Trap> activeTrapMap = new HashMap<>();
    Map<ClassType, PriorityQueue<Trap>> overlappingTraps = new HashMap<>();

    Trap nextStartingTrap = trapStart.poll();
    Trap nextEndingTrap = trapEnd.poll();
    for (int i = 0, stmtsSize = blocks.size(); i < stmtsSize; i++) {
      List<Stmt> block = blocks.get(i);
      Stmt headStmt = block.get(0);

      boolean trapsChanged = false;
      while (nextEndingTrap != null
          && (nextEndingTrap.getEndStmt() == headStmt || nextEndingTrap.getEndStmt() == null)) {
        Trap trap = nextEndingTrap;
        nextEndingTrap = trapEnd.poll();
        // endStmt is exclusive! -> trap ends before this stmt -> remove exception info here
        final ClassType exceptionType = trap.getExceptionType();
        final boolean isRemovedFromActive = activeTrapMap.remove(exceptionType, trap);
        final PriorityQueue<Trap> overridenTrapHandlers = overlappingTraps.get(exceptionType);
        if (overridenTrapHandlers != null) {
          // System.out.println("overlapping traps found");
          if (isRemovedFromActive) {
            // is there an overridden traprange that needs to take its place?
            if (!overridenTrapHandlers.isEmpty()) {
              // System.out.println("update activeTrapMap with next trap from overlaps");
              activeTrapMap.put(exceptionType, overridenTrapHandlers.poll());
            }
          } else {
            // check if there is an overlapping trap that has a less specific TrapRange which is
            // ending before it gets the active exception information again
            // not logical as a compiler output... but possible.
            overridenTrapHandlers.remove(trap);
            // System.out.println("remove from overlapping: " + trap);
          }
        }

        trapsChanged = true;
      }

      // e.g. LabelNode as last instruction to denote the end of a trap including the last Stmt in
      // serializd form

      while (nextStartingTrap != null && nextStartingTrap.getBeginStmt() == headStmt) {
        Trap trap = nextStartingTrap;
        nextStartingTrap = trapStart.poll();
        final Trap existingTrapForException = activeTrapMap.get(trap.getExceptionType());
        if (existingTrapForException == null) {
          activeTrapMap.put(trap.getExceptionType(), trap);
        } else {
          final PriorityQueue<Trap> overridenTraps =
              overlappingTraps.computeIfAbsent(
                  trap.getExceptionType(), k -> new PriorityQueue<>(trapComparator));

          Trap trapToApply;
          if (trapComparator.compare(existingTrapForException, trap) < 0) {
            overridenTraps.add(trap);
            trapToApply = existingTrapForException;
          } else {
            overridenTraps.add(existingTrapForException);
            trapToApply = trap;
          }

          activeTrapMap.put(trapToApply.getExceptionType(), trapToApply);
        }
        trapsChanged = true;
      }

      if (trapsChanged) {
        exceptionToHandlerMap.clear();
        activeTrapMap.forEach(
            (type, trap) -> exceptionToHandlerMap.put(type, trap.getHandlerStmt()));

        /* debugprint
         System.out.println("-- "+ i +" --");
         activeTrapMap.values().stream().sorted(getTrapComparator(trapstmtToIdx)).forEach(t -> System.out.println( t.getExceptionType() + " "+ trapstmtToIdx.get(t.getBeginStmt()) + " " + trapstmtToIdx.get(t.getEndStmt()) + " -> " +trapstmtToIdx.get(t.getHandlerStmt())));
        */
      }

      final MutableBasicBlockImpl current = newBlocks[i];
      exceptionToHandlerMap.forEach(
          (type, handler) -> {
            MutableBasicBlockImpl handlerBlock = headToBlock.get(handler);
            current.linkExc(type, handlerBlock != null ? handlerBlock : blockStartingAt(handler));
          });
    }

    // a Trap whose end is in no Block reaches the end of the body, so it is never closed above
    while (nextEndingTrap != null
        && trapstmtToIdx.get(nextEndingTrap.getEndStmt()) == blocks.size()) {
      nextEndingTrap = trapEnd.poll();
    }

    if (nextStartingTrap != null || nextEndingTrap != null) {
      throw new IllegalStateException("The Traps are not iterated completely/correctly!");
      //      System.out.println("The Traps are not iterated completely/correctly");
    }

    // link blocks
    for (int blockIdx = 0, blockStmtsSize = blocks.size(); blockIdx < blockStmtsSize; blockIdx++) {
      List<Stmt> block = blocks.get(blockIdx);
      Stmt tailStmt = block.get(block.size() - 1);

      int succIdxOffset;
      if (tailStmt instanceof FallsThroughStmt) {
        succIdxOffset = 1;
        int fallsThroughTargetIdx = blockIdx + 1;
        if (fallsThroughTargetIdx >= blocks.size()) {
          throw new IllegalStateException(
              "FallsthroughStmt '"
                  + tailStmt
                  + "' falls into the abyss - as there is no following Block!");
        }
        List<Stmt> followingBlock = blocks.get(fallsThroughTargetIdx);
        Stmt followingBlocksHead = followingBlock.get(0);
        blockOf(tailStmt).link(0, headToBlock.get(followingBlocksHead));
      } else {
        succIdxOffset = 0;
      }

      if (tailStmt instanceof BranchingStmt) {
        // => end of Block
        final List<Stmt> targets = successorMap.get(tailStmt);
        int expectedBranchingEntries = tailStmt.getExpectedSuccessorCount() - succIdxOffset;
        if (targets == null || targets.size() != expectedBranchingEntries) {
          int targetCount = targets == null ? 0 : targets.size();

          throw new IllegalArgumentException(
              "The corresponding successorMap entry for the BranchingStmt ('"
                  + tailStmt
                  + "') needs to have exactly the amount of targets as the BranchingStmt has successors blockIdx.e. "
                  + expectedBranchingEntries
                  + " but has "
                  + targetCount
                  + ".");
        }
        final BranchingStmt bStmt = (BranchingStmt) tailStmt;
        for (int k = 0; k < targets.size(); k++) {
          Stmt target = targets.get(k);
          // a possible fallsthrough (e.g. from IfStmt) is not in successorMap
          blockOf(bStmt).link(k + succIdxOffset, blockStartingAt(target));
        }
      }
    }
    // the given blocks need not be maximal
    for (MutableBasicBlockImpl b : new ArrayList<>(this.blocks)) {
      normalize(b);
    }
  }

  /** checks the legacy block representation before anything is created */
  private void validateLegacyBlocks(
      @NonNull List<List<Stmt>> blocks, @NonNull Map<BranchingStmt, List<Stmt>> successorMap) {
    Set<Stmt> seen = Collections.newSetFromMap(new IdentityHashMap<>());
    for (int i = 0; i < blocks.size(); i++) {
      List<Stmt> block = blocks.get(i);
      if (block.isEmpty()) {
        throw new IllegalArgumentException("Block " + i + " is empty.");
      }
      for (int j = 0; j < block.size(); j++) {
        Stmt s = block.get(j);
        if (stmtToBlock.containsKey(s) || !seen.add(s)) {
          throw new IllegalArgumentException("Stmt '" + s + "' is contained twice.");
        }
        if (j < block.size() - 1
            && (s instanceof BranchingStmt || !(s instanceof FallsThroughStmt))) {
          throw new IllegalArgumentException(
              "Only the last Stmt of a Block may branch or end the flow: '" + s + "'.");
        }
      }
      Stmt tail = block.get(block.size() - 1);
      int offset = 0;
      if (tail instanceof FallsThroughStmt) {
        offset = 1;
        if (i + 1 >= blocks.size()) {
          throw new IllegalStateException(
              "FallsthroughStmt '"
                  + tail
                  + "' falls into the abyss - as there is no following Block!");
        }
      }
      if (tail instanceof BranchingStmt) {
        List<Stmt> targets = successorMap.get(tail);
        int expected = tail.getExpectedSuccessorCount() - offset;
        int targetCount = targets == null ? 0 : targets.size();
        if (targetCount != expected) {
          throw new IllegalArgumentException(
              "The corresponding successorMap entry for the BranchingStmt ('"
                  + tail
                  + "') needs to have exactly the amount of targets as the BranchingStmt has successors i.e. "
                  + expected
                  + " but has "
                  + targetCount
                  + ".");
        }
      }
    }
  }

  private static int getTrapApplicationComparator(
      HashMap<Stmt, Integer> trapstmtToIdx, Trap trapA, Trap trapB) {
    if (trapA.getEndStmt() == trapB.getEndStmt()) {
      if (trapA.getBeginStmt() == trapB.getBeginStmt()) {
        final Integer handlerIdxA = trapstmtToIdx.get(trapA.getHandlerStmt());
        final Integer handlerIdxB = trapstmtToIdx.get(trapB.getHandlerStmt());
        return handlerIdxA - handlerIdxB;
      } else {
        final Integer beginIdxA = trapstmtToIdx.get(trapA.getBeginStmt());
        final Integer beginIdxB = trapstmtToIdx.get(trapB.getBeginStmt());
        return beginIdxB - beginIdxA;
      }
    } else {
      final Integer endIdxA = trapstmtToIdx.get(trapA.getEndStmt());
      final Integer endIdxB = trapstmtToIdx.get(trapB.getEndStmt());
      return endIdxA - endIdxB;
    }
  }

  private static void duplicateCatchAllTrapRemover(
      @NonNull List<Trap> traps, Map<Stmt, Integer> trapstmtToIdx) {
    /*
     * handle duplicate catchall traps here - aka integrated "DuplicateCatchAllTrapRemover" Transformer/Interceptor
     *
     * Some compilers generate duplicate traps:
     *
     * <p>Exception table: from to target type
     *  9 30 37 Class java/lang/Throwable
     *  9 30 44  any
     *  37 46 44 any
     *
     * <p>The semantics is as follows:
     *
     * <p>try { // block } catch { // handler 1 } finally { // handler 2 }
     *
     * or (e.g. with java.lang.Exception):
     *
     * <p> try{        try { // block } catch { // handler 1 }      }catch { // handler 2 }
     *
     *
     * <p>In this case, the first trap covers the block and jumps to handler 1. The second trap also
     * covers the block and jumps to handler 2. The third trap covers handler 1 and jumps to handler 2.
     * If we treat "any" as java.lang. Throwable, the second handler is clearly unnecessary. Worse, it
     * violates Soot's invariant that there may only be one handler per combination of covered code
     * region and jump target.
     *
     *
     * <p>This interceptor detects and removes such unnecessary traps.
     *
     * @author Steven Arzt
     * @auhor Markus Schmidt
     */

    if (traps.size() > 2) {
      // Find two traps that use java.lang.Throwable as their type and that span the same code
      // region
      for (int i = 0, trapsSize = traps.size(); i < trapsSize; i++) {
        Trap trap1 = traps.get(i);
        // [ms]: maybe it needs more generalization to be applicable with more exception types?
        final String fullyQualifiedName1 = trap1.getExceptionType().getFullyQualifiedName();
        // FIXME(#430): [ms] adapt to work with java module, too
        if (fullyQualifiedName1.equals("java.lang.Throwable")
            || fullyQualifiedName1.equals("java.lang.Exception")) {
          for (int j = 0; j < trapsSize; j++) {
            Trap trap2 = traps.get(j);
            final String fullyQualifiedName2 = trap2.getExceptionType().getFullyQualifiedName();
            if (trap1 != trap2
                && trap1.getBeginStmt() == trap2.getBeginStmt()
                && trap1.getEndStmt() == trap2.getEndStmt()
                && fullyQualifiedName2.equals(fullyQualifiedName1)) {
              // Both traps (t1, t2) span the same code and catch java.lang.Throwable.
              // Check if one trap jumps to a target that then jumps to the target of the other trap
              for (int k = 0; k < trapsSize; k++) {

                Trap trap3 = traps.get(k);
                final int trap3StartIdx = trapstmtToIdx.get(trap3.getBeginStmt());
                final int trap3EndIdx =
                    trapstmtToIdx.get(trap3.getEndStmt()); // endstmt is exclusive!

                if (trap3 != trap1
                    && trap3 != trap2
                    && trap3
                        .getExceptionType()
                        .getFullyQualifiedName()
                        .equals(fullyQualifiedName2)) {
                  int trap1HandlerIdx = trapstmtToIdx.get(trap1.getHandlerStmt());
                  if (trap3StartIdx <= trap1HandlerIdx
                      && trap1HandlerIdx < trap3EndIdx
                      && trap3.getHandlerStmt() == trap2.getHandlerStmt()) {
                    // c -> t1 -> t3 -> t2 && x -> t2
                    traps.remove(trap2);
                    j--;
                    trapsSize--;
                    break;
                  } else {
                    int trap2HandlerIdx = trapstmtToIdx.get(trap2.getHandlerStmt());
                    if ((trap3StartIdx <= trap2HandlerIdx && trap2HandlerIdx < trap3EndIdx)
                        && trap3.getHandlerStmt() == trap1.getHandlerStmt()) {
                      // c -> t2 -> t3 -> t1 && c -> t1
                      traps.remove(trap1);
                      i--;
                      trapsSize--;
                      break;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // ===========================================================================================
  // structural primitives

  /** call before every change */
  private void modified() {
    stmtsInOrder = null;
  }

  @NonNull
  private MutableBasicBlockImpl blockOf(@NonNull Stmt stmt) {
    MutableBasicBlockImpl block = stmtToBlock.get(stmt);
    if (block == null) {
      throw new IllegalArgumentException(
          "Stmt '" + stmt + "' is not contained in the BlockControlFlowGraph");
    }
    return block;
  }

  /** creates a block holding only stmt */
  @NonNull
  private MutableBasicBlockImpl newBlock(@NonNull Stmt stmt) {
    if (stmtToBlock.containsKey(stmt)) {
      throw new IllegalArgumentException("Stmt is already in the graph!");
    }
    MutableBasicBlockImpl block = new MutableBasicBlockImpl();
    block.addStmt(stmt);
    stmtToBlock.put(stmt, block);
    blocks.add(block);
    return block;
  }

  /** the block whose head is stmt; creates it or splits the containing block */
  @NonNull
  private MutableBasicBlockImpl blockStartingAt(@NonNull Stmt stmt) {
    MutableBasicBlockImpl block = stmtToBlock.get(stmt);
    if (block == null) {
      return newBlock(stmt);
    }
    int idx = block.indexOf(stmt);
    return idx == 0 ? block : split(block, idx);
  }

  /**
   * Splits block before idx: the stmts [idx, n) move to a new block which takes over the successors
   * and gets the same exceptional successors; block falls through into it.
   *
   * @return the block whose head is the stmt formerly at idx
   */
  @NonNull
  private MutableBasicBlockImpl split(@NonNull MutableBasicBlockImpl block, int idx) {
    if (idx <= 0 || idx >= block.size()) {
      throw new IndexOutOfBoundsException("split index " + idx + " of " + block);
    }
    modified();
    MutableBasicBlockImpl[] targets = successorsOf(block);
    block.unlinkAllSuccessors();
    MutableBasicBlockImpl second = new MutableBasicBlockImpl();
    for (Stmt s : block.removeStmtsFrom(idx)) {
      second.addStmt(s);
      stmtToBlock.put(s, second);
    }
    relink(second, targets);
    second.copyExcFrom(block);
    block.link(0, second);
    blocks.add(second);
    return second;
  }

  /** splits around stmt so that it is the only stmt of its block */
  @NonNull
  private MutableBasicBlockImpl isolate(@NonNull Stmt stmt) {
    MutableBasicBlockImpl block = blockOf(stmt);
    int idx = block.indexOf(stmt);
    if (idx > 0) {
      block = split(block, idx);
    }
    if (block.size() > 1) {
      split(block, 1);
    }
    return block;
  }

  private static MutableBasicBlockImpl[] successorsOf(MutableBasicBlockImpl block) {
    MutableBasicBlockImpl[] targets = new MutableBasicBlockImpl[block.successorSlots()];
    for (int i = 0; i < targets.length; i++) {
      targets[i] = block.successorAt(i);
    }
    return targets;
  }

  private static void relink(MutableBasicBlockImpl block, MutableBasicBlockImpl[] targets) {
    for (int i = 0; i < targets.length; i++) {
      if (targets[i] != null) {
        block.link(i, targets[i]);
      }
    }
  }

  /**
   * true if b can be appended to a: a falls through into b only, b is reached only from a, b is no
   * trap handler and not the start, both have the same traps.
   */
  private boolean isMergeable(@NonNull MutableBasicBlockImpl a, @NonNull MutableBasicBlockImpl b) {
    if (a == b || a.size() == 0 || b.size() == 0) {
      return false;
    }
    Stmt tail = a.getTail();
    return !tail.branches()
        && tail instanceof FallsThroughStmt
        && a.successorAt(0) == b
        && b.normalPredecessors().size() == 1
        && b.exceptionalPredecessors().isEmpty()
        && b.getHead() != startingStmt
        && a.hasSameExceptionalSuccessors(b);
  }

  /**
   * joins a with its fall-through successor b (must be mergeable). The stmts of the smaller block
   * move; returns the surviving block. The other block leaves the graph but keeps its stmt list, so
   * callers holding it can still read its head.
   */
  @NonNull
  private MutableBasicBlockImpl merge(
      @NonNull MutableBasicBlockImpl a, @NonNull MutableBasicBlockImpl b) {
    modified();
    a.unlink(0);
    if (a.size() >= b.size()) {
      MutableBasicBlockImpl[] targets = successorsOf(b);
      b.unlinkAllSuccessors();
      b.unlinkAllExc();
      for (Stmt s : b.getStmts()) {
        a.addStmt(s);
        stmtToBlock.put(s, a);
      }
      relink(a, targets);
      blocks.remove(b);
      return a;
    }
    // a is smaller: put its stmts in front of b, b takes over a's predecessors
    redirectPredecessors(a, b);
    a.unlinkAllExc();
    List<Stmt> front = new ArrayList<>(a.getStmts());
    b.prependStmts(front);
    for (Stmt s : front) {
      stmtToBlock.put(s, b);
    }
    blocks.remove(a);
    return b;
  }

  /** merges block with its predecessor and/or successor where possible; returns the survivor */
  @Nullable
  private MutableBasicBlockImpl normalize(@Nullable MutableBasicBlockImpl block) {
    if (block == null || !blocks.contains(block)) {
      return null;
    }
    List<MutableBasicBlockImpl> preds = block.normalPredecessors();
    if (preds.size() == 1 && isMergeable(preds.get(0), block)) {
      block = merge(preds.get(0), block);
    }
    if (block.successorSlots() == 1) {
      MutableBasicBlockImpl succ = block.successorAt(0);
      if (succ != null && isMergeable(block, succ)) {
        block = merge(block, succ);
      }
    }
    return block;
  }

  /** the block that block falls through into, or null */
  @Nullable
  private static MutableBasicBlockImpl fallThroughSuccessor(@NonNull MutableBasicBlockImpl block) {
    return block.getTail() instanceof FallsThroughStmt ? block.successorAt(0) : null;
  }

  /** true if the fall-through chain starting at from reaches to */
  private static boolean fallsThroughTo(
      @Nullable MutableBasicBlockImpl from, @NonNull MutableBasicBlockImpl to) {
    Set<MutableBasicBlockImpl> seen = Collections.newSetFromMap(new IdentityHashMap<>());
    for (MutableBasicBlockImpl c = from; c != null && seen.add(c); c = fallThroughSuccessor(c)) {
      if (c == to) {
        return true;
      }
    }
    return false;
  }

  private static IllegalArgumentException fallThroughCycle(Stmt from, Stmt to) {
    return new IllegalArgumentException(
        "A fall-through edge from '"
            + from
            + "' to '"
            + to
            + "' closes a fall-through cycle, which can not be linearized - use a goto.");
  }

  /** removes every edge into and out of block */
  private void detach(@NonNull MutableBasicBlockImpl block) {
    block.unlinkAllSuccessors();
    block.unlinkAllExc();
    detachPredecessors(block);
  }

  /** removes every normal and exceptional edge into block */
  private static void detachPredecessors(@NonNull MutableBasicBlockImpl block) {
    for (MutableBasicBlockImpl p : MutableBasicBlockImpl.distinct(block.normalPredecessors())) {
      p.unlinkAllTo(block);
    }
    for (MutableBasicBlockImpl p :
        MutableBasicBlockImpl.distinct(block.exceptionalPredecessors())) {
      p.unlinkExcTo(block);
    }
  }

  /** re-points every normal and exceptional edge into from to to */
  private static void redirectPredecessors(
      @NonNull MutableBasicBlockImpl from, @NonNull MutableBasicBlockImpl to) {
    for (MutableBasicBlockImpl p : MutableBasicBlockImpl.distinct(from.normalPredecessors())) {
      for (int i = 0; i < p.successorSlots(); i++) {
        if (p.successorAt(i) == from) {
          p.link(i, to);
        }
      }
    }
    for (MutableBasicBlockImpl p : MutableBasicBlockImpl.distinct(from.exceptionalPredecessors())) {
      for (Map.Entry<ClassType, MutableBasicBlockImpl> e :
          new ArrayList<>(p.exceptionalSuccessorMap().entrySet())) {
        if (e.getValue() == from) {
          p.linkExc(e.getKey(), to);
        }
      }
    }
  }

  /** adds the traps to every block holding one of stmts; handlers become block heads */
  private void applyTraps(
      @NonNull List<? extends Stmt> stmts, @NonNull Map<ClassType, Stmt> traps) {
    if (traps.isEmpty()) {
      return;
    }
    Map<ClassType, MutableBasicBlockImpl> handlerBlocks = new HashMap<>();
    traps.forEach((type, handler) -> handlerBlocks.put(type, blockStartingAt(handler)));
    Set<MutableBasicBlockImpl> done = Collections.newSetFromMap(new IdentityHashMap<>());
    for (Stmt s : stmts) {
      MutableBasicBlockImpl block = blockOf(s);
      if (done.add(block)) {
        handlerBlocks.forEach(block::linkExc);
      }
    }
  }

  /** creates one (unlinked) block of the stmts, which must fall through except the last one */
  @NonNull
  private MutableBasicBlockImpl newBlock(@NonNull List<? extends Stmt> stmts) {
    MutableBasicBlockImpl block = newBlock(stmts.get(0));
    for (int i = 1; i < stmts.size(); i++) {
      block.addStmt(stmts.get(i));
      stmtToBlock.put(stmts.get(i), block);
    }
    return block;
  }

  // ===========================================================================================
  // mutations

  @Override
  public void addExceptionalEdge(
      @NonNull Stmt stmt, @NonNull ClassType exceptionType, @NonNull Stmt traphandlerStmt) {
    MutableBasicBlockImpl block = blockOf(stmt);
    MutableBasicBlockImpl current = block.exceptionalSuccessor(exceptionType);
    if (current != null && current.getHead() == traphandlerStmt) {
      return;
    }
    modified();
    isolate(stmt);
    applyTraps(
        Collections.singletonList(stmt), Collections.singletonMap(exceptionType, traphandlerStmt));
    normalize(blockOf(stmt));
  }

  @Override
  public void removeExceptionalEdge(@NonNull Stmt node, @NonNull ClassType exceptionType) {
    MutableBasicBlockImpl block = blockOf(node);
    if (block.exceptionalSuccessor(exceptionType) == null) {
      throw new IllegalArgumentException(
          "there is no handler for the given ClassType: " + exceptionType);
    }
    modified();
    MutableBasicBlockImpl separated = isolate(node);
    separated.unlinkExc(exceptionType);
    normalize(separated);
  }

  @Override
  public void clearExceptionalEdges(@NonNull Stmt node) {
    MutableBasicBlockImpl block = blockOf(node);
    if (block.exceptionalSuccessorMap().isEmpty()) {
      return;
    }
    modified();
    MutableBasicBlockImpl separated = isolate(node);
    separated.unlinkAllExc();
    normalize(separated);
  }

  @Override
  public void removeExceptionalFlowFromAllBlocks(
      @NonNull ClassType exceptionType, @NonNull Stmt exceptionHandlerStmt) {
    MutableBasicBlockImpl handler = stmtToBlock.get(exceptionHandlerStmt);
    if (handler == null || handler.getHead() != exceptionHandlerStmt) {
      return;
    }
    modified();
    List<MutableBasicBlockImpl> affected = new ArrayList<>();
    for (MutableBasicBlockImpl p :
        MutableBasicBlockImpl.distinct(handler.exceptionalPredecessors())) {
      if (p.exceptionalSuccessor(exceptionType) == handler) {
        p.unlinkExc(exceptionType);
        affected.add(p);
      }
    }
    affected.forEach(this::normalize);
  }

  @Override
  @NonNull
  public Set<? extends BasicBlock<?>> getBlocks() {
    return blocks;
  }

  @NonNull
  public List<? extends BasicBlock<?>> getBlocksSorted() {
    return new ReversePostOrderBlockTraversal(this).getBlockTraversal();
  }

  /**
   * Adds a block. Its Stmts must be new to the graph, except: the first one may be an existing
   * single-Stmt block without successors, the last one may be the head of an existing block (it is
   * then linked as successor). Only the last Stmt may branch or end the flow.
   */
  @Override
  public void addBlock(@NonNull List<Stmt> stmts, @NonNull Map<ClassType, Stmt> trapMap) {
    if (stmts.isEmpty()) {
      return;
    }
    final int n = stmts.size();
    final Stmt first = stmts.get(0);
    final MutableBasicBlockImpl existingFirst = stmtToBlock.get(first);
    if (existingFirst != null) {
      if (existingFirst.getHead() != first || existingFirst.hasSuccessor()) {
        throw new IllegalArgumentException(
            "The first Stmt in the List is already in the ControlFlowGraph and is not the head of a Block where currently no successor are set, yet.");
      } else if (existingFirst.size() > 1) {
        throw new IllegalArgumentException(
            "The first Stmt in the List is already in the ControlFlowGraph and has at least one (fallsthrough) successor in its Block.");
      }
    }
    boolean lastIsExistingHead = false;
    Set<Stmt> seen = Collections.newSetFromMap(new IdentityHashMap<>());
    seen.add(first);
    for (int i = 1; i < n; i++) {
      Stmt s = stmts.get(i);
      MutableBasicBlockImpl b = stmtToBlock.get(s);
      if (b != null || !seen.add(s)) {
        if (i == n - 1 && b != null && b.getHead() == s) {
          lastIsExistingHead = true;
        } else {
          throw new IllegalArgumentException(
              "the Stmt '"
                  + s
                  + "' you want to add as a Stmt of a whole Block is already in this ControlFlowGraph.");
        }
      }
    }
    for (int i = 0; i < n - 1; i++) {
      Stmt s = stmts.get(i);
      if (s instanceof BranchingStmt || !(s instanceof FallsThroughStmt)) {
        throw new IllegalArgumentException(
            "Can't add another Stmt to a Block after a BranchingStmt.");
      }
    }

    modified();
    final int end = lastIsExistingHead ? n - 1 : n;
    MutableBasicBlockImpl block = existingFirst != null ? existingFirst : newBlock(first);
    for (int i = 1; i < end; i++) {
      block.addStmt(stmts.get(i));
      stmtToBlock.put(stmts.get(i), block);
    }
    applyTraps(stmts.subList(0, end), trapMap);
    if (lastIsExistingHead) {
      MutableBasicBlockImpl tailBlock = blockOf(stmts.get(end - 1));
      tailBlock.link(0, blockOf(stmts.get(n - 1)));
      normalize(tailBlock);
    }
  }

  @Override
  public void removeBlock(BasicBlock<?> block) {
    // membership by stmt index: callers may already have removed it from getBlocks()
    if (!(block instanceof MutableBasicBlockImpl)
        || block.getStmtCount() == 0
        || stmtToBlock.get(block.getHead()) != block) {
      throw new IllegalArgumentException(
          "The given block is not contained in this MutableBlockControlFlowGraph.");
    }
    modified();
    MutableBasicBlockImpl b = (MutableBasicBlockImpl) block;
    detach(b);
    for (Stmt s : b.getStmts()) {
      stmtToBlock.remove(s);
      if (s == startingStmt) {
        startingStmt = null;
      }
    }
    blocks.remove(b);
  }

  @Override
  public void addNode(@NonNull Stmt stmt, @NonNull Map<ClassType, Stmt> exceptions) {
    MutableBasicBlockImpl block = stmtToBlock.get(stmt);
    if (block == null) {
      modified();
      newBlock(stmt);
      applyTraps(Collections.singletonList(stmt), exceptions);
      return;
    }
    if (isExceptionalFlowDifferent(exceptions, block)) {
      modified();
      MutableBasicBlockImpl separated = isolate(stmt);
      separated.unlinkAllExc();
      applyTraps(Collections.singletonList(stmt), exceptions);
      normalize(blockOf(stmt));
    }
  }

  private static boolean isExceptionalFlowDifferent(
      Map<ClassType, Stmt> exceptions, MutableBasicBlockImpl block) {
    Map<ClassType, MutableBasicBlockImpl> current = block.exceptionalSuccessorMap();
    if (exceptions.size() != current.size()) {
      return true;
    }
    for (Map.Entry<ClassType, MutableBasicBlockImpl> e : current.entrySet()) {
      if (exceptions.get(e.getKey()) != e.getValue().getHead()) {
        return true;
      }
    }
    return false;
  }

  public void removeNode(@NonNull Stmt stmt) {
    removeNode(stmt, true);
  }

  /**
   * Removes a Stmt from the ControlFlowGraph.
   *
   * <p>It can optionally keep the flow (edges) of the statement by connecting the predecessors of
   * the statement with successors of the statement. Keeping the flow does not work when the
   * statement has multiple successors.
   *
   * @param stmt the Stmt to be removed
   * @param keepFlow flag indicating whether to keep the flow or not
   * @throws IllegalArgumentException if keepFlow is true but the stmt has multiple successors
   */
  public void removeNode(@NonNull Stmt stmt, boolean keepFlow) {
    final MutableBasicBlockImpl block = stmtToBlock.get(stmt);
    if (block == null) {
      throw new IllegalArgumentException(
          "stmt '" + stmt + "' is not contained in this ControlFlowGraph!");
    }
    final int n = block.size();
    if (keepFlow && stmt instanceof BranchingStmt) {
      if (n == 1) {
        // check for successorCount == 1 is not enough as it could be that we want to replace a
        // Branching Stmt via a FallsThroughStmt and the linearized ControlFlowGraph would have no
        // necessary goto anymore.
        throw new IllegalArgumentException("Cannot keep the flow if we remove a BranchingStmt!");
      }
      if (stmt.getExpectedSuccessorCount() > 1) {
        throw new IllegalArgumentException(
            "Cannot keep the flows of a removed BranchingStmt if there is more than one successor.");
      }
    }
    modified();
    final int idx = block.indexOf(stmt);

    if (n == 1) {
      MutableBasicBlockImpl succ =
          keepFlow && block.successorSlots() == 1 ? block.successorAt(0) : null;
      if (succ == block) {
        succ = null;
      }
      if (succ != null) {
        for (MutableBasicBlockImpl p : MutableBasicBlockImpl.distinct(block.normalPredecessors())) {
          for (int i = 0; i < p.successorSlots(); i++) {
            if (p.successorAt(i) == block) {
              p.link(i, succ);
            }
          }
        }
      }
      if (stmt == startingStmt) {
        startingStmt = succ != null ? succ.getHead() : null;
      }
      detach(block);
      stmtToBlock.remove(stmt);
      blocks.remove(block);
      normalize(succ);
      return;
    }

    if (idx == 0) {
      if (!keepFlow) {
        detachPredecessors(block);
      }
      block.removeStmt(0);
      stmtToBlock.remove(stmt);
      if (stmt == startingStmt) {
        startingStmt = keepFlow ? block.getHead() : null;
      }
      return;
    }

    if (idx == n - 1) {
      if (!keepFlow) {
        block.unlinkAllSuccessors();
      }
      // the new tail keeps the successor slots it can hold
      block.removeStmt(idx);
      stmtToBlock.remove(stmt);
      if (keepFlow) {
        normalize(block);
      }
      return;
    }

    // stmt is in the middle of the block
    if (!keepFlow) {
      split(block, idx + 1);
      block.unlink(0);
    }
    block.removeStmt(idx);
    stmtToBlock.remove(stmt);
  }

  @Override
  public void replaceNode(@NonNull Stmt oldStmt, @NonNull Stmt newStmt) {
    if (oldStmt == newStmt) {
      return;
    }
    final MutableBasicBlockImpl block = stmtToBlock.get(oldStmt);
    if (block == null) {
      throw new IllegalArgumentException("oldStmt does not exist in the ControlFlowGraph!");
    }
    if (stmtToBlock.containsKey(newStmt)) {
      throw new IllegalArgumentException("newStmt is already in the ControlFlowGraph!");
    }
    final int idx = block.indexOf(oldStmt);
    if (oldStmt.branches() && newStmt.branches()) {
      // keep remaining flows (in order) if they fit, e.g. JIfStmt with a removed edge ->
      // JGotoStmt; missing flows can be added by the caller afterwards
      int remaining = block.getSuccessors().size();
      if (remaining > newStmt.getExpectedSuccessorCount()) {
        throw new IllegalArgumentException(
            "We can't keep the flows if we replace a Stmt ("
                + remaining
                + " remaining flows) by another Stmt which expects less ("
                + newStmt.getExpectedSuccessorCount()
                + ") successors.");
      }
    }
    if (newStmt instanceof FallsThroughStmt
        && !newStmt.branches()
        && !(oldStmt instanceof FallsThroughStmt)
        && block.successorSlots() > 0
        && fallsThroughTo(block.successorAt(0), block)) {
      throw fallThroughCycle(newStmt, block.successorAt(0).getHead());
    }
    modified();
    final boolean newIsInner = newStmt instanceof FallsThroughStmt && !newStmt.branches();
    if (!newIsInner && idx < block.size() - 1) {
      // only a tail may branch or end the flow
      split(block, idx + 1);
    }
    final boolean isTail = idx == block.size() - 1;
    block.setStmt(idx, newStmt);
    stmtToBlock.remove(oldStmt);
    stmtToBlock.put(newStmt, block);
    if (oldStmt == startingStmt) {
      startingStmt = newStmt;
    }
    if (isTail) {
      normalize(block);
    }
  }

  /** checks the internal consistency; throws an IllegalStateException on a violation */
  public void validateBlocks() {
    for (MutableBasicBlockImpl block : blocks) {
      if (block.size() == 0) {
        throw new IllegalStateException("empty block in graph");
      }
      for (int i = 0; i < block.size(); i++) {
        Stmt stmt = block.get(i);
        if (stmtToBlock.get(stmt) != block) {
          throw new IllegalStateException("wrong stmt to block mapping!");
        }
        if (block.indexOf(stmt) != i) {
          throw new IllegalStateException("index numbering is out of sync!");
        }
      }
      for (int i = 0; i < block.successorSlots(); i++) {
        MutableBasicBlockImpl s = block.successorAt(i);
        if (s != null && !blocks.contains(s)) {
          throw new IllegalStateException("dangling successor of " + block);
        }
      }
      for (MutableBasicBlockImpl h : block.exceptionalSuccessorMap().values()) {
        if (!blocks.contains(h)) {
          throw new IllegalStateException("dangling exceptional successor of " + block);
        }
      }
    }
  }

  /*
   * Insert a list of FallsThroughStmts before an existing Stmt in this ControlFlowGraph.
   * After insertion, all predecessors of the existing Stmt are the predecessors of the first inserted Stmt.
   * If link one predecessor(partial) of the existing Stmt, please use insertAfter(predecessorStmt, stmts)
   *
   * @param existingStmt: the Stmt which succeeds the inserted Stmts (it is NOT preceeding as this
   *                   simplifies the handling of BranchingStmts)
   * @param stmts: a list of FallsThroughStmts except for IfStmt
   * @param exceptionMap: trap map of the inserted stmts.
   *
   * @return a block containing the inserted Stmts.
   */
  @NonNull
  public BasicBlock<?> insertBefore(
      @NonNull Stmt existingStmt,
      @NonNull List<FallsThroughStmt> stmts,
      @NonNull Map<ClassType, Stmt> exceptionMap) {
    MutableBasicBlockImpl existingBlock = blockOf(existingStmt);
    if (stmts.isEmpty()) {
      return existingBlock;
    }
    if (!insertable(stmts)) {
      throw new IllegalArgumentException("The given list contains IfStmt or illegal order!");
    }
    checkNew(stmts);
    modified();
    final MutableBasicBlockImpl target = blockStartingAt(existingStmt);
    newBlock(stmts);
    applyTraps(stmts, exceptionMap);
    final Stmt first = stmts.get(0);
    redirectPredecessors(target, blockOf(first));
    blockOf(stmts.get(stmts.size() - 1)).link(0, target);
    if (existingStmt == startingStmt) {
      startingStmt = first;
    }
    normalize(blockOf(first));
    normalize(blockOf(existingStmt));
    return blockOf(first);
  }

  /*
   * Insert a list of FallsThroughStmts after an existing Stmt in this ControlFlowGraph.
   *
   * @param existingStmt: the Stmt which precedes the inserted Stmts, it should be a FallsThroughStmt except for IfStmt
   * @param stmts: a list of FallsThroughStmts except for IfStmt
   * @param exceptionMap: trap map of the inserted stmts.
   *
   * @return a block containing the inserted Stmts.
   */
  @NonNull
  public BasicBlock<?> insertAfter(
      @NonNull Stmt existingStmt,
      @NonNull List<FallsThroughStmt> stmts,
      @NonNull Map<ClassType, Stmt> exceptionMap) {
    MutableBasicBlockImpl existingBlock = blockOf(existingStmt);
    if (stmts.isEmpty()) {
      return existingBlock;
    }
    if (!(existingStmt instanceof FallsThroughStmt) || existingStmt instanceof JIfStmt) {
      throw new IllegalArgumentException(
          "The given existingStmt doesn't fall through or is an IfStmt");
    }
    if (!insertable(stmts)) {
      throw new IllegalArgumentException("The given list contains IfStmt or illegal order!");
    }
    if (containsIdentity(stmts) && !(existingStmt instanceof JIdentityStmt)) {
      throw new IllegalArgumentException(
          "JIdentityStmt should be not after Stmts with other types!");
    }
    checkNew(stmts);
    modified();
    int idx = existingBlock.indexOf(existingStmt);
    if (idx < existingBlock.size() - 1) {
      split(existingBlock, idx + 1);
    }
    final MutableBasicBlockImpl oldSuccessor = existingBlock.successorAt(0);
    newBlock(stmts);
    applyTraps(stmts, exceptionMap);
    final Stmt first = stmts.get(0);
    final Stmt last = stmts.get(stmts.size() - 1);
    blockOf(existingStmt).link(0, blockOf(first));
    if (oldSuccessor != null) {
      blockOf(last).link(0, oldSuccessor);
    }
    normalize(blockOf(first));
    normalize(blockOf(last));
    return blockOf(first);
  }

  private void checkNew(List<? extends Stmt> stmts) {
    Set<Stmt> seen = Collections.newSetFromMap(new IdentityHashMap<>());
    for (Stmt s : stmts) {
      if (stmtToBlock.containsKey(s) || !seen.add(s)) {
        throw new IllegalArgumentException("Stmt '" + s + "' is already in the ControlFlowGraph.");
      }
    }
  }

  // check the validity of the inserted stmts. They should contain no JIfStmt. The inserted
  // JIdentityStmts should be not
  // after any stmt with other stmt-type.
  private boolean insertable(List<FallsThroughStmt> stmts) {
    boolean hasIdentity = false;
    boolean identityBehindAssign = false;
    for (Stmt stmt : stmts) {
      if (identityBehindAssign) {
        return false;
      }
      if (stmt instanceof JIfStmt) {
        return false;
      }
      if (stmt instanceof JIdentityStmt) {
        hasIdentity = true;
      }
      if (hasIdentity && !(stmt instanceof JIdentityStmt)) {
        identityBehindAssign = true;
      }
    }
    return true;
  }

  private boolean containsIdentity(List<FallsThroughStmt> stmts) {
    return stmts.stream().anyMatch(stmt -> stmt instanceof JIdentityStmt);
  }

  /** Replaces all SuccessorEdge(s) of from to oldTo by newTo */
  @Override
  public boolean replaceSucessorEdge(@NonNull Stmt from, @NonNull Stmt oldTo, @NonNull Stmt newTo) {
    final MutableBasicBlockImpl fromBlock = blockOf(from);
    final MutableBasicBlockImpl oldBlock = blockOf(oldTo);
    if (fromBlock.getTail() != from) {
      // the only successor of an inner stmt is its next stmt
      if (fromBlock == oldBlock && fromBlock.indexOf(oldTo) == fromBlock.indexOf(from) + 1) {
        removeEdge(from, oldTo);
        putEdge((FallsThroughStmt) from, newTo);
        return true;
      }
      return false;
    }
    if (oldBlock.getHead() != oldTo) {
      return false;
    }
    List<Integer> idx = new ArrayList<>();
    for (int i = 0; i < fromBlock.successorSlots(); i++) {
      if (fromBlock.successorAt(i) == oldBlock) {
        idx.add(i);
      }
    }
    if (idx.isEmpty()) {
      return false;
    }
    for (int i : idx) {
      putEdgeInternal(from, i, newTo);
    }
    return true;
  }

  public void putEdge(@NonNull FallsThroughStmt stmtA, @NonNull Stmt stmtB) {
    putEdgeInternal(stmtA, 0, stmtB);
  }

  public void putEdge(@NonNull BranchingStmt stmtA, int succesorIdx, @NonNull Stmt stmtB) {
    if (0 > succesorIdx || succesorIdx >= stmtA.getExpectedSuccessorCount()) {
      throw new IllegalArgumentException(
          "SuccessorIdx '"
              + succesorIdx
              + "' is out of bounds - needs to be [0, "
              + (stmtA.getExpectedSuccessorCount() - 1)
              + "]");
    }
    putEdgeInternal(stmtA, succesorIdx, stmtB);
  }

  /**
   * sets the successor idx of stmtA to stmtB. A falling through, non-branching stmtA is followed by
   * a new stmtB in its block; an existing stmtB must then be the head of a block.
   */
  private void putEdgeInternal(@NonNull Stmt stmtA, int succesorIdx, @NonNull Stmt stmtB) {
    MutableBasicBlockImpl blockA = stmtToBlock.get(stmtA);
    if (blockA != null && blockA.getTail() != stmtA) {
      // if StmtA is not at the end of the block -> it needs to branch to reach StmtB or is
      // falling through to another Block
      throw new IllegalArgumentException(
          "StmtA '"
              + stmtA
              + "' is not at the end of a block but it must be to reach StmtB '"
              + stmtB
              + "'.");
    }
    final MutableBasicBlockImpl existingB = stmtToBlock.get(stmtB);
    final boolean fallsThrough = !stmtA.branches();
    // a JIfStmt may use its fall-through slot as back edge (analysis graphs do); a plain
    // fall-through cycle can never be linearized
    if (!stmtA.branches()
        && blockA != null
        && existingB != null
        && fallsThroughTo(existingB, blockA)) {
      throw fallThroughCycle(stmtA, stmtB);
    }
    if (fallsThrough && existingB != null) {
      if (existingB.getHead() != stmtB) {
        throw new IllegalArgumentException(
            "StmtB '"
                + stmtB
                + "' is already in the Graph and has already a non-branching predecessor!");
      }
    }

    modified();
    if (blockA == null) {
      blockA = newBlock(stmtA);
    }
    final MutableBasicBlockImpl old = blockA.successorAt(succesorIdx);

    if (!fallsThrough) {
      MutableBasicBlockImpl target = blockStartingAt(stmtB);
      blockOf(stmtA).link(succesorIdx, target);
    } else if (existingB == null) {
      // a new stmtB just follows stmtA in its block (and gets its traps)
      blockA.unlink(0);
      blockA.addStmt(stmtB);
      stmtToBlock.put(stmtB, blockA);
    } else {
      blockA.link(0, existingB);
      normalize(existingB);
    }
    if (old != null && blocks.contains(old) && old.getHead() != stmtB) {
      normalize(old);
    }
  }

  @Override
  public List<Integer> removeEdge(@NonNull Stmt from, @NonNull Stmt to) {
    final MutableBasicBlockImpl blockOfFrom = stmtToBlock.get(from);
    final MutableBasicBlockImpl blockOfTo = stmtToBlock.get(to);
    if (blockOfFrom == null || blockOfTo == null) {
      // Stmt is not existing anymore in this graph - so neither a connection.
      return Collections.emptyList();
    }

    if (blockOfFrom.getTail() == from && blockOfTo.getHead() == to) {
      // tail and head of their blocks: they connect different blocks or a loop of the same block
      List<Integer> removed = blockOfFrom.unlinkAllTo(blockOfTo);
      if (removed.isEmpty()) {
        return Collections.emptyList();
      }
      modified();
      // the removal might have created blocks that can be merged
      normalize(blockOfTo);
      normalize(blockOfFrom);
      return removed;
    }
    if (blockOfFrom == blockOfTo) {
      int fromIdx = blockOfFrom.indexOf(from);
      if (blockOfFrom.indexOf(to) == fromIdx + 1) {
        // inner stmts: split the block and don't link the parts
        modified();
        split(blockOfFrom, fromIdx + 1);
        blockOfFrom.unlink(0);
        return Collections.singletonList(0);
      }
    }
    return Collections.emptyList();
  }

  @Override
  public void setEdges(@NonNull BranchingStmt fromStmt, @NonNull List<Stmt> targets) {
    if (fromStmt.getExpectedSuccessorCount() != targets.size()) {
      throw new IllegalArgumentException(
          "Size of Targets is not the amount of from's expected successors.");
    }
    MutableBasicBlockImpl fromBlock = stmtToBlock.get(fromStmt);
    if (fromBlock != null && fromBlock.getTail() != fromStmt) {
      throw new IllegalArgumentException("fromStmt '" + fromStmt + "' must be the tail of a block");
    }
    modified();
    if (fromBlock == null) {
      fromBlock = newBlock(fromStmt);
    }
    List<MutableBasicBlockImpl> oldTargets = Arrays.asList(successorsOf(fromBlock));
    fromBlock.unlinkAllSuccessors();
    for (int i = 0; i < targets.size(); i++) {
      putEdgeInternal(fromStmt, i, targets.get(i));
    }
    for (MutableBasicBlockImpl old : oldTargets) {
      normalize(old);
    }
  }

  // ===========================================================================================
  // queries

  @Nullable
  @Override
  public Stmt getStartingStmt() {
    // is the stmt currently in a block associated with the graph?
    if (startingStmt == null || !stmtToBlock.containsKey(startingStmt)) {
      return null;
    }
    return startingStmt;
  }

  @Override
  @Nullable
  public BasicBlock<?> getStartingStmtBlock() {
    Stmt start = getStartingStmt();
    return start == null ? null : stmtToBlock.get(start);
  }

  @Override
  @NonNull
  public List<BasicBlock<?>> getTailStmtBlocks() {
    return getTails().stream().map(this::getBlockOf).collect(Collectors.toList());
  }

  @Override
  @NonNull
  public BasicBlock<?> getBlockOf(@NonNull Stmt stmt) {
    final MutableBasicBlockImpl block = stmtToBlock.get(stmt);
    if (block == null) {
      throw new IllegalArgumentException(
          "stmt '" + stmt + "' does not exist in this ControlFlowGraph!");
    }
    return block;
  }

  @NonNull
  @Override
  public ControlFlowGraph<?> unmodifiableControlFlowGraph() {
    return new ForwardingControlFlowGraph<>(this);
  }

  public void setStartingStmt(@NonNull Stmt startingStmt) {
    modified();
    // the start must be a block head, otherwise iteration would skip the stmts before it
    blockStartingAt(startingStmt);
    this.startingStmt = startingStmt;
  }

  /**
   * The statements in {@link #iterator()} order. The order is computed once until the graph
   * changes; each call returns a new list, as before.
   */
  @NonNull
  @Override
  public List<Stmt> getStmts() {
    if (stmtsInOrder == null) {
      stmtsInOrder = super.getStmts().toArray(new Stmt[0]);
    }
    return new ArrayList<>(Arrays.asList(stmtsInOrder));
  }

  @NonNull
  @Override
  public Set<Stmt> getNodes() {
    return stmtToBlock.keySet();
  }

  @Override
  public boolean containsNode(@NonNull Stmt node) {
    return stmtToBlock.containsKey(node);
  }

  @NonNull
  @Override
  public List<Stmt> predecessors(@NonNull Stmt node) {
    MutableBasicBlockImpl block = blockOf(node);
    if (node == block.getHead()) {
      List<MutableBasicBlock> predecessorBlocks = block.getPredecessors();
      List<Stmt> preds = new ArrayList<>(predecessorBlocks.size());
      predecessorBlocks.forEach(p -> preds.add(p.getTail()));
      return preds;
    }
    return Collections.singletonList(block.get(block.indexOf(node) - 1));
  }

  @NonNull
  @Override
  public List<Stmt> exceptionalPredecessors(@NonNull Stmt node) {
    MutableBasicBlockImpl block = blockOf(node);
    if (block.getHead() != node) {
      // a traphandler is a blocks head and only an exception handler stmt can have exceptional
      // predecessors
      return Collections.emptyList();
    }
    return exceptionalPredecessors(block);
  }

  public List<Stmt> exceptionalPredecessors(@NonNull MutableBasicBlock block) {
    List<Stmt> exceptionalPred = new ArrayList<>();
    for (BasicBlock<?> pBlock : exceptionalPredecessorBlocks(block)) {
      exceptionalPred.addAll(pBlock.getStmts());
    }
    return exceptionalPred;
  }

  public List<? extends BasicBlock<?>> exceptionalPredecessorBlocks(@NonNull BasicBlock<?> block) {
    Stmt head = block.getHead();
    if (!(head instanceof JIdentityStmt
        && ((JIdentityStmt) head).getRightOp() instanceof JCaughtExceptionRef)) {
      // only an exception handler stmt can have exceptional predecessors
      return Collections.emptyList();
    }
    if (block instanceof MutableBasicBlockImpl) {
      return MutableBasicBlockImpl.distinct(
          ((MutableBasicBlockImpl) block).exceptionalPredecessors());
    }
    List<BasicBlock<?>> exceptionalPred = new ArrayList<>();
    for (BasicBlock<?> pBlock : block.getPredecessors()) {
      if (pBlock.getExceptionalSuccessors().containsValue(block)
          && !exceptionalPred.contains(pBlock)) {
        exceptionalPred.add(pBlock);
      }
    }
    return exceptionalPred;
  }

  @NonNull
  @Override
  public List<Stmt> successors(@NonNull Stmt node) {
    MutableBasicBlockImpl block = blockOf(node);
    if (node == block.getTail()) {
      List<MutableBasicBlock> successorBlocks = block.getSuccessors();
      List<Stmt> succs = new ArrayList<>(successorBlocks.size());
      successorBlocks.forEach(p -> succs.add(p.getHead()));
      return succs;
    }
    return Collections.singletonList(block.get(block.indexOf(node) + 1));
  }

  @NonNull
  @Override
  public Map<ClassType, Stmt> exceptionalSuccessors(@NonNull Stmt node) {
    MutableBasicBlockImpl block = blockOf(node);
    Map<ClassType, MutableBasicBlockImpl> exc = block.exceptionalSuccessorMap();
    if (exc.isEmpty()) {
      return new HashMap<>(0);
    }
    Map<ClassType, Stmt> map = new HashMap<>();
    exc.forEach((type, handler) -> map.put(type, handler.getHead()));
    return map;
  }

  @Override
  public int inDegree(@NonNull Stmt node) {
    MutableBasicBlockImpl block = blockOf(node);
    return node == block.getHead() ? block.getPredecessors().size() : 1;
  }

  @Override
  public int outDegree(@NonNull Stmt node) {
    MutableBasicBlockImpl block = blockOf(node);
    return node == block.getTail() ? block.getSuccessors().size() : 1;
  }

  @Override
  public boolean hasEdgeConnecting(@NonNull Stmt source, @NonNull Stmt target) {
    MutableBasicBlockImpl blockA = blockOf(source);
    if (source == blockA.getTail()) {
      blockOf(target);
      for (MutableBasicBlock successorBlock : blockA.getSuccessors()) {
        if (successorBlock.getHead() == target) {
          return true;
        }
      }
      return false;
    }
    return blockA.get(blockA.indexOf(source) + 1) == target;
  }
}
