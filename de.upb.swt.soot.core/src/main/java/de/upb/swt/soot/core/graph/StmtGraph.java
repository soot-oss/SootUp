package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.*;
import javax.annotation.Nonnull;

/**
 * Interface for control flow graphs on Jimple Stmts
 *
 * @author Markus Schmidt
 */
public interface StmtGraph {
  @Nonnull
  Set<Stmt> nodes();

  boolean isDirected();

  boolean allowsSelfLoops();

  @Nonnull
  Set<Stmt> adjacentNodes(@Nonnull Stmt node);

  @Nonnull
  Set<Stmt> predecessors(@Nonnull Stmt node);

  @Nonnull
  Set<Stmt> successors(@Nonnull Stmt node);

  int degree(@Nonnull Stmt node);

  int inDegree(@Nonnull Stmt node);

  int outDegree(@Nonnull Stmt node);

  boolean hasEdgeConnecting(@Nonnull Stmt nodeU, @Nonnull Stmt nodeV);
}
