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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.ClassType;

/**
 * A truly immutable snapshot of a {@link ControlFlowGraph}. Constructed from any existing graph;
 * subsequent mutations to the source do not affect this instance.
 *
 * <p>Provides O(1) bidirectional stmt-to-integer-index mapping via {@link #indexOf(Stmt)} and
 * {@link #getStmt(int)}, enabling bitset-based analysis passes (liveness, reaching definitions,
 * etc.) without per-lookup HashMap overhead.
 *
 * <p>Construction uses a two-phase algorithm:
 *
 * <ol>
 *   <li>Linearise the source graph in RPO order into a flat {@code Stmt[]} array and create {@link
 *       ImmutableBasicBlock} shells.
 *   <li>Wire predecessor/successor/exceptional-successor links between the shells.
 * </ol>
 *
 * @author Markus Schmidt
 */
public class ImmutableBlockControlFlowGraph
    extends ControlFlowGraph<ImmutableBlockControlFlowGraph.ImmutableBasicBlock> {

  @Nullable private final Stmt startingStmt;

  /** Traversal-ordered (RPO) flat snapshot of every stmt in the graph — no duplicates. */
  private final Stmt[] stmts;

  /** O(1) stmt → position index; identity-keyed to match mutable-graph semantics. */
  private final IdentityHashMap<Stmt, Integer> stmtToIdx;

  /** O(1) stmt → containing block; identity-keyed. */
  private final IdentityHashMap<Stmt, ImmutableBasicBlock> stmtToBlock;

  /** Unmodifiable, RPO-ordered block list. */
  private final List<ImmutableBasicBlock> blocks;

  /**
   * Constructs an immutable snapshot of {@code source}. The source graph is only read during
   * construction; later changes to it do not affect this instance.
   */
  public ImmutableBlockControlFlowGraph(@NonNull ControlFlowGraph<? extends BasicBlock<?>> source) {

    // Use RPO-sorted blocks as the primary order, then append any blocks that are only reachable
    // via exceptional edges and therefore not included in the RPO traversal.
    List<? extends BasicBlock<?>> sortedBlocks = source.getBlocksSorted();
    Collection<? extends BasicBlock<?>> allBlocks = source.getBlocks();

    final List<BasicBlock<?>> srcBlocks;
    if (sortedBlocks.size() == allBlocks.size()) {
      srcBlocks = Collections.unmodifiableList(new ArrayList<>(sortedBlocks));
    } else {
      srcBlocks = new ArrayList<>(allBlocks.size());
      srcBlocks.addAll(sortedBlocks);
      // use identity-based set to detect blocks already in the sorted list
      Set<BasicBlock<?>> inSorted = Collections.newSetFromMap(new IdentityHashMap<>());
      inSorted.addAll(sortedBlocks);
      for (BasicBlock<?> b : allBlocks) {
        if (!inSorted.contains(b)) {
          srcBlocks.add(b);
        }
      }
    }

    // ── Phase 1: build flat stmt array + allocate ImmutableBasicBlock shells ──────────────────
    int totalStmts = 0;
    for (BasicBlock<?> b : srcBlocks) {
      totalStmts += b.getStmtCount();
    }

    Stmt[] flatStmts = new Stmt[totalStmts];
    IdentityHashMap<Stmt, Integer> idxMap = new IdentityHashMap<>(totalStmts * 2);
    IdentityHashMap<Stmt, ImmutableBasicBlock> s2b = new IdentityHashMap<>(totalStmts * 2);

    // temporary map: source block → its immutable counterpart
    IdentityHashMap<BasicBlock<?>, ImmutableBasicBlock> srcToImm =
        new IdentityHashMap<>(srcBlocks.size() * 2);

    ImmutableBasicBlock[] immBlockArr = new ImmutableBasicBlock[srcBlocks.size()];
    int cursor = 0;
    for (int b = 0; b < srcBlocks.size(); b++) {
      BasicBlock<?> srcBlock = srcBlocks.get(b);
      int blockStart = cursor;
      for (Stmt stmt : srcBlock.getStmts()) {
        flatStmts[cursor] = stmt;
        idxMap.put(stmt, cursor);
        cursor++;
      }
      int blockEnd = cursor - 1; // inclusive
      ImmutableBasicBlock ib = new ImmutableBasicBlock(blockStart, blockEnd);
      immBlockArr[b] = ib;
      srcToImm.put(srcBlock, ib);
      for (int i = blockStart; i <= blockEnd; i++) {
        s2b.put(flatStmts[i], ib);
      }
    }

    // ── Phase 2: wire predecessor / successor / exceptional-successor links ───────────────────
    for (int b = 0; b < srcBlocks.size(); b++) {
      BasicBlock<?> srcBlock = srcBlocks.get(b);
      ImmutableBasicBlock ib = immBlockArr[b];

      List<ImmutableBasicBlock> preds = new ArrayList<>();
      for (BasicBlock<?> pred : srcBlock.getPredecessors()) {
        ImmutableBasicBlock immPred = srcToImm.get(pred);
        if (immPred != null) {
          preds.add(immPred);
        }
      }

      List<ImmutableBasicBlock> succs = new ArrayList<>();
      for (BasicBlock<?> succ : srcBlock.getSuccessors()) {
        ImmutableBasicBlock immSucc = srcToImm.get(succ);
        if (immSucc != null) {
          succs.add(immSucc);
        }
      }

      Map<ClassType, ImmutableBasicBlock> exSuccs = new HashMap<>();
      srcBlock
          .getExceptionalSuccessors()
          .forEach(
              (type, block) -> {
                ImmutableBasicBlock immBlock = srcToImm.get(block);
                if (immBlock != null) {
                  exSuccs.put(type, immBlock);
                }
              });

      ib.setLinks(
          Collections.unmodifiableList(preds),
          Collections.unmodifiableList(succs),
          Collections.unmodifiableMap(exSuccs));
    }

    this.startingStmt = source.getStartingStmt();
    this.stmts = flatStmts;
    this.stmtToIdx = idxMap;
    this.stmtToBlock = s2b;
    this.blocks = Collections.unmodifiableList(Arrays.asList(immBlockArr));
  }

  // ── New public API: O(1) bidirectional stmt ↔ index mapping ───────────────────────────────

  /**
   * Returns the traversal-order position of {@code stmt} in this graph's flat statement array. O(1)
   * — identity-based IdentityHashMap lookup.
   *
   * @throws NoSuchElementException if {@code stmt} is not contained in this graph
   */
  public int indexOf(@NonNull Stmt stmt) {
    Integer idx = stmtToIdx.get(stmt);
    if (idx == null) {
      throw new NoSuchElementException(
          "Stmt not contained in this ImmutableBlockControlFlowGraph: " + stmt);
    }
    return idx;
  }

  /**
   * Returns the stmt at the given traversal-order index. O(1) array access.
   *
   * @throws ArrayIndexOutOfBoundsException if {@code index} is out of range
   */
  @NonNull
  public Stmt getStmt(int index) {
    return stmts[index];
  }

  /** Total number of stmts in this graph — equals the length of the internal flat array. */
  public int getNodeCount() {
    return stmts.length;
  }

  // ── ControlFlowGraph abstract method implementations ──────────────────────────────────────

  @Override
  @Nullable
  public Stmt getStartingStmt() {
    return startingStmt;
  }

  @Override
  @Nullable
  public BasicBlock<?> getStartingStmtBlock() {
    return startingStmt == null ? null : stmtToBlock.get(startingStmt);
  }

  @Override
  @NonNull
  public List<BasicBlock<?>> getTailStmtBlocks() {
    List<BasicBlock<?>> tails = new ArrayList<>();
    for (ImmutableBasicBlock b : blocks) {
      if (stmts[b.endIdx].getExpectedSuccessorCount() == 0) {
        tails.add(b);
      }
    }
    return tails;
  }

  @Override
  @NonNull
  public Collection<Stmt> getNodes() {
    return Collections.unmodifiableList(Arrays.asList(stmts));
  }

  @Override
  @NonNull
  public List<Stmt> getStmts() {
    return Collections.unmodifiableList(Arrays.asList(stmts));
  }

  @Override
  @NonNull
  public Iterator<Stmt> iterator() {
    return Arrays.asList(stmts).iterator();
  }

  @Override
  @NonNull
  public Collection<ImmutableBasicBlock> getBlocks() {
    return blocks;
  }

  @Override
  @NonNull
  public List<ImmutableBasicBlock> getBlocksSorted() {
    return blocks;
  }

  @Override
  @NonNull
  public ImmutableBasicBlock getBlockOf(@NonNull Stmt stmt) {
    ImmutableBasicBlock block = stmtToBlock.get(stmt);
    if (block == null) {
      throw new IllegalArgumentException(
          "Stmt '" + stmt + "' does not exist in this ImmutableBlockControlFlowGraph");
    }
    return block;
  }

  @Override
  public boolean containsNode(@NonNull Stmt node) {
    return stmtToIdx.containsKey(node);
  }

  @Override
  @NonNull
  public List<Stmt> predecessors(@NonNull Stmt node) {
    ImmutableBasicBlock block = getBlockOf(node);
    int idx = stmtToIdx.get(node);
    if (idx == block.startIdx) {
      // node is the head: predecessors are the tails of each predecessor block
      List<ImmutableBasicBlock> predBlocks = block.getPredecessors();
      List<Stmt> preds = new ArrayList<>(predBlocks.size());
      for (ImmutableBasicBlock pred : predBlocks) {
        preds.add(stmts[pred.endIdx]);
      }
      return preds;
    }
    // node is not the head: exactly one predecessor (the preceding stmt in this block)
    return Collections.singletonList(stmts[idx - 1]);
  }

  @Override
  @NonNull
  public List<Stmt> exceptionalPredecessors(@NonNull Stmt node) {
    ImmutableBasicBlock block = getBlockOf(node);
    int idx = stmtToIdx.get(node);
    // only a block head that is a catch handler can have exceptional predecessors
    if (idx != block.startIdx) {
      return Collections.emptyList();
    }
    Stmt head = stmts[block.startIdx];
    if (!(head instanceof JIdentityStmt
        && ((JIdentityStmt) head).getRightOp() instanceof JCaughtExceptionRef)) {
      return Collections.emptyList();
    }
    List<Stmt> result = new ArrayList<>();
    for (ImmutableBasicBlock pred : block.getPredecessors()) {
      if (pred.getExceptionalSuccessors().containsValue(block)) {
        for (int i = pred.startIdx; i <= pred.endIdx; i++) {
          result.add(stmts[i]);
        }
      }
    }
    return result;
  }

  @Override
  @NonNull
  public List<Stmt> successors(@NonNull Stmt node) {
    ImmutableBasicBlock block = getBlockOf(node);
    int idx = stmtToIdx.get(node);
    if (idx == block.endIdx) {
      // node is the tail: successors are the heads of each successor block
      List<ImmutableBasicBlock> succBlocks = block.getSuccessors();
      List<Stmt> succs = new ArrayList<>(succBlocks.size());
      for (ImmutableBasicBlock succ : succBlocks) {
        succs.add(stmts[succ.startIdx]);
      }
      return succs;
    }
    // node is not the tail: exactly one successor (the next stmt in this block)
    return Collections.singletonList(stmts[idx + 1]);
  }

  @Override
  @NonNull
  public Map<ClassType, Stmt> exceptionalSuccessors(@NonNull Stmt node) {
    ImmutableBasicBlock block = getBlockOf(node);
    Map<ClassType, ImmutableBasicBlock> exSuccs = block.getExceptionalSuccessors();
    if (exSuccs.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<ClassType, Stmt> result = new HashMap<>(exSuccs.size());
    exSuccs.forEach((type, b) -> result.put(type, stmts[b.startIdx]));
    return result;
  }

  @Override
  public int inDegree(@NonNull Stmt node) {
    ImmutableBasicBlock block = getBlockOf(node);
    int idx = stmtToIdx.get(node);
    return idx == block.startIdx ? block.getPredecessors().size() : 1;
  }

  @Override
  public int outDegree(@NonNull Stmt node) {
    ImmutableBasicBlock block = getBlockOf(node);
    int idx = stmtToIdx.get(node);
    return idx == block.endIdx ? block.getSuccessors().size() : 1;
  }

  @Override
  public boolean hasEdgeConnecting(@NonNull Stmt source, @NonNull Stmt target) {
    for (Stmt s : successors(source)) {
      if (s == target) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void removeExceptionalFlowFromAllBlocks(
      ClassType exceptionType, Stmt exceptionHandlerStmt) {
    throw new UnsupportedOperationException(
        "ImmutableBlockControlFlowGraph does not support mutation");
  }

  // ── Inner class: ImmutableBasicBlock ──────────────────────────────────────────────────────

  /**
   * An immutable basic block backed by a range [{@link #startIdx}, {@link #endIdx}] of the
   * enclosing graph's flat statement array. No separate statement list is stored.
   */
  public class ImmutableBasicBlock implements BasicBlock<ImmutableBasicBlock> {

    /** First stmt index in the enclosing graph's {@code stmts[]} array (inclusive). */
    final int startIdx;

    /** Last stmt index in the enclosing graph's {@code stmts[]} array (inclusive). */
    final int endIdx;

    private List<ImmutableBasicBlock> predecessors;
    private List<ImmutableBasicBlock> successors;
    private Map<ClassType, ImmutableBasicBlock> exceptionalSuccessors;

    private ImmutableBasicBlock(int startIdx, int endIdx) {
      this.startIdx = startIdx;
      this.endIdx = endIdx;
    }

    /**
     * Called exactly once from the enclosing constructor during Phase 2 to wire block links. The
     * passed collections must already be unmodifiable.
     */
    void setLinks(
        List<ImmutableBasicBlock> predecessors,
        List<ImmutableBasicBlock> successors,
        Map<ClassType, ImmutableBasicBlock> exceptionalSuccessors) {
      this.predecessors = predecessors;
      this.successors = successors;
      this.exceptionalSuccessors = exceptionalSuccessors;
    }

    @NonNull
    @Override
    public List<ImmutableBasicBlock> getPredecessors() {
      return predecessors;
    }

    @NonNull
    @Override
    public List<ImmutableBasicBlock> getSuccessors() {
      return successors;
    }

    @NonNull
    @Override
    public Map<ClassType, ImmutableBasicBlock> getExceptionalPredecessors() {
      // Computed on demand: predecessor blocks whose exceptional successors include this block.
      Map<ClassType, ImmutableBasicBlock> result = new HashMap<>();
      for (ImmutableBasicBlock pred : predecessors) {
        pred.getExceptionalSuccessors()
            .forEach(
                (type, handler) -> {
                  if (handler == this) {
                    result.put(type, pred);
                  }
                });
      }
      return result;
    }

    @NonNull
    @Override
    public Map<ClassType, ImmutableBasicBlock> getExceptionalSuccessors() {
      return exceptionalSuccessors;
    }

    /**
     * Returns an unmodifiable list view of the stmts in this block. Backed directly by the
     * enclosing graph's flat array — no copy.
     */
    @NonNull
    @Override
    public List<Stmt> getStmts() {
      return Collections.unmodifiableList(
          Arrays.asList(ImmutableBlockControlFlowGraph.this.stmts).subList(startIdx, endIdx + 1));
    }

    @Override
    public int getStmtCount() {
      return endIdx - startIdx + 1;
    }

    @NonNull
    @Override
    public Stmt getHead() {
      return ImmutableBlockControlFlowGraph.this.stmts[startIdx];
    }

    @NonNull
    @Override
    public Stmt getTail() {
      return ImmutableBlockControlFlowGraph.this.stmts[endIdx];
    }

    @Override
    public String toString() {
      return "ImmutableBlock" + getStmts();
    }
  }
}
