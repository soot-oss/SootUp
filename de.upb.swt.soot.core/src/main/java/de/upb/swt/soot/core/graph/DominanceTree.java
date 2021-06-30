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

import org.checkerframework.checker.units.qual.A;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/*@author Zun Wang*/
public class DominanceTree {

  private Map<Integer, Block> idxToBlock;
  private Map<Block, Integer> blockToIdx;
  public List<Integer>[] children;
  public int[] parents;

  public DominanceTree(DominanceFinder dominanceFinder) {
    this.idxToBlock = dominanceFinder.getIdxToBlock();
    this.blockToIdx = dominanceFinder.getBlockToIdx();
    int[] iDoms = dominanceFinder.getImmediateDominators();
    int treeSize = iDoms.length;
    children = new ArrayList[treeSize];
    parents = new int[treeSize];
    for(int i = 0; i < treeSize; i++){
      children[i] = new ArrayList<>();
      parents[i] = -1;
    }

    for(int i = 0; i < treeSize; i++){
      if(iDoms[i] != i){
        parents[i] = iDoms[i];
        children[iDoms[i]].add(i);
      }
    }
  }

  @Nonnull
  public List<Block> getChildren(@Nonnull Block block) {
    List<Block> childList= new ArrayList<>();
    int idx = blockToIdx.get(block);
    for(int i : children[idx]){
      childList.add(idxToBlock.get(i));
    }
    return childList;
  }

  @Nullable
  public Block getParent(@Nonnull Block block) {
    int idx = blockToIdx.get(block);
    if(parents[idx] == -1){
      return null;
    }
    return idxToBlock.get(parents[idx]);
  }

  @Nonnull
  public Block getRoot(){
    return this.idxToBlock.get(0);
  }

}
