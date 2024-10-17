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
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * @see <a
 *     href="https://www.researchgate.net/publication/2569680_A_Simple_Fast_Dominance_Algorithm">
 *     https://www.researchgate.net/publication/2569680_A_Simple_Fast_Dominance_Algorithm </a>
 */
public class DominanceFinder {

  private List<BasicBlock<?>> blocks;
  private Map<BasicBlock<?>, Integer> blockToIdx = new HashMap<>();
  private int[] doms;
  private ArrayList<Integer>[] domFrontiers;
  private BlockAnalysisDirection direction;

  public DominanceFinder(StmtGraph<?> blockGraph) {
    // normal DominanceFinder should be in reverse post order
    this(blockGraph, BlockAnalysisDirection.REVERSEPOSTORDERFORWARD);
  }

  protected DominanceFinder(@Nonnull StmtGraph<?> blockGraph, BlockAnalysisDirection direction) {

    // define the blocks' order
    this.direction = direction;
    blocks = direction.getSortedBlocks(blockGraph);
    for (int i = 0; i < blocks.size(); i++) {
      BasicBlock<?> block = blocks.get(i);
      blockToIdx.put(block, i);
    }

    // initialize doms
    final BasicBlock<?> startBlock;
    if (direction == BlockAnalysisDirection.REVERSEPOSTORDERFORWARD) {
      startBlock = blockGraph.getStartingStmtBlock();
    } else if (direction == BlockAnalysisDirection.POSTORDERBACKWARD) {
      // todo: improve algorithm for graph with multiple tail-blocks
      List<BasicBlock<?>> tails = blockGraph.getTailStmtBlocks();
      if (tails.size() > 1) {
        throw new RuntimeException(
            "BlockAnalysisDirection 'BACKWARD' supports block-graphs containing only one tail-block!");
      }
      startBlock = tails.get(0);
    } else {
      throw new RuntimeException("Invalid BlockAnalysisDirection!");
    }

    int startBlockId = blockToIdx.get(startBlock);
    doms = new int[blocks.size()];
    Arrays.fill(doms, -1);
    doms[startBlockId] = startBlockId;

    // calculate immediate dominator for each block
    boolean isChanged = true;
    while (isChanged) {
      isChanged = false;
      for (BasicBlock<?> block : blocks) {
        if (block.equals(startBlock)) {
          continue;
        }
        int blockIdx = blockToIdx.get(block);
        // ms: should not be necessary preds.addAll(block.getExceptionalPredecessors());
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

    doms[startBlockId] = -1;
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
          domFrontiers[i].remove(new Integer(i));
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

  public BasicBlock<?> getImmediateDominator(@Nonnull BasicBlock<?> block) {
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
