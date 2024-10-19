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

import com.google.common.collect.Lists;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import sootup.core.jimple.basic.Trap;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.ClassType;

// FIXME: implement!
public class ImmutableBlockStmtGraph
    extends StmtGraph<ImmutableBlockStmtGraph.ImmutableBasicBlock> {

  private final List<ImmutableBasicBlock> blocks;
  private final Map<Stmt, ImmutableBasicBlock> stmtToBlock = new HashMap<>();

  public ImmutableBlockStmtGraph(@Nonnull MutableStmtGraph graph) {

    final List<? extends BasicBlock<?>> mblocks = graph.getBlocksSorted();
    blocks = Lists.newArrayListWithExpectedSize(mblocks.size());
    for (BasicBlock<?> block : mblocks) {
      /* TODO: link predecessors/successors as well..
      // final ImmutableBasicBlock ib = new ImmutableBasicBlock();
      block.getStmts().forEach(stmt -> stmtToBlock.put(stmt, ib));

      blocks.add(ib);*/
    }
  }

  @Nullable
  @Override
  public Stmt getStartingStmt() {
    return blocks.isEmpty() ? null : blocks.get(0).getHead();
  }

  @Override
  public BasicBlock<?> getStartingStmtBlock() {
    throw new UnsupportedOperationException("Not implemented yet!");
  }

  @Override
  public BasicBlock<ImmutableBasicBlock> getBlockOf(@Nonnull Stmt stmt) {
    throw new UnsupportedOperationException("Not implemented yet!");
  }

  @Nonnull
  @Override
  public List<Stmt> getNodes() {
    int size = 0;
    for (ImmutableBasicBlock block : blocks) {
      size += block.getStmtCount();
    }
    final ArrayList<Stmt> stmts = new ArrayList<>(size);
    for (ImmutableBasicBlock block : blocks) {
      stmts.addAll(block.getStmts());
    }
    return stmts;
  }

  @Nonnull
  @Override
  public Collection<ImmutableBasicBlock> getBlocks() {
    return blocks;
  }

  @Nonnull
  @Override
  public List<? extends BasicBlock<?>> getBlocksSorted() {
    return blocks;
  }

  @Override
  public boolean containsNode(@Nonnull Stmt node) {
    throw new UnsupportedOperationException("Not implemented yet!");
  }

  @Nonnull
  @Override
  public List<Stmt> predecessors(@Nonnull Stmt node) {
    throw new UnsupportedOperationException("Not implemented yet!");
  }

  @Nonnull
  @Override
  public List<Stmt> exceptionalPredecessors(@Nonnull Stmt node) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Nonnull
  @Override
  public List<Stmt> successors(@Nonnull Stmt node) {
    throw new UnsupportedOperationException("Not implemented yet!");
  }

  @Nonnull
  @Override
  public Map<ClassType, Stmt> exceptionalSuccessors(@Nonnull Stmt node) {
    // FIXME! implement
    throw new RuntimeException("not implemented");
  }

  @Override
  public int inDegree(@Nonnull Stmt node) {
    throw new UnsupportedOperationException("Not implemented yet!");
  }

  @Override
  public int outDegree(@Nonnull Stmt node) {
    throw new UnsupportedOperationException("Not implemented yet!");
  }

  @Override
  public boolean hasEdgeConnecting(@Nonnull Stmt source, @Nonnull Stmt target) {
    throw new UnsupportedOperationException("Not implemented yet!");
  }

  @Nonnull
  @Override
  public List<Trap> buildTraps() {
    throw new UnsupportedOperationException("Not implemented yet!");
  }

  @Override
  public void removeExceptionalFlowFromAllBlocks(ClassType classType, Stmt exceptionHandlerStmt) {
    throw new UnsupportedOperationException("Not implemented yet!");
  }

  @Nonnull
  @Override
  public Iterator<Stmt> iterator() {
    return new Iterator<Stmt>() {
      final Iterator<ImmutableBasicBlock> blockIt = blocks.iterator();
      Iterator<Stmt> stmtIt = blockIt.next().getStmts().iterator();

      @Override
      public boolean hasNext() {
        // hint: there are no empty blocks!
        return stmtIt.hasNext() || blockIt.hasNext();
      }

      @Override
      public Stmt next() {
        if (stmtIt.hasNext()) {
          return stmtIt.next();
        }
        stmtIt = blockIt.next().getStmts().iterator();
        return stmtIt.next();
      }
    };
  }

  public class ImmutableBasicBlock implements BasicBlock<ImmutableBasicBlock> {
    private final MutableBasicBlock graph;
    private final int startIdx;
    private final int endIdx;
    private final List<ImmutableBasicBlock> successors;
    private final List<ImmutableBasicBlock> predecessors;

    private ImmutableBasicBlock(
        MutableBasicBlock graph,
        int startIdx,
        int endIdx,
        List<ImmutableBasicBlock> successors,
        List<ImmutableBasicBlock> predecessors) {
      this.graph = graph;
      this.startIdx = startIdx;
      this.endIdx = endIdx;
      this.successors = successors;
      this.predecessors = predecessors;
    }

    @Nonnull
    @Override
    public List<ImmutableBasicBlock> getPredecessors() {
      return predecessors;
    }

    @Nonnull
    @Override
    public List<ImmutableBasicBlock> getSuccessors() {
      return successors;
    }

    @Override
    @Nonnull
    public Map<ClassType, ImmutableBasicBlock> getExceptionalPredecessors() {
      throw new UnsupportedOperationException("not implemented yet");
    }

    @Nonnull
    @Override
    public Map<? extends ClassType, ImmutableBasicBlock> getExceptionalSuccessors() {
      throw new UnsupportedOperationException("not implemented yet");
    }

    @Nonnull
    @Override
    public List<Stmt> getStmts() {
      throw new UnsupportedOperationException();
    }

    @Override
    public int getStmtCount() {
      return endIdx - startIdx + 1;
    }

    @Nonnull
    @Override
    public Stmt getHead() {
      throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public Stmt getTail() {
      throw new UnsupportedOperationException();
    }
  }
}
