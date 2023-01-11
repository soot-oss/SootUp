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
import javax.annotation.Nullable;

/*@author Zun Wang*/
public class DominanceTree {

  private List<BasicBlock<?>> blocks;
  private Map<BasicBlock<?>, Integer> blockToIdx;
  private List<Integer>[] children;
  private int[] parents;

  public DominanceTree(@Nonnull DominanceFinder dominanceFinder) {
    this.blocks = dominanceFinder.getIdxToBlock();
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
  public List<BasicBlock<?>> getChildren(@Nonnull BasicBlock<?> block) {
    List<BasicBlock<?>> childList = new ArrayList<>();
    int idx = blockToIdx.get(block);
    for (int i : children[idx]) {
      childList.add(blocks.get(i));
    }
    return childList;
  }

  @Nullable
  public BasicBlock<?> getParent(@Nonnull BasicBlock<?> block) {
    int idx = blockToIdx.get(block);
    if (parents[idx] == -1) {
      return null;
    }
    return blocks.get(parents[idx]);
  }

  @Nonnull
  public BasicBlock<?> getRoot() {
    return this.blocks.get(0);
  }

  public void replaceNode(@Nonnull BasicBlock<?> oldBlock, @Nonnull BasicBlock<?> newBlock) {
    if (!this.blockToIdx.containsKey(oldBlock)) {
      throw new RuntimeException(
          "The given replaced block " + oldBlock + "is not in the DominanceTree");
    }
    int idx = this.blockToIdx.get(oldBlock);
    this.blocks.set(idx, newBlock);
    this.blockToIdx.remove(oldBlock);
    this.blockToIdx.put(newBlock, idx);
  }

  @Nonnull
  public List<BasicBlock<?>> getAllNodesDFS() {
    List<BasicBlock<?>> blocks = new ArrayList<>();
    Deque<BasicBlock<?>> queue = new ArrayDeque<>();
    queue.add(getRoot());
    while (!queue.isEmpty()) {
      BasicBlock<?> fb = queue.removeFirst();
      blocks.add(fb);
      if (!getChildren(fb).isEmpty()) {
        List<BasicBlock<?>> children = getChildren(fb);
        for (int i = children.size() - 1; i >= 0; i--) {
          queue.addFirst(children.get(i));
        }
      }
    }
    return blocks;
  }
}
