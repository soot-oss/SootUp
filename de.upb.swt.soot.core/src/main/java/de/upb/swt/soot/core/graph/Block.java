package de.upb.swt.soot.core.graph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999 - 2021 Patrice Pominville, Raja Vallee-Rai, Zun Wang
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

import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import javax.annotation.Nonnull;

/**
 * Represents BasicBlocks that partition a method body.
 *
 * @author Zun Wang
 */
public class Block {

  private Stmt head;
  private Stmt tail;

  public Block(@Nonnull Stmt head, @Nonnull Stmt tail) {
    this.head = head;
    this.tail = tail;
  }

  public Stmt getHead() {
    return this.head;
  }

  public Stmt getTail() {
    return this.tail;
  }

  public void setHead(Stmt newHead) {
    this.head = newHead;
  }

  public void setTail(Stmt newTail) {
    this.tail = newTail;
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("[ ");
    builder.append(head.toString());
    if (head != tail) {
      builder.append(", ... ,");
      builder.append(tail.toString());
    }
    builder.append(" ]");
    return builder.toString();
  }

  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Block)) {
      return false;
    }
    Block block = (Block) o;
    if (this.head != block.head) {
      return false;
    }
    if (this.tail != block.tail) {
      return false;
    }
    return true;
  }

  public boolean equivTo(@Nonnull Object o, @Nonnull JimpleComparator comparator) {
    return comparator.caseBlock(this, o);
  }
}
