package sootup.core.graph;

import com.google.common.collect.ComparisonChain;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.LocalGenerator;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Trap;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.stmt.BranchingStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.Stmt;
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
  @Nonnull private final Map<Stmt, MutableBasicBlock> stmtToBlock = new HashMap<>();

  @Nonnull private final Set<MutableBasicBlock> blocks = new HashSet<>();

  public MutableBlockStmtGraph() {}

  public MutableBlockStmtGraph(boolean isStatic, MethodSignature sig, LocalGenerator localgen) {
    final List<Stmt> stmts = new ArrayList<>(sig.getParameterTypes().size() + (isStatic ? 0 : 1));
    if (!isStatic) {
      ClassType thisType = sig.getDeclClassType();
      Local thisLocal = localgen.generateThisLocal(thisType);
      Stmt stmt =
          Jimple.newIdentityStmt(
              thisLocal, Jimple.newThisRef(thisType), StmtPositionInfo.createNoStmtPositionInfo());
      stmts.add(stmt);
    }
    int i = 0;
    for (Type parameterType : sig.getParameterTypes()) {
      Stmt stmt =
          Jimple.newIdentityStmt(
              localgen.generateParameterLocal(parameterType, i),
              Jimple.newParameterRef(parameterType, i++),
              StmtPositionInfo.createNoStmtPositionInfo());
      stmts.add(stmt);
    }
    if (!stmts.isEmpty()) {
      setStartingStmt(stmts.get(0));
      addBlock(stmts);
    }
  }

  /** copies a StmtGraph into this Mutable instance */
  public MutableBlockStmtGraph(@Nonnull StmtGraph<? extends BasicBlock<?>> graph) {
    setStartingStmt(graph.getStartingStmt());
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
              final MutableBasicBlock blockOf = stmtToBlock.get(b.getTail());
              b.getSuccessors()
                  .forEach(succ -> linkBlocks(blockOf, stmtToBlock.get(succ.getHead())));
            });
  }

  /**
   * Creates a Graph representation from the 'legacy' representation i.e. a List of Stmts and Traps.
   */
  public void initializeWith(
      @Nonnull List<Stmt> stmts,
      @Nonnull Map<BranchingStmt, List<Stmt>> branchingMap,
      @Nonnull List<Trap> traps) {

    if (stmts.isEmpty()) {
      return;
    }

    final Stmt lastStmt = stmts.get(stmts.size() - 1);
    if (lastStmt.fallsThrough()) {
      throw new IllegalArgumentException(
          "Theres a fallsthrough Stmt at the end of the list of stmts ('"
              + lastStmt
              + "') which has no sucessor - which means it currently falls into the abyss i.e. it can't fall through to another Stmt.");
    }

    HashMap<Stmt, Integer> trapstmtToIdx = new HashMap<>();
    PriorityQueue<Trap> trapStart =
        new PriorityQueue<>(
            Comparator.comparingInt((Trap t) -> trapstmtToIdx.get(t.getBeginStmt())));
    PriorityQueue<Trap> trapEnd =
        new PriorityQueue<>(Comparator.comparingInt((Trap t) -> trapstmtToIdx.get(t.getEndStmt())));

    traps.forEach(
        trap -> {
          trapstmtToIdx.put(trap.getBeginStmt(), stmts.indexOf(trap.getBeginStmt()));
          trapstmtToIdx.put(trap.getEndStmt(), stmts.indexOf(trap.getEndStmt()));
          trapstmtToIdx.put(trap.getHandlerStmt(), stmts.indexOf(trap.getHandlerStmt()));
        });

    duplicateCatchAllTrapRemover(traps, trapstmtToIdx);

    traps.forEach(
        trap -> {
          trapStart.add(trap);
          trapEnd.add(trap);
        });

    // traps.sort(getTrapComparator(trapstmtToIdx));
    /* debug print:
         traps.forEach(t ->  System.out.println(t.getExceptionType() + " "+ trapstmtToIdx.get(t.getBeginStmt()) + " " + trapstmtToIdx.get(t.getEndStmt()) + " -> " + trapstmtToIdx.get(t.getHandlerStmt()) + " " + t.getHandlerStmt()  ));
    */
    setStartingStmt(stmts.get(0));
    Map<ClassType, Stmt> exceptionToHandlerMap = new HashMap<>();
    Map<ClassType, Trap> currentTrapMap = new HashMap<>();
    Map<ClassType, PriorityQueue<Trap>> overlappingTraps = new HashMap<>();

    Trap nextStartingTrap = trapStart.poll();
    Trap nextEndingTrap = trapEnd.poll();
    for (int i = 0, stmtsSize = stmts.size(); i < stmtsSize; i++) {
      Stmt stmt = stmts.get(i);

      boolean trapsChanged = false;
      while (nextEndingTrap != null && nextEndingTrap.getEndStmt() == stmt) {
        Trap trap = nextEndingTrap;
        nextEndingTrap = trapEnd.poll();
        // endStmt is exclusive! -> trap ends before this stmt -> remove exception info here
        final ClassType exceptionType = trap.getExceptionType();
        final boolean isRemoved = currentTrapMap.remove(exceptionType, trap);
        final PriorityQueue<Trap> overridenTrapHandlers = overlappingTraps.get(exceptionType);
        if (overridenTrapHandlers != null) {
          if (!isRemoved && overridenTrapHandlers.size() > 0) {
            // check if theres an overlapping trap that has a less specific TrapRange which is
            // ending before it gets the active exception information again
            // not logical as a compiler output... but possible.
            overridenTrapHandlers.remove(trap);
          }

          if (overridenTrapHandlers.size() > 0) {
            currentTrapMap.put(exceptionType, overridenTrapHandlers.poll());
          }
        }

        trapsChanged = true;
      }

      while (nextStartingTrap != null && nextStartingTrap.getBeginStmt() == stmt) {
        Trap trap = nextStartingTrap;
        nextStartingTrap = trapStart.poll();
        final Trap existingTrapForException = currentTrapMap.get(trap.getExceptionType());
        if (existingTrapForException == null) {
          currentTrapMap.put(trap.getExceptionType(), trap);
        } else {
          final PriorityQueue<Trap> overridenTraps =
              overlappingTraps.computeIfAbsent(
                  trap.getExceptionType(),
                  k ->
                      new PriorityQueue<Trap>(
                          (trapA, trapB) -> {
                            if (trapA.getEndStmt() == trapB.getEndStmt()) {
                              final Integer startIdxA = trapstmtToIdx.get(trapA.getBeginStmt());
                              final Integer startIdxB = trapstmtToIdx.get(trapB.getBeginStmt());
                              return startIdxB - startIdxA;
                            } else {
                              final Integer idxA = trapstmtToIdx.get(trapA.getEndStmt());
                              final Integer idxB = trapstmtToIdx.get(trapB.getEndStmt());
                              return idxA - idxB;
                            }
                          }));

          overridenTraps.add(existingTrapForException);
          overridenTraps.add(trap);

          // remove element which is the trap with the next ending traprange
          Trap trapToApply = overridenTraps.poll();
          currentTrapMap.put(trapToApply.getExceptionType(), trapToApply);
        }
        trapsChanged = true;
      }
      // TODO: [ms] use more performant addBlock() as we already know where the Blocks borders are
      if (trapsChanged) {
        exceptionToHandlerMap.clear();
        currentTrapMap.forEach(
            (type, trap) -> exceptionToHandlerMap.put(type, trap.getHandlerStmt()));

        /* debugprint
         System.out.println("-- "+ i +" --");
         currentTrapMap.values().stream().sorted(getTrapComparator(trapstmtToIdx)).forEach(t -> System.out.println( t.getExceptionType() + " "+ trapstmtToIdx.get(t.getBeginStmt()) + " " + trapstmtToIdx.get(t.getEndStmt()) + " -> " +trapstmtToIdx.get(t.getHandlerStmt())));
        */
      }

      addNode(stmt, exceptionToHandlerMap);

      if (stmt.fallsThrough()) {
        // hint: possible bad performance if stmts is not instanceof RandomAccess
        putEdge(stmt, stmts.get(i + 1));
      }

      if (stmt instanceof BranchingStmt) {
        // => end of Block
        final List<Stmt> targets = branchingMap.get(stmt);
        int expectedBranchEntries =
            stmt.getExpectedSuccessorCount() - (stmt.fallsThrough() ? 1 : 0);
        if (targets == null || targets.size() != expectedBranchEntries) {
          int targetCount;
          if (targets == null) {
            targetCount = 0;
          } else {
            targetCount = targets.size();
          }

          throw new IllegalArgumentException(
              "The corresponding branchingMap entry for the BranchingStmt ('"
                  + stmt
                  + "') needs to have exactly the amount of targets as the BranchingStmt has successors i.e. "
                  + expectedBranchEntries
                  + " but has "
                  + targetCount
                  + ".");
        }

        for (Stmt target : targets) {
          // a possible fallsthrough (i.e. from IfStmt) is not in branchingMap
          putEdge(stmt, target);
        }
      }
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

    MutableBasicBlock block = stmtToBlock.get(stmt);
    if (block == null) {
      throw new IllegalArgumentException("Stmt is not in the StmtGraph!");
    }

    final Map<ClassType, MutableBasicBlock> exceptionalSuccessors =
        block.getExceptionalSuccessors();
    final MutableBasicBlock trapBlock = exceptionalSuccessors.get(exceptionType);
    if (trapBlock != null && trapBlock.getHead() == traphandlerStmt) {
      // edge already exists
      return;
    }

    MutableBasicBlock seperatedBlock = excludeStmtFromBlock(stmt, block);
    seperatedBlock.addExceptionalSuccessorBlock(exceptionType, getOrCreateBlock(traphandlerStmt));
    tryMergeIntoSurroundingBlocks(seperatedBlock);
  }

  @Override
  public void removeExceptionalEdge(@Nonnull Stmt node, @Nonnull ClassType exceptionType) {
    final MutableBasicBlock block = stmtToBlock.get(node);
    if (block == null) {
      throw new IllegalArgumentException("Stmt is not in the StmtGraph!");
    }
    block.removeExceptionalSuccessorBlock(exceptionType);
    tryMergeIntoSurroundingBlocks(block);
  }

  @Override
  public void clearExceptionalEdges(@Nonnull Stmt node) {
    final MutableBasicBlock block = stmtToBlock.get(node);
    if (block == null) {
      throw new IllegalArgumentException("Stmt is not in the StmtGraph!");
    }
    block.clearExceptionalSuccessorBlocks();
    tryMergeIntoSurroundingBlocks(block);
  }

  @Override
  @Nonnull
  public Set<? extends BasicBlock<?>> getBlocks() {
    return blocks.stream().map(ForwardingBasicBlock::new).collect(Collectors.toSet());
  }

  @Nonnull
  public List<? extends BasicBlock<?>> getBlocksSorted() {
    return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(blocks.iterator(), Spliterator.ORDERED), false)
        .map(ForwardingBasicBlock::new)
        .collect(Collectors.toList());
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
      @Nonnull List<Stmt> stmts, Map<ClassType, Stmt> trapMap) {
    final Iterator<Stmt> iterator = stmts.iterator();
    final Stmt node = iterator.next();
    MutableBasicBlock block = getOrCreateBlock(node);
    if (block.getHead() != node || block.getSuccessors().size() > 0) {
      throw new IllegalArgumentException(
          "The first Stmt in the List is already in the StmtGraph and and is not the head of a Block where currently no successor are set, yet.");
    } else if (block.getStmtCount() > 1) {
      throw new IllegalArgumentException(
          "The first Stmt in the List is already in the StmtGraph and has at least one (fallsthrough) successor in its Block.");
    }

    while (iterator.hasNext()) {
      Stmt stmt = iterator.next();
      final MutableBasicBlock overwrittenBlock = addNodeToBlock(block, stmt);
      if (overwrittenBlock != null) {
        if (iterator.hasNext()) {
          throw new IllegalArgumentException(
              "the Stmt '"
                  + stmt
                  + "' you want to add as a Stmt of a whole Block is already in this StmtGraph.");
        } else {
          // existing is last element of stmtlist
          // TODO: hint: we can allow other n-th elements as well e.g. if a sequence of stmts exists
          // already and can/should be inside that added block as well.

          if (overwrittenBlock.getHead() == stmt) {
            // last stmt is head of another block

            // cleanup started add action
            stmtToBlock.put(stmt, overwrittenBlock);
            block.removeStmt(stmt);

            // try to merge
            if (!tryMergeBlocks(block, overwrittenBlock)) {
              // otherwise link them
              linkBlocks(block, overwrittenBlock);
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
            block.addExceptionalSuccessorBlock(type, getOrCreateBlock(handlerStmt)));
    return block;
  }

  @Override
  public void addNode(@Nonnull Stmt stmt, @Nonnull Map<ClassType, Stmt> exceptions) {
    MutableBasicBlock block = stmtToBlock.get(stmt);
    if (block == null) {
      // Stmt does not exist in the graph -> create
      block = createStmtsBlock(stmt);
    }
    boolean isExceptionalFlowDifferent = false;
    if (block.getExceptionalSuccessors().size() == exceptions.size()) {
      for (Map.Entry<ClassType, MutableBasicBlock> entry :
          block.getExceptionalSuccessors().entrySet()) {
        final Stmt targetStmt = exceptions.get(entry.getKey());
        if (targetStmt == null) {
          isExceptionalFlowDifferent = true;
          break;
        } else if (targetStmt != entry.getValue().getHead()) {
          isExceptionalFlowDifferent = true;
          break;
        }
      }
    } else {
      isExceptionalFlowDifferent = true;
    }
    final MutableBasicBlock separatedBlock;
    if (isExceptionalFlowDifferent) {
      separatedBlock = excludeStmtFromBlock(stmt, block);
      separatedBlock.clearExceptionalSuccessorBlocks();

      // apply exceptional flow info to seperated block
      exceptions.forEach(
          (type, trapHandler) -> {
            MutableBasicBlock trapHandlerBlock = getOrCreateBlock(trapHandler);
            separatedBlock.addExceptionalSuccessorBlock(type, trapHandlerBlock);
            trapHandlerBlock.addPredecessorBlock(separatedBlock);
          });
      tryMergeIntoSurroundingBlocks(separatedBlock);
    }
  }

  /**
   * splits a block depending on the situation in multiple (0-3) Blocks so that at the end splitStmt
   * is the only Stmt in its BasicBlock. The flow between the splitted BasicBlock(s) is still
   * maintained.
   */
  @Nonnull
  private MutableBasicBlock excludeStmtFromBlock(@Nonnull Stmt splitStmt, MutableBasicBlock block) {
    final MutableBasicBlock excludedFromOrigBlock;
    if (block.getStmtCount() > 1) {
      final List<Stmt> blockStmts = block.getStmts();
      int stmtIdx = blockStmts.indexOf(splitStmt);

      if (stmtIdx < 0) {
        throw new IllegalArgumentException("splitStmt does not exist in this block!");
      }

      if (stmtIdx == 0) {
        // stmt is the head -> maybe just a single split is necessary
        excludedFromOrigBlock = block;
      } else {
        // i.e. stmt != block.getHead() -> there is a "middle" or end Block containing the splitStmt
        // which needs
        // to be seperated
        excludedFromOrigBlock = new MutableBasicBlock();
        addNodeToBlock(excludedFromOrigBlock, splitStmt);
        // add blocks exceptional flows
        block
            .getExceptionalSuccessors()
            .forEach(
                (type, trapHandlerBlock) -> {
                  excludedFromOrigBlock.addExceptionalSuccessorBlock(type, trapHandlerBlock);
                  trapHandlerBlock.addPredecessorBlock(excludedFromOrigBlock);
                });
        blocks.add(excludedFromOrigBlock);
      }

      if (stmtIdx + 1 < blockStmts.size()) {
        // "third"/after/leftover block is necessary as there are stmts after the splitElement
        final MutableBasicBlock restOfOrigBlock = new MutableBasicBlock();
        for (int i = stmtIdx + 1; i < blockStmts.size(); i++) {
          // stmtToBlock is already updated while inserting each Stmt into another Block
          addNodeToBlock(restOfOrigBlock, blockStmts.get(i));
        }

        // copy successors of block which are now the successors of the "third"/leftover block
        block
            .getSuccessors()
            .forEach(
                successor -> {
                  linkBlocks(restOfOrigBlock, successor);
                });
        block.clearSuccessorBlocks();

        // link third/leftover block with previous stmts from the separated block
        linkBlocks(excludedFromOrigBlock, restOfOrigBlock);
        block.clearSuccessorBlocks();

        // add blocks exceptional flows
        block
            .getExceptionalSuccessors()
            .forEach(
                (type, trapHandlerBlock) -> {
                  restOfOrigBlock.addExceptionalSuccessorBlock(type, trapHandlerBlock);
                  trapHandlerBlock.addPredecessorBlock(restOfOrigBlock);
                });

        blocks.add(restOfOrigBlock);

      } else {
        // there are no more stmts after stmtIdx -> less than 3 blocks are necessary
        // copy origin successors to second block as its now the last part of the origin block
        block
            .getSuccessors()
            .forEach(
                successorBlock -> {
                  linkBlocks(excludedFromOrigBlock, successorBlock);
                });
        block.clearSuccessorBlocks();
        linkBlocks(block, excludedFromOrigBlock);
      }

      // cleanup original block -> "beforeBlock" -> remove now copied Stmts
      for (int i = blockStmts.size() - 1; i >= stmtIdx; i--) {
        block.removeStmt(blockStmts.get(i));
      }

      return excludedFromOrigBlock;

    } else {
      // just a single stmt in the block -> e.g. its already the block we want to seperate
      return block;
    }
  }

  /** Merges block into Predecessor/Successor if possible. */
  private void tryMergeIntoSurroundingBlocks(@Nonnull MutableBasicBlock block) {
    // merge with predecessor if possible
    block = tryMergeWithPredecessorBlock(block);
    // and/or merge with successorBlock
    tryMergeWithSuccessorBlock(block);
  }

  @Nonnull
  private MutableBasicBlock tryMergeWithSuccessorBlock(@Nonnull MutableBasicBlock block) {
    final List<MutableBasicBlock> successors = block.getSuccessors();
    if (successors.size() == 1) {
      final MutableBasicBlock singleSuccessor = successors.get(0);
      if (tryMergeBlocks(block, singleSuccessor)) {
        return singleSuccessor;
      }
    }
    return block;
  }

  @Nonnull
  private MutableBasicBlock tryMergeWithPredecessorBlock(@Nonnull MutableBasicBlock block) {
    final List<MutableBasicBlock> predecessors = block.getPredecessors();
    if (predecessors.size() == 1) {
      final MutableBasicBlock singlePredecessor = predecessors.get(0);
      if (tryMergeBlocks(singlePredecessor, block)) {
        return singlePredecessor;
      }
    }
    return block;
  }

  @Nonnull
  private MutableBasicBlock getOrCreateBlock(@Nonnull Stmt stmt) {
    MutableBasicBlock trapHandlerBlock = stmtToBlock.get(stmt);
    if (trapHandlerBlock == null) {
      // traphandlerStmt does not exist in the graph -> create
      trapHandlerBlock = createStmtsBlock(stmt);
    }
    return trapHandlerBlock;
  }

  protected boolean isMergeable(
      @Nonnull MutableBasicBlock firstBlock, @Nonnull MutableBasicBlock followingBlock) {
    if (firstBlock.getTail().branches()) {
      return false;
    }
    final List<MutableBasicBlock> fBlocksuccessors = firstBlock.getSuccessors();
    if (fBlocksuccessors.size() != 1 || fBlocksuccessors.get(0) != followingBlock) {
      return false;
    }
    // if we are here the datastructure should have managed that the next if is true..
    final List<MutableBasicBlock> sBlockPredecessors = followingBlock.getPredecessors();
    if (sBlockPredecessors.size() != 1 || sBlockPredecessors.get(0) != firstBlock) {
      return false;
    }
    // check if the same traps are applied to both blocks
    if (!firstBlock.getExceptionalSuccessors().equals(followingBlock.getExceptionalSuccessors())) {
      return false;
    }
    return true;
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
      followingBlock.getSuccessors().forEach(succ -> linkBlocks(firstBlock, succ));
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
  protected MutableBasicBlock createStmtsBlock(@Nonnull Stmt stmt) {
    // add Block to graph, add+register Stmt to Block
    MutableBasicBlock block = new MutableBasicBlock();
    if (addNodeToBlock(block, stmt) != null) {
      throw new IllegalArgumentException("Stmt is already in the graph!");
    }
    blocks.add(block);
    return block;
  }

  /** Adds a Stmt to the end of a block i.e. stmt will become the new tail. */
  protected MutableBasicBlock addNodeToBlock(@Nonnull MutableBasicBlock block, @Nonnull Stmt stmt) {
    block.addStmt(stmt);
    return stmtToBlock.put(stmt, block);
  }

  public void removeNode(@Nonnull Stmt stmt) {
    // TODO: [ms] whats intuitive? removing the flows to the block too? or is deleting a stmt
    // keeping the flows to it
    // is the answer different if its the tail? consistency vs intuitivity..
    removeNode(stmt, true);
  }

  public void removeNode(@Nonnull Stmt stmt, boolean keepFlow) {

    MutableBasicBlock blockOfRemovedStmt = stmtToBlock.remove(stmt);
    if (blockOfRemovedStmt == null) {
      throw new IllegalArgumentException("Stmt is not in the StmtGraph!");
    }

    if (stmt == startingStmt) {
      startingStmt = null;
    }

    final boolean isHead = blockOfRemovedStmt.getHead() == stmt;
    final boolean isTail = blockOfRemovedStmt.getTail() == stmt;

    // do edges from or to this node exist -> remove them?
    if (isHead && !keepFlow) {
      final MutableBasicBlock finalBlockOfRemovedStmt = blockOfRemovedStmt;
      blockOfRemovedStmt
          .getPredecessors()
          .forEach(
              b -> {
                b.removeSuccessorBlock(finalBlockOfRemovedStmt);
                finalBlockOfRemovedStmt.removePredecessorBlock(b);
              });
      blockOfRemovedStmt.clearPredecessorBlocks();
    }

    if (isTail) {
      if (stmt.branches() && !keepFlow) {
        blockOfRemovedStmt.clearSuccessorBlocks();
      }
    }

    // cleanup or merge blocks if necesssary (stmt itself is not removed from the block yet)
    if (blockOfRemovedStmt.getStmtCount() > 1) {
      blockOfRemovedStmt.removeStmt(stmt);

      if (isHead) {
        blockOfRemovedStmt = tryMergeWithPredecessorBlock(blockOfRemovedStmt);
      }
      if (isTail) {
        tryMergeWithSuccessorBlock(blockOfRemovedStmt);
      }

    } else {
      // cleanup block (i.e. remove!) as its not needed in the graph anymore if it only contains
      // stmt - which is
      // now deleted
      blocks.remove(blockOfRemovedStmt);
      blockOfRemovedStmt.clearPredecessorBlocks();
      blockOfRemovedStmt.clearSuccessorBlocks();
      blockOfRemovedStmt.clearExceptionalSuccessorBlocks();
      blockOfRemovedStmt.removeStmt(stmt);
    }
  }

  @Override
  public void replaceNode(@Nonnull Stmt oldStmt, @Nonnull Stmt newStmt) {

    final MutableBasicBlock blockOfOldStmt = stmtToBlock.get(oldStmt);
    if (blockOfOldStmt == null) {
      throw new IllegalArgumentException("oldStmt does not exist in the StmtGraph!");
    }

    // is oldStmt the startingStmt? replace startingStmt with newStmt
    if (oldStmt == startingStmt) {
      startingStmt = newStmt;
    }

    if (oldStmt.getExpectedSuccessorCount() != newStmt.getExpectedSuccessorCount()) {
      final MutableBasicBlock excludedBlock = excludeStmtFromBlock(oldStmt, blockOfOldStmt);
      excludedBlock.replaceStmt(oldStmt, newStmt);
      stmtToBlock.remove(oldStmt);
      stmtToBlock.put(newStmt, excludedBlock);
    } else {
      stmtToBlock.remove(oldStmt);
      blockOfOldStmt.replaceStmt(oldStmt, newStmt);
      stmtToBlock.put(newStmt, blockOfOldStmt);
    }
  }

  public void validateBlocks() {
    for (MutableBasicBlock block : blocks) {
      for (Stmt stmt : block.getStmts()) {
        if (stmtToBlock.get(stmt) != block) {
          throw new IllegalStateException("wrong stmt to block mapping");
        }
      }
    }
  }

  /**
   * @param beforeStmt the Stmt which succeeds the inserted Stmts (its NOT preceeding as this
   *     simplifies the handling of BranchingStmts)
   * @param stmts can only allow fallsthrough Stmts except for the last Stmt in the List there is a
   *     single BranchingStmt allowed!
   */
  public void insertBefore(
      @Nonnull Stmt beforeStmt,
      @Nonnull List<Stmt> stmts,
      @Nonnull Map<ClassType, Stmt> exceptionMap) {
    if (stmts.isEmpty()) {
      return;
    }
    final MutableBasicBlock block = stmtToBlock.get(beforeStmt);
    if (block == null) {
      throw new IllegalArgumentException(
          "beforeStmt '" + beforeStmt + "' does not exists in this StmtGraph.");
    }
    if (block.getHead() == beforeStmt) {
      // insert before a Stmt that is at the beginning of a Block? -> new block, reconnect, try to
      // merge blocks - performance hint: if exceptionMap equals the current blocks exception and
      // the stmts have only fallsthrough Stmts there could be some allocation/deallocation be saved
      final MutableBasicBlock predecessorBlock = addBlockInternal(stmts, exceptionMap);
      for (MutableBasicBlock predecessor : block.getPredecessors()) {
        // cleanup old
        predecessor.removeSuccessorBlock(block);
        block.removePredecessorBlock(predecessor);
        // add new link
        linkBlocks(predecessor, predecessorBlock);
      }
      tryMergeBlocks(predecessorBlock, block);
    } else {
      final MutableBasicBlock successorBlock = block.splitBlockLinked(beforeStmt, true);
      exceptionMap.forEach(
          (type, handler) ->
              successorBlock.addExceptionalSuccessorBlock(type, getOrCreateBlock(handler)));
      stmts.forEach(stmt -> addNodeToBlock(block, stmt));
      tryMergeBlocks(block, successorBlock);
    }

    if (beforeStmt == getStartingStmt()) {
      setStartingStmt(stmts.get(0));
    }
  }

  @Override
  public void putEdge(@Nonnull Stmt stmtA, @Nonnull Stmt stmtB) {
    MutableBasicBlock blockA = stmtToBlock.get(stmtA);
    MutableBasicBlock blockB = stmtToBlock.get(stmtB);

    if (blockA == null) {
      // stmtA is is not in the graph (i.e. no reference to BlockA) -> create
      blockA = createStmtsBlock(stmtA);
    } else {
      if (blockA.getTail() != stmtA) {
        // if StmtA is not at the end of the block -> it needs to branch to reach StmtB or is
        // falling through to another Block
        throw new IllegalArgumentException(
            "StmtA '"
                + stmtA
                + "' is not at the end of a block but it must be to reach StmtB '"
                + stmtB
                + "'.");
      }
    }

    if (blockA.getSuccessors().size() >= stmtA.getExpectedSuccessorCount()) {
      throw new IllegalArgumentException(
          "Can't add another flow - there are already enough flows i.e. "
              + stmtA.getExpectedSuccessorCount()
              + " outgoing from StmtA '"
              + stmtA
              + "'");
    }

    if (stmtA.branches()) {
      // branching Stmt A indicates the end of BlockA and connects to another BlockB: reuse or
      // create new
      // one
      if (blockB == null) {
        blockB = createStmtsBlock(stmtB);
        linkBlocks(blockA, blockB);
      } else {
        if (blockB.getHead() == stmtB) {
          // stmtB is at the beginning of the second Block -> connect blockA and blockB
          linkBlocks(blockA, blockB);
        } else {

          MutableBasicBlock newBlock = blockB.splitBlockLinked(stmtB, true);
          newBlock.copyExceptionalFlowFrom(blockB);
          blocks.add(newBlock);
          newBlock.getStmts().forEach(stmt -> stmtToBlock.put(stmt, newBlock));

          if (blockA == blockB) {
            // successor of block is the origin: end of block flows to beginning of new splitted
            // block (i.e.
            // the same block)
            linkBlocks(newBlock, newBlock);
          } else {
            linkBlocks(blockA, newBlock);
          }
        }
      }

    } else {
      // stmtA does not branch
      if (blockB == null) {
        // stmtB is new in the graph -> just add it to the same block
        addNodeToBlock(blockA, stmtB);
      } else {
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
          } else {
            // stmtA does not branch but stmtB is already a branch target or has different traps =>
            // link blocks
            linkBlocks(blockA, blockB);
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

  /**
   * makes blockA the predecessor of BlockB and BlockB the Successor of BlockA in a combined Method
   */
  private void linkBlocks(@Nonnull MutableBasicBlock blockA, @Nonnull MutableBasicBlock blockB) {
    blockA.addSuccessorBlock(blockB);
    blockB.addPredecessorBlock(blockA);
  }

  @Override
  public void removeEdge(@Nonnull Stmt from, @Nonnull Stmt to) {
    // FIXME: how to handle "partial" removals of targets of flows starting from a Branching Stmt..
    // e.g. because one of the targets are removed.. that changes the whole logic there..

    MutableBasicBlock blockOfFrom = stmtToBlock.get(from);
    MutableBasicBlock blockOfTo = stmtToBlock.get(to);

    if (blockOfFrom == null || blockOfTo == null) {
      // one of the Stmts is not existing anymore in this graph - so neither a connection.
      return;
    }

    removeBlockBorderEdgesInternal(from, blockOfFrom);

    // divide block if from and to are from the same block
    if (blockOfFrom == blockOfTo) {
      // divide block and don't link them
      final List<Stmt> stmtsOfBlock = blockOfFrom.getStmts();
      int toIdx = stmtsOfBlock.indexOf(from) + 1;
      // from is not the tail Stmt and the from-Stmt is directly before the to-Stmt
      if (toIdx < stmtsOfBlock.size() && stmtsOfBlock.get(toIdx) == to) {
        MutableBasicBlock newBlock = blockOfFrom.splitBlockUnlinked(from, to);
        newBlock.copyExceptionalFlowFrom(blockOfFrom);
        blockOfFrom.getSuccessors().forEach(newBlock::addSuccessorBlock);
        blockOfFrom.clearSuccessorBlocks();
        blocks.add(newBlock);
        newBlock.getStmts().forEach(s -> stmtToBlock.put(s, newBlock));
      } else {
        // throw new IllegalArgumentException("Can't seperate the flow from '"+from+"' to '"+to+"'.
        // The Stmts are not connected in this graph!");
      }
    }
  }

  protected void removeBlockBorderEdgesInternal(
      @Nonnull Stmt from, @Nonnull MutableBasicBlock blockOfFrom) {
    // TODO: is it intuitive to remove connections to the BasicBlock in the case we cant merge the
    // blocks?
    // TODO: reuse tryMerge*Block?

    // add BlockB to BlockA if blockA has no branchingstmt as tail && same traps
    if (blockOfFrom.getStmts().size() > 0 && from == blockOfFrom.getTail()) {
      if (blockOfFrom.getPredecessors().size() == 1) {
        MutableBasicBlock singlePreviousBlock = blockOfFrom.getPredecessors().get(0);
        if (!singlePreviousBlock.getTail().branches() && singlePreviousBlock != blockOfFrom) {
          if (singlePreviousBlock
              .getExceptionalSuccessors()
              .equals(blockOfFrom.getExceptionalSuccessors())) {
            blockOfFrom
                .getStmts()
                .forEach(
                    k -> {
                      addNodeToBlock(blockOfFrom, k);
                    });
            return;
          }
        }
      }

      // remove outgoing connections from blockA if from stmt is the tail
      if (!from.branches()) {
        if (blockOfFrom.getStmts().size() > 0 && blockOfFrom.getSuccessors().size() == 1) {
          // merge previous block if possible i.e. no branchingstmt as tail && same traps && no
          // other predesccorblocks
          MutableBasicBlock singleSuccessorBlock = blockOfFrom.getSuccessors().get(0);
          if (singleSuccessorBlock.getPredecessors().size() == 1
              && singleSuccessorBlock.getPredecessors().get(0) == blockOfFrom) {
            if (singleSuccessorBlock
                .getExceptionalSuccessors()
                .equals(blockOfFrom.getExceptionalSuccessors())) {
              singleSuccessorBlock
                  .getStmts()
                  .forEach(
                      k -> {
                        addNodeToBlock(blockOfFrom, k);
                      });
            }
          }
        }
      } else {
        blockOfFrom.clearSuccessorBlocks();
      }
    }
  }

  @Override
  public void setEdges(@Nonnull Stmt fromStmt, @Nonnull List<Stmt> targets) {
    if (fromStmt.getExpectedSuccessorCount() != targets.size()) {
      throw new IllegalArgumentException(
          "Size of Targets is not the amount of from's expected successors.");
    }
    MutableBasicBlock fromBlock = getOrCreateBlock(fromStmt);
    if (fromBlock.getTail() == fromStmt) {
      // cleanup existing edges
      fromBlock.clearSuccessorBlocks();
    }
    targets.forEach(target -> putEdge(fromStmt, target));
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
    final MutableBasicBlock mutableBasicBlock = stmtToBlock.get(stmt);
    if (mutableBasicBlock == null) {
      throw new IllegalArgumentException("stmt '" + stmt + "' does not exist in this StmtGraph!");
    }
    return new ForwardingBasicBlock<>(mutableBasicBlock);
  }

  @Nonnull
  @Override
  public StmtGraph<?> unmodifiableStmtGraph() {
    return new ForwardingStmtGraph<>(this);
  }

  public void setStartingStmt(@Nonnull Stmt startingStmt) {
    if (stmtToBlock.get(startingStmt) == null) {
      MutableBasicBlock block = stmtToBlock.get(startingStmt);
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
    MutableBasicBlock block = stmtToBlock.get(node);
    if (block == null) {
      throw new IllegalArgumentException(
          "Stmt '" + node + "' is not contained in the BlockStmtGraph");
    }

    if (node == block.getHead()) {
      List<MutableBasicBlock> predecessorBlocks = block.getPredecessors();
      List<Stmt> preds = new ArrayList<>(predecessorBlocks.size());
      predecessorBlocks.forEach(p -> preds.add(p.getTail()));
      return preds;
    } else {
      // argh indexOf.. possibly expensive..
      List<Stmt> stmts = block.getStmts();
      final int i = stmts.indexOf(node);
      // assert (stmts.size() > 0) : "no stmts in " + block + " " + block.hashCode();
      // assert (i > 0) : " stmt not found in " + block;
      return Collections.singletonList(stmts.get(i - 1));
    }
  }

  @Nonnull
  @Override
  public List<Stmt> exceptionalPredecessors(@Nonnull Stmt node) {

    final MutableBasicBlock block = stmtToBlock.get(node);
    if (block == null) {
      throw new IllegalArgumentException("Stmt is not in the StmtGraph.");
    }

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
        && ((JIdentityStmt<?>) head).getRightOp() instanceof JCaughtExceptionRef)) {
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

  public List<MutableBasicBlock> exceptionalPredecessorBlocks(@Nonnull MutableBasicBlock block) {

    Stmt head = block.getHead();
    if (!(head instanceof JIdentityStmt
        && ((JIdentityStmt<?>) head).getRightOp() instanceof JCaughtExceptionRef)) {
      // only an exception handler stmt can have exceptional predecessors
      return Collections.emptyList();
    }

    List<MutableBasicBlock> exceptionalPred = new ArrayList<>();
    for (MutableBasicBlock pBlock : block.getPredecessors()) {
      if (pBlock.getExceptionalSuccessors().containsValue(pBlock)) {
        exceptionalPred.add(pBlock);
      }
    }
    return exceptionalPred;
  }

  @Nonnull
  @Override
  public List<Stmt> successors(@Nonnull Stmt node) {
    MutableBasicBlock block = stmtToBlock.get(node);
    if (block == null) {
      throw new IllegalArgumentException(
          "Stmt '" + node + "' is not contained in the BlockStmtGraph");
    }

    if (node == block.getTail()) {
      List<MutableBasicBlock> successorBlocks = block.getSuccessors();
      List<Stmt> succs = new ArrayList<>(successorBlocks.size());
      successorBlocks.forEach(p -> succs.add(p.getHead()));
      return succs;
    } else {
      // argh indexOf.. possibly expensive..
      List<Stmt> stmts = block.getStmts();
      return Collections.singletonList(stmts.get(stmts.indexOf(node) + 1));
    }
  }

  @Nonnull
  @Override
  public Map<ClassType, Stmt> exceptionalSuccessors(@Nonnull Stmt node) {
    MutableBasicBlock block = stmtToBlock.get(node);
    if (block == null) {
      throw new IllegalArgumentException(
          "Stmt '" + node + "' is not contained in the BlockStmtGraph");
    }
    Map<ClassType, Stmt> map = new HashMap<>();
    for (Map.Entry<ClassType, MutableBasicBlock> b : block.getExceptionalSuccessors().entrySet()) {
      map.put(b.getKey(), b.getValue().getHead());
    }
    return map;
  }

  @Override
  public int inDegree(@Nonnull Stmt node) {
    MutableBasicBlock block = stmtToBlock.get(node);
    if (block == null) {
      throw new IllegalArgumentException(
          "Stmt '" + node + "' is not contained in the BlockStmtGraph");
    }

    if (node == block.getHead()) {
      return block.getPredecessors().size();
    } else {
      return 1;
    }
  }

  @Override
  public int outDegree(@Nonnull Stmt node) {
    MutableBasicBlock block = stmtToBlock.get(node);
    if (block == null) {
      throw new IllegalArgumentException(
          "Stmt '" + node + "' is not contained in the BlockStmtGraph");
    }

    if (node == block.getTail()) {
      return block.getSuccessors().size();
    } else {
      return 1;
    }
  }

  @Override
  public boolean hasEdgeConnecting(@Nonnull Stmt source, @Nonnull Stmt target) {
    MutableBasicBlock blockA = stmtToBlock.get(source);
    if (blockA == null) {
      throw new IllegalArgumentException(
          "source Stmt is not contained in the BlockStmtGraph: " + source);
    }

    if (source == blockA.getTail()) {
      MutableBasicBlock blockB = stmtToBlock.get(source);
      if (blockB == null) {
        throw new IllegalArgumentException(
            "target Stmt is not contained in the BlockStmtGraph: " + source);
      }
      return blockA.getSuccessors().stream()
          .anyMatch(
              successorBlock -> /*successorBlock == blockB && */
                  successorBlock.getHead() == target);
    } else {
      List<Stmt> stmtsA = blockA.getStmts();
      return stmtsA.get(stmtsA.indexOf(source) + 1) == target;
    }
  }

  /** Comparator which sorts the trap output in getTraps() */
  public Comparator<Trap> getTrapComparator(@Nonnull HashMap<Stmt, Integer> stmtsBlockIdx) {
    return (a, b) ->
        ComparisonChain.start()
            .compare(stmtsBlockIdx.get(a.getBeginStmt()), stmtsBlockIdx.get(b.getBeginStmt()))
            .compare(stmtsBlockIdx.get(a.getEndStmt()), stmtsBlockIdx.get(b.getEndStmt()))
            // [ms] would be nice to have the traps ordered by exception hierarchy as well
            .compare(a.getExceptionType().toString(), b.getExceptionType().toString())
            .result();
  }

  /** hint: little expensive getter - its more of a build/create */
  @Override
  public List<Trap> getTraps() {
    // [ms] try to incorporate it into the serialisation of jimple printing so the other half of
    // iteration information is not wasted..
    BlockGraphIteratorAndTrapAggregator it =
        new BlockGraphIteratorAndTrapAggregator(new MutableBasicBlock());
    // it.getTraps() is valid/completely build when the iterator is done.
    HashMap<Stmt, Integer> stmtsBlockIdx = new HashMap<>();
    int i = 0;
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
}
