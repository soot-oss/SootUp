package sootup.jimple.frontend;

/*-
 * #%L
 * SootUp\
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

import java.util.*;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.common.stmt.BranchingStmt;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.Stmt;

/**
 * Splits the parsed Stmts into Blocks and omits Stmts whose unreachability is apparent from the
 * structure of the serialized Jimple:
 *
 * <ul>
 *   <li>an unlabeled Stmt that does not follow a FallsThroughStmt can neither be a branch target
 *       nor a Trap boundary/handler.
 *   <li>a trailing sequence of Blocks that ends with a FallsThroughStmt and is not entered via
 *       fallthrough has no successor Block. e.g. the JimplePrinter appends a labeled nop as last
 *       Stmt to denote the (exclusive) end of a Trap that covers the last Stmt of the body - a Trap
 *       ending there reaches the end of the body.
 * </ul>
 */
class BlockBuilder {

  @NonNull private final List<List<Stmt>> blocks;
  @NonNull private final Map<BranchingStmt, List<String>> unresolvedBranches;

  @NonNull
  private final Set<Stmt> omittedStmts = Collections.newSetFromMap(new IdentityHashMap<>());

  @NonNull private List<Stmt> currentBlock = new ArrayList<>();
  private Stmt previousStmt = null;
  private boolean unreachable = false;

  BlockBuilder(
      @NonNull List<List<Stmt>> blocks,
      @NonNull Map<BranchingStmt, List<String>> unresolvedBranches) {
    this.blocks = blocks;
    this.unresolvedBranches = unresolvedBranches;
  }

  void add(@NonNull Stmt stmt, boolean isLabeled) {
    if (isLabeled) {
      unreachable = false;
      closeBlock();
    } else if (previousStmt != null && !(previousStmt instanceof FallsThroughStmt)) {
      unreachable = true;
    }
    previousStmt = stmt;

    if (unreachable) {
      omit(stmt);
      return;
    }

    currentBlock.add(stmt);
    if (stmt.branches()) {
      closeBlock();
    }
  }

  void finish() {
    closeBlock();
    if (blocks.isEmpty() || !(tailOf(blocks.get(blocks.size() - 1)) instanceof FallsThroughStmt)) {
      return;
    }
    int segmentStart = blocks.size() - 1;
    while (segmentStart > 0 && tailOf(blocks.get(segmentStart - 1)) instanceof FallsThroughStmt) {
      segmentStart--;
    }
    if (segmentStart == 0) {
      // reachable from the starting Stmt: leave it to the ControlFlowGraph creation to report it
      return;
    }
    while (blocks.size() > segmentStart) {
      blocks.remove(blocks.size() - 1).forEach(this::omit);
    }
  }

  boolean isOmitted(Stmt stmt) {
    return omittedStmts.contains(stmt);
  }

  private void closeBlock() {
    if (!currentBlock.isEmpty()) {
      blocks.add(currentBlock);
      currentBlock = new ArrayList<>();
    }
  }

  private void omit(@NonNull Stmt stmt) {
    omittedStmts.add(stmt);
    if (stmt instanceof BranchingStmt) {
      unresolvedBranches.remove(stmt);
    }
  }

  private static Stmt tailOf(List<Stmt> block) {
    return block.get(block.size() - 1);
  }
}
