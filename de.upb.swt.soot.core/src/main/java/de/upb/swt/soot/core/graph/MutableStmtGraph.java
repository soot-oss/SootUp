package de.upb.swt.soot.core.graph;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2020 Markus Schmidt
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

import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.types.ClassType;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

/** @author Markus Schmidt */
public abstract class MutableStmtGraph extends StmtGraph<MutableBasicBlock> {
  @Nonnull
  public abstract StmtGraph<?> unmodifiableStmtGraph();

  public abstract void setStartingStmt(@Nonnull Stmt firstStmt);

  public void setStartingStmtBlock(@Nonnull MutableBasicBlock firstBlock) {
    setStartingStmt(firstBlock.getHead());
  }

  public void addNode(@Nonnull Stmt node) {
    addNode(node, Collections.emptyList());
  }

  public abstract void addNode(@Nonnull Stmt node, @Nonnull List<ClassType> traps);

  // maybe refactor addBlock into MutableBlockStmtGraph..
  public abstract void addBlock(@Nonnull MutableBasicBlock block);

  // public abstract void replaceBlock(@Nonnull MutableBasicBlock oldBlock, @Nonnull
  // MutableBasicBlock newBlock);

  /** Modification of nodes (without manipulating any flows) */
  public abstract void replaceNode(@Nonnull Stmt oldStmt, @Nonnull Stmt newStmt);

  public abstract void removeNode(@Nonnull Stmt node);

  /** Modifications of unexceptional flows */
  public abstract void putEdge(@Nonnull Stmt from, @Nonnull Stmt to);

  public abstract void setEdges(@Nonnull Stmt from, @Nonnull List<Stmt> targets);

  public abstract void removeEdge(@Nonnull Stmt from, @Nonnull Stmt to);

  /** Modifications of exceptional flows */
  public abstract void clearExceptionalEdges(@Nonnull Stmt stmt);

  public abstract void addExceptionalEdge(
      @Nonnull Stmt stmt, @Nonnull ClassType exception, @Nonnull Stmt traphandlerStmt);

  public abstract void removeExceptionalEdge(@Nonnull Stmt stmt, @Nonnull ClassType exception);

  // FIXME: legacy/remove!
  @Deprecated
  public void setTraps(List<Trap> traps) {
    this.traps = traps;
  }

  protected boolean isMergeable(MutableBasicBlock firstBlock, MutableBasicBlock followingBlock) {
    if (firstBlock.getSuccessors().size() != 1
        || firstBlock.getSuccessors().get(0) != followingBlock) {
      return false;
    }
    if (followingBlock.getPredecessors().size() != 1
        || followingBlock.getPredecessors().get(0) != firstBlock) {
      return false;
    }
    if (!firstBlock.getExceptionalSuccessors().equals(followingBlock.getExceptionalSuccessors())) {
      // TODO: check if equals considers order
      return false;
    }
    return true;
  }

  /** merges Blocks of the Datastructure */
  protected void mergeBlocks(MutableBasicBlock firstBlock, MutableBasicBlock followingBlock) {
    // FIXME: implement

  }

  /** hints the Datastructure that two following blocks could be possibly merged */
  public boolean hintMergeBlocks(MutableBasicBlock firstBlock, MutableBasicBlock followingBlock) {
    final boolean mergeable = isMergeable(firstBlock, followingBlock);
    if (mergeable) {
      mergeBlocks(firstBlock, followingBlock);
    }
    return mergeable;
  }
}
