package de.upb.swt.soot.core.graph;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2021 Zun Wang
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
import java.util.*;
import javax.annotation.Nonnull;

public final class ExceptionalStmtGraph extends ForwardingStmtGraph {

  @Nonnull private final MutableExceptionalStmtGraph stmtGraph;

  public ExceptionalStmtGraph(@Nonnull MutableExceptionalStmtGraph stmtGraph) {
    super(stmtGraph);
    this.stmtGraph = stmtGraph;
  }

  @Nonnull
  public List<Stmt> exceptionalPredecessors(@Nonnull Stmt stmt) {
    return stmtGraph.exceptionalPredecessors(stmt);
  }

  @Nonnull
  public List<Stmt> exceptionalSuccessors(@Nonnull Stmt stmt) {
    return stmtGraph.exceptionalSuccessors(stmt);
  }

  @Nonnull
  public List<Trap> getDestTraps(@Nonnull Stmt stmt) {
    return stmtGraph.getDestTraps(stmt);
  }
}
