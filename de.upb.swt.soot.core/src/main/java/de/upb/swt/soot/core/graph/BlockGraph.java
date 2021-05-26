package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.common.stmt.BranchingStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import java.util.*;
import javax.annotation.Nonnull;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2021 Patrice Pominville, Raja Vallee-Rai, Zun Wang
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
public class BlockGraph implements Iterable<Block> {

  private final Body body;
  private final Block startingBlock;
  private final Map<Integer, Block> idxToBlock = new HashMap<>();
  private final Map<Block, Integer> blockToIdx = new HashMap<>();
  private final Map<Stmt, Block> headToBlock = new HashMap<>();
  private final Map<Stmt, Block> tailToBlock = new HashMap<>();

  private final ArrayList<List<Block>> predecessors = new ArrayList<>();
  private final ArrayList<List<Block>> successors = new ArrayList<>();

  public BlockGraph(@Nonnull Body body) {
    this.body = body;
    int nextFreeIdx = 0;

    StmtGraph graph = this.body.getStmtGraph();

    Iterator<Stmt> iterator = graph.iterator();
    while (iterator.hasNext()) {

      Stmt stmt = iterator.next();
      // decide, whether stmt is a head. If so, construct a block
      if (isHead(stmt, graph)) {
        Stmt head = stmt;
        Stmt tail = stmt;
        List<Stmt> blockStmts = new ArrayList<>();
        blockStmts.add(head);
        while (!isTail(tail, graph)) {
          List<Stmt> succs = graph.successors(tail);
          tail = succs.get(0);
          blockStmts.add(tail);
        }
        Block block = new Block(head, tail, blockStmts, this.body);
        idxToBlock.put(nextFreeIdx, block);
        blockToIdx.put(block, nextFreeIdx);
        headToBlock.put(head, block);
        tailToBlock.put(tail, block);
        nextFreeIdx++;
      }
    }
    startingBlock = idxToBlock.get(0);

    // Initialize predecessors and successors
    for (int i = 0; i < nextFreeIdx; i++) {
      predecessors.add(new ArrayList<>());
      successors.add(new ArrayList<>());
    }
    for (Block block : blockToIdx.keySet()) {
      // find predecessor-blocks
      Stmt head = block.getHead();
      int idx = blockToIdx.get(block);
      List<Stmt> preds = graph.predecessors(head);
      if (!preds.isEmpty()) {
        List<Block> predBlocks = new ArrayList<>();
        for (Stmt pred : preds) {
          predBlocks.add(tailToBlock.get(pred));
        }
        predecessors.set(idx, predBlocks);
      }
      // find successors-blocks
      Stmt tail = block.getTail();
      List<Stmt> succs = graph.successors(tail);
      if (!succs.isEmpty()) {
        List<Block> succBlocks = new ArrayList<>();
        for (Stmt succ : succs) {
          succBlocks.add(headToBlock.get(succ));
        }
        successors.set(idx, succBlocks);
      }
    }
  }

  @Nonnull
  public Block getStartingBlock() {
    return this.startingBlock;
  }

  @Nonnull
  public List<Block> predecessors(@Nonnull Block block) {
    Integer idx = blockToIdx.get(block);
    if (idx != null) {
      return predecessors.get(idx);
    } else {
      throw new RuntimeException("The given block:\n" + block.toString() + "\n is not in body");
    }
  }

  @Nonnull
  public List<Block> successors(@Nonnull Block block) {
    Integer idx = blockToIdx.get(block);
    if (idx != null) {
      return successors.get(idx);
    } else {
      throw new RuntimeException("The given block:\n" + block.toString() + "\n is not in body");
    }
  }

  @Nonnull
  public List<Block> predecessors(@Nonnull Stmt stmt) {
    if (!body.getStmts().contains(stmt)) {
      throw new RuntimeException("The given stmt: " + stmt.toString() + " is not in body");
    }
    if (isHead(stmt, body.getStmtGraph())) {
      return predecessors(headToBlock.get(stmt));
    }
    Stmt tail = stmt;
    StmtGraph graph = body.getStmtGraph();
    while (!isTail(tail, graph)) {
      tail = graph.successors(tail).get(0);
    }
    return predecessors(tailToBlock.get(tail));
  }

  @Nonnull
  public List<Block> successors(@Nonnull Stmt stmt) {
    if (!body.getStmts().contains(stmt)) {
      throw new RuntimeException("The given stmt: " + stmt.toString() + " is not in body");
    }
    if (isHead(stmt, body.getStmtGraph())) {
      return successors(headToBlock.get(stmt));
    }
    Stmt tail = stmt;
    StmtGraph graph = body.getStmtGraph();
    while (!isTail(tail, graph)) {
      tail = graph.successors(tail).get(0);
    }
    return successors(tailToBlock.get(tail));
  }

  @Nonnull
  public Block getBlock(@Nonnull Stmt stmt) {
    if (!body.getStmts().contains(stmt)) {
      throw new RuntimeException("The given stmt: " + stmt.toString() + " is not in body");
    }
    if (isHead(stmt, body.getStmtGraph())) {
      return this.headToBlock.get(stmt);
    }
    Stmt tail = stmt;
    StmtGraph graph = body.getStmtGraph();
    while (!isTail(tail, graph)) {
      tail = graph.successors(tail).get(0);
    }
    return this.tailToBlock.get(tail);
  }

  @Nonnull
  public List<Block> getBlocks() {
    List<Block> blocks = new ArrayList<>();
    for (int i = 0; i < idxToBlock.size(); i++) {
      blocks.add(idxToBlock.get(i));
    }
    return blocks;
  }

  @Override
  @Nonnull
  public Iterator<Block> iterator() {
    return getBlocks().iterator();
  }

  /**
   * Decide, whether a given stmt in the given graph could be a head of a block. If a stmt has no
   * predecessors, or it is successor of a BranchingStmt. then it could be a head of a block.
   *
   * @param stmt
   * @param graph
   * @return
   */
  private boolean isHead(Stmt stmt, StmtGraph graph) {

    if (graph.predecessors(stmt).isEmpty()) {
      return true;
    }
    List<Stmt> preds = graph.predecessors(stmt);
    for (Stmt pred : preds) {
      if (pred instanceof BranchingStmt) {
        return true;
      }
    }
    return false;
  }

  /**
   * Decide, whether a given stmt in the given graph could be a tail of a block. If a stmt has no
   * successors, or it is a BranchingStmt. then it could be a tail of a block.
   *
   * @param stmt
   * @param graph
   * @return
   */
  private boolean isTail(Stmt stmt, StmtGraph graph) {
    if (stmt instanceof BranchingStmt || graph.successors(stmt).isEmpty()) {
      return true;
    }
    Stmt succ = graph.successors(stmt).get(0);
    if (isHead(succ, graph)) {
      return true;
    }
    return false;
  }
}
