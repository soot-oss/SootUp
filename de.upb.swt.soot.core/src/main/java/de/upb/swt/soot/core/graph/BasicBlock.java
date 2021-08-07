package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public interface BasicBlock {
  @Nonnull
  List<? extends BasicBlock> getPredecessors();

  @Nonnull
  List<? extends BasicBlock> getSuccessors();

  @Nonnull
  List<? extends BasicBlock>
      getExceptionalPredecessors(); // not necessary as an exceptionhandler is not called in an
  // unexceptional flow i.e. its the same as getPredecessors?

  @Nonnull
  List<? extends BasicBlock> getExceptionalSuccessors();

  @Nonnull
  List<Stmt> getStmts();

  int getStmtCount();

  @Nonnull
  Stmt getHead();

  @Nonnull
  Stmt getTail();

  @Nonnull
  List<? extends Trap> getTraps();
}

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
    predecessorBlocks.add(block);
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
  public List<? extends BasicBlock> getExceptionalPredecessors() {
    return exceptionalPredecessorBlocks;
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
    return stmts.get(0);
  }

  @Nonnull
  @Override
  public Stmt getTail() {
    int size = stmts.size();
    if (size < 1) {
      throw new IllegalStateException("Cant get a tail - this block has no assigned Stmts.");
    }
    return stmts.get(size - 1);
  }

  @Nonnull
  @Override
  public List<Trap> getTraps() {
    return traps;
  }

  public MutableBasicBlock splitBlockUnlinked(@Nonnull Stmt newTail, @Nonnull Stmt newHead) {
    int splitIdx = stmts.indexOf(newTail);
    if (splitIdx < 0) {
      throw new IllegalArgumentException("Stmt is not contained in this Block.");
    }
    if (stmts.get(splitIdx + 1) != newHead) {
      throw new IllegalArgumentException("Those Stmts are not connected.");
    }
    return splitBlockUnlinked(splitIdx);
  }

  protected MutableBasicBlock splitBlockUnlinked(int splitIdx) {
    MutableBasicBlock secondBlock = new MutableBasicBlock();

    // move stmts from current/ first into new second block
    secondBlock.setStmts(new ArrayList<>(stmts.size() - splitIdx + 1));
    for (int i = splitIdx; i < stmts.size(); i++) {
      secondBlock.addStmt(stmts.remove(i));
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
   * @return second half with splitStmt as the head of the second BasicBlock
   * @oaram splitStmt the stmt which determines where to split the BasicBlock
   */
  @Nonnull
  public MutableBasicBlock splitBlockLinked(@Nonnull Stmt splitStmt, boolean shouldBeNewHead) {

    int splitIdx = stmts.indexOf(splitStmt);
    if (splitIdx < 0) {
      throw new IllegalArgumentException("Stmt is not contained in this Block.");
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
