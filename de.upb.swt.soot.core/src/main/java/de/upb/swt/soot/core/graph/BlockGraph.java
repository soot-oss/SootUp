package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.BranchingStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
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

  public final MutableExceptionalStmtGraph stmtGraph;
  private Block startingBlock;
  public final Map<Integer, Block> idxToBlock = new HashMap<>();
  public final Map<Block, Integer> blockToIdx = new HashMap<>();
  public final Map<Stmt, Block> headToBlock = new HashMap<>();
  public final Map<Stmt, Block> tailToBlock = new HashMap<>();

  public final List<List<Block>> blockPreds = new ArrayList<>();
  public final List<List<Block>> blockSuccs = new ArrayList<>();
  public List<List<Block>> exceptionalBlockPreds = new ArrayList<>();
  public List<List<Block>> exceptionalBlockSuccs = new ArrayList<>();

  public BlockGraph(@Nonnull StmtGraph stmtGraph) {

    this.stmtGraph = new MutableExceptionalStmtGraph(stmtGraph);

    int nextFreeIdx = 0;

    Iterator<Stmt> iterator = stmtGraph.iterator();
    while (iterator.hasNext()) {

      Stmt stmt = iterator.next();
      // decide, whether stmt is a head. If so, construct a block
      if (isHead(stmt, stmtGraph)) {
        Stmt head = stmt;
        Stmt tail = stmt;
        while (!isTail(tail, stmtGraph)) {
          List<Stmt> succs = stmtGraph.successors(tail);
          tail = succs.get(0);
        }
        Block block = new Block(head, tail);
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
      blockPreds.add(new ArrayList<>());
      blockSuccs.add(new ArrayList<>());
      exceptionalBlockPreds.add(new ArrayList<>());
      exceptionalBlockSuccs.add(new ArrayList<>());
    }
    for (Block block : blockToIdx.keySet()) {
      // find predecessor-blocks
      Stmt head = block.getHead();
      int idx = blockToIdx.get(block);
      List<Stmt> preds = stmtGraph.predecessors(head);
      if (!preds.isEmpty()) {
        List<Block> predBlocks = new ArrayList<>();
        for (Stmt pred : preds) {
          predBlocks.add(tailToBlock.get(pred));
        }
        blockPreds.set(idx, predBlocks);
      }
      // find exceptional predecessor blocks
      List<Stmt> exceptionalPreds = this.stmtGraph.exceptionalPredecessors(head);
      if (!exceptionalPreds.isEmpty()) {
        Set<Block> ePredBlocksSet = new LinkedHashSet<>();
        for (Stmt epred : exceptionalPreds) {
          ePredBlocksSet.add(getBlock(epred));
        }
        exceptionalBlockPreds.set(idx, new ArrayList<>(ePredBlocksSet));
      }
      // find successors-blocks
      Stmt tail = block.getTail();
      List<Stmt> succs = stmtGraph.successors(tail);
      if (!succs.isEmpty()) {
        List<Block> succBlocks = new ArrayList<>();
        for (Stmt succ : succs) {
          succBlocks.add(headToBlock.get(succ));
        }
        blockSuccs.set(idx, succBlocks);
      }

      // find exceptional successor blocks
      List<Stmt> exceptionalSuccs = this.stmtGraph.exceptionalSuccessors(head);
      if (!exceptionalSuccs.isEmpty()) {
        Set<Block> eSuccBlocksSet = new LinkedHashSet<>();
        for (Stmt esucc : exceptionalSuccs) {
          eSuccBlocksSet.add(getBlock(esucc));
        }
        exceptionalBlockSuccs.set(idx, new ArrayList<>(eSuccBlocksSet));
      }
    }
  }

  @Nonnull
  public Block getStartingBlock() {
    return this.startingBlock;
  }

  @Nonnull
  public MutableExceptionalStmtGraph getStmtGraph() {
    return this.stmtGraph;
  }

  @Nonnull
  public List<Block> blockPredecessors(@Nonnull Block block) {
    Integer idx = blockToIdx.get(block);
    if (idx != null) {
      return blockPreds.get(idx);
    } else {
      throw new RuntimeException(
          "The given block:\n" + block.toString() + "\n is not in StmtGraph!");
    }
  }

  @Nonnull
  public List<Block> blockSuccessors(@Nonnull Block block) {
    Integer idx = blockToIdx.get(block);
    if (idx != null) {
      return blockSuccs.get(idx);
    } else {
      throw new RuntimeException(
          "The given block:\n" + block.toString() + "\n is not in StmtGraph!");
    }
  }

  @Nonnull
  public List<Block> exceptionalBlockPredecessors(@Nonnull Block block) {
    Integer idx = blockToIdx.get(block);
    if (idx != null) {
      return exceptionalBlockPreds.get(idx);
    } else {
      throw new RuntimeException(
          "The given block:\n" + block.toString() + "\n is not in StmtGraph!");
    }
  }

  @Nonnull
  public List<Block> exceptionalBlockSuccessors(@Nonnull Block block) {
    Integer idx = blockToIdx.get(block);
    if (idx != null) {
      return exceptionalBlockSuccs.get(idx);
    } else {
      throw new RuntimeException(
          "The given block:\n" + block.toString() + "\n is not in StmtGraph!");
    }
  }

  @Nonnull
  public List<Block> blockPredecessors(@Nonnull Stmt stmt) {
    if (!stmtGraph.containsNode(stmt)) {
      throw new RuntimeException("The given stmt: " + stmt.toString() + " is not in StmtGraph!");
    }
    if (isHead(stmt, stmtGraph)) {
      return blockPredecessors(headToBlock.get(stmt));
    }
    Stmt tail = stmt;
    while (!isTail(tail, stmtGraph)) {
      tail = stmtGraph.successors(tail).get(0);
    }
    return blockPredecessors(tailToBlock.get(tail));
  }

  @Nonnull
  public List<Block> blockSuccessors(@Nonnull Stmt stmt) {
    if (!stmtGraph.containsNode(stmt)) {
      throw new RuntimeException("The given stmt: " + stmt.toString() + " is not in StmtGraph!");
    }
    if (isHead(stmt, stmtGraph)) {
      return blockSuccessors(headToBlock.get(stmt));
    }
    Stmt tail = stmt;
    while (!isTail(tail, stmtGraph)) {
      tail = stmtGraph.successors(tail).get(0);
    }
    return blockSuccessors(tailToBlock.get(tail));
  }

  @Nonnull
  public List<Block> exceptionalBlockPredecessors(@Nonnull Stmt stmt) {
    if (!stmtGraph.containsNode(stmt)) {
      throw new RuntimeException("The given stmt: " + stmt.toString() + " is not in StmtGraph!");
    }
    return exceptionalBlockPredecessors(getBlock(stmt));
  }

  @Nonnull
  public List<Block> exceptionalBlockSuccessors(@Nonnull Stmt stmt) {
    if (!stmtGraph.containsNode(stmt)) {
      throw new RuntimeException("The given stmt: " + stmt.toString() + " is not in StmtGraph!");
    }
    return exceptionalBlockSuccessors(getBlock(stmt));
  }

  @Nonnull
  public Block getBlock(@Nonnull Stmt stmt) {
    if (!stmtGraph.containsNode(stmt)) {
      throw new RuntimeException("The given stmt: " + stmt.toString() + " is not in StmtGraph!");
    }
    if (isHead(stmt, stmtGraph)) {
      return this.headToBlock.get(stmt);
    }
    Stmt tail = stmt;
    while (!isTail(tail, stmtGraph)) {
      tail = stmtGraph.successors(tail).get(0);
    }
    return this.tailToBlock.get(tail);
  }

  @Nonnull
  public List<Stmt> getBlockStmts(@Nonnull Block block) {
    if (!blockToIdx.containsKey(block)) {
      throw new RuntimeException("The given block: " + block.toString() + " is not in BlockGraph!");
    }
    List<Stmt> stmts = new ArrayList<>();
    Stmt head = block.getHead();
    Stmt tail = block.getTail();
    stmts.add(head);
    while (head != tail) {
      head = stmtGraph.successors(head).get(0);
      stmts.add(head);
    }
    return stmts;
  }

  @Nonnull
  /** return a list of Blocks with reverse postorder */
  public List<Block> getBlocks() {
    Map<Block, List<Block>> adjacentMap = new HashMap<>();
    HashSet<Block> visited = new HashSet<>();
    for (Block block : this.blockToIdx.keySet()) {
      List<Block> succs = new ArrayList<>(blockSuccessors(block));
      succs.addAll(exceptionalBlockSuccessors(block));
      adjacentMap.put(block, succs);
    }
    Set<Block> postOrder = new LinkedHashSet<>();
    Deque<Block> stack = new ArrayDeque<>();
    stack.add(startingBlock);
    visited.add(startingBlock);
    while (!stack.isEmpty()) {
      Block top = stack.getFirst();
      List<Block> adjacentList = adjacentMap.get(top);
      if (adjacentList.isEmpty()) {
        postOrder.add(top);
        stack.removeFirst();
      } else {
        Block next = adjacentList.remove(0);
        if (!visited.contains(next) && !postOrder.contains(next)) {
          stack.addFirst(next);
          visited.add(next);
        }
      }
    }
    Deque<Block> reversePostOrder = new ArrayDeque<>();
    for (Block block : postOrder) {
      reversePostOrder.addFirst(block);
    }
    return new ArrayList<>(reversePostOrder);
  }

  /**
   * Add a stmt on the top of a given block
   *
   * @param stmt
   * @param block
   */
  public void addStmtOnTopOfBlock(Stmt stmt, Block block) {
    if (!blockToIdx.containsKey(block)) {
      throw new RuntimeException("The given block: " + block.toString() + " is not in BlockGraph!");
    }
    Stmt head = block.getHead();
    stmtGraph.insertNode(stmt, head);
    block.setHead(stmt);
  }

  public void replaceStmtInBlock(Stmt oldStmt, Stmt newStmt, Block block) {
    if (!blockToIdx.containsKey(block)) {
      throw new RuntimeException("The given block: " + block.toString() + " is not in BlockGraph!");
    }
    if (!stmtGraph.containsNode(oldStmt)) {
      throw new RuntimeException("The given stmt: " + oldStmt.toString() + " is not in StmtGraph!");
    }
    stmtGraph.replaceNode(oldStmt, newStmt);
    if (oldStmt == block.getHead()) {
      block.setHead(newStmt);
    }
    if (oldStmt == block.getTail()) {
      block.setTail(newStmt);
    }
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
    List<Stmt> preds = graph.predecessors(stmt);
    if (preds.size() > 1) {
      return true;
    }
    if (preds.isEmpty()) {
      return true;
    }

    for (Stmt pred : preds) {
      if (pred instanceof BranchingStmt) {
        return true;
      }
    }

    List<Trap> traps = graph.getTraps();
    for (Trap trap : traps) {
      if (stmt == trap.getBeginStmt() || stmt == trap.getEndStmt()) {
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
    return isHead(succ, graph);
  }

  public void replaceBlock(@Nonnull Block oldBlock, @Nonnull Block newBlock) {

    if (!blockToIdx.keySet().contains(oldBlock)) {
      throw new RuntimeException(
          "The given block: " + oldBlock.toString() + " is not in BlockGraph!");
    }

    List<Block> preds = blockPredecessors(oldBlock);
    for (Block pred : preds) {
      int pidx = blockToIdx.get(pred);
      blockSuccs.get(pidx).remove(oldBlock);
      blockSuccs.get(pidx).add(newBlock);
    }

    List<Block> succs = blockSuccessors(oldBlock);
    for (Block succ : succs) {
      int sidx = blockToIdx.get(succ);
      blockPreds.get(sidx).remove(oldBlock);
      blockPreds.get(sidx).add(newBlock);
    }

    Stmt head = oldBlock.getHead();
    Stmt tail = oldBlock.getTail();

    int idx = blockToIdx.get(oldBlock);
    idxToBlock.replace(idx, oldBlock, newBlock);

    blockToIdx.remove(oldBlock);
    blockToIdx.put(newBlock, idx);

    headToBlock.remove(head);
    headToBlock.put(newBlock.getHead(), newBlock);

    tailToBlock.remove(tail);
    tailToBlock.put(newBlock.getTail(), newBlock);

    if (oldBlock == this.startingBlock) {
      this.startingBlock = newBlock;
    }
  }
}
