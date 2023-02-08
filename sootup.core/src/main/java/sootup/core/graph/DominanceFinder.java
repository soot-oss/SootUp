package sootup.core.graph;

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

  private List<BasicBlock<?>> blocks;
  private Map<BasicBlock<?>, Integer> blockToIdx = new HashMap<>();
  private int[] doms;
  private ArrayList<Integer>[] domFrontiers;

  public DominanceFinder(StmtGraph<?> blockGraph) {

    // assign each block a integer id, startBlock's id must be 0
    blocks = new ArrayList<>(blockGraph.getBlocks());
    final BasicBlock<?> startingStmtBlock = blockGraph.getStartingStmtBlock();
    {
      int i = 1;
      for (BasicBlock<?> block : blocks) {
        if (startingStmtBlock == block) {
          blockToIdx.put(block, 0);
        } else {
          blockToIdx.put(block, i);
          i++;
        }
      }
    }

    // initialize doms
    doms = new int[blocks.size()];
    doms[0] = 0;
    for (int i = 1; i < doms.length; i++) {
      doms[i] = -1;
    }

    // calculate immediate dominator for each block
    boolean isChanged = true;
    while (isChanged) {
      isChanged = false;
      for (BasicBlock<?> block : blocks) {
        if (block == startingStmtBlock) {
          continue;
        }
        int blockIdx = blockToIdx.get(block);
        List<BasicBlock<?>> preds = new ArrayList<>(block.getPredecessors());
        // ms: should not be necessary preds.addAll(block.getExceptionalPredecessors());
        int newIdom = getFirstDefinedBlockPredIdx(preds);
        if (!preds.isEmpty() && newIdom != -1) {
          preds.remove(blocks.get(newIdom));
          for (BasicBlock<?> pred : preds) {
            int predIdx = blockToIdx.get(pred);
            if (this.doms[predIdx] != -1) {
              newIdom = isIntersecting(newIdom, predIdx);
            }
          }
          if (doms[blockIdx] != newIdom) {
            doms[blockIdx] = newIdom;
            isChanged = true;
          }
        }
      }
    }

    // initialize domFrontiers
    domFrontiers = new ArrayList[blockGraph.getBlocks().size()];
    for (int i = 0; i < domFrontiers.length; i++) {
      domFrontiers[i] = new ArrayList<>();
    }

    // calculate dominance frontiers for each block
    for (BasicBlock<?> block : blocks) {
      List<BasicBlock<?>> preds = new ArrayList<>(block.getPredecessors());
      // ms: should not be necessary  preds.addAll(block.getExceptionalPredecessors());
      if (preds.size() > 1) {
        int blockId = blockToIdx.get(block);
        for (BasicBlock<?> pred : preds) {
          int predId = blockToIdx.get(pred);
          while (predId != doms[blockId]) {
            domFrontiers[predId].add(blockId);
            predId = doms[predId];
          }
        }
      }
    }
  }

  public void replaceBlock(@Nonnull BasicBlock<?> newBlock, BasicBlock<?> oldBlock) {
    if (!blockToIdx.containsKey(oldBlock)) {
      throw new RuntimeException("The given block: " + oldBlock + " is not in BlockGraph!");
    }
    final Integer idx = blockToIdx.get(oldBlock);
    blockToIdx.put(newBlock, idx);
    blockToIdx.remove(oldBlock);
    blocks.set(idx, newBlock);
  }

  @Nonnull
  public BasicBlock<?> getImmediateDominator(@Nonnull BasicBlock<?> block) {
    if (!blockToIdx.containsKey(block)) {
      throw new RuntimeException("The given block: " + block + " is not in BlockGraph!");
    }
    int idx = blockToIdx.get(block);
    int idomIdx = this.doms[idx];
    return blocks.get(idomIdx);
  }

  @Nonnull
  public Set<BasicBlock<?>> getDominanceFrontiers(@Nonnull BasicBlock<?> block) {
    if (!blockToIdx.containsKey(block)) {
      throw new RuntimeException("The given block: " + block + " is not in BlockGraph!");
    }
    int idx = blockToIdx.get(block);
    Set<BasicBlock<?>> dFs = new HashSet<>();
    ArrayList<Integer> dFs_idx = this.domFrontiers[idx];
    for (Integer i : dFs_idx) {
      dFs.add(blocks.get(i));
    }
    return dFs;
  }

  @Nonnull
  public List<BasicBlock<?>> getIdxToBlock() {
    return blocks;
  }

  @Nonnull
  public Map<BasicBlock<?>, Integer> getBlockToIdx() {
    return blockToIdx;
  }

  @Nonnull
  public int[] getImmediateDominators() {
    return this.doms;
  }

  private int getFirstDefinedBlockPredIdx(List<BasicBlock<?>> preds) {
    for (BasicBlock<?> block : preds) {
      int idx = blockToIdx.get(block);
      if (doms[idx] != -1) {
        return idx;
      }
    }
    return -1;
  }

  private int isIntersecting(int a, int b) {
    while (a != b) {
      if (a > b) {
        a = doms[a];
      } else {
        b = doms[b];
      }
    }
    return a;
  }
}
