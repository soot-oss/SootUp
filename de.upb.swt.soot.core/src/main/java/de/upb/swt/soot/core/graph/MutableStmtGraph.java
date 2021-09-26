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
import java.util.List;
import javax.annotation.Nonnull;

/** @author Markus Schmidt */
public abstract class MutableStmtGraph extends StmtGraph {
  @Nonnull
  public abstract StmtGraph unmodifiableStmtGraph();

  public abstract void setStartingStmt(@Nonnull Stmt firstStmt);

  public abstract void addNode(@Nonnull Stmt node);

  public abstract void replaceNode(@Nonnull Stmt oldStmt, @Nonnull Stmt newStmt);

  public abstract void removeNode(@Nonnull Stmt node);

  public abstract void putEdge(@Nonnull Stmt from, @Nonnull Stmt to);

  public abstract void setEdges(@Nonnull Stmt from, @Nonnull List<Stmt> targets);

  public abstract void removeEdge(@Nonnull Stmt from, @Nonnull Stmt to);

  public abstract void setTraps(@Nonnull List<Trap> traps);

  public abstract void addTrap(
      ClassType throwableSig, Stmt fromStmt, Stmt toStmt, Stmt handlerStmt);

  public abstract void removeTrap(
      ClassType throwableSig, Stmt fromStmt, Stmt toStmt, Stmt handlerStmt);
}
