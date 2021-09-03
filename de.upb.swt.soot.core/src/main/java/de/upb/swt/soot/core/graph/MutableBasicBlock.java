package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

class MutableBasicBlock implements BasicBlock {
  @Nonnull private final List<MutableBasicBlock> predecessorBlocks = new ArrayList<>();
  @Nonnull private final List<MutableBasicBlock> successorBlocks = new ArrayList<>();

  @Nonnull private final List<MutableBasicBlock> exceptionalPredecessorBlocks = new ArrayList<>();
  @Nonnull private final List<MutableBasicBlock> exceptionalSuccessorBlocks = new ArrayList<>();

  @Nonnull private List<Stmt> stmts = new ArrayList<>();
  @Nonnull private List<Trap> traps = new ArrayList<>();

  public MutableBasicBlock() {}

  public void addStmt(@Nonnull Stmt stmt) {
    stmts.add(stmt);
  }

  public void removeStmt(@Nonnull Stmt stmt) {
    if (stmt == getHead()) {
      // TODO: [ms] whats intuitive? removing the flows to the block too? or is deleting a stmt
      // keeping the flows to it
      // is the answer it different if its the tail?
      predecessorBlocks.forEach(b -> b.removeSuccessorBlock(this));
      predecessorBlocks.clear();
    }
    if (stmt == getTail()) {
      // TODO: [ms] see question above..
      // switch, if, goto vs. usual stmt
      if (stmt.branches()) {
        successorBlocks.forEach(b -> b.removePredecessorBlock(this));
        successorBlocks.clear();
      }
    }
    stmts.remove(stmt);
  }

  public void setStmts(@Nonnull List<Stmt> stmts) {
    this.stmts = stmts;
  }

  public void addTrap(@Nonnull Trap trap) {
    traps.add(trap);
  }

  public void setTraps(@Nonnull List<Trap> traps) {
    this.traps = traps;
  }

  public void addPredecessorBlock(@Nonnull MutableBasicBlock block) {
    predecessorBlocks.add(block);
  }

  public void addSuccessorBlock(@Nonnull MutableBasicBlock block) {
    successorBlocks.add(block);
  }

  public void removePredecessorBlock(MutableBasicBlock b) {
    predecessorBlocks.remove(b);
  }

  public void removeSuccessorBlock(MutableBasicBlock b) {
    successorBlocks.remove(b);
  }

  @Nonnull
  @Override
  public List<MutableBasicBlock> getPredecessors() {
    return predecessorBlocks;
  }

  @Nonnull
  @Override
  public List<MutableBasicBlock> getSuccessors() {
    return successorBlocks;
  }

  @Nonnull
  @Override
  public List<? extends BasicBlock> getExceptionalSuccessors() {
    return exceptionalSuccessorBlocks;
  }

  @Nonnull
  @Override
  public List<Stmt> getStmts() {
    return stmts;
  }

  public int getStmtCount() {
    return stmts.size();
  }

  @Nonnull
  @Override
  public Stmt getHead() {
    /*if (stmts.size() < 1) {
      throw new IllegalStateException("Cant get a head - this block has no assigned Stmts.");
    }*/
    return stmts.get(0);
  }

  @Nonnull
  @Override
  public Stmt getTail() {
    int size = stmts.size();
    /*if (size < 1) {
      throw new IllegalStateException("Cant get a tail - this block has no assigned Stmts.");
    }*/
    return stmts.get(size - 1);
  }

  @Nonnull
  @Override
  public List<Trap> getTraps() {
    return traps;
  }

  /**
   * splits a single MutableBasicBlock into two at splitIndex position, so that the Stmt at the
   * splitIdx is the Head of the second MutableBasicBlock. this method does not link the splitted
   * blocks.
   */
  public MutableBasicBlock splitBlockUnlinked(@Nonnull Stmt newTail, @Nonnull Stmt newHead) {
    int splitIdx = stmts.indexOf(newTail);
    if (splitIdx < 0) {
      throw new IllegalArgumentException(
          "Can not split by that Stmt - it is not contained in this Block.");
    }
    if (stmts.get(splitIdx + 1) != newHead) {
      throw new IllegalArgumentException("Can not split - those Stmts are not connected.");
    }
    return splitBlockUnlinked(splitIdx);
  }

  /** @param splitIdx should be in [1, stmts.size()-1] */
  protected MutableBasicBlock splitBlockUnlinked(int splitIdx) {
    MutableBasicBlock secondBlock = new MutableBasicBlock();

    if (splitIdx < 1 || splitIdx >= stmts.size()) {
      throw new IndexOutOfBoundsException(
          "splitIdx makes no sense. please copy/create a new block.");
    }

    // copy stmts from current i.e. first block to new i.e. second block
    secondBlock.setStmts(new ArrayList<>(stmts.size() - splitIdx));
    for (int i = splitIdx; i < stmts.size(); i++) {
      secondBlock.addStmt(stmts.get(i));
    }

    // remove stmt refernces from current i.e. first block
    if (stmts.size() > splitIdx) {
      stmts.subList(splitIdx, stmts.size()).clear();
    }

    // copy traps
    secondBlock.setTraps(getTraps());

    return secondBlock;
  }

  /**
   * splits a BasicBlock into first|second
   *
   * @param shouldBeNewHead if true: splitStmt is the Head of the second BasicBlock if false
   *     splitStmt is the tail of the first BasicBlock
   * @param splitStmt the stmt which determines where to split the BasicBlock
   * @return second half with splitStmt as the head of the second BasicBlock
   */
  @Nonnull
  public MutableBasicBlock splitBlockLinked(@Nonnull Stmt splitStmt, boolean shouldBeNewHead) {

    int splitIdx = stmts.indexOf(splitStmt);
    if (splitIdx < 0) {
      throw new IllegalArgumentException("splitting Stmt is not contained in this Block.");
    }

    if (!shouldBeNewHead) {
      splitIdx++;
    }

    MutableBasicBlock secondBlock = splitBlockUnlinked(splitIdx);
    secondBlock.addPredecessorBlock(this);
    successorBlocks.forEach(secondBlock::addSuccessorBlock);
    successorBlocks.clear();
    successorBlocks.add(secondBlock);
    return secondBlock;
  }

  public void clearSuccessorBlocks() {
    successorBlocks.forEach(b -> b.removePredecessorBlock(this));
    successorBlocks.clear();
  }
}

/*
class ImmmutableBasicBlock implements BasicBlock{

    @Nonnull private final List<BasicBlock> predecessorBlocks;
    @Nonnull private final List<BasicBlock> successorBlocks;
    @Nonnull private final List<Stmt> stmts;
    @Nonnull private final List<Trap> traps;


    public ImmmutableBasicBlock(@Nonnull List<BasicBlock> predecessorBlocks, @Nonnull List<BasicBlock> successorBlocks, @Nonnull List<Stmt> stmts, @Nonnull List<Trap> traps) {
        this.predecessorBlocks = predecessorBlocks;
        this.successorBlocks = successorBlocks;
        this.stmts = stmts;
        this.traps = traps;
    }

    @Nonnull
    @Override
    public List<BasicBlock> getPredecessors() {
        return predecessorBlocks;
    }

    @Nonnull
    @Override
    public List<BasicBlock> getSuccessors() {
        return successorBlocks;
    }

    @Nonnull
    @Override
    public List<Stmt> getStmts() {
        return stmts;
    }

    @Nonnull
    @Override
    public Stmt getHead() {
        return null;
    }

    @Nonnull
    @Override
    public Stmt getTail() {
        return null;
    }

    @Nonnull
    @Override
    public List<Trap> getTraps() {
        return traps;
    }
}

// memory optimization ideas
class SingleStmtImmutableBasicBlock{}
class ExceptionlessImmutableBasicBlock{}
// would make use of the way Stmts are stored in the cfg / ArrayList<Stmt>
class NonBranchTargetImmutableBasicBlock{}
class NonBranchingImmutableBasicBlock{}
class NonBranchingOrBranchTargetImmutableBasicBlock{}
*/
