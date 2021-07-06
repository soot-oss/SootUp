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
import javax.annotation.Nullable;

/*@author Zun Wang*/
public class DominanceTree {

  private Map<Integer, Block> idxToBlock;
  private Map<Block, Integer> blockToIdx;
  public List<Integer>[] children;
  public int[] parents;

  public DominanceTree(@Nonnull DominanceFinder dominanceFinder) {
    this.idxToBlock = dominanceFinder.getIdxToBlock();
    this.blockToIdx = dominanceFinder.getBlockToIdx();
    int[] iDoms = dominanceFinder.getImmediateDominators();
    int treeSize = iDoms.length;
    children = new ArrayList[treeSize];
    parents = new int[treeSize];
    for (int i = 0; i < treeSize; i++) {
      children[i] = new ArrayList<>();
      parents[i] = -1;
    }

    for (int i = 0; i < treeSize; i++) {
      if (iDoms[i] != i) {
        parents[i] = iDoms[i];
        children[iDoms[i]].add(i);
      }
    }
  }

  @Nonnull
  public List<Block> getChildren(@Nonnull Block block) {
    List<Block> childList = new ArrayList<>();
    int idx = blockToIdx.get(block);
    for (int i : children[idx]) {
      childList.add(idxToBlock.get(i));
    }
    return childList;
  }

  @Nullable
  public Block getParent(@Nonnull Block block) {
    int idx = blockToIdx.get(block);
    if (parents[idx] == -1) {
      return null;
    }
    return idxToBlock.get(parents[idx]);
  }

  @Nonnull
  public Block getRoot() {
    return this.idxToBlock.get(0);
  }

  public void replaceNode(@Nonnull Block oldBlock, @Nonnull Block newBlock) {
    if (!this.blockToIdx.keySet().contains(oldBlock)) {
      throw new RuntimeException(
          "The given replaced block " + oldBlock.toString() + "is not in the DominanceTree");
    }
    int idx = this.blockToIdx.get(oldBlock);
    this.idxToBlock.replace(idx, oldBlock, newBlock);
    this.blockToIdx.remove(oldBlock);
    this.blockToIdx.put(newBlock, idx);
  }

  @Nonnull
  public List<Block> getALLNodesDFS() {
    List<Block> blocks = new ArrayList<>();
    Deque<Block> queue = new ArrayDeque<>();
    queue.add(getRoot());
    while (!queue.isEmpty()) {
      Block fb = queue.removeFirst();
      blocks.add(fb);
      if (!getChildren(fb).isEmpty()) {
        List<Block> children = getChildren(fb);
        for (int i = children.size() - 1; i >= 0; i--) {
          queue.addFirst(children.get(i));
        }
      }
    }
    return blocks;
  }
}
