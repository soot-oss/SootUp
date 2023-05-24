package sootup.core.graph;

import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
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
  @Nonnull private final V backingBlock;

  ForwardingBasicBlock(@Nonnull V block) {
    backingBlock = block;
  }

  @Nonnull
  @Override
  public List<V> getPredecessors() {
    return backingBlock.getPredecessors();
  }

  @Nonnull
  @Override
  public List<V> getSuccessors() {
    return backingBlock.getSuccessors();
  }

  @Nonnull
  @Override
  public Map<ClassType, V> getExceptionalPredecessors() {
    return backingBlock.getExceptionalPredecessors();
  }

  @Nonnull
  @Override
  public Map<? extends ClassType, V> getExceptionalSuccessors() {
    return backingBlock.getExceptionalSuccessors();
  }

  @Nonnull
  @Override
  public List<Stmt> getStmts() {
    return backingBlock.getStmts();
  }

  @Override
  public int getStmtCount() {
    return backingBlock.getStmtCount();
  }

  @Nonnull
  @Override
  public Stmt getHead() {
    return backingBlock.getHead();
  }

  @Nonnull
  @Override
  public Stmt getTail() {
    return backingBlock.getTail();
  }

  @Override
  public boolean equivTo(@Nonnull Object o, @Nonnull JimpleComparator comparator) {
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
