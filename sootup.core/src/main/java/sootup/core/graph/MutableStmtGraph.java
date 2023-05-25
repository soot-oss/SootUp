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
import javax.annotation.Nonnull;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.ClassType;

/**
 * @author Markus Schmidt
 *     <p>performance suggestions for multiple operations on sequential Stmts: addNode():
 *     top-&gt;down removeNode(s): bottom-&gt;up as then there is no need for copying inside the
 *     MutableBasicBlock
 */
public abstract class MutableStmtGraph extends StmtGraph<MutableBasicBlock> {
  @Nonnull
  public abstract StmtGraph<?> unmodifiableStmtGraph();

  public abstract void setStartingStmt(@Nonnull Stmt firstStmt);

  /** inserts a "stmt" into the StmtGraph */
  public void addNode(@Nonnull Stmt stmt) {
    addNode(stmt, Collections.emptyMap());
  }

  /** inserts a "stmt" with exceptional flows "traps" into the StmtGraph */
  public abstract void addNode(@Nonnull Stmt stmt, @Nonnull Map<ClassType, Stmt> traps);

  /** creates a whole BasicBlock with the details from the parameters */
  public abstract void addBlock(@Nonnull List<Stmt> stmts, @Nonnull Map<ClassType, Stmt> traps);

  /**
   * creates a whole BasicBlock which contains the sequence of (n-1)*fallsthrough()-stmt + optional
   * a non-fallsthrough() stmt at the end of the list
   */
  public void addBlock(@Nonnull List<Stmt> stmts) {
    addBlock(stmts, Collections.emptyMap());
  }

  /**
   * Modification of stmts (without manipulating any flows; possible assigned exceptional flows stay
   * the same as well)
   */
  public abstract void replaceNode(@Nonnull Stmt oldStmt, @Nonnull Stmt newStmt);

  public abstract void insertBefore(
      @Nonnull Stmt beforeStmt,
      @Nonnull List<Stmt> stmts,
      @Nonnull Map<ClassType, Stmt> exceptionMap);

  /**
   * inserts the "newStmt" before the position of "beforeStmt" i.e.
   * newStmt.successors().contains(beforeStmt) will be true
   */
  public void insertBefore(@Nonnull Stmt beforeStmt, @Nonnull Stmt newStmt) {
    insertBefore(beforeStmt, Collections.singletonList(newStmt), Collections.emptyMap());
  }

  /** removes "stmt" from the StmtGraph */
  public abstract void removeNode(@Nonnull Stmt stmt);

  /**
   * Modifications of unexceptional flows
   *
   * <p>Adds a flow "from" to "to". if at least one of the parameter Stmts is not already in the
   * StmtGraph it will be added. if "to" needs to be added to the StmtGraph i.e. "to" is not already
   * in the StmtGraph the method assumes "to" has the same exceptional flows as "from".
   */
  public abstract void putEdge(@Nonnull Stmt from, @Nonnull Stmt to);

  /** replaces the current outgoing flows of "from" to "targets" */
  public abstract void setEdges(@Nonnull Stmt from, @Nonnull List<Stmt> targets);

  /** replaces the current outgoing flows of "from" to each target of "targets" */
  public void setEdges(@Nonnull Stmt from, @Nonnull Stmt... targets) {
    setEdges(from, Arrays.asList(targets));
  }

  /** removes the current outgoing flows of "from" to "targets" */
  public abstract void removeEdge(@Nonnull Stmt from, @Nonnull Stmt to);

  /** Modifications of exceptional flows removes all exceptional flows from "stmt" */
  public abstract void clearExceptionalEdges(@Nonnull Stmt stmt);

  /**
   * Adds an exceptional flow with the type "exception" to a "stmt" which will reach
   * "traphandlerStmt"
   */
  public abstract void addExceptionalEdge(
      @Nonnull Stmt stmt, @Nonnull ClassType exception, @Nonnull Stmt traphandlerStmt);

  /** removes an exceptional flow of the type "exception" flow from "stmt" */
  public abstract void removeExceptionalEdge(@Nonnull Stmt stmt, @Nonnull ClassType exception);
}
