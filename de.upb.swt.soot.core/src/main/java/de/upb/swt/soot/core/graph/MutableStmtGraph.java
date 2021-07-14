package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.List;
import javax.annotation.Nonnull;

public interface MutableStmtGraph extends StmtGraph {
  @Nonnull
  StmtGraph unmodifiableStmtGraph();

  void setStartingStmt(@Nonnull Stmt firstStmt);

  void setTraps(@Nonnull List<Trap> traps);

  void addNode(@Nonnull Stmt node);

  void replaceNode(@Nonnull Stmt oldStmt, @Nonnull Stmt newStmt);

  void removeNode(@Nonnull Stmt node);

  void putEdge(@Nonnull Stmt from, @Nonnull Stmt to);

  void setEdges(@Nonnull Stmt from, @Nonnull List<Stmt> targets);

  void removeEdge(@Nonnull Stmt from, @Nonnull Stmt to);
}
