package sootup.core.graph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2020 Markus Schmidt
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

import java.util.*;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.common.stmt.BranchingStmt;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.ClassType;

/**
 * @author Markus Schmidt
 *     <p>performance suggestions for multiple operations on sequential Stmts: addNode():
 *     top-&gt;down removeNode(s): bottom-&gt;up as then there is no need for copying inside the
 *     MutableBasicBlock
 */
public abstract class MutableStmtGraph extends StmtGraph<MutableBasicBlock> {
  @NonNull
  public abstract StmtGraph<?> unmodifiableStmtGraph();

  public abstract void setStartingStmt(@NonNull Stmt firstStmt);

  /** inserts a "stmt" into the StmtGraph */
  public void addNode(@NonNull Stmt stmt) {
    addNode(stmt, Collections.emptyMap());
  }

  /** inserts a "stmt" with exceptional flows "traps" into the StmtGraph */
  public abstract void addNode(@NonNull Stmt stmt, @NonNull Map<ClassType, Stmt> traps);

  /** creates a whole BasicBlock with the details from the parameters */
  public abstract void addBlock(@NonNull List<Stmt> stmts, @NonNull Map<ClassType, Stmt> traps);

  public abstract void removeBlock(BasicBlock<?> block);

  /**
   * creates a whole BasicBlock which contains the sequence of (n-1)*fallsthrough()-stmt + optional
   * a non-fallsthrough() stmt at the end of the list
   */
  public void addBlock(@NonNull List<Stmt> stmts) {
    addBlock(stmts, Collections.emptyMap());
  }

  /**
   * Modification of stmts (without manipulating any flows; possible assigned exceptional flows stay
   * the same as well)
   */
  public abstract void replaceNode(@NonNull Stmt oldStmt, @NonNull Stmt newStmt);

  public abstract BasicBlock<?> insertBefore(
      @NonNull Stmt beforeStmt,
      @NonNull List<FallsThroughStmt> stmts,
      @NonNull Map<ClassType, Stmt> exceptionMap);

  public abstract BasicBlock<?> insertAfter(
      @NonNull Stmt afterStmt,
      @NonNull List<FallsThroughStmt> stmts,
      @NonNull Map<ClassType, Stmt> exceptionMap);

  /**
   * inserts the "newStmt" before the position of "beforeStmt" i.e.
   * newStmt.successors().contains(beforeStmt) will be true
   */
  public BasicBlock<?> insertBefore(@NonNull Stmt beforeStmt, @NonNull FallsThroughStmt newStmt) {
    return insertBefore(beforeStmt, Collections.singletonList(newStmt), Collections.emptyMap());
  }

  /** inserts the "newStmt" after the position of "afterStmt" */
  public BasicBlock<?> insertAfter(@NonNull Stmt afterStmt, @NonNull FallsThroughStmt newStmt) {
    return insertAfter(afterStmt, Collections.singletonList(newStmt), Collections.emptyMap());
  }

  /** removes "stmt" from the StmtGraph */
  public abstract void removeNode(@NonNull Stmt stmt);

  public abstract void removeNode(@NonNull Stmt stmt, boolean keepFlow);

  /**
   * Modifications of unexceptional flows
   *
   * <p>Adds a flow "from" to "to". if at least one of the parameter Stmts is not already in the
   * StmtGraph it will be added. if "to" needs to be added to the StmtGraph i.e. "to" is not already
   * in the StmtGraph the method assumes "to" has the same exceptional flows as "from".
   */
  public abstract void putEdge(@NonNull FallsThroughStmt from, @NonNull Stmt to);

  public abstract void putEdge(@NonNull BranchingStmt from, int successorIdx, @NonNull Stmt to);

  public abstract boolean replaceSucessorEdge(
      @NonNull Stmt from, @NonNull Stmt oldTo, @NonNull Stmt newTo);

  /** replaces the current outgoing flows of "from" to "targets" */
  public abstract void setEdges(@NonNull BranchingStmt from, @NonNull List<Stmt> targets);

  /** replaces the current outgoing flows of "from" to each target of "targets" */
  public void setEdges(@NonNull BranchingStmt from, @NonNull Stmt... targets) {
    setEdges(from, Arrays.asList(targets));
  }

  /**
   * removes the current outgoing flows of "from" to "to"
   *
   * @return returns List of the successor indices of "from" that were connected to "to" - items are
   *     0 in case of FallsThroughStmts or idx &gt; 0 in case of BranchingStmts with multiple
   *     successors
   */
  public abstract List<Integer> removeEdge(@NonNull Stmt from, @NonNull Stmt to);

  /** Modifications of exceptional flows removes all exceptional flows from "stmt" */
  public abstract void clearExceptionalEdges(@NonNull Stmt stmt);

  /**
   * Adds an exceptional flow with the type "exception" to a "stmt" which will reach
   * "traphandlerStmt"
   */
  public abstract void addExceptionalEdge(
      @NonNull Stmt stmt, @NonNull ClassType exception, @NonNull Stmt traphandlerStmt);

  /** removes an exceptional flow of the type "exception" flow from "stmt" */
  public abstract void removeExceptionalEdge(@NonNull Stmt stmt, @NonNull ClassType exception);
}
