package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface for control flow graphs on Jimple Stmts. Its a CFG
 *
 * @author Markus Schmidt
 */

// TODO: javadoc
public abstract class StmtGraph {

  @Nullable protected Stmt firstStmt;

  @Nullable
  public Stmt getStartingStmt() {
    return firstStmt;
  }

  @Nonnull
  public abstract Set<Stmt> nodes();

  @Nonnull
  public abstract List<Stmt> predecessors(@Nonnull Stmt node);

  @Nonnull
  public abstract List<Stmt> successors(@Nonnull Stmt node);

  public abstract int degree(@Nonnull Stmt node);

  public abstract int inDegree(@Nonnull Stmt node);

  public abstract int outDegree(@Nonnull Stmt node);

  public abstract boolean hasEdgeConnecting(@Nonnull Stmt nodeU, @Nonnull Stmt nodeV);
}
