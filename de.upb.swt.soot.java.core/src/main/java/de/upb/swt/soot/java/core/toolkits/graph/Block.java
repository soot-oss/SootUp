package de.upb.swt.soot.java.core.toolkits.graph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999 - 2000 Patrice Pominville, Raja Vallee-Rai
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

import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

/**
 * Represents BasicBlocks that partition a method body. It is implemented as view on an underlying Body instance; as a
 * consequence, changes made on a Block will be automatically reflected in its enclosing method body. Blocks also exist in
 * the context of a BlockGraph, a CFG for a method where Block instances are the nodes of the graph. Hence, a Block can be
 * queried for its successors and predecessors Blocks, as found in this graph.
 */
public class Block implements Iterable<Stmt> {
  private static final Logger logger = LoggerFactory.getLogger(Block.class);
  private Stmt mHead, mTail;
  private final Body.BodyBuilder bodyBuilder;
  private List<Block> mPreds, mSuccessors;
  private int mBlockLength = 0, mIndexInMethod = 0;

  /**
   * Constructs a Block in the context of a BlockGraph, and enclosing Body instances.
   *
   * @param aHead
   *          The first unit ir this Block.
   * @param aTail
   *          The last unit in this Block.
   * @param bodyBuilder
   *          The Block's enclosing Body instance.
   * @param aIndexInMethod
   *          The index of this Block in the list of Blocks that partition it's enclosing Body instance.
   * @param aBlockLength
   *          The number of units that makeup this block.
   * @param aBlockGraph
   *          The Graph of Blocks in which this block lives.
   *
   * @see Body
   * @see Chain
   * @see soot.toolkits.graph.BlockGraph
   * @see Unit
   * @see SootMethod
   */
  public Block(Stmt aHead, Stmt aTail, Body.BodyBuilder bodyBuilder, int aIndexInMethod, int aBlockLength, BlockGraph aBlockGraph) {
    mHead = aHead;
    mTail = aTail;
    this.bodyBuilder = bodyBuilder;
    mIndexInMethod = aIndexInMethod;
    mBlockLength = aBlockLength;
  }

  /**
   * Returns the Block's enclosing Body instance.
   *
   * @return The block's chain of instructions.
   * @see soot.jimple.JimpleBody
   * @see BafBody
   * @see Body
   */
  public Body.BodyBuilder getBodyBuilder() {
    return bodyBuilder;
  }

  /**
   * Returns an iterator for the linear chain of Units that make up the block.
   *
   * @return An iterator that iterates over the block's units.
   * @see Chain
   * @see Unit
   */
  @Override
  public Iterator<Stmt> iterator() {
    return bodyBuilder == null ? null : bodyBuilder.getStmts().iterator();
  }

  /**
   * Inserts a Unit before some other Unit in this block.
   *
   *
   * @param toInsert
   *          A Unit to be inserted.
   * @param point
   *          A Unit in the Block's body before which we wish to insert the Unit.
   * @see Unit
   * @see Chain
   */
  public void insertBefore(Stmt toInsert, Stmt point) {
    if (point == mHead) {
      mHead = toInsert;
    }
    bodyBuilder.getStmts().insertBefore(toInsert, point);
  }

  /**
   * Inserts a Unit after some other Unit in the Block.
   *
   * @param toInsert
   *          A Unit to be inserted.
   * @param point
   *          A Unit in the Block after which we wish to insert the Unit.
   * @see Unit
   */
  public void insertAfter(Stmt toInsert, Stmt point) {
    if (point == mTail) {
      mTail = toInsert;
    }
    bodyBuilder.getStmts().insertAfter(toInsert, point);
  }

  /**
   * Removes a Unit occurring before some other Unit in the Block.
   *
   * @param item
   *          A Unit to be remove from the Block's Unit Chain.
   * @return True if the item could be found and removed.
   *
   */
  public boolean remove(Stmt item) {
    StmtGraph methodBody = bodyBuilder.getStmtGraph();

    if (item == mHead) {
      mHead = methodBody.getSuccOf(item);
    } else if (item == mTail) {
      mTail = methodBody.getPredOf(item);
    }

    return methodBody.remove(item);
  }

  /**
   * Returns the Unit occurring immediately after some other Unit in the block.
   *
   * @param aItem
   *          The Unit from which we wish to get it's successor.
   * @return The successor or null if <code>aItem</code> is the tail for this Block.
   *
   */
  public Stmt getSuccOf(Stmt aItem) {
    return aItem == mTail ? null : bodyBuilder.getStmts().getSuccOf(aItem);
  }

  /**
   * Returns the Unit occurring immediately before some other Unit in the block.
   *
   * @param aItem
   *          The Unit from which we wish to get it's predecessor.
   * @return The predecessor or null if <code>aItem</code> is the head for this Block.
   */
  public Stmt getPredOf(Stmt aItem) {
    return aItem == mHead ? null : bodyBuilder.getStmts().getPredOf(aItem);
  }

  /**
   * Set the index of this Block in the list of Blocks that partition its enclosing Body instance.
   *
   * @param aIndexInMethod
   *          The index of this Block in the list of Blocks that partition it's enclosing Body instance.
   **/
  public void setIndexInMethod(int aIndexInMethod) {
    mIndexInMethod = aIndexInMethod;
  }

  /**
   * Returns the index of this Block in the list of Blocks that partition it's enclosing Body instance.
   *
   * @return The index of the block in it's enclosing Body instance.
   */
  public int getIndexInMethod() {
    return mIndexInMethod;
  }

  /**
   * Returns the first unit in this block.
   *
   * @return The first unit in this block.
   */
  public Stmt getHead() {
    return mHead;
  }

  /**
   * Returns the last unit in this block.
   *
   * @return The last unit in this block.
   */
  public Stmt getTail() {
    return mTail;
  }

  /**
   * Sets the list of Blocks that are predecessors of this block in it's enclosing BlockGraph instance.
   *
   * @param preds
   *          The a List of Blocks that precede this block.
   *
   * @see soot.toolkits.graph.BlockGraph
   */
  public void setPreds(List<Block> preds) {
    mPreds = preds;
  }

  /**
   * Returns the List of Block that are predecessors to this block,
   *
   * @return A list of predecessor blocks.
   * @see soot.toolkits.graph.BlockGraph
   */
  public List<Block> getPreds() {
    return mPreds;
  }

  /**
   * Sets the list of Blocks that are successors of this block in it's enclosing BlockGraph instance.
   *
   * @param succs
   *          The a List of Blocks that succede this block.
   *
   * @see soot.toolkits.graph.BlockGraph
   */
  public void setSuccs(List<Block> succs) {
    mSuccessors = succs;
  }

  /**
   * Returns the List of Blocks that are successors to this block,
   *
   * @return A list of successorblocks.
   * @see soot.toolkits.graph.BlockGraph
   */
  public List<Block> getSuccs() {
    return mSuccessors;
  }

  public String toShortString() {
    return "Block #" + mIndexInMethod;
  }

  @Override
  public String toString() {
    StringBuilder strBuf = new StringBuilder();
    strBuf.append("Block ").append(mIndexInMethod).append(':').append(System.lineSeparator());

    // print out predecessors and successors.
    strBuf.append("[preds: ");
    if (mPreds != null) {
      for (Block b : mPreds) {
        strBuf.append(b.getIndexInMethod()).append(' ');
      }
    }
    strBuf.append("] [succs: ");
    if (mSuccessors != null) {
      for (Block b : mSuccessors) {
        strBuf.append(b.getIndexInMethod()).append(' ');
      }
    }
    strBuf.append(']').append(System.lineSeparator());

    // print out Units in the Block
    final Stmt tail = mTail;
    Iterator<Stmt> basicBlockIt = bodyBuilder.getStmts().iterator();
    if (basicBlockIt.hasNext()) {
      Stmt someUnit = basicBlockIt.next();
      strBuf.append(someUnit.toString()).append(';').append(System.lineSeparator());
      while (basicBlockIt.hasNext()) {
        someUnit = basicBlockIt.next();
        if (someUnit == tail) {
          break;
        }
        strBuf.append(someUnit.toString()).append(';').append(System.lineSeparator());
      }
      if (tail == null) {
        strBuf.append("error: null tail found; block length: ").append(mBlockLength).append(System.lineSeparator());
      } else if (tail != mHead) {
        strBuf.append(tail.toString()).append(';').append(System.lineSeparator());
      }
    }
    // Or, it could be an empty block (e.g. Start or Stop Block) --NU
    // else
    // logger.debug("No basic blocks found; must be interface class.");

    return strBuf.toString();
  }
}
