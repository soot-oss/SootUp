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

  default List<? extends BasicBlock> getExceptionalPredecessorBlocks() {
    List<BasicBlock> exceptionalPredecessorBlocks = new ArrayList<>();
    for (BasicBlock pb : getPredecessors()) {
      if (pb.getExceptionalSuccessors().contains(this)) {
        exceptionalPredecessorBlocks.add(pb);
      }
    }
    return exceptionalPredecessorBlocks;
  }

  @Nonnull
  List<? extends BasicBlock> getExceptionalSuccessors();

  @Nonnull
  List<Stmt> getStmts();

  int getStmtCount();

  default boolean isEmpty() {
    return getStmtCount() > 0;
  }

  @Nonnull
  Stmt getHead();

  @Nonnull
  Stmt getTail();

  @Nonnull
  List<? extends Trap> getTraps();
}
