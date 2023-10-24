package sootup.core.graph;

import java.util.*;

/*
    @author Markus Schmidt
    Structure is like in old soot.use with care! not memory or runtime efficient!.

*

public class MutableStmtGraphList extends MutableStmtGraph {

  class IntervalTree<V> {
    // FIXME: currently not really a tree! replace with an actual interval tree implementation..!!!
    // addresses: overlapping ranges, duplicate startIdx/endIdx

    final List<V> backingData = new ArrayList<>();
    final List<Integer> starts = new ArrayList<>();

    // ends are not included int the range!
    final List<Integer> ends = new ArrayList<>();

    IntervalTree() {}

    <T> int binarySearchWithDuplicatesReturnLowestIndex(
        List<? extends Comparable<? super T>> data, T value) {
      int i = Collections.binarySearch(data, value);
      if (i < 0) {
        // key not found -> transform insertion point: (-(insertion point) - 1)
        i = -i;
      }
      while (i > 0 && data.get(i - 1) == value) {
        i--;
      }
      return i;
    }

    public void add(int from, int to, V value) {
      int newIdx = binarySearchWithDuplicatesReturnLowestIndex(starts, from);
      // insert ordered so that ends is ordered too
      while (newIdx < backingData.size() && starts.get(newIdx + 1) == from) {
        if (to > ends.get(newIdx)) {
          newIdx++;
        } else {
          break;
        }
      }
      if (newIdx < starts.size()) {
        starts.add(newIdx, from);
        ends.add(newIdx, to);
        backingData.add(newIdx, value);
      } else {
        starts.add(from);
        ends.add(to);
        backingData.add(value);
      }
    }

    / * * returns the applying ranges (traps) for the range; ordered by the (trap-) range starts *
    public List<V> computeContainedRangeValues(int value) {
      List<V> result = new ArrayList<>();
      for (int i = 0; i < starts.size() && starts.get(i) < value; i++) {
        if (value < ends.get(i)) {
          result.add(backingData.get(i));
        }
      }

      return result;
    }

    public boolean hitsStart(int idx) {
      return Collections.binarySearch(starts, idx) >= 0;
    }

    public boolean hitsEnd(int idx) {
      return ends.contains(idx);
    }
  }

  private final List<Stmt> stmts;
  private final List<Trap> traps;
  private final Map<BranchingStmt, List<Stmt>> stmtsBranchTargets;

  Map<Stmt, Integer> stmtIdx = new HashMap<>();
  private final IntervalTree<Trap> trapRange = new IntervalTree<>();

  public MutableStmtGraphList(
      @Nonnull List<Stmt> stmts,
      @Nonnull Map<BranchingStmt, List<Stmt>> stmtsBranchTargets,
      @Nonnull List<Trap> traps) {
    this.stmts = stmts;
    this.traps = traps;
    this.stmtsBranchTargets = stmtsBranchTargets;

    int idx = 0;
    for (Stmt stmt : stmts) {
      stmtIdx.put(stmt, idx++);
      // integrity..
      if (stmt instanceof BranchingStmt) {
        if (!stmtsBranchTargets.containsKey(stmt)) {
          // throw new IllegalArgumentException("BranchingStmt needs targets!");
          stmtsBranchTargets.put((BranchingStmt) stmt, new ArrayList<>());
        }
      }
    }

    // fill in exceptions
    for (Trap trap : traps) {
      trapRange.add(stmtIdx.get(trap.getBeginStmt()), stmtIdx.get(trap.getEndStmt()), trap);
    }
  }

  @Nonnull
  @Override
  public StmtGraph<?> unmodifiableStmtGraph() {
    return new ForwardingStmtGraph<>(this);
  }

  @Nonnull
  @Override
  public Iterator<Stmt> iterator() {
    return stmts.iterator();
  }

  @Override
  public void setStartingStmt(@Nonnull Stmt firstStmt) {
    if (stmts.isEmpty()) {
      stmts.add(firstStmt);
    } else {
      if (stmts.get(0) != firstStmt) {
        stmts.add(0, firstStmt);
      }
    }
  }

  @Override
  public void addNode(@Nonnull Stmt node, @Nonnull Map<ClassType, Stmt> traps) {
    final int newIdx = stmts.size();
    stmts.add(node);
    stmtIdx.put(node, newIdx);
    // FIXME: add traps
  }

  public void insertBefore(
      @Nonnull Stmt beforeStmt,
      @Nonnull List<Stmt> stmtsToAdd,
      @Nonnull Map<ClassType, Stmt> exceptionMap) {

    final Integer beforeNodeIdx = stmtIdx.get(beforeStmt);
    if (beforeNodeIdx == null) {
      throw new IllegalArgumentException("beforeNodeIdx does not exist in this StmtGraph.");
    }

    stmts.addAll(beforeNodeIdx, stmtsToAdd);

    // FIXME: add traps!
    /*
    exceptionMap.forEach( (type, handler) -> {
      final Integer fromIdx = stmtIdx.get();
      final Integer to = stmtIdx.get();
      trapRange.add(fromIdx, to, ) );
    });
     *

    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public void addBlock(@Nonnull List<Stmt> stmts, @Nonnull Map<ClassType, Stmt> traps) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public void replaceNode(@Nonnull Stmt oldStmt, @Nonnull Stmt newStmt) {
    // if possible please implement a better approach in your subclass
    final MutableBasicBlock blockOf = getBlockOf(oldStmt);
    if (blockOf == null) {
      throw new IllegalArgumentException("oldStmt does not exist in the StmtGraph!");
    }
    final Map<ClassType, Stmt> exceptionMap = new HashMap<>();
    blockOf
        .getExceptionalSuccessors()
        .forEach((type, handlerBlock) -> exceptionMap.put(type, handlerBlock.getHead()));
    insertBefore(oldStmt, Collections.singletonList(newStmt), exceptionMap);
    // removeNode(oldStmt);
    final Integer removedStmtIdx = stmtIdx.remove(oldStmt);
    stmts.remove(removedStmtIdx);
  }

  @Override
  public void removeNode(@Nonnull Stmt node) {
    final Integer nodeIdx = stmtIdx.remove(node);
    if (nodeIdx == null) {
      return;
    }
    stmts.remove(nodeIdx.intValue());
    if (node instanceof BranchingStmt) {
      stmtsBranchTargets.remove(node);
    }
    // TODO: adapt trap range as well
  }

  @Override
  public void putEdge(@Nonnull Stmt from, @Nonnull Stmt to) {
    if (from.branches()) {
      final List<Stmt> targets =
          stmtsBranchTargets.computeIfAbsent((BranchingStmt) from, n -> new ArrayList<>());
      if (from.getExpectedSuccessorCount() >= targets.size()) {
        throw new IllegalArgumentException(
            "The BranchingStmt from already has the amount of expected Successors.");
      }
      targets.add(to);
    } else {
      throw new UnsupportedOperationException(
          "the following Stmt of a non branching Stmt is determined by the next call to addNode.");
    }
  }

  @Override
  public void setEdges(@Nonnull Stmt from, @Nonnull List<Stmt> targets) {
    if (from.branches()) {
      final List<Stmt> bTargets =
          stmtsBranchTargets.computeIfAbsent((BranchingStmt) from, n -> new ArrayList<>());
      bTargets.addAll(targets);
    } else {
      throw new UnsupportedOperationException(
          "the following Stmt of a non branching Stmt is determined by the next call to addNode.");
    }
  }

  @Override
  public void removeEdge(@Nonnull Stmt from, @Nonnull Stmt to) {
    if (from.branches()) {
      final List<Stmt> bTargets =
          stmtsBranchTargets.computeIfAbsent((BranchingStmt) from, n -> new ArrayList<>());
      bTargets.remove(to);
    } else {
      throw new UnsupportedOperationException(
          "An edge to a fallsthrough Stmt can only be removed if the to Stmt is removed from the graph.");
    }
  }

  @Override
  public void clearExceptionalEdges(@Nonnull Stmt node) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public void addExceptionalEdge(
      @Nonnull Stmt stmt, @Nonnull ClassType exception, @Nonnull Stmt traphandlerStmt) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public void removeExceptionalEdge(@Nonnull Stmt node, @Nonnull ClassType exception) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public Stmt getStartingStmt() {
    return stmts.size() > 0 ? stmts.get(0) : null;
  }

  @Override
  public MutableBasicBlock getStartingStmtBlock() {
    return getBlockOf(getStartingStmt());
  }

  @Override
  public MutableBasicBlock getBlockOf(@Nonnull Stmt stmt) {
    // go to top..
    // iterate until end
    final Integer stmtsIdx = stmtIdx.get(stmt);
    if (stmtsIdx == null) {
      throw new IllegalArgumentException("Stmt not in the graph.");
    }
    // go up..
    int start = 0;
    for (int i = stmtsIdx - 1; i >= 0; i--) {
      final Stmt prevStmt = stmts.get(i);
      // prev is blockend
      if (!prevStmt.fallsThrough() || prevStmt.branches() || trapRange.hitsEnd(i)) {
        start = i + 1;
        break;
      }
      // current Stmt is blockstart
      if (isBranchTarget(stmt)
          || (stmt instanceof JIdentityStmt
              && ((JIdentityStmt) stmt).getRightOp() instanceof JCaughtExceptionRef)
          || trapRange.hitsStart(i)) {
        start = i;
        break;
      }
    }

    final MutableBasicBlock mutableBasicBlock = new MutableBasicBlock();
    for (int i = start; i < stmts.size(); i++) {
      Stmt itStmt = stmts.get(i);
      mutableBasicBlock.addStmt(itStmt);
      if (!itStmt.fallsThrough() || itStmt.branches() || trapRange.hitsEnd(i)) {
        break;
      }
    }
    return mutableBasicBlock;
  }

  @Nonnull
  @Override
  public Collection<Stmt> nodes() {
    return stmts;
  }

  @Nonnull
  @Override
  public List<MutableBasicBlock> getBlocks() {
    if (stmts.isEmpty()) {
      return Collections.emptyList();
    }

    List<MutableBasicBlock> blocks = new ArrayList<>();
    MutableBasicBlock block = new MutableBasicBlock();
    block.addStmt(stmts.get(0));

    for (int i = 0, stmtsSize = stmts.size(); i < stmtsSize; i++) {
      Stmt stmt = stmts.get(i);

      // trapbegin -> start
      // traphandler -> start
      // branchtarget -> start
      if (!block.isEmpty()) {
        if (isBranchTarget(stmt)
            || (stmt instanceof JIdentityStmt
                && ((JIdentityStmt) stmt).getRightOp() instanceof JCaughtExceptionRef)
            || trapRange.hitsStart(i)) {
          blocks.add(block);
          block = new MutableBasicBlock();
        }
      }

      block.addStmt(stmt);

      // FIXME: add exceptions!

      // branches -> end
      // trapend -> end
      // !fallsthrough -> end
      if (!stmt.fallsThrough() || stmt.branches() || trapRange.hitsEnd(i)) {
        blocks.add(block);
        block = new MutableBasicBlock();
      }
    }

    if (!block.isEmpty()) {
      blocks.add(block);
    }

    return blocks;
  }

  @Override
  public boolean containsNode(@Nonnull Stmt node) {
    return stmts.contains(node);
  }

  protected boolean isBranchTarget(@Nonnull Stmt stmt) {
    for (Map.Entry<BranchingStmt, List<Stmt>> branchMapping : stmtsBranchTargets.entrySet()) {
      for (Stmt branchTarget : branchMapping.getValue()) {
        if (branchTarget == stmt) {
          return true;
        }
      }
    }
    return false;
  }

  @Nonnull
  @Override
  public List<Stmt> predecessors(@Nonnull Stmt node) {
    List<Stmt> result = new ArrayList<>();
    final Integer nodeIdx = stmtIdx.get(node);
    if (nodeIdx == null) {
      throw new IllegalArgumentException("node does not exist");
    }

    // add previous stmt in list if exists and is neither branching stmt (which is covered in the
    // next step) nor sth that does not fall throug to this stmt
    if (nodeIdx > 0) {
      final Stmt prevStmt = stmts.get(nodeIdx - 1);
      if (prevStmt.fallsThrough() && !prevStmt.branches()) {
        result.add(prevStmt);
      }
    }

    // check branching stmts
    for (Map.Entry<BranchingStmt, List<Stmt>> branchMapping : stmtsBranchTargets.entrySet()) {
      for (Stmt branchTarget : branchMapping.getValue()) {
        if (branchTarget == node) {
          result.add(branchMapping.getKey());
        }
      }
    }
    return result;
  }

  @Nonnull
  @Override
  public List<Stmt> exceptionalPredecessors(@Nonnull Stmt node) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Nonnull
  @Override
  public List<Stmt> successors(@Nonnull Stmt node) {

    final Integer nodeIdx = stmtIdx.get(node);
    if (nodeIdx == null) {
      throw new IllegalArgumentException("node does not exist in this StmtGraph.");
    }

    if (node instanceof BranchingStmt) {
      return Collections.unmodifiableList(stmtsBranchTargets.get((BranchingStmt) node));
    } else {
      if (node.getExpectedSuccessorCount() != 0) {
        return Collections.singletonList(stmts.get(nodeIdx + 1));
      } else {
        return Collections.emptyList();
      }
    }
  }

  @Nonnull
  @Override
  public Map<ClassType, Stmt> exceptionalSuccessors(@Nonnull Stmt node) {
    final Integer nodeIdx = stmtIdx.get(node);
    if (nodeIdx == null) {
      throw new IllegalArgumentException("node does not exist in this StmtGraph.");
    }

    // build map from trap ranges
    final List<Trap> coveringTraps = trapRange.computeContainedRangeValues(nodeIdx);

    // remove duplicate/overlapping exceptions! the last trap with the same exception is the most
    // inner range that applies to the stmts -> iterate in reverse order
    Map<ClassType, Stmt> res = new HashMap<>();
    for (int i = coveringTraps.size() - 1; i >= 0; i--) {
      Trap coveringTrap = coveringTraps.get(i);
      if (!res.containsKey(coveringTrap.getExceptionType())) {
        res.put(coveringTrap.getExceptionType(), coveringTrap.getHandlerStmt());
      }
    }

    //    throw new UnsupportedOperationException("not implemented");
    return res;
  }

  @Override
  public int inDegree(@Nonnull Stmt node) {
    int result = 0;
    final Integer nodeIdx = stmtIdx.get(node);
    if (nodeIdx == null) {
      throw new IllegalArgumentException("node does not exist in this StmtGraph.");
    }

    // add previous stmt in list if exists and is neither branching stmt (which is covered in the
    // next step) nor sth that does not fall throug to this stmt
    if (nodeIdx > 0) {
      final Stmt prevStmt = stmts.get(nodeIdx - 1);
      if (prevStmt.fallsThrough() && !prevStmt.branches()) {
        result++;
      }
    }

    // add branching stmts
    for (List<Stmt> branchLists : stmtsBranchTargets.values()) {
      for (Stmt branchTarget : branchLists) {
        if (branchTarget == node) {
          result++;
        }
      }
    }
    return result;
  }

  @Override
  public int outDegree(@Nonnull Stmt node) {
    final Integer nodeIdx = stmtIdx.get(node);
    if (nodeIdx == null) {
      throw new IllegalArgumentException("node does not exist in this StmtGraph.");
    }

    if (node instanceof BranchingStmt) {
      return stmtsBranchTargets.get((BranchingStmt) node).size();
    } else {
      if (node.fallsThrough() && nodeIdx < stmts.size()) {
        return 1;
      }
    }
    return 0;
  }

  @Override
  public boolean hasEdgeConnecting(@Nonnull Stmt source, @Nonnull Stmt target) {

    final Integer sourceNodeIdx = stmtIdx.get(source);
    if (sourceNodeIdx == null) {
      throw new IllegalArgumentException("sourceNode does not exist in this StmtGraph.");
    }

    final Integer targetNodeIdx = stmtIdx.get(source);
    if (targetNodeIdx == null) {
      throw new IllegalArgumentException("targetNode does not exist in this StmtGraph.");
    }

    if (source.fallsThrough()) {
      if (targetNodeIdx == sourceNodeIdx + 1) {
        return true;
      }
    }

    if (source instanceof BranchingStmt) {
      final List<Stmt> branchTargets = stmtsBranchTargets.get(source);
      if (branchTargets == null) {
        // branching Targets for this nodes are currently not set!
        // TODO: or throw an exception?!
        return false;
      }
      return branchTargets.contains(target);
    }

    return false;
  }

  @Override
  public List<Trap> getTraps() {
    return traps;
  }

  public void setTraps(@Nonnull List<Trap> traps) {
    this.traps.clear();
    this.traps.addAll(traps);
  }
}
*/
