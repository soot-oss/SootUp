package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.*;
import javax.annotation.Nonnull;

/**
 * Interface for control flow graphs on Jimple Stmts
 *
 * @author Markus Schmidt
 */
public abstract class StmtGraph {

  public final boolean isDirected() {
    return true;
  }

  public final boolean allowsSelfLoops() {
    return false;
  }

  @Nonnull
  public abstract Set<Stmt> nodes();

  @Nonnull
  public abstract Set<Stmt> adjacentNodes(@Nonnull Stmt node);

  @Nonnull
  public abstract Set<Stmt> predecessors(@Nonnull Stmt node);

  @Nonnull
  public abstract Set<Stmt> successors(@Nonnull Stmt node);

  public abstract int degree(@Nonnull Stmt node);

  public abstract int inDegree(@Nonnull Stmt node);

  public abstract int outDegree(@Nonnull Stmt node);

  public abstract boolean hasEdgeConnecting(@Nonnull Stmt nodeU, @Nonnull Stmt nodeV);
}
