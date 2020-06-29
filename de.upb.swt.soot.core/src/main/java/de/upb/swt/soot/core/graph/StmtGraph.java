package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.common.stmt.*;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JSwitchStmt;
import java.util.*;
import javax.annotation.Nonnull;

/**
 * Interface for control flow graphs on Jimple Stmts. A StmtGraph is directed and connected (except
 * for traphandlers - those are not connected to the unexceptional flow). Its edges represent flows
 * between Stmts. If the edge starts in a branching Stmt there is a flow for each flow to the target
 * Stmt. This can include duplicate flows to the same target e.g. for JSwitchStmt, so that every
 * label has its own flow to a target.
 *
 * @author Markus Schmidt
 */
public abstract class StmtGraph {

  public abstract Stmt getStartingStmt();

  /** returns the nodes in this graph. */
  @Nonnull
  public abstract Set<Stmt> nodes();

  /** returns the ingoing flows to node as an ordered List. */
  @Nonnull
  public abstract List<Stmt> predecessors(@Nonnull Stmt node);

  /** returns the outgoing flows of node as ordered List. The List can have duplicate entries! */
  @Nonnull
  public abstract List<Stmt> successors(@Nonnull Stmt node);

  /** returns the amount of flows with node as source or target. */
  public abstract int degree(@Nonnull Stmt node);

  /** returns the amount of ingoing flows into node */
  public abstract int inDegree(@Nonnull Stmt node);

  /** returns the amount of flows that start from node */
  public abstract int outDegree(@Nonnull Stmt node);

  /** returns true if there is a flow between source and target */
  public abstract boolean hasEdgeConnecting(@Nonnull Stmt source, @Nonnull Stmt target);

  /** validates whether the each Stmt has the correct amount of outgoing flows. */
  public void validateStmtConnectionsInGraph() {
    for (Stmt stmt : nodes()) {

      final List<Stmt> successors = successors(stmt);
      final int successorCount = successors.size();
      if (stmt instanceof BranchingStmt) {

        for (Stmt target : successors) {
          if (target == stmt) {
            throw new RuntimeException(stmt + ": a Stmt cannot branch to itself.");
          }
        }

        if (stmt instanceof JSwitchStmt) {
          if (successorCount != ((JSwitchStmt) stmt).getValueCount()) {
            throw new RuntimeException(
                stmt
                    + ": size of outgoing flows (i.e. "
                    + successorCount
                    + ") does not match the amount of switch statements case labels (i.e. "
                    + ((JSwitchStmt) stmt).getValueCount()
                    + ").");
          }
        } else if (stmt instanceof JIfStmt) {
          if (successorCount != 2) {
            throw new IllegalStateException(
                stmt + ": must have '2' outgoing flow but has '" + successorCount + "'.");
          }
        } else if (stmt instanceof JGotoStmt) {
          if (successorCount != 1) {
            throw new RuntimeException(
                stmt + ": Goto must have '1' outgoing flow but has '" + successorCount + "'.");
          }
        }

      } else if (stmt instanceof JReturnStmt
          || stmt instanceof JReturnVoidStmt
          || stmt instanceof JThrowStmt) {
        if (successorCount != 0) {
          throw new RuntimeException(
              stmt + ": must have '0' outgoing flow but has '" + successorCount + "'.");
        }
      } else {
        if (successorCount != 1) {
          throw new RuntimeException(
              stmt + ": must have '1' outgoing flow but has '" + successorCount + "'.");
        }
      }
    }
  }
}
