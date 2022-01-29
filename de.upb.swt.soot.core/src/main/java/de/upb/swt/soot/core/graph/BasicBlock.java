package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.types.ClassType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

public interface BasicBlock {
  @Nonnull
  List<? extends BasicBlock> getPredecessors();

  @Nonnull
  List<? extends BasicBlock> getSuccessors();

  default List<? extends BasicBlock> getExceptionalPredecessors() {
    List<BasicBlock> exceptionalPredecessorBlocks = new ArrayList<>();
    for (BasicBlock pb : getPredecessors()) {
      if (pb.getExceptionalSuccessors().containsValue(this)) {
        exceptionalPredecessorBlocks.add(pb);
      }
    }
    return exceptionalPredecessorBlocks;
  }

  @Nonnull
  Map<? extends ClassType, ? extends BasicBlock> getExceptionalSuccessors();

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
}
