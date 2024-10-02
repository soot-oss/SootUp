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

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.LocalGenerator;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Trap;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.stmt.*;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;

/*
 * Implementation of a Control Flow Graph which stores Stmts, each Trap- and Branching Information directly in its Blocks.
 *
 * This implementation builds the blocks directly after a manipulation operation is assigned - which may be not always necessary and could be delayed when needed e.g. in cases of multiple changes this could create more overhead than necessary.
 *
 * @author Markus Schmidt
 * */
public class MutableBlockStmtGraph extends MutableStmtGraph {
  @Nullable private Stmt startingStmt = null;

  @Nonnull
  private final Map<Stmt, Pair<Integer, MutableBasicBlock>> stmtToBlock = new IdentityHashMap<>();

  @Nonnull private final Set<MutableBasicBlock> blocks = new HashSet<>();

  public MutableBlockStmtGraph() {}

  public MutableBlockStmtGraph(boolean isStatic, MethodSignature sig, LocalGenerator localgen) {
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

  /** copies a StmtGraph into this Mutable instance */
  public MutableBlockStmtGraph(@Nonnull StmtGraph<? extends BasicBlock<?>> graph) {
    final Stmt startStmt = graph.getStartingStmt();
    if (startStmt != null) {
      setStartingStmt(startStmt);
    }
    // copy blocks into this graph
    graph
        .getBlocks()
        .forEach(
            b -> {
              final Map<? extends ClassType, ? extends BasicBlock<?>> exceptionalSuccessors =
                  b.getExceptionalSuccessors();
              final Map<ClassType, Stmt> exSuccs = new HashMap<>();
              exceptionalSuccessors.forEach((k, v) -> exSuccs.put(k, v.getHead()));
              addBlock(b.getStmts(), exSuccs);
            });

    // copy links between the blocks
    graph
        .getBlocks()
        .forEach(
            b -> {
              // getBlockOf is necessary to find the new existing/copied block which are refering to
              // the same a immutable Stmt
              final MutableBasicBlock blockOf = stmtToBlock.get(b.getTail()).getRight();
              List<? extends BasicBlock<?>> successors = b.getSuccessors();
              for (int i = 0; i < successors.size(); i++) {
                BasicBlock<?> succ = successors.get(i);
                blockOf.linkSuccessor(i, stmtToBlock.get(succ.getHead()).getRight());
              }
            });
  }

  public static StmtGraph<?> createUnmodifiableStmtGraph(StmtGraph<?> stmtGraph) {
    if (stmtGraph instanceof MutableStmtGraph) {
      return ((MutableStmtGraph) stmtGraph).unmodifiableStmtGraph();
    }
    return stmtGraph;
  }

  /**
   * Creates a Graph representation from the 'legacy' representation i.e. a List of Stmts and Traps.
   */
  public void initializeWith(
      @Nonnull List<List<Stmt>> blocks,
      @Nonnull Map<BranchingStmt, List<Stmt>> successorMap,
      @Nonnull List<Trap> traps) {

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
      int i = 0;
      for (List<Stmt> block : blocks) {
        blockIdxMap.put(block.get(0), i++);
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
            if (endIdx == null) {
              endIdx = blockIdxMap.size();
              // throw new AssertionError();
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

    setStartingStmt(blocks.get(0).get(0));
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

      addBlock(block, exceptionToHandlerMap);
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
        putEdge((FallsThroughStmt) tailStmt, followingBlocksHead);
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
          putEdge(bStmt, k + succIdxOffset, target);
        }
      }
    }
  }

  private static int getTrapApplicationComparator(
      HashMap<Stmt, Integer> trapstmtToIdx, Trap trapA, Trap trapB) {
    if (trapA.getEndStmt() == trapB.getEndStmt()) {
      final Integer startIdxA = trapstmtToIdx.get(trapA.getBeginStmt());
      final Integer startIdxB = trapstmtToIdx.get(trapB.getBeginStmt());
      return startIdxB - startIdxA;
    } else {
      final Integer idxA = trapstmtToIdx.get(trapA.getEndStmt());
      final Integer idxB = trapstmtToIdx.get(trapB.getEndStmt());
      return idxA - idxB;
    }
  }

  private static void duplicateCatchAllTrapRemover(
      @Nonnull List<Trap> traps, Map<Stmt, Integer> trapstmtToIdx) {
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

  @Override
  public void addExceptionalEdge(
      @Nonnull Stmt stmt, @Nonnull ClassType exceptionType, @Nonnull Stmt traphandlerStmt) {

    Pair<Integer, MutableBasicBlock> blockPair = stmtToBlock.get(stmt);
    if (blockPair == null) {
      throw new IllegalArgumentException("Stmt is not in the StmtGraph!");
    }
    MutableBasicBlock block = blockPair.getRight();

    final Map<ClassType, MutableBasicBlock> exceptionalSuccessors =
        block.getExceptionalSuccessors();
    final MutableBasicBlock trapBlock = exceptionalSuccessors.get(exceptionType);
    if (trapBlock != null && trapBlock.getHead() == traphandlerStmt) {
      // edge already exists
      return;
    }

    MutableBasicBlock seperatedBlock = splitAndExcludeStmtFromBlock(stmt, block);
    seperatedBlock.linkExceptionalSuccessorBlock(exceptionType, getOrCreateBlock(traphandlerStmt));
    tryMergeIntoSurroundingBlocks(seperatedBlock);
  }

  @Override
  public void removeExceptionalEdge(@Nonnull Stmt node, @Nonnull ClassType exceptionType) {
    Pair<Integer, MutableBasicBlock> blockPair = stmtToBlock.get(node);
    if (blockPair == null) {
      throw new IllegalArgumentException(
          "Stmt '" + node + "' is not contained in the BlockStmtGraph");
    }
    MutableBasicBlock block = blockPair.getRight();
    block.removeExceptionalSuccessorBlock(exceptionType);
    tryMergeIntoSurroundingBlocks(block);
  }

  @Override
  public void clearExceptionalEdges(@Nonnull Stmt node) {
    Pair<Integer, MutableBasicBlock> blockPair = stmtToBlock.get(node);
    if (blockPair == null) {
      throw new IllegalArgumentException(
          "Stmt '" + node + "' is not contained in the BlockStmtGraph");
    }
    MutableBasicBlock block = blockPair.getRight();
    block.clearExceptionalSuccessorBlocks();
    tryMergeIntoSurroundingBlocks(block);
  }

  @Override
  @Nonnull
  public Set<? extends BasicBlock<?>> getBlocks() {
    return blocks;
  }

  @Nonnull
  public List<? extends BasicBlock<?>> getBlocksSorted() {
    return ReversePostOrderBlockTraversal.getBlocksSorted(this);
  }

  /**
   * The list of Stmts must contain only fallsthrough Stmts; A flow manipulating Stmt
   * (BranchingStmt, return, throw) is only allowed at the Tail. (like the conditions of a Block)
   */
  @Override
  public void addBlock(@Nonnull List<Stmt> stmts, @Nonnull Map<ClassType, Stmt> trapMap) {
    if (stmts.isEmpty()) {
      return;
    }
    addBlockInternal(stmts, trapMap);
  }

  /**
   * @param stmts List has to be non-empty!
   * @param trapMap
   */
  private MutableBasicBlock addBlockInternal(
      @Nonnull List<? extends Stmt> stmts, Map<ClassType, Stmt> trapMap) {
    final Iterator<? extends Stmt> iterator = stmts.iterator();
    final Stmt node = iterator.next();
    MutableBasicBlock block = getOrCreateBlock(node);
    if (block.getHead() != node || block.getSuccessors().stream().anyMatch(Objects::nonNull)) {
      throw new IllegalArgumentException(
          "The first Stmt in the List is already in the StmtGraph and and is not the head of a Block where currently no successor are set, yet.");
    } else if (block.getStmtCount() > 1) {
      throw new IllegalArgumentException(
          "The first Stmt in the List is already in the StmtGraph and has at least one (fallsthrough) successor in its Block.");
    }

    while (iterator.hasNext()) {
      Stmt stmt = iterator.next();
      final Pair<Integer, MutableBasicBlock> overwrittenBlockPair = addNodeToBlock(block, stmt);
      if (overwrittenBlockPair != null) {
        if (iterator.hasNext()) {
          throw new IllegalArgumentException(
              "the Stmt '"
                  + stmt
                  + "' you want to add as a Stmt of a whole Block is already in this StmtGraph.");
        } else {
          // existing is last element of stmtlist
          // TODO: hint: we can allow other n-th elements as well e.g. if a sequence of stmts exists
          // already and can/should be inside that added block as well.

          if (overwrittenBlockPair.getRight().getHead() == stmt) {
            // last stmt is head of another block

            // cleanup started add action
            stmtToBlock.put(stmt, overwrittenBlockPair);
            block.removeStmt(overwrittenBlockPair.getLeft());

            // try to merge
            if (!tryMergeBlocks(block, overwrittenBlockPair.getRight())) {
              // otherwise link them
              block.linkSuccessor(0, overwrittenBlockPair.getRight());
            }
          } else {
            throw new IllegalArgumentException(
                "the Stmt '"
                    + stmt
                    + "' you want to add as a Stmt of a whole Block is already in this StmtGraph.");
          }
        }
      }
    }

    trapMap.forEach(
        (type, handlerStmt) ->
            block.linkExceptionalSuccessorBlock(type, getOrCreateBlock(handlerStmt)));
    return block;
  }

  @Override
  public void removeBlock(BasicBlock<?> block) {
    Pair<Integer, MutableBasicBlock> blockOfPair = stmtToBlock.get(block.getHead());
    if (blockOfPair.getRight() != block) {
      throw new IllegalArgumentException(
          "The given block is not contained in this MutableBlockStmtGraph.");
    }
    MutableBasicBlock blockOf = blockOfPair.getRight();

    List<Stmt> stmts = block.getStmts();
    stmts.forEach(stmtToBlock::remove);

    // unlink block from graph
    blockOf.clearPredecessorBlocks();
    blockOf.clearSuccessorBlocks();
    blockOf.clearExceptionalSuccessorBlocks();

    blocks.remove(blockOf);
  }

  @Override
  public void addNode(@Nonnull Stmt stmt, @Nonnull Map<ClassType, Stmt> exceptions) {
    Pair<Integer, MutableBasicBlock> blockPair = stmtToBlock.get(stmt);
    if (blockPair == null) {
      // Stmt does not exist in the graph -> create
      blockPair = createStmtsBlock(stmt);
    }
    MutableBasicBlock block = blockPair.getRight();
    boolean isExceptionalFlowDifferent =
        isExceptionalFlowDifferent(exceptions, block.getExceptionalSuccessors());
    final MutableBasicBlock separatedBlock;
    if (isExceptionalFlowDifferent) {
      separatedBlock = splitAndExcludeStmtFromBlock(stmt, block);
      separatedBlock.clearExceptionalSuccessorBlocks();

      // apply exceptional flow info to seperated block
      exceptions.forEach(
          (type, trapHandler) -> {
            MutableBasicBlock trapHandlerBlock = getOrCreateBlock(trapHandler);
            separatedBlock.linkExceptionalSuccessorBlock(type, trapHandlerBlock);
          });
      tryMergeIntoSurroundingBlocks(separatedBlock);
    }
  }

  private static boolean isExceptionalFlowDifferent(
      Map<ClassType, Stmt> exceptionsA, Map<ClassType, MutableBasicBlock> exceptionsB) {
    if (exceptionsA.size() != exceptionsB.size()) {
      return true;
    }
    for (Map.Entry<ClassType, MutableBasicBlock> entry : exceptionsB.entrySet()) {
      final Stmt targetStmt = exceptionsA.get(entry.getKey());
      if (targetStmt == null) {
        return true;
      }
      if (targetStmt != entry.getValue().getHead()) {
        return true;
      }
    }
    return false;
  }

  /**
   * splits a block depending on the situation in multiple (0-3) Blocks so that at the end splitStmt
   * is the only Stmt in its BasicBlock. The flow between the splitted BasicBlock(s) is kept!
   *
   * @return the splitted block with the splitStmt as head
   */
  @Nonnull
  private MutableBasicBlock splitAndExcludeStmtFromBlock(
      @Nonnull Stmt splitStmt, MutableBasicBlock block) {
    if (block.getStmtCount() <= 1) {
      // just a single stmt in the block -> e.g. it is already the block we want
      return block;
    }

    final MutableBasicBlock excludedFromOrigBlock;
    final List<Stmt> blockStmts = block.getStmts();
    Pair<Integer, MutableBasicBlock> splitStmtBlockPair = stmtToBlock.get(splitStmt);

    if (splitStmtBlockPair == null) {
      throw new IllegalArgumentException("splitStmt does not exist in this block!");
    }
    int stmtIdx = splitStmtBlockPair.getLeft();

    if (stmtIdx == 0) {
      // stmt is the head -> just a single split is necessary
      excludedFromOrigBlock = block;
    } else {
      // i.e. stmt != block.getHead() -> there is a "middle" or/and an end Block containing the
      // splitStmt
      // which needs to be seperated
      excludedFromOrigBlock = new MutableBasicBlockImpl();
      addNodeToBlock(excludedFromOrigBlock, splitStmt);
      // add blocks exceptional flows
      block
          .getExceptionalSuccessors()
          .forEach(excludedFromOrigBlock::linkExceptionalSuccessorBlock);
      blocks.add(excludedFromOrigBlock);
    }

    if (block.getTail() != splitStmt) {
      // "third"/after/leftover block is necessary as there are stmts after the splitElement
      final MutableBasicBlockImpl restOfOrigBlock = new MutableBasicBlockImpl();
      for (int i = stmtIdx + 1; i < blockStmts.size(); i++) {
        // stmtToBlock is already updated while inserting each Stmt into another Block
        addNodeToBlock(restOfOrigBlock, blockStmts.get(i));
      }

      // copy successors of block which are now the successors of the "third"/leftover block
      List<MutableBasicBlock> successors = block.getSuccessors();
      for (int i = 0; i < successors.size(); i++) {
        MutableBasicBlock successor = successors.get(i);
        restOfOrigBlock.linkSuccessor(i, successor);
      }
      block.clearSuccessorBlocks();

      // link third/leftover block with previous stmts from the separated block
      excludedFromOrigBlock.linkSuccessor(0, restOfOrigBlock);
      block.clearSuccessorBlocks();

      // add blocks exceptional flows
      block
          .getExceptionalSuccessors()
          .forEach(
              (type, trapHandlerBlock) -> {
                restOfOrigBlock.linkExceptionalSuccessorBlock(type, trapHandlerBlock);
                trapHandlerBlock.addPredecessorBlock(restOfOrigBlock);
              });

      blocks.add(restOfOrigBlock);

      // cleanup original block -> "beforeBlock" -> remove now copied Stmts
      for (int i = blockStmts.size() - 1; i >= stmtIdx; i--) {
        block.removeStmt(i);
      }

    } else {
      // there are no more stmts after stmtIdx -> less than 3 blocks are necessary
      // copy origin successors to second block as it is now the last part of the origin block
      List<MutableBasicBlock> successors = block.getSuccessors();
      for (int i = 0; i < successors.size(); i++) {
        MutableBasicBlock successorBlock = successors.get(i);
        excludedFromOrigBlock.linkSuccessor(i, successorBlock);
      }
      block.clearSuccessorBlocks();
      // cleanup original block -> "beforeBlock" -> remove now copied Stmts
      for (int i = blockStmts.size() - 1; i >= stmtIdx; i--) {
        block.removeStmt(i);
      }
      block.linkSuccessor(0, excludedFromOrigBlock);
    }

    return excludedFromOrigBlock;
  }

  /** Merges block into Predecessor/Successor if possible. */
  private void tryMergeIntoSurroundingBlocks(@Nonnull MutableBasicBlock block) {
    // merge with predecessor if possible
    block = tryMergeWithPredecessorBlock(block);
    // and/or merge with successorBlock
    tryMergeWithSuccessorBlock(block);
  }

  /** @return the successor block of block if the merge happended, if not merged: block */
  @Nonnull
  private MutableBasicBlock tryMergeWithSuccessorBlock(@Nonnull MutableBasicBlock block) {
    final List<MutableBasicBlock> successors = block.getSuccessors();
    if (successors.size() == 1) {
      final MutableBasicBlock singleSuccessor = successors.get(0);
      if (tryMergeBlocks(block, singleSuccessor)) {
        updateIndexRangeAfterMerge(block, singleSuccessor);
        return singleSuccessor;
      }
    }
    return block;
  }

  protected void updateIndexRangeAfterMerge(
      @Nonnull MutableBasicBlock firstBlock, @Nonnull MutableBasicBlock secondBlock) {
    int startIdx = firstBlock.getStmtCount() - secondBlock.getStmtCount();
    List<Stmt> stmts = firstBlock.getStmts();
    for (int i = startIdx, stmtsSize = stmts.size(); i < stmtsSize; i++) {
      Stmt stmt = stmts.get(i);
      stmtToBlock.put(stmt, new MutablePair<>(i, firstBlock));
      // TODO: reuse previous assigned Pairs/ shift offset
    }
  }

  /** @return the predecessor block of block if the merge happended, if not merged: block */
  @Nonnull
  private MutableBasicBlock tryMergeWithPredecessorBlock(@Nonnull MutableBasicBlock block) {
    final List<MutableBasicBlock> predecessors = block.getPredecessors();
    if (predecessors.size() == 1) {
      final MutableBasicBlock singlePredecessor = predecessors.get(0);
      if (tryMergeBlocks(singlePredecessor, block)) {
        updateIndexRangeAfterMerge(singlePredecessor, block);
        return singlePredecessor;
      }
    }
    return block;
  }

  @Nonnull
  private MutableBasicBlock getOrCreateBlock(@Nonnull Stmt stmt) {
    Pair<Integer, MutableBasicBlock> trapHandlerBlock = stmtToBlock.get(stmt);
    if (trapHandlerBlock == null) {
      // traphandlerStmt does not exist in the graph -> create
      trapHandlerBlock = createStmtsBlock(stmt);
    }
    return trapHandlerBlock.getRight();
  }

  protected boolean isMergeable(
      @Nonnull MutableBasicBlock firstBlock, @Nonnull MutableBasicBlock followingBlock) {
    if (firstBlock.getTail().branches()) {
      return false;
    }
    final List<MutableBasicBlock> fBlocksuccessors = firstBlock.getSuccessors();
    if (fBlocksuccessors.size() > 1
        || (fBlocksuccessors.size() == 1 && fBlocksuccessors.get(0) != followingBlock)) {
      return false;
    }
    // if we are here the datastructure should have managed that the next if is true..
    final List<MutableBasicBlock> sBlockPredecessors = followingBlock.getPredecessors();
    if (sBlockPredecessors.size() != 1 || sBlockPredecessors.get(0) != firstBlock) {
      return false;
    }
    // check if the same traps are applied to both blocks
    return firstBlock.getExceptionalSuccessors().equals(followingBlock.getExceptionalSuccessors());
  }

  /** trys to merge the second block into the first one if possible */
  protected boolean tryMergeBlocks(
      @Nonnull MutableBasicBlock firstBlock, @Nonnull MutableBasicBlock followingBlock) {
    final boolean mergeable = isMergeable(firstBlock, followingBlock);
    if (mergeable) {
      for (Stmt stmt : followingBlock.getStmts()) {
        addNodeToBlock(firstBlock, stmt);
      }

      // i.e. can just be the single followingblock which we merge now
      firstBlock.clearSuccessorBlocks();

      // update linking info into firstBlock
      // done in clearPredecessorBlock      firstBlock.removeSuccessorBlock(followingBlock);
      List<MutableBasicBlock> successors = followingBlock.getSuccessors();
      for (int i = 0; i < successors.size(); i++) {
        MutableBasicBlock succ = successors.get(i);
        firstBlock.linkSuccessor(i, succ);
      }
      followingBlock.clearSuccessorBlocks();

      blocks.remove(followingBlock);

      // cleanup old block..
      followingBlock.clearPredecessorBlocks();
    }
    return mergeable;
  }

  /**
   * creates a Block and inserts the given Stmt.
   *
   * @return -1 if Stmt is already in the graph!
   */
  @Nonnull
  protected Pair<Integer, MutableBasicBlock> createStmtsBlock(@Nonnull Stmt stmt) {
    // add Block to graph, add+register Stmt to Block
    MutableBasicBlock block = new MutableBasicBlockImpl();
    if (addNodeToBlock(block, stmt) != null) {
      throw new IllegalArgumentException("Stmt is already in the graph!");
    }
    blocks.add(block);
    return new MutablePair<>(0, block);
  }

  /** Adds a Stmt to the end of a block i.e. stmt will become the new tail. */
  protected Pair<Integer, MutableBasicBlock> addNodeToBlock(
      @Nonnull MutableBasicBlock block, @Nonnull Stmt stmt) {
    int stmtIdx = block.getStmtCount();
    block.addStmt(stmt);
    return stmtToBlock.put(stmt, new MutablePair<>(stmtIdx, block));
  }

  public void removeNode(@Nonnull Stmt stmt) {
    // TODO: [ms] whats intuitive? removing the flows to the block too? or is deleting a stmt
    // keeping the flows to it
    // is the answer different if its the tail? consistency vs intuitivity..
    removeNode(stmt, true);
  }

  /**
   * Removes a Stmt from the StmtGraph.
   *
   * <p>It can optionally keep the flow (edges) of the statement by connecting the predecessors of
   * the statement with successors of the statement. Keeping the flow does not work when the
   * statement has multiple successors.
   *
   * @param stmt the Stmt to be removed
   * @param keepFlow flag indicating whether to keep the flow or not
   * @throws IllegalArgumentException if keepFlow is true but the stmt has multiple successors
   */
  public void removeNode(@Nonnull Stmt stmt, boolean keepFlow) {
    Pair<Integer, MutableBasicBlock> blockOfRemovedStmtPair = stmtToBlock.get(stmt);
    if (blockOfRemovedStmtPair == null) {
      throw new IllegalArgumentException("stmt '" + stmt + "' is not contained in this StmtGraph!");
    }
    MutableBasicBlock blockOfRemovedStmt = blockOfRemovedStmtPair.getRight();

    List<MutableBasicBlock> successors = blockOfRemovedStmt.getSuccessors();

    if (blockOfRemovedStmt.getStmtCount() <= 1) {
      // remove the complete block as it has only one Stmt that is now removed
      if (keepFlow) {
        if (stmt instanceof BranchingStmt) {
          // check for successorCount == 1 is not enough as it could be that we want to replace a
          // Branching Stmt via a FallsThroughStmt and the linearized StmtGraph would have no
          // necessary goto anymore.
          throw new IllegalArgumentException("Cannot keep the flow if we remove a BranchingStmt!");
        }
        if (successors.size() == 1) {
          MutableBasicBlock successorBlock = successors.get(0);
          for (MutableBasicBlock predecessor : blockOfRemovedStmt.getPredecessors()) {
            predecessor.replaceSuccessorBlock(blockOfRemovedStmt, successorBlock);
            if (!successorBlock.replacePredecessorBlock(blockOfRemovedStmt, predecessor)) {
              // happens when blockOfRemovedStmt.predecessors().size() > 1
              successorBlock.addPredecessorBlock(predecessor);
            }
          }

          if (stmt == startingStmt) {
            startingStmt = successorBlock.getHead();
          }
        }
      } else {
        if (stmt == startingStmt) {
          startingStmt = null;
        }
      }

      blockOfRemovedStmt.clearPredecessorBlocks();
      blockOfRemovedStmt.clearSuccessorBlocks();
      blockOfRemovedStmt.clearExceptionalSuccessorBlocks();
      blockOfRemovedStmt.removeStmt(blockOfRemovedStmtPair.getLeft());
      blocks.remove(blockOfRemovedStmt);

    } else if (blockOfRemovedStmt.getHead() == stmt) {
      // stmt2bRemoved is at the beginning of a Block
      blockOfRemovedStmt.removeStmt(blockOfRemovedStmtPair.getLeft());
      if (!keepFlow) {
        blockOfRemovedStmt.clearPredecessorBlocks();
        if (stmt == startingStmt) {
          startingStmt = null;
        }
      } else {
        // update starting stmt if necessary
        if (stmt == startingStmt) {
          startingStmt = blockOfRemovedStmt.getHead();
        }
      }

      // update indices
      List<Stmt> stmts = blockOfRemovedStmt.getStmts();
      for (int i = blockOfRemovedStmtPair.getLeft(), stmtsSize = stmts.size(); i < stmtsSize; i++) {
        Stmt s = stmts.get(i);
        stmtToBlock.put(s, new MutablePair<>(i, blockOfRemovedStmt));
      }

    } else {
      if (blockOfRemovedStmt.getTail() == stmt) {
        // stmt2bRemoved is at the end of a Block
        if (keepFlow) {
          if (stmt.branches()) {
            if (stmt.getExpectedSuccessorCount() > 1) {
              throw new IllegalArgumentException(
                  "Cannot keep the flows of a removed BranchingStmt if there is more than one successor.");
            }
            tryMergeWithSuccessorBlock(blockOfRemovedStmt);
          }

        } else {
          blockOfRemovedStmt.clearSuccessorBlocks();
        }

        blockOfRemovedStmt.removeStmt(blockOfRemovedStmtPair.getLeft());
      } else {
        // stmt2bRemoved is in the middle of a Block
        if (keepFlow) {
          int startIdx = blockOfRemovedStmtPair.getLeft();
          blockOfRemovedStmt.removeStmt(startIdx);
          List<Stmt> stmts = blockOfRemovedStmt.getStmts();
          for (int i = startIdx, stmtsSize = stmts.size(); i < stmtsSize; i++) {
            Stmt s = stmts.get(i);
            stmtToBlock.put(s, new MutablePair<>(i, blockOfRemovedStmt));
          }
        } else {
          int splitIdx = blockOfRemovedStmtPair.getLeft();
          MutableBasicBlock secondBlock = blockOfRemovedStmt.splitBlockUnlinked(splitIdx + 1);
          blockOfRemovedStmt.removeStmt(
              splitIdx); // remove after splitting the blocks to save stmt copying (its the last
          // element now)
          blocks.add(secondBlock);
          int idx = 0;
          for (Stmt s : secondBlock.getStmts()) {
            stmtToBlock.put(s, new MutablePair<>(idx++, secondBlock));
          }
        }
      }
    }
    stmtToBlock.remove(stmt);
  }

  @Override
  public void replaceNode(@Nonnull Stmt oldStmt, @Nonnull Stmt newStmt) {
    if (oldStmt == newStmt) {
      return;
    }

    final Pair<Integer, MutableBasicBlock> blockOfOldStmtPair = stmtToBlock.get(oldStmt);
    if (blockOfOldStmtPair == null) {
      throw new IllegalArgumentException("oldStmt does not exist in the StmtGraph!");
    }
    final MutableBasicBlock blockOfOldStmt = blockOfOldStmtPair.getRight();

    // is oldStmt the startingStmt? replace startingStmt with newStmt
    if (oldStmt == startingStmt) {
      startingStmt = newStmt;
    }

    if (!oldStmt.branches() && !newStmt.branches()) {
      // nothing branches -> just replace actual Stmt inside oldStmts block
      blockOfOldStmt.replaceStmt(blockOfOldStmtPair.getLeft(), newStmt);
      stmtToBlock.put(newStmt, blockOfOldStmtPair);

    } else if (!oldStmt.branches() && newStmt.branches()) {
      // split block
      MutableBasicBlock newBlock = splitAndExcludeStmtFromBlock(oldStmt, blockOfOldStmt);
      blockOfOldStmt.replaceStmt(oldStmt, newStmt);
      // update index
      stmtToBlock.put(newStmt, blockOfOldStmtPair);
      int idx = 0;
      for (Stmt stmt : newBlock.getStmts()) {
        stmtToBlock.put(stmt, new MutablePair<>(idx++, newBlock));
      }

    } else if (oldStmt.branches() && !newStmt.branches()) {
      blockOfOldStmt.replaceStmt(oldStmt, newStmt);
      blockOfOldStmtPair.setValue(blockOfOldStmt);
      stmtToBlock.put(newStmt, blockOfOldStmtPair);
      if (oldStmt.getExpectedSuccessorCount() > newStmt.getExpectedSuccessorCount()) {
        // throw new IllegalArgumentException("We can't keep the flows if we replace a Stmt ("+
        // oldStmt.getExpectedSuccessorCount() +") by another Stmt which expects a different amount
        // ("+ newStmt.getExpectedSuccessorCount() +") of successors.");

        // prune additional flows - keep successorIdx:0
        MutableBasicBlock successor = blockOfOldStmt.getSuccessors().get(0);
        blockOfOldStmt.clearSuccessorBlocks();
        if (newStmt.getExpectedSuccessorCount() > 0) {
          blockOfOldStmt.setSuccessorBlock(0, successor);
        }
      }

      tryMergeWithSuccessorBlock(blockOfOldStmt);

    } else /* ==> if(oldStmt.branches() && newStmt.branches()) */ {

      blockOfOldStmt.replaceStmt(oldStmt, newStmt);
      stmtToBlock.put(newStmt, blockOfOldStmtPair);
      if (oldStmt.getExpectedSuccessorCount() != newStmt.getExpectedSuccessorCount()) {
        // TODO: or should we just assume to use successorIdx:0
        throw new IllegalArgumentException(
            "We can't keep the flows if we replace a Stmt ("
                + oldStmt.getExpectedSuccessorCount()
                + ") by another Stmt which expects a different amount ("
                + newStmt.getExpectedSuccessorCount()
                + ") of successors.");
      }
    }

    stmtToBlock.remove(oldStmt);
  }

  public void validateBlocks() {
    for (MutableBasicBlock block : blocks) {

      List<Stmt> blockStmts = block.getStmts();
      for (int i = 0, blockStmtsSize = blockStmts.size(); i < blockStmtsSize; i++) {
        Stmt stmt = blockStmts.get(i);
        Pair<Integer, MutableBasicBlock> integerMutableBasicBlockPair = stmtToBlock.get(stmt);
        if (integerMutableBasicBlockPair.getLeft() != i) {
          throw new IllegalStateException("index numbering is out of sync!");
        }

        if (integerMutableBasicBlockPair.getRight() != block) {
          throw new IllegalStateException("wrong stmt to block mapping!");
        }
      }
    }
  }

  /*
   * Note: if there is a stmt branching to the beforeStmt this is not updated to the new stmt
   *
   * @param beforeStmt the Stmt which succeeds the inserted Stmts (its NOT preceeding as this
   *                   simplifies the handling of BranchingStmts)
   * @param stmts
   */
  public void insertBefore(
      @Nonnull Stmt beforeStmt,
      @Nonnull List<FallsThroughStmt> stmts,
      @Nonnull Map<ClassType, Stmt> exceptionMap) {
    if (stmts.isEmpty()) {
      return;
    }
    final Pair<Integer, MutableBasicBlock> beforeStmtBlockPair = stmtToBlock.get(beforeStmt);
    if (beforeStmtBlockPair == null) {
      throw new IllegalArgumentException(
          "beforeStmt '" + beforeStmt + "' does not exists in this StmtGraph.");
    }
    final MutableBasicBlock block = beforeStmtBlockPair.getRight();
    if (block.getHead() == beforeStmt) {
      // insert before a Stmt that is at the beginning of a Block? -> new block, reconnect, try to
      // merge blocks - performance hint: if exceptionMap equals the current blocks exception and
      // the stmts have only fallsthrough Stmts there could be some allocation/deallocation be saved
      final MutableBasicBlock predecessorBlock = addBlockInternal(stmts, exceptionMap);
      for (MutableBasicBlock predecessor : Lists.newArrayList(block.getPredecessors())) {
        // cleanup old & add new link
        predecessor.replaceSuccessorBlock(block, predecessorBlock);
        block.removePredecessorBlock(predecessor);
        predecessorBlock.addPredecessorBlock(predecessor);
      }
      if (!tryMergeBlocks(predecessorBlock, block)) {
        // all inserted Stmts are FallingThrough: so successorIdx = 0
        predecessorBlock.linkSuccessor(0, block);
      }

    } else {
      // TODO: check conditions before splitting if split will be necessary instead of
      // split-and-merge
      final MutableBasicBlock successorBlock =
          block.splitBlockLinked(beforeStmtBlockPair.getLeft());
      exceptionMap.forEach(
          (type, handler) ->
              successorBlock.linkExceptionalSuccessorBlock(type, getOrCreateBlock(handler)));
      stmts.forEach(stmt -> addNodeToBlock(block, stmt));
      if (tryMergeBlocks(block, successorBlock)) {
        // blocks are merged: update index of the merged stmts
        int idx = block.getStmtCount() - successorBlock.getStmtCount();
        for (Stmt stmt : successorBlock.getStmts()) {
          stmtToBlock.put(stmt, new MutablePair<>(idx++, block));
        }
      } else {
        // update index: for stmts of the split block
        int idx = 0;
        for (Stmt stmt : successorBlock.getStmts()) {
          stmtToBlock.put(stmt, new MutablePair<>(idx++, successorBlock));
        }
        blocks.add(successorBlock);
      }
    }

    if (beforeStmt == getStartingStmt()) {
      setStartingStmt(stmts.get(0));
    }
  }

  /** Replaces all SuccessorEdge(s) of from to oldTo by mewTo */
  @Override
  public boolean replaceSucessorEdge(@Nonnull Stmt from, @Nonnull Stmt oldTo, @Nonnull Stmt newTo) {
    final Pair<Integer, MutableBasicBlock> mutableBasicBlockPair = stmtToBlock.get(from);
    if (mutableBasicBlockPair == null) {
      throw new IllegalArgumentException("stmt '" + from + "' does not exist in this StmtGraph!");
    }
    final MutableBasicBlock mutableBasicBlock = mutableBasicBlockPair.getRight();

    final Pair<Integer, MutableBasicBlock> oldTargetBlockPair = stmtToBlock.get(oldTo);
    if (oldTargetBlockPair == null) {
      throw new IllegalArgumentException("stmt '" + oldTo + "' does not exist in this StmtGraph!");
    }
    final MutableBasicBlock oldTargetBlock = stmtToBlock.get(oldTo).getRight();

    boolean found = false;
    for (ListIterator<MutableBasicBlock> iterator =
            mutableBasicBlock.getSuccessors().listIterator();
        iterator.hasNext(); ) {
      MutableBasicBlock block = iterator.next();
      if (block == oldTargetBlock) {
        iterator.set(getOrCreateBlock(newTo));
        found = true;
      }
    }
    return found;
  }

  public void putEdge(@Nonnull FallsThroughStmt stmtA, @Nonnull Stmt stmtB) {
    putEdge_internal(stmtA, 0, stmtB);
  }

  public void putEdge(@Nonnull BranchingStmt stmtA, int succesorIdx, @Nonnull Stmt stmtB) {
    if (0 > succesorIdx || succesorIdx >= stmtA.getExpectedSuccessorCount()) {
      throw new IllegalArgumentException(
          "SuccessorIdx '"
              + succesorIdx
              + "' is out of bounds - needs to be [0, "
              + (stmtA.getExpectedSuccessorCount() - 1)
              + "]");
    }
    putEdge_internal(stmtA, succesorIdx, stmtB);
  }

  protected void putEdge_internal(@Nonnull Stmt stmtA, int succesorIdx, @Nonnull Stmt stmtB) {

    Pair<Integer, MutableBasicBlock> blockAPair = stmtToBlock.get(stmtA);
    Pair<Integer, MutableBasicBlock> blockBPair = stmtToBlock.get(stmtB);

    MutableBasicBlock blockA;
    if (blockAPair == null) {
      // stmtA is is not in the graph (i.e. no reference to BlockA) -> create
      blockA = createStmtsBlock(stmtA).getRight();
    } else {
      if (blockAPair.getRight().getTail() != stmtA) {
        // if StmtA is not at the end of the block -> it needs to branch to reach StmtB or is
        // falling through to another Block
        throw new IllegalArgumentException(
            "StmtA '"
                + stmtA
                + "' is not at the end of a block but it must be to reach StmtB '"
                + stmtB
                + "'.");
      }
      blockA = blockAPair.getRight();
    }

    // TODO: [ms] check to refactor this directly into putEdge - Attention: JIfStmt is
    // FallsThroughStmt AND BranchingStmt
    MutableBasicBlock blockB;
    if (stmtA.branches()) {
      // branching Stmt A indicates the end of BlockA and connects to another BlockB: reuse or
      // create new
      // one
      if (blockBPair == null) {
        blockB = createStmtsBlock(stmtB).getRight();
        blockA.linkSuccessor(succesorIdx, blockB);
      } else {
        blockB = blockBPair.getRight();
        if (blockB.getHead() == stmtB) {
          // stmtB is at the beginning of the second Block -> connect blockA and blockB

          blockA.linkSuccessor(succesorIdx, blockB);
        } else {

          MutableBasicBlock newBlock = blockB.splitBlockLinked(blockBPair.getLeft());
          newBlock.copyExceptionalFlowFrom(blockB);
          blocks.add(newBlock);
          int idx = 0;
          for (Stmt stmt : newBlock.getStmts()) {
            stmtToBlock.put(stmt, new MutablePair<>(idx++, newBlock));
          }

          if (blockA == blockB) {
            // successor of block is the origin: end of block flows to beginning of new splitted
            // block (i.e.
            // the same block)
            newBlock.linkSuccessor(succesorIdx, newBlock);

          } else {
            blockA.linkSuccessor(succesorIdx, newBlock);
          }
        }
      }

    } else {
      // stmtA does not branch
      if (blockBPair == null) {
        // stmtB is new in the graph -> just add it to the same block
        // TODO: think about exceptions.. could add a Stmt to an exception range
        addNodeToBlock(blockA, stmtB);
      } else {
        blockB = blockBPair.getRight();
        if (blockB.getHead() == stmtB) {
          // stmtB is at the beginning of the second Block -> try to connect blockA and blockB
          // is stmtB already a branch target and do their blocks have the same traps?
          if (blockB.getPredecessors().isEmpty()
              && blockA.getExceptionalSuccessors().equals(blockB.getExceptionalSuccessors())) {
            // merge blockB into blockA and remove now obsolete Block B
            for (Stmt stmt : blockB.getStmts()) {
              addNodeToBlock(blockA, stmt);
            }
            blocks.remove(blockB);
            // update exceptional predecessors to the new block!
            blockB
                .getExceptionalSuccessors()
                .values()
                .forEach(
                    eb -> {
                      eb.removePredecessorBlock(blockB);
                      eb.addPredecessorBlock(blockA);
                    });

          } else {
            // stmtA does not branch but stmtB is already a branch target or has different traps =>
            // link blocks
            blockA.linkSuccessor(succesorIdx, blockB);
          }
        } else {
          throw new IllegalArgumentException(
              "StmtB '"
                  + stmtB
                  + "' is already in the Graph and has already a non-branching predecessor!");
        }
      }
    }
  }

  @Override
  public List<Integer> removeEdge(@Nonnull Stmt from, @Nonnull Stmt to) {
    Pair<Integer, MutableBasicBlock> blockOfFromPair = stmtToBlock.get(from);
    if (blockOfFromPair == null) {
      // Stmt is not existing anymore in this graph - so neither a connection.
      return Collections.emptyList();
    }
    MutableBasicBlock blockOfFrom = blockOfFromPair.getRight();

    Pair<Integer, MutableBasicBlock> blockOfToPair = stmtToBlock.get(to);
    if (blockOfToPair == null) {
      // Stmt is not existing anymore in this graph - so neither a connection.
      return Collections.emptyList();
    }
    MutableBasicBlock blockOfTo = blockOfToPair.getRight();

    if (blockOfFrom.getTail() == from && blockOfTo.getHead() == to) {
      // `from` and `to` are the tail and head of their respective blocks,
      // meaning they either connect different blocks,
      // or are a loop of the same block

      // remove the connection between the blocks
      boolean predecessorRemoved = blockOfTo.removePredecessorBlock(blockOfFrom);
      List<Integer> successorIdxList = blockOfFrom.replaceSuccessorBlock(blockOfTo, null);
      boolean successorRemoved = !successorIdxList.isEmpty();
      assert predecessorRemoved == successorRemoved;

      if (!predecessorRemoved) {
        // the blocks weren't connected
        return Collections.emptyList();
      }

      // the removal of the edge between `from` and `to` might have created blocks that can be
      // merged
      tryMergeWithPredecessorBlock(blockOfTo);
      tryMergeWithSuccessorBlock(blockOfFrom);

      return successorIdxList;
    } else if (blockOfFrom == blockOfTo) {
      // `from` and `to` are part of the same block but aren't the tail and head,
      // which means they are "inner" statements in the block and the block needs to be divided

      // divide block and don't link them
      // from is not the tail Stmt and the from-Stmt is directly before the to-Stmt
      if (blockOfToPair.getLeft() - blockOfFromPair.getLeft() == 1) {
        MutableBasicBlock newBlock = blockOfFrom.splitBlockUnlinked(blockOfFromPair.getLeft() + 1);
        newBlock.copyExceptionalFlowFrom(blockOfFrom);
        List<MutableBasicBlock> successors = blockOfFrom.getSuccessors();
        for (int i = 0; i < successors.size(); i++) {
          MutableBasicBlock successor = successors.get(i);
          successor.removePredecessorBlock(blockOfFrom);
          newBlock.linkSuccessor(i, successor);
        }
        blockOfFrom.clearSuccessorBlocks();
        blocks.add(newBlock);
        int idx = 0;
        for (Stmt s : newBlock.getStmts()) {
          stmtToBlock.put(s, new MutablePair<>(idx++, newBlock));
        }
        return Collections.singletonList(0);
      } else {
        // `from` and `to` are not successive statements in the block
        return Collections.emptyList();
      }
    } else {
      // `from` and `to` are part of different blocks,
      // and aren't tail and head of their respective block,
      // which means they aren't connected
      return Collections.emptyList();
    }
  }

  @Override
  public void setEdges(@Nonnull BranchingStmt fromStmt, @Nonnull List<Stmt> targets) {
    if (fromStmt.getExpectedSuccessorCount() != targets.size()) {
      throw new IllegalArgumentException(
          "Size of Targets is not the amount of from's expected successors.");
    }
    MutableBasicBlock fromBlock = getOrCreateBlock(fromStmt);
    if (fromBlock.getTail() == fromStmt) {
      // cleanup existing edges
      fromBlock.clearSuccessorBlocks();
    }
    for (int i = 0; i < targets.size(); i++) {
      Stmt target = targets.get(i);
      putEdge(fromStmt, i, target);
    }
  }

  @Nullable
  @Override
  public Stmt getStartingStmt() {
    // is the stmt currently in a block associated with the graph?
    if (stmtToBlock.get(startingStmt) == null) {
      return null;
    }
    return startingStmt;
  }

  @Override
  @Nullable
  public BasicBlock<?> getStartingStmtBlock() {
    return getBlockOf(startingStmt);
  }

  @Override
  @Nullable
  public BasicBlock<?> getBlockOf(@Nonnull Stmt stmt) {
    final Pair<Integer, MutableBasicBlock> mutableBasicBlock = stmtToBlock.get(stmt);
    if (mutableBasicBlock == null) {
      throw new IllegalArgumentException("stmt '" + stmt + "' does not exist in this StmtGraph!");
    }
    return mutableBasicBlock.getRight();
  }

  @Nonnull
  @Override
  public StmtGraph<?> unmodifiableStmtGraph() {
    return new ForwardingStmtGraph<>(this);
  }

  public void setStartingStmt(@Nonnull Stmt startingStmt) {
    if (stmtToBlock.get(startingStmt) == null) {
      Pair<Integer, MutableBasicBlock> block = stmtToBlock.get(startingStmt);
      if (block == null) {
        // Stmt does not exist in the graph
        createStmtsBlock(startingStmt);
      }
    }
    this.startingStmt = startingStmt;
  }

  @Nonnull
  @Override
  public Set<Stmt> getNodes() {
    return stmtToBlock.keySet();
  }

  @Override
  public boolean containsNode(@Nonnull Stmt node) {
    return stmtToBlock.containsKey(node);
  }

  @Nonnull
  @Override
  public List<Stmt> predecessors(@Nonnull Stmt node) {
    Pair<Integer, MutableBasicBlock> blockPair = stmtToBlock.get(node);
    if (blockPair == null) {
      throw new IllegalArgumentException(
          "Stmt '" + node + "' is not contained in the BlockStmtGraph");
    }
    MutableBasicBlock block = blockPair.getRight();

    if (node == block.getHead()) {
      List<MutableBasicBlock> predecessorBlocks = block.getPredecessors();
      List<Stmt> preds = new ArrayList<>(predecessorBlocks.size());
      predecessorBlocks.forEach(p -> preds.add(p.getTail()));
      return preds;
    } else {
      List<Stmt> stmts = block.getStmts();
      final int idx = blockPair.getLeft();
      // we know: i != 0 (-> i>0) as node is not the blocks' head
      return Collections.singletonList(stmts.get(idx - 1));
    }
  }

  @Nonnull
  @Override
  public List<Stmt> exceptionalPredecessors(@Nonnull Stmt node) {

    Pair<Integer, MutableBasicBlock> blockPair = stmtToBlock.get(node);
    if (blockPair == null) {
      throw new IllegalArgumentException(
          "Stmt '" + node + "' is not contained in the BlockStmtGraph");
    }
    MutableBasicBlock block = blockPair.getRight();

    if (block.getHead() != node) {
      // a traphandler is a blocks head and only an exception handler stmt can have exceptional
      // predecessors
      return Collections.emptyList();
    }

    return exceptionalPredecessors(block);
  }

  public List<Stmt> exceptionalPredecessors(@Nonnull MutableBasicBlock block) {

    Stmt head = block.getHead();
    if (!(head instanceof JIdentityStmt
        && ((JIdentityStmt) head).getRightOp() instanceof JCaughtExceptionRef)) {
      // only an exception handler stmt can have exceptional predecessors
      return Collections.emptyList();
    }

    List<Stmt> exceptionalPred = new ArrayList<>();
    for (BasicBlock<?> pBlock : block.getPredecessors()) {
      if (pBlock.getExceptionalSuccessors().containsValue(pBlock)) {
        exceptionalPred.addAll(pBlock.getStmts());
      }
    }
    return exceptionalPred;
  }

  public List<? extends BasicBlock<?>> exceptionalPredecessorBlocks(@Nonnull BasicBlock<?> block) {

    Stmt head = block.getHead();
    if (!(head instanceof JIdentityStmt
        && ((JIdentityStmt) head).getRightOp() instanceof JCaughtExceptionRef)) {
      // only an exception handler stmt can have exceptional predecessors
      return Collections.emptyList();
    }

    List<BasicBlock<?>> exceptionalPred = new ArrayList<>();
    for (BasicBlock<?> pBlock : block.getPredecessors()) {
      if (pBlock.getExceptionalSuccessors().containsValue(block)) {
        exceptionalPred.add(pBlock);
      }
    }
    return exceptionalPred;
  }

  @Nonnull
  @Override
  public List<Stmt> successors(@Nonnull Stmt node) {
    Pair<Integer, MutableBasicBlock> blockPair = stmtToBlock.get(node);
    if (blockPair == null) {
      throw new IllegalArgumentException(
          "Stmt '" + node + "' is not contained in the BlockStmtGraph");
    }
    MutableBasicBlock block = blockPair.getRight();

    if (node == block.getTail()) {
      List<MutableBasicBlock> successorBlocks = block.getSuccessors();
      List<Stmt> succs = new ArrayList<>(successorBlocks.size());
      successorBlocks.forEach(p -> succs.add(p.getHead()));
      return succs;
    } else {
      List<Stmt> stmts = block.getStmts();
      return Collections.singletonList(stmts.get(blockPair.getLeft() + 1));
    }
  }

  @Nonnull
  @Override
  public Map<ClassType, Stmt> exceptionalSuccessors(@Nonnull Stmt node) {
    Pair<Integer, MutableBasicBlock> blockPair = stmtToBlock.get(node);
    if (blockPair == null) {
      throw new IllegalArgumentException(
          "Stmt '" + node + "' is not contained in the BlockStmtGraph");
    }
    MutableBasicBlock block = blockPair.getRight();
    Map<ClassType, Stmt> map = new HashMap<>();
    for (Map.Entry<ClassType, MutableBasicBlock> b : block.getExceptionalSuccessors().entrySet()) {
      map.put(b.getKey(), b.getValue().getHead());
    }
    return map;
  }

  @Override
  public int inDegree(@Nonnull Stmt node) {
    Pair<Integer, MutableBasicBlock> blockPair = stmtToBlock.get(node);
    if (blockPair == null) {
      throw new IllegalArgumentException(
          "Stmt '" + node + "' is not contained in the BlockStmtGraph");
    }
    MutableBasicBlock block = blockPair.getRight();

    if (node == block.getHead()) {
      return block.getPredecessors().size();
    } else {
      return 1;
    }
  }

  @Override
  public int outDegree(@Nonnull Stmt node) {
    Pair<Integer, MutableBasicBlock> blockPair = stmtToBlock.get(node);
    if (blockPair == null) {
      throw new IllegalArgumentException(
          "Stmt '" + node + "' is not contained in the BlockStmtGraph");
    }
    MutableBasicBlock block = blockPair.getRight();

    if (node == block.getTail()) {
      return block.getSuccessors().size();
    } else {
      return 1;
    }
  }

  @Override
  public boolean hasEdgeConnecting(@Nonnull Stmt source, @Nonnull Stmt target) {
    Pair<Integer, MutableBasicBlock> blockAPair = stmtToBlock.get(source);
    if (blockAPair == null) {
      throw new IllegalArgumentException(
          "Stmt '" + source + "' is not contained in the BlockStmtGraph");
    }
    MutableBasicBlock blockA = blockAPair.getRight();

    if (source == blockA.getTail()) {
      Pair<Integer, MutableBasicBlock> blockBPair = stmtToBlock.get(target);
      if (blockBPair == null) {
        throw new IllegalArgumentException(
            "Stmt '" + target + "' is not contained in the BlockStmtGraph");
      }

      return blockA.getSuccessors().stream()
          .anyMatch(
              successorBlock -> /*successorBlock == blockB && */
                  successorBlock.getHead() == target);
    } else {
      List<Stmt> stmtsA = blockA.getStmts();
      return stmtsA.get(blockAPair.getLeft() + 1) == target;
    }
  }

  /** Comparator which sorts the trap output in getTraps() */
  public Comparator<Trap> getTrapComparator(@Nonnull Map<Stmt, Integer> stmtsBlockIdx) {
    return (a, b) ->
        ComparisonChain.start()
            .compare(stmtsBlockIdx.get(a.getBeginStmt()), stmtsBlockIdx.get(b.getBeginStmt()))
            .compare(stmtsBlockIdx.get(a.getEndStmt()), stmtsBlockIdx.get(b.getEndStmt()))
            // [ms] would be nice to have the traps ordered by exception hierarchy as well
            .compare(a.getExceptionType().toString(), b.getExceptionType().toString())
            .result();
  }

  /** hint: little expensive getter - its more of a build/create - currently no overlaps */
  @Override
  public List<Trap> buildTraps() {
    // [ms] try to incorporate it into the serialisation of jimple printing so the other half of
    // iteration information is not wasted..
    BlockGraphIteratorAndTrapAggregator it =
        new BlockGraphIteratorAndTrapAggregator(new MutableBasicBlockImpl());
    // it.getTraps() is valid/completely build when the iterator is done.
    Map<Stmt, Integer> stmtsBlockIdx = new IdentityHashMap<>();
    int i = 0;
    // collect BlockIdx positions to sort the traps according to the numbering
    while (it.hasNext()) {
      final BasicBlock<?> nextBlock = it.next();
      stmtsBlockIdx.put(nextBlock.getHead(), i);
      stmtsBlockIdx.put(nextBlock.getTail(), i);
      i++;
    }
    final List<Trap> traps = it.getTraps();
    traps.sort(getTrapComparator(stmtsBlockIdx));
    return traps;
  }

  @Override
  public void removeExceptionalFlowFromAllBlocks(
      @Nonnull ClassType exceptionType, @Nonnull Stmt exceptionHandlerStmt) {
    for (Iterator<BasicBlock<?>> it = getBlockIterator(); it.hasNext(); ) {
      MutableBasicBlock block = (MutableBasicBlock) it.next();

      Map<? extends ClassType, ?> exceptionalSuccessors = block.getExceptionalSuccessors();

      MutableBasicBlock trapBlock = (MutableBasicBlock) exceptionalSuccessors.get(exceptionType);

      if (trapBlock != null && trapBlock.getHead() == exceptionHandlerStmt) {
        removeExceptionalEdge(block.getHead(), exceptionType);
      }
    }
  }
}
