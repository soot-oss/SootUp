package sootup.core.graph;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 1997 - 2024 Raja Vallée-Rai and others
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

import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.ClassType;

/**
 * Block of a {@link MutableBlockControlFlowGraph}. Read-only view: modify blocks through the graph,
 * which keeps both ends of every edge and its stmt index consistent.
 */
public interface MutableBasicBlock extends BasicBlock<MutableBasicBlock> {

  /** normal and exceptional predecessors, one entry per edge */
  @NonNull
  @Override
  List<MutableBasicBlock> getPredecessors();

  @NonNull
  @Override
  List<MutableBasicBlock> getSuccessors();

  @Override
  Map<ClassType, MutableBasicBlock> getExceptionalPredecessors();

  @NonNull
  @Override
  Map<ClassType, MutableBasicBlock> getExceptionalSuccessors();

  int getStmtCount();

  @NonNull
  @Override
  List<Stmt> getStmts();

  @NonNull
  @Override
  Stmt getHead();

  @NonNull
  @Override
  Stmt getTail();
}
