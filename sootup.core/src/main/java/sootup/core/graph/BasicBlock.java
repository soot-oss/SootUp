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
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.ClassType;

public interface BasicBlock<V extends BasicBlock<V>> {
  @NonNull List<V> getPredecessors();

  @NonNull List<V> getSuccessors();

  Map<ClassType, V> getExceptionalPredecessors();

  @NonNull Map<? extends ClassType, V> getExceptionalSuccessors();

  @NonNull List<Stmt> getStmts();

  int getStmtCount();

  default boolean isEmpty() {
    return getStmtCount() <= 0;
  }

  @NonNull Stmt getHead();

  @NonNull Stmt getTail();

  default boolean equivTo(@NonNull Object o, @NonNull JimpleComparator comparator) {
    return comparator.caseBlock(this, o);
  }
}
