package sootup.core.graph;

import java.util.*;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.jimple.common.stmt.BranchingStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.ClassType;

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

public class MutableBasicBlockImpl implements MutableBasicBlock {
  @NonNull private final ArrayList<MutableBasicBlock> predecessorBlocks = new ArrayList<>();
  private MutableBasicBlock[] successorBlocks =
      new MutableBasicBlock[1]; // 1 := most probable amount of successors/elements

  @NonNull private final Map<ClassType, MutableBasicBlock> exceptionalSuccessorBlocks;

  @NonNull private final List<Stmt> stmts;

  public MutableBasicBlockImpl() {
    exceptionalSuccessorBlocks = new HashMap<>();
    stmts = new ArrayList<>();
  }

  public MutableBasicBlockImpl(List<Stmt> stmts, Map<ClassType, MutableBasicBlock> exceptionMap) {
    this.stmts = stmts;
    this.exceptionalSuccessorBlocks = exceptionMap;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof ForwardingBasicBlock) {
      return o.equals(this);
    }
    return super.equals(o);
  }

  @Override
  public void addStmt(@NonNull Stmt newStmt) {
    if (getStmtCount() > 0 && getTail() instanceof BranchingStmt) {
      throw new IllegalArgumentException(
          "Can't add another Stmt to a Block after a BranchingStmt.");
    }
    stmts.add(newStmt);
  }

  @Override
  public void removeStmt(int idx) {
    stmts.remove(idx);
  }

  @Override
  public void removeStmt(@NonNull Stmt stmt) {
    final int idx = stmts.indexOf(stmt);
    removeStmt(idx);
  }

  @Override
  public void replaceStmt(Stmt oldStmt, Stmt newStmt) {
    final int idx = stmts.indexOf(oldStmt);
    replaceStmt(idx, newStmt);
  }

  @Override
  public void replaceStmt(int idx, Stmt newStmt) {
    if (idx < 0) {
      throw new IllegalArgumentException("oldStmt does not exist in this Block!");
    }
    stmts.set(idx, newStmt);
  }

  protected void updateSuccessorContainer(@NonNull Stmt newStmt) {
    // we are not keeping/copying the currently stored flows as they are associated with a specific
    // stmt
    final int expectedSuccessorCount = newStmt.getExpectedSuccessorCount();
    if (expectedSuccessorCount != successorBlocks.length) {
      // will not happen that often as only the last item can have more (or less) than one successor
      // - n-1 items must have 1 successor as they are FallsThrough
      successorBlocks = new MutableBasicBlockImpl[expectedSuccessorCount];
    }
  }

  @Override
  public void addPredecessorBlock(@NonNull MutableBasicBlock block) {
    predecessorBlocks.add(block);
  }

  /**
   * makes blockA the predecessor of BlockB and BlockB the Successor of BlockA in a combined Method
   */
  @Override
  public void linkSuccessor(int successorIdx, MutableBasicBlock blockB) {
    setSuccessorBlock(successorIdx, blockB);
    blockB.addPredecessorBlock(this);
  }

  @Override
  public void setSuccessorBlock(int successorIdx, @Nullable MutableBasicBlock block) {
    updateSuccessorContainer(getTail());
    if (successorIdx >= successorBlocks.length) {
      throw new IndexOutOfBoundsException(
          "successorIdx '"
              + successorIdx
              + "' is out of bounds ('"
              + successorBlocks.length
              + " for "
              + getTail()
              + "')");
    }
    successorBlocks[successorIdx] = block;
  }

  @Override
  public boolean removePredecessorBlock(@NonNull MutableBasicBlock b) {
    return predecessorBlocks.remove(b);
  }

  @Override
  public void removeFromSuccessorBlocks(@NonNull MutableBasicBlock b) {
    for (int i = 0; i < successorBlocks.length; i++) {
      if (successorBlocks[i] == b) {
        successorBlocks[i] = null;
      }
    }
  }

  @Override
  public void linkExceptionalSuccessorBlock(@NonNull ClassType exception, MutableBasicBlock b) {
    exceptionalSuccessorBlocks.put(exception, b);
    b.addPredecessorBlock(this);
  }

  @Override
  public void removeExceptionalSuccessorBlock(@NonNull ClassType exception) {
    final MutableBasicBlock removedHandlerBlock = exceptionalSuccessorBlocks.remove(exception);
    if (removedHandlerBlock == null) {
      throw new IllegalArgumentException(
          "there is no handler for the given ClassType: " + exception);
    }
    removedHandlerBlock.removePredecessorBlock(this);
  }

  @Override
  public Collection<ClassType> collectExceptionalSuccessorBlocks(@NonNull MutableBasicBlock block) {
    // hint: there can be multiple Exceptions pointing to a handler
    Collection<ClassType> q = new ArrayDeque<>();
    for (Map.Entry<ClassType, MutableBasicBlock> entry : exceptionalSuccessorBlocks.entrySet()) {
      if (entry.getValue() == block) {
        q.add(entry.getKey());
      }
    }
    return q;
  }

  @NonNull
  @Override
  public List<MutableBasicBlock> getPredecessors() {
    return Collections.unmodifiableList(predecessorBlocks);
  }

  @NonNull
  @Override
  public List<MutableBasicBlock> getSuccessors() {
    if (stmts.isEmpty()) {
      throw new IllegalStateException(
          "Can't determine how many successors would be allowed as this block contains no Stmts(, yet).");
    }

    List<MutableBasicBlock> objects = new ArrayList<>(getTail().getExpectedSuccessorCount());
    Arrays.stream(successorBlocks).filter(Objects::nonNull).forEach(objects::add);
    return objects;
  }

  @Override
  public Map<ClassType, MutableBasicBlock> getExceptionalPredecessors() {
    final HashMap<ClassType, MutableBasicBlock> excPreds = new HashMap<>();
    getPredecessors()
        .forEach(
            (pb) -> {
              pb.getExceptionalSuccessors()
                  .forEach(
                      (exceptionType, handlerBlock) -> {
                        if (this == handlerBlock) {
                          excPreds.put(exceptionType, pb);
                        }
                      });
            });
    return excPreds;
  }

  @NonNull
  @Override
  public Map<ClassType, MutableBasicBlock> getExceptionalSuccessors() {
    return Collections.unmodifiableMap(exceptionalSuccessorBlocks);
  }

  @NonNull
  @Override
  public List<Stmt> getStmts() {
    return Collections.unmodifiableList(stmts);
  }

  @Override
  public int getStmtCount() {
    return stmts.size();
  }

  @NonNull
  @Override
  public Stmt getHead() {
    if (stmts.isEmpty()) {
      throw new IndexOutOfBoundsException("Cant get the head - this Block has no assigned Stmts.");
    }
    return stmts.get(0);
  }

  @NonNull
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
  @Override
  public MutableBasicBlockImpl splitBlockUnlinked(@NonNull Stmt newTail, @NonNull Stmt newHead) {
    int splitIdx = stmts.indexOf(newTail); // possibly expensive!
    if (splitIdx < 0) {
      throw new IllegalArgumentException(
          "Can not split by that Stmt - it is not contained in this Block.");
    }
    int newHeadsIdx = splitIdx + 1;
    if (stmts.get(newHeadsIdx) != newHead) {
      throw new IllegalArgumentException("Can't split - the given Stmts are not connected.");
    }
    return splitBlockUnlinked(newHeadsIdx);
  }

  /**
   * @param splitIdx should be in [1, stmts.size()-1]
   */
  @Override
  public MutableBasicBlockImpl splitBlockUnlinked(int splitIdx) {
    if (splitIdx < 1 || splitIdx >= stmts.size()) {
      throw new IndexOutOfBoundsException(
          "splitIdx " + splitIdx + "  makes no sense. please copy/create a new block.");
    }

    MutableBasicBlockImpl secondBlock =
        new MutableBasicBlockImpl(new ArrayList<>(stmts.size() - splitIdx), new HashMap<>());
    // copy stmts from current i.e. first block to new i.e. second block
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
   * splits a BasicBlock into first|second we know splitStmt must be a FallsThroughStmt
   *
   * @param shouldBeNewHead if true: splitStmt is the Head of the second BasicBlock. if
   *     shouldBeNewHead is false splitStmt is the tail of the first BasicBlock
   * @param splitStmt the stmt which determines where to split the BasicBlock
   * @return BasicBlock with the second part of Stmts
   */
  @Override
  @NonNull
  public MutableBasicBlockImpl splitBlockLinked(@NonNull Stmt splitStmt, boolean shouldBeNewHead) {
    int splitIdx = stmts.indexOf(splitStmt);
    if (splitIdx < 0) {
      throw new IllegalArgumentException("splitting Stmt is not contained in this Block.");
    }

    if (!shouldBeNewHead) {
      splitIdx++;
    }
    return splitBlockLinked(splitIdx);
  }

  @Override
  @NonNull
  public MutableBasicBlockImpl splitBlockLinked(int splitIdx) {

    MutableBasicBlockImpl newBlock = splitBlockUnlinked(splitIdx);

    for (int i = 0; i < successorBlocks.length; i++) {
      MutableBasicBlock succBlock = successorBlocks[i]; // copy successors to the newBlock
      if (succBlock == null) {
        continue;
      }
      newBlock.setSuccessorBlock(i, succBlock);
      // and relink predecessors of the successors to newblock as well
      succBlock.removePredecessorBlock(this);
      succBlock.addPredecessorBlock(newBlock);
    }
    successorBlocks = new MutableBasicBlockImpl[1];

    newBlock.addPredecessorBlock(this);
    newBlock.copyExceptionalFlowFrom(this);
    setSuccessorBlock(
        0, newBlock); // 0 as this can only be a block if the Stmts before the last Stmt are
    return newBlock;
  }

  @Override
  public void copyExceptionalFlowFrom(MutableBasicBlock sourceBlock) {
    // copy trap info
    exceptionalSuccessorBlocks.putAll(sourceBlock.getExceptionalSuccessors());
    exceptionalSuccessorBlocks.forEach((type, exBlock) -> exBlock.addPredecessorBlock(this));
  }

  @Override
  public void clearSuccessorBlocks() {
    Stream.of(successorBlocks)
        .filter(Objects::nonNull)
        .forEach(b -> b.removePredecessorBlock(this));
    successorBlocks = new MutableBasicBlockImpl[1];
  }

  @Override
  public void clearExceptionalSuccessorBlocks() {
    exceptionalSuccessorBlocks.values().forEach(b -> b.removePredecessorBlock(this));
    exceptionalSuccessorBlocks.clear();
  }

  @Override
  public void clearPredecessorBlocks() {
    Map<MutableBasicBlock, Collection<ClassType>> exceptionalFlowstoRemove = new HashMap<>();
    predecessorBlocks.forEach(
        pb -> {
          pb.removeFromSuccessorBlocks(this);
          exceptionalFlowstoRemove.put(pb, pb.collectExceptionalSuccessorBlocks(this));
        });

    exceptionalFlowstoRemove.forEach(
        (predBlock, cltypes) -> {
          for (ClassType type : cltypes) {
            predBlock.removeExceptionalSuccessorBlock(type);
          }
        });

    predecessorBlocks.clear();
  }

  @Override
  public String toString() {
    return "Block " + getStmts();
  }

  /** set newBlock to null to unset.. */
  @Override
  public List<Integer> replaceSuccessorBlock(
      @NonNull MutableBasicBlock oldBlock, @Nullable MutableBasicBlock newBlock) {
    List<Integer> found =
        new ArrayList<>(successorBlocks.length); // max.. almost definitely smaller
    for (int i = 0; i < successorBlocks.length; i++) {
      if (successorBlocks[i] == oldBlock) {
        successorBlocks[i] = newBlock;
        found.add(i);
      }
    }
    return found;
  }

  @Override
  public boolean replacePredecessorBlock(MutableBasicBlock oldBlock, MutableBasicBlock newBlock) {
    boolean found = false;

    for (ListIterator<MutableBasicBlock> iterator = predecessorBlocks.listIterator();
        iterator.hasNext(); ) {
      MutableBasicBlock predecessorBlock = iterator.next();
      if (predecessorBlock == oldBlock) {
        iterator.set(newBlock);
        found = true;
      }
    }
    return found;
  }
}
