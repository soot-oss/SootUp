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

  private final List<Stmt> stmts;
  private final List<ImmutableBasicBlock> blocks;

  public ImmutableBlockStmtGraph(@Nonnull StmtGraph<?> graph) {
    stmts = Lists.newArrayListWithExpectedSize(graph.nodes().size());
    // linearize..
    // TODO: add blocks in linearized order!
    for (Stmt stmt : graph) {
      stmts.add(stmt);
    }

    final List<? extends BasicBlock<?>> blocks = graph.getBlocks();
    this.blocks = new ArrayList<>(blocks.size());
    // TODO: copy
  }

  @Nullable
  @Override
  public Stmt getStartingStmt() {
    return stmts.isEmpty() ? null : stmts.get(0);
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
    return stmts;
  }

  @Nonnull
  @Override
  public List<ImmutableBasicBlock> getBlocks() {
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
    private final ImmutableStmtGraph graph;
    private final int startIdx;
    private final int endIdx;
    private final List<ImmutableBasicBlock> successors;
    private final List<ImmutableBasicBlock> predecessors;

    private ImmutableBasicBlock(
        ImmutableStmtGraph graph,
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
