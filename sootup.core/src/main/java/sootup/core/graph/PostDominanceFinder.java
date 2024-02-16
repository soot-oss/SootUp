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
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;

/**
 * @author Zun Wang
 * @see <a
 *     href="https://www.researchgate.net/publication/2569680_A_Simple_Fast_Dominance_Algorithm">
 *     https://www.researchgate.net/publication/2569680_A_Simple_Fast_Dominance_Algorithm </a>
 */
public class PostDominanceFinder {

  private final List<BasicBlock<?>> blocks;
  private final Map<BasicBlock<?>, Integer> blockToIdx = new HashMap<>();
  private final int[] pdoms;
  private final ArrayList<Integer>[] pdomFrontiers;

  public PostDominanceFinder(StmtGraph<?> blockGraph) {
    // we're locked into providing a List<BasicBlock<?>>, not a List<? extends BasicBlock<?>>, so
    // we'll use the block iterator directly (which provides this type) rather than
    // #getBlocksSorted.
    blocks =
        StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                    blockGraph.getBlockIterator(), Spliterator.ORDERED),
                false)
            .collect(Collectors.toList());
    Collections.reverse(blocks);

    final MutableBasicBlock syntheticTailStmtBlock = new MutableBasicBlock();
    blocks.add(0, syntheticTailStmtBlock);

    // assign each block a integer id. The tail block must have id 0; rely on
    // getBlocksSorted to have put the tail block first.
    for (int i = 0; i < blocks.size(); i++) {
      BasicBlock<?> block = blocks.get(i);
      blockToIdx.put(block, i);
    }

    // initialize pdoms
    pdoms = new int[blocks.size()];
    pdoms[0] = 0;
    for (int i = 1; i < pdoms.length; i++) {
      pdoms[i] = -1;
    }

    // calculate immediate postdominator for each block
    boolean isChanged = true;
    while (isChanged) {
      isChanged = false;
      for (BasicBlock<?> block : blocks) {
        if (block.equals(syntheticTailStmtBlock)) {
          continue;
        }
        int blockIdx = blockToIdx.get(block);
        List<BasicBlock<?>> successors = new ArrayList<>(block.getSuccessors());
        // ms: should not be necessary successors.addAll(block.getExceptionalPredecessors());
        int newIpdom = getFirstDefinedBlockPredIdx(successors);
        if (!successors.isEmpty() && newIpdom != -1) {
          successors.remove(blocks.get(newIpdom));
          for (BasicBlock<?> pred : successors) {
            int predIdx = blockToIdx.get(pred);
            if (this.pdoms[predIdx] != -1) {
              newIpdom = isIntersecting(newIpdom, predIdx);
            }
          }
          if (pdoms[blockIdx] != newIpdom) {
            pdoms[blockIdx] = newIpdom;
            isChanged = true;
          }
        }
      }
    }

    // initialize domFrontiers
    pdomFrontiers = new ArrayList[blockGraph.getBlocks().size() + 1];
    for (int i = 0; i < pdomFrontiers.length; i++) {
      pdomFrontiers[i] = new ArrayList<>();
    }

    // calculate post-dominance frontiers for each block
    for (BasicBlock<?> block : blocks) {
      List<BasicBlock<?>> preds = new ArrayList<>(block.getSuccessors());
      // ms: should not be necessary  preds.addAll(block.getExceptionalPredecessors());
      if (preds.size() > 1) {
        int blockId = blockToIdx.get(block);
        for (BasicBlock<?> pred : preds) {
          int predId = blockToIdx.get(pred);
          while (predId != pdoms[blockId]) {
            pdomFrontiers[predId].add(blockId);
            predId = pdoms[predId];
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
  public BasicBlock<?> getImmediatePostDominator(@Nonnull BasicBlock<?> block) {
    if (!blockToIdx.containsKey(block)) {
      throw new RuntimeException("The given block: " + block + " is not in BlockGraph!");
    }
    int idx = blockToIdx.get(block);
    int ipdomIdx = this.pdoms[idx];
    return blocks.get(ipdomIdx);
  }

  @Nonnull
  public Set<BasicBlock<?>> getPostDominanceFrontiers(@Nonnull BasicBlock<?> block) {
    if (!blockToIdx.containsKey(block)) {
      throw new RuntimeException("The given block: " + block + " is not in BlockGraph!");
    }
    int idx = blockToIdx.get(block);
    Set<BasicBlock<?>> pdfs = new HashSet<>();
    ArrayList<Integer> pdfs_idx = this.pdomFrontiers[idx];
    for (Integer i : pdfs_idx) {
      pdfs.add(blocks.get(i));
    }
    return pdfs;
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
  public int[] getImmediatePostDominators() {
    return this.pdoms;
  }

  private int getFirstDefinedBlockPredIdx(List<BasicBlock<?>> preds) {
    for (BasicBlock<?> block : preds) {
      int idx = blockToIdx.get(block);
      if (pdoms[idx] != -1) {
        return idx;
      }
    }
    return -1;
  }

  private int isIntersecting(int a, int b) {
    while (a != b) {
      if (a > b) {
        a = pdoms[a];
      } else {
        b = pdoms[b];
      }
    }
    return a;
  }
}
