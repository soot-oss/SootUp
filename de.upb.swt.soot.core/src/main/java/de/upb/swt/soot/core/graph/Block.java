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
import de.upb.swt.soot.core.model.Body;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 * Represents BasicBlocks that partition a method body.
 *
 * @author Zun Wang
 */
public class Block {

  private Body body;
  private Stmt head;
  private Stmt tail;
  private List<Stmt> blockStmts;
  private final Map<Stmt, Integer> stmtToPos = new HashMap();
  private int blockLength;

  public Block(Stmt head, Stmt tail, Body body) {
    ImmutableExceptionalStmtGraph graph = body.getStmtGraph();
    if (!graph.containsNode(head) || !graph.containsNode(tail)) {
      if (!graph.containsNode(head)) {
        throw new RuntimeException("The given head: " + head.toString() + " is not in graph!");
      } else {
        throw new RuntimeException("The given tail: " + tail.toString() + " is not in graph!");
      }
    }
    this.head = head;
    this.tail = tail;
    this.body = body;
    int num = 0;
    Stmt stmt = head;
    blockStmts = new ArrayList<>();
    while (stmt != tail) {
      stmtToPos.put(stmt, num);
      blockStmts.add(stmt);
      num++;
      List<Stmt> succs = graph.successors(stmt);
      if (succs.size() == 1) {
        stmt = succs.get(0);
      } else {
        StringBuilder builder = new StringBuilder();
        succs.forEach(succ -> builder.append(succ.toString() + " "));
        throw new RuntimeException(
            "These successors " + builder.toString() + " should be in different blocks!");
      }
    }
    blockStmts.add(tail);
    stmtToPos.put(tail, num);
    blockLength = num + 1;
  }

  public Block(Stmt head, Stmt tail, List<Stmt> blockStmts, Body body) {
    this.body = body;
    this.head = head;
    this.tail = tail;
    this.blockStmts = blockStmts;
    this.blockLength = blockStmts.size();
    for (int i = 0; i < this.blockLength; i++) {
      this.stmtToPos.put(this.blockStmts.get(i), i);
    }
  }

  private Block() {}

  public static Block getEmptyBlock() {
    return new Block();
  }

  public Body getBody() {
    return this.body;
  }

  public Stmt getHead() {
    return this.head;
  }

  public Stmt getTail() {
    return this.tail;
  }

  public void replaceBlockStmt(Stmt oldStmt, Stmt newStmt) {
    if (!this.stmtToPos.containsKey(oldStmt)) {
      throw new RuntimeException(
          "The given oldStmt: " + oldStmt.toString() + " is not this block!");
    }
    if (oldStmt == head) {
      this.head = newStmt;
      this.blockStmts.set(0, newStmt);
    } else if (oldStmt == tail) {
      this.tail = newStmt;
      this.blockStmts.set(this.blockLength - 1, newStmt);
    } else {
      this.blockStmts.set(this.stmtToPos.get(oldStmt), newStmt);
    }
  }

  /**
   * Getter of block stmts, the order is same as in StmtGraph of body
   *
   * @return
   */
  public List<Stmt> getBlockStmts() {
    return this.blockStmts;
  }

  public int getBlockLength() {
    return this.blockLength;
  }

  public boolean isInBlock(Stmt stmt) {
    if (this.blockStmts.contains(stmt)) {
      return true;
    }
    return false;
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("[");
    for (Stmt stmt : blockStmts) {
      builder.append("  " + stmt.toString() + "\n");
    }
    builder.delete(1, 2);
    builder.replace(builder.length() - 1, builder.length(), " ]");
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
    if (!this.body.equals(block.getBody())) {
      return false;
    }
    if (this.blockLength != block.getBlockLength()) {
      return false;
    }
    for (int i = 0; i < blockLength; i++) {
      return (this.blockStmts.get(i) == block.getBlockStmts().get(i));
    }
    return true;
  }

  public boolean equivTo(@Nonnull Object o, @Nonnull JimpleComparator comparator) {
    return comparator.caseBlock(this, o);
  }
}
