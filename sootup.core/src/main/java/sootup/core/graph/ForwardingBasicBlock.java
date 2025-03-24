package sootup.core.graph;

import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.ClassType;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2022 Markus Schmidt
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

public class ForwardingBasicBlock<V extends BasicBlock<V>> implements BasicBlock<V> {
  @NonNull private final V backingBlock;

  ForwardingBasicBlock(@NonNull V block) {
    backingBlock = block;
  }

  @NonNull
  @Override
  public List<V> getPredecessors() {
    return backingBlock.getPredecessors();
  }

  @NonNull
  @Override
  public List<V> getSuccessors() {
    return backingBlock.getSuccessors();
  }

  @NonNull
  @Override
  public Map<ClassType, V> getExceptionalPredecessors() {
    return backingBlock.getExceptionalPredecessors();
  }

  @NonNull
  @Override
  public Map<? extends ClassType, V> getExceptionalSuccessors() {
    return backingBlock.getExceptionalSuccessors();
  }

  @NonNull
  @Override
  public List<Stmt> getStmts() {
    return backingBlock.getStmts();
  }

  @Override
  public int getStmtCount() {
    return backingBlock.getStmtCount();
  }

  @NonNull
  @Override
  public Stmt getHead() {
    return backingBlock.getHead();
  }

  @NonNull
  @Override
  public Stmt getTail() {
    return backingBlock.getTail();
  }

  @Override
  public boolean equivTo(@NonNull Object o, @NonNull JimpleComparator comparator) {
    return backingBlock.equivTo(o, comparator);
  }

  @Override
  public boolean equals(Object o) {
    return backingBlock.equals(o);
  }

  @Override
  public int hashCode() {
    return backingBlock.hashCode();
  }

  @Override
  public String toString() {
    return backingBlock.toString();
  }
}
