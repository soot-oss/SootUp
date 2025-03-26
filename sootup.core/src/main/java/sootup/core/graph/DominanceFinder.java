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
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see <a
 *     href="https://www.researchgate.net/publication/2569680_A_Simple_Fast_Dominance_Algorithm">
 *     https://www.researchgate.net/publication/2569680_A_Simple_Fast_Dominance_Algorithm </a>
 */
public class DominanceFinder {

  private static Logger LOGGER = LoggerFactory.getLogger(DominanceFinder.class);

  private List<BasicBlock<?>> blocks;
  private Map<BasicBlock<?>, Integer> blockToIdx = new HashMap<>();
  private int[] doms;
  private ArrayList<Integer>[] domFrontiers;
  private BlockAnalysisDirection direction;

  public DominanceFinder(StmtGraph<?> blockGraph) {
    // normal DominanceFinder should be in reverse post order
    this(blockGraph, BlockAnalysisDirection.REVERSEPOSTORDERFORWARD);
  }

  protected DominanceFinder(@NonNull StmtGraph<?> blockGraph, BlockAnalysisDirection direction) {

    // define the blocks' order
    this.direction = direction;
    blocks = direction.getSortedBlocks(blockGraph);
    for (int i = 0; i < blocks.size(); i++) {
      BasicBlock<?> block = blocks.get(i);
      blockToIdx.put(block, i);
    }

    // initialize doms
    final BasicBlock<?> startBlock;
    if (direction == BlockAnalysisDirection.REVERSEPOSTORDERFORWARD
        || direction == BlockAnalysisDirection.POSTORDERBACKWARD) {
      startBlock = blocks.get(0);
      if (direction == BlockAnalysisDirection.POSTORDERBACKWARD) {
        // todo: Postdominantor (POSTORDERBACKWARD) doesn't work for with multiple tail-blocks.
        List<BasicBlock<?>> tails = blockGraph.getTailStmtBlocks();
        if (tails.size() > 1) {
          LOGGER.debug(
              "BlockGraph has multiple tail-blocks, the Post-Dominators Computation could be incorrect!");
        }
      }
    } else {
      throw new RuntimeException("Invalid BlockAnalysisDirection!");
    }
    doms = new int[blocks.size()];
    Arrays.fill(doms, -1);
    doms[0] = 0;

    // calculate immediate dominator for each block
    boolean isChanged = true;
    while (isChanged) {
      isChanged = false;
      for (BasicBlock<?> block : blocks) {
        if (block.equals(startBlock)) {
          continue;
        }
        int blockIdx = blockToIdx.get(block);
        List<BasicBlock<?>> preds = new ArrayList<>(direction.getPredecessors(block));
        int newIdom = getFirstDefinedBlockPredIdx(preds);
        if (!preds.isEmpty() && newIdom != -1) {
          BasicBlock<?> processed = blocks.get(newIdom);
          for (BasicBlock<?> pred : preds) {
            if (pred.equals(processed)) {
              continue;
            }
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
    domFrontiers = new ArrayList[blocks.size()];
    for (int i = 0; i < domFrontiers.length; i++) {
      domFrontiers[i] = new ArrayList<>();
    }

    doms[0] = -1;
    // calculate dominance frontiers for each block
    for (BasicBlock<?> block : blocks) {
      List<BasicBlock<?>> preds = new ArrayList<>(direction.getPredecessors(block));
      if (preds.size() > 1) {
        int blockId = blockToIdx.get(block);
        for (BasicBlock<?> pred : preds) {
          int predId = blockToIdx.get(pred);
          while (predId != -1 && predId != doms[blockId]) {
            domFrontiers[predId].add(blockId);
            predId = doms[predId];
          }
        }
      }
    }

    if (direction == BlockAnalysisDirection.POSTORDERBACKWARD) {
      for (int i = 0; i < domFrontiers.length; i++) {
        if (domFrontiers[i].contains(i)) {
          domFrontiers[i].remove(Integer.valueOf(i));
        }
      }
    }
  }

  public void replaceBlock(@NonNull BasicBlock<?> newBlock, BasicBlock<?> oldBlock) {
    if (!blockToIdx.containsKey(oldBlock)) {
      throw new RuntimeException("The given block: " + oldBlock + " is not in BlockGraph!");
    }
    final Integer idx = blockToIdx.get(oldBlock);
    blockToIdx.put(newBlock, idx);
    blockToIdx.remove(oldBlock);
    blocks.set(idx, newBlock);
  }

  public BasicBlock<?> getImmediateDominator(@NonNull BasicBlock<?> block) {
    if (!blockToIdx.containsKey(block)) {
      throw new RuntimeException("The given block: " + block + " is not in BlockGraph!");
    }
    int idx = blockToIdx.get(block);
    int idomIdx = this.doms[idx];
    if (idomIdx == -1) {
      // start block should not have immediate dominator
      return null;
    }
    return blocks.get(idomIdx);
  }

  @NonNull
  public Set<BasicBlock<?>> getDominanceFrontiers(@NonNull BasicBlock<?> block) {
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

  @NonNull
  public List<BasicBlock<?>> getIdxToBlock() {
    return blocks;
  }

  @NonNull
  public Map<BasicBlock<?>, Integer> getBlockToIdx() {
    return blockToIdx;
  }

  @NonNull
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

  /** Finds the common dominator of two nodes using the meet operator in a dominator tree. */
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
