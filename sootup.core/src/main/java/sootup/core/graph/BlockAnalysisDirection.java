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

import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * This enum class is used to specify the direction of block analysis. Every enum direction name
 * consists two parts: Block Order and Analysis Direction. eg: POSTORDERBACKWARD indicates that the
 * sorted blocks are ordered in post-order(POSTORDER) and the analysis direction is from root to
 * leaves (BACKWARD).
 */
public enum BlockAnalysisDirection {
  POSTORDERBACKWARD {
    @Override
    @NonNull List<BasicBlock<?>> getPredecessors(BasicBlock<?> block) {
      return (List<BasicBlock<?>>) block.getSuccessors();
    }

    @NonNull
    @Override
    List<BasicBlock<?>> getSortedBlocks(StmtGraph<?> blockGraph) {
      PostOrderBlockTraversal traversal = new PostOrderBlockTraversal(blockGraph);
      return Collections.unmodifiableList(traversal.getBlocksSorted());
    }
  },
  REVERSEPOSTORDERFORWARD {
    @Override
    @NonNull List<BasicBlock<?>> getPredecessors(BasicBlock<?> block) {
      return (List<BasicBlock<?>>) block.getPredecessors();
    }

    @NonNull
    @Override
    List<BasicBlock<?>> getSortedBlocks(StmtGraph<?> blockGraph) {
      ReversePostOrderBlockTraversal traversal = new ReversePostOrderBlockTraversal(blockGraph);
      return Collections.unmodifiableList(traversal.getBlocksSorted());
    }
  };

  @NonNull
  abstract List<BasicBlock<?>> getPredecessors(BasicBlock<?> block);

  @NonNull
  abstract List<BasicBlock<?>> getSortedBlocks(StmtGraph<?> blockGraph);
}
