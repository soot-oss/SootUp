package de.upb.swt.soot.core.graph;

import com.google.common.collect.Lists;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.types.ClassType;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// FIXME: implement
public class ImmutableBlockStmtGraph extends StmtGraph {

  private final List<ImmutableBasicBlock> blocks;
  private final Map<Stmt, ImmutableBasicBlock> stmtToBlock = new HashMap<>();

  public ImmutableBlockStmtGraph(@Nonnull MutableStmtGraph graph) {

    final List<MutableBasicBlock> mblocks = graph.getBlocksSorted();
    blocks = Lists.newArrayListWithExpectedSize(mblocks.size());
    for (MutableBasicBlock block : mblocks) {
      // TODO: link predecessors/successors as well..
      final ImmutableBasicBlock ib = new ImmutableBasicBlock();
      block.getStmts().forEach(stmt -> stmtToBlock.put(stmt, ib));
      blocks.add(ib);
    }
  }

  @Nullable
  @Override
  public Stmt getStartingStmt() {
    return blocks.isEmpty() ? null : blocks.get(0).getHead();
  }

  @Override
  public BasicBlock<ImmutableBasicBlock> getStartingStmtBlock() {
    throw new UnsupportedOperationException("Not implemented yet!");
  }

  @Override
  public BasicBlock<ImmutableBasicBlock> getBlockOf(@Nonnull Stmt stmt) {
    throw new UnsupportedOperationException("Not implemented yet!");
  }

  @Nonnull
  @Override
  public List<Stmt> nodes() {
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
  public List<ImmutableBasicBlock> getBlocksSorted() {
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
  public List<Trap> getTraps() {
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

  private class ImmutableBasicBlock implements BasicBlock<ImmutableBasicBlock> {
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
    public List<ImmutableBasicBlock> getExceptionalPredecessors() {
      return null;
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
