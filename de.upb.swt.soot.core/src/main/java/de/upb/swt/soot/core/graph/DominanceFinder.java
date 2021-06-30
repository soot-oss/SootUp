package de.upb.swt.soot.core.graph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2021 Zun Wang
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
import javax.annotation.Nonnull;

/**
 * @author Zun Wang
 * @see <a
 *     href="https://www.cs.rice.edu/~keith/EMBED/dom.pdf">https://www.cs.rice.edu/~keith/EMBED/dom.pdf</a>
 */
public class DominanceFinder {

  private Map<Integer, Block> idxToBlock = new HashMap<>();
  private Map<Block, Integer> blockToIdx = new HashMap<>();
  private int[] doms;
  private ArrayList<Integer>[] domFrontiers;

  public DominanceFinder(BlockGraph blockGraph) {

    // assign each block a integer id, startBlock's id must be 0
    Iterator<Block> blockIterator = blockGraph.iterator();
    int idx = 0;
    while (blockIterator.hasNext()) {
      Block block = blockIterator.next();
      idxToBlock.put(idx, block);
      blockToIdx.put(block, idx);
      idx++;
    }

    // initialize doms
    doms = new int[blockGraph.getBlocks().size()];
    doms[0] = 0;
    for (int i = 1; i < doms.length; i++) {
      doms[i] = -1;
    }

    // calculate immediate dominator for each block
    boolean isChanged = true;
    List<Block> blocks = blockGraph.getBlocks();
    blocks.remove(blockGraph.getStartingBlock());
    while (isChanged) {
      isChanged = false;
      for (Block block : blocks) {
        int blockIdx = this.blockToIdx.get(block);
        List<Block> preds = new ArrayList<>(blockGraph.blockPredecessors(block));
        int newIdom = getFirstDefinedBlockPredIdx(preds);
        if (!preds.isEmpty() && newIdom != -1) {
          preds.remove(this.idxToBlock.get(newIdom));
          for (Block pred : preds) {
            int predIdx = this.blockToIdx.get(pred);
            if (this.doms[predIdx] != -1) {
              newIdom = intersect(newIdom, predIdx);
            }
          }
          if (doms[blockIdx] != newIdom) {
            doms[blockIdx] = newIdom;
            isChanged = true;
          }
        }
      }
    }

    // initialize doms
    domFrontiers = new ArrayList[blockGraph.getBlocks().size()];
    for (int i = 0; i < domFrontiers.length; i++) {
      domFrontiers[i] = new ArrayList<>();
    }

    // calculate dominance frontiers for each block
    for (Block block : blocks) {
      List<Block> preds = blockGraph.blockPredecessors(block);
      if (preds.size() > 1) {
        int blockId = this.blockToIdx.get(block);
        for (Block pred : preds) {
          int predId = this.blockToIdx.get(pred);
          while (predId != doms[blockId]) {
            domFrontiers[predId].add(blockId);
            predId = doms[predId];
          }
        }
      }
    }
  }


  @Nonnull
  public void replaceBlock(@Nonnull Block newBlock, Block oldBlock) {
    if (!blockToIdx.keySet().contains(oldBlock)) {
      throw new RuntimeException(
          "The given block: " + oldBlock.toString() + " is not in BlockGraph!");
    }
    this.blockToIdx.put(newBlock, this.blockToIdx.get(oldBlock));
    this.blockToIdx.remove(oldBlock);
    this.idxToBlock.put(this.blockToIdx.get(newBlock), newBlock);
  }

  @Nonnull
  public Block getImmediateDominator(@Nonnull Block block) {
    if (!blockToIdx.keySet().contains(block)) {
      throw new RuntimeException("The given block: " + block.toString() + " is not in BlockGraph!");
    }
    int idx = this.blockToIdx.get(block);
    int idomIdx = this.doms[idx];
    return this.idxToBlock.get(idomIdx);
  }

  @Nonnull
  public Set<Block> getDominanceFrontiers(@Nonnull Block block) {
    if (!blockToIdx.keySet().contains(block)) {
      throw new RuntimeException("The given block: " + block.toString() + " is not in BlockGraph!");
    }
    int idx = this.blockToIdx.get(block);
    Set<Block> dFs = new HashSet<>();
    ArrayList<Integer> dFs_idx = this.domFrontiers[idx];
    for (Integer i : dFs_idx) {
      dFs.add(this.idxToBlock.get(i));
    }
    return dFs;
  }

  @Nonnull
  public Map<Integer, Block> getIdxToBlock(){
    return this.idxToBlock;
  }

  @Nonnull
  public Map<Block, Integer> getBlockToIdx(){
    return this.blockToIdx;
  }

  @Nonnull
  public int[] getImmediateDominators(){
    return this.doms;
  }


  @Nonnull
  private int getFirstDefinedBlockPredIdx(List<Block> preds) {
    for (Block block : preds) {
      int idx = this.blockToIdx.get(block);
      if (this.doms[idx] != -1) {
        return idx;
      }
    }
    return -1;
  }

  @Nonnull
  private int intersect(int b1, int b2) {
    int f1 = b1;
    int f2 = b2;
    while (f1 != f2) {
      if (f1 > f2) {
        f1 = doms[f1];
      } else if (f2 > f1) {
        f2 = doms[f2];
      }
    }
    return f1;
  }
}
