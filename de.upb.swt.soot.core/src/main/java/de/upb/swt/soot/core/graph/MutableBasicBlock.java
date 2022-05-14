package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.common.stmt.BranchingStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.types.ClassType;
import java.util.*;
import javax.annotation.Nonnull;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2021 Markus Schmidt
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

public class MutableBasicBlock implements BasicBlock<MutableBasicBlock> {
  @Nonnull private final List<MutableBasicBlock> predecessorBlocks = new ArrayList<>();
  @Nonnull private final List<MutableBasicBlock> successorBlocks = new ArrayList<>();

  @Nonnull
  private final Map<ClassType, MutableBasicBlock> exceptionalSuccessorBlocks =
      new LinkedHashMap<>();

  @Nonnull private List<Stmt> stmts = new ArrayList<>();

  public MutableBasicBlock() {}

  public void addStmt(@Nonnull Stmt stmt) {
    if (getStmtCount() > 0 && getTail() instanceof BranchingStmt) {
      throw new IllegalArgumentException(
          "Can't add another Stmt to a Block after a BranchingStmt.");
    }
    stmts.add(stmt);
  }

  public void removeStmt(@Nonnull Stmt stmt) {
    stmts.remove(stmt);
  }

  public void setStmts(@Nonnull List<Stmt> stmts) {
    this.stmts = stmts;
  }

  public void addPredecessorBlock(@Nonnull MutableBasicBlock block) {
    predecessorBlocks.add(block);
  }

  public void addSuccessorBlock(@Nonnull MutableBasicBlock block) {
    successorBlocks.add(block);
  }

  public void removePredecessorBlock(@Nonnull MutableBasicBlock b) {
    predecessorBlocks.remove(b);
  }

  public void removeSuccessorBlock(@Nonnull MutableBasicBlock b) {
    successorBlocks.remove(b);
  }

  public void addExceptionalSuccessorBlock(@Nonnull ClassType exception, MutableBasicBlock b) {
    exceptionalSuccessorBlocks.put(exception, b);
  }

  public void removeExceptionalSuccessorBlock(@Nonnull ClassType exception) {
    exceptionalSuccessorBlocks.remove(exception);
  }

  @Nonnull
  @Override
  public List<MutableBasicBlock> getPredecessors() {
    return Collections.unmodifiableList(predecessorBlocks);
  }

  @Nonnull
  @Override
  public List<MutableBasicBlock> getSuccessors() {
    return Collections.unmodifiableList(successorBlocks);
  }

  @Override
  public List<MutableBasicBlock> getExceptionalPredecessors() {
    throw new UnsupportedOperationException("not implemented.");
  }

  @Nonnull
  @Override
  public Map<ClassType, MutableBasicBlock> getExceptionalSuccessors() {
    return Collections.unmodifiableMap(exceptionalSuccessorBlocks);
  }

  @Nonnull
  @Override
  public List<Stmt> getStmts() {
    return Collections.unmodifiableList(stmts);
  }

  public int getStmtCount() {
    return stmts.size();
  }

  @Nonnull
  @Override
  public Stmt getHead() {
    if (stmts.size() < 1) {
      throw new IndexOutOfBoundsException("Cant get the head - this Block has no assigned Stmts.");
    }
    return stmts.get(0);
  }

  @Nonnull
  @Override
  public Stmt getTail() {
    int size = stmts.size();
    if (size < 1) {
      throw new IndexOutOfBoundsException("Cant get the tail - this Block has no assigned Stmts.");
    }
    return stmts.get(size - 1);
  }

  /**
   * splits a single MutableBasicBlock into two at splitIndex position, so that the Stmt at the
   * splitIdx is the Head of the second MutableBasicBlock. this method does not link the splitted
   * blocks.
   */
  public MutableBasicBlock splitBlockUnlinked(@Nonnull Stmt newTail, @Nonnull Stmt newHead) {
    int splitIdx = stmts.indexOf(newTail); // not cheap.
    if (splitIdx < 0) {
      throw new IllegalArgumentException(
          "Can not split by that Stmt - it is not contained in this Block.");
    }
    if (stmts.get(splitIdx + 1) != newHead) {
      throw new IllegalArgumentException("Can't split - the given Stmts are not connected.");
    }
    return splitBlockUnlinked(splitIdx + 1);
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

    // remove stmt references from current i.e. first block
    if (splitIdx < stmts.size()) {
      stmts.subList(splitIdx, stmts.size()).clear();
    }

    return secondBlock;
  }

  /**
   * splits a BasicBlock into first|second
   *
   * @param shouldBeNewHead if true: splitStmt is the Head of the second BasicBlock. if
   *     shouldBeNewHead is false splitStmt is the tail of the first BasicBlock
   * @param splitStmt the stmt which determines where to split the BasicBlock
   * @return BasicBlock with the second part of Stmts
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

    MutableBasicBlock newBlock = splitBlockUnlinked(splitIdx);
    successorBlocks.forEach(
        succBlock -> {
          // copy successors to the newBlock
          newBlock.addSuccessorBlock(succBlock);
          // and relink predecessors of the successors to newblock as well
          succBlock.removePredecessorBlock(this);
          succBlock.addPredecessorBlock(newBlock);
        });
    successorBlocks.clear();

    newBlock.addPredecessorBlock(this);
    addSuccessorBlock(newBlock);

    return newBlock;
  }

  public void copyExceptionalFlowFrom(MutableBasicBlock sourceBlock) {
    // copy trap info
    exceptionalSuccessorBlocks.putAll(sourceBlock.getExceptionalSuccessors());
  }

  public void clearSuccessorBlocks() {
    successorBlocks.forEach(b -> b.removePredecessorBlock(this));
    successorBlocks.clear();
  }

  public void clearExceptionalSuccessorBlocks() {
    exceptionalSuccessorBlocks.forEach((e, b) -> b.removePredecessorBlock(this));
    exceptionalSuccessorBlocks.clear();
  }

  public void clearPredecessorBlocks() {
    predecessorBlocks.forEach(b -> b.removeSuccessorBlock(this));
    predecessorBlocks.clear();
  }

  @Override
  public String toString() {
    return "Block " + getStmts();
  }
}

/*
class ImmmutableBasicBlock implements BasicBlock{

    @Nonnull private final List<BasicBlock> predecessorBlocks;
    @Nonnull private final List<BasicBlock> successorBlocks;
    @Nonnull private final List<Stmt> stmts;
    @Nonnull private final Map<ClassType, BasicBlock> exceptionalSuccessorBlocks;


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
