package sootup.core.graph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2024 Junjie Shen
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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.jimple.common.stmt.FallsThroughStmt;

/**
 * A block iterator that iterates through the blocks of a ControlFlowGraph in reverse post-order.
 */
public class ReversePostOrderBlockIterator implements BlockIterator {
  private List<BasicBlock<?>> blocks;
  private int i = 0;

  public ReversePostOrderBlockIterator(@NonNull BasicBlock<?> startNode) {
    blocks =
        StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                    new PostOrderBlockIterator(startNode), Spliterator.ORDERED),
                false)
            .collect(Collectors.toList());
    Collections.reverse(blocks);
    ensureFallthroughConsecutive(blocks);
  }

  /**
   * Scan the RPO list and move each fallthrough successor immediately after its source block. When
   * a block ends with a FallsThroughStmt, the generated code expects no explicit jump to reach the
   * next block; keeping source and target adjacent avoids gratuitous goto instructions.
   *
   * <p>Only forward moves are performed (j > i+1): if the fallthrough target is already ahead of
   * the source or unreachable (back-edge targets in loops), we leave the order as-is.
   */
  private static void ensureFallthroughConsecutive(@NonNull List<BasicBlock<?>> rpo) {
    for (int i = 0; i < rpo.size() - 1; i++) {
      BasicBlock<?> block = rpo.get(i);
      if (!(block.getTail() instanceof FallsThroughStmt)) {
        continue;
      }
      List<? extends BasicBlock<?>> succs = block.getSuccessors();
      if (succs.isEmpty()) {
        continue;
      }
      BasicBlock<?> fallthrough = succs.get(0);
      int j = rpo.indexOf(fallthrough);
      if (j > i + 1) {
        rpo.remove(j);
        rpo.add(i + 1, fallthrough);
      }
    }
  }

  @Override
  public boolean hasNext() {
    return i < blocks.size();
  }

  @Override
  @Nullable
  public BasicBlock<?> next() {
    if (!hasNext()) {
      return null;
    }
    i++;
    return blocks.get(i - 1);
  }
}
