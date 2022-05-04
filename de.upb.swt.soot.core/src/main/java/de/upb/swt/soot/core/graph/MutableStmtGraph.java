package de.upb.swt.soot.core.graph;
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

import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.types.ClassType;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 * @author Markus Schmidt
 *     <p>performance suggestions for multiple operations on sequential Stmts: addNode(): top->down
 *     removeNode(s): bottom->up as then there is no need for copying inside the MutableBasicBlock
 */
public abstract class MutableStmtGraph extends StmtGraph<MutableBasicBlock> {
  @Nonnull
  public abstract StmtGraph<?> unmodifiableStmtGraph();

  public abstract void setStartingStmt(@Nonnull Stmt firstStmt);

  public void addNode(@Nonnull Stmt node) {
    addNode(node, Collections.emptyMap());
  }

  public abstract void addNode(@Nonnull Stmt node, @Nonnull Map<ClassType, Stmt> traps);

  // maybe refactor addBlock into MutableBlockStmtGraph..
  public abstract void addBlock(@Nonnull List<Stmt> stmts, @Nonnull Map<ClassType, Stmt> traps);

  /** Modification of nodes (without manipulating any flows) */
  public void replaceNode(@Nonnull Stmt oldStmt, @Nonnull Stmt newStmt) {
    // if possible please implement a better approach in your subclass
    removeNode(oldStmt);
    addNode(newStmt);
  }

  public abstract void removeNode(@Nonnull Stmt node);

  /** Modifications of unexceptional flows */
  public abstract void putEdge(@Nonnull Stmt from, @Nonnull Stmt to);

  public abstract void setEdges(@Nonnull Stmt from, @Nonnull List<Stmt> targets);

  public abstract void removeEdge(@Nonnull Stmt from, @Nonnull Stmt to);

  /** Modifications of exceptional flows */
  public abstract void clearExceptionalEdges(@Nonnull Stmt node);

  public abstract void addExceptionalEdge(
      @Nonnull Stmt stmt, @Nonnull ClassType exception, @Nonnull Stmt traphandlerStmt);

  public abstract void removeExceptionalEdge(@Nonnull Stmt node, @Nonnull ClassType exception);

  public void setTraps(List<Trap> newTraps) {
    throw new UnsupportedOperationException("deprecated");
  }
}
