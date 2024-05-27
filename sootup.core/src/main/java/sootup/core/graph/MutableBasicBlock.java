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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.ClassType;

public interface MutableBasicBlock extends BasicBlock<MutableBasicBlock> {

  void addStmt(@Nonnull Stmt newStmt);

  void removeStmt(int idx);

  void removeStmt(@Nonnull Stmt stmt);

  void replaceStmt(Stmt oldStmt, Stmt newStmt);

  void addPredecessorBlock(@Nonnull MutableBasicBlock block);

  void linkSuccessor(int successorIdx, MutableBasicBlock blockB);

  boolean removePredecessorBlock(@Nonnull MutableBasicBlock b);

  void setSuccessorBlock(int successorIdx, @Nullable MutableBasicBlock block);

  void removePredecessorFromSuccessorBlock(@Nonnull MutableBasicBlock b);

  void linkExceptionalSuccessorBlock(@Nonnull ClassType exception, MutableBasicBlock b);

  void removeExceptionalSuccessorBlock(@Nonnull ClassType exception);

  @Nonnull
  MutableBasicBlockImpl splitBlockLinked(int splitIdx);

  void copyExceptionalFlowFrom(MutableBasicBlock sourceBlock);

  MutableBasicBlock splitBlockUnlinked(@Nonnull Stmt newTail, @Nonnull Stmt newHead);

  MutableBasicBlockImpl splitBlockUnlinked(int splitIdx);

  @Nonnull
  MutableBasicBlock splitBlockLinked(@Nonnull Stmt splitStmt, boolean shouldBeNewHead);

  void clearSuccessorBlocks();

  void clearExceptionalSuccessorBlocks();

  void clearPredecessorBlocks();

  List<Integer> replaceSuccessorBlock(
      @Nonnull MutableBasicBlock oldBlock, @Nullable MutableBasicBlock newBlock);

  boolean replacePredecessorBlock(MutableBasicBlock oldBlock, MutableBasicBlock newBlock);

  Collection<ClassType> collectExceptionalSuccessorBlocks(@Nonnull MutableBasicBlock block);

  @Nonnull
  @Override
  List<MutableBasicBlock> getPredecessors();

  @Nonnull
  @Override
  List<MutableBasicBlock> getSuccessors();

  @Override
  Map<ClassType, MutableBasicBlock> getExceptionalPredecessors();

  @Nonnull
  @Override
  Map<ClassType, MutableBasicBlock> getExceptionalSuccessors();

  int getStmtCount();

  @Nonnull
  @Override
  List<Stmt> getStmts();

  @Nonnull
  @Override
  Stmt getHead();

  @Nonnull
  @Override
  Stmt getTail();

  void replaceStmt(int idx, Stmt newStmt);
}
