package sootup.interceptors;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Christian Brüggemann, Zun Wang
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
import sootup.core.graph.BasicBlock;
import sootup.core.graph.MutableBasicBlock;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.model.Body;
import sootup.core.transform.BodyInterceptor;
import sootup.core.views.View;

/**
 * A BodyInterceptor that removes all unreachable stmts from the given Body.
 *
 * @author Zun Wang, Sahil Agichani
 */
public class UnreachableCodeEliminator implements BodyInterceptor {

  @Override
  public void interceptBody(Body.@NonNull BodyBuilder builder, @NonNull View view) {

    MutableStmtGraph graph = builder.getStmtGraph();

    // Because there is a case in android, where the statement graph will be empty
    if (graph.getStmts().isEmpty() && graph.getNodes().isEmpty()) {
      return;
    }

    Collection<? extends BasicBlock<?>> allBlocks = graph.getBlocks();
    MutableBasicBlock startingStmtBlock = (MutableBasicBlock) graph.getStartingStmtBlock();
    Set<MutableBasicBlock> reachableNodes = new HashSet<>();
    Deque<MutableBasicBlock> stack = new ArrayDeque<>();
    stack.push(startingStmtBlock);

    // Traverse the call graph using DFS
    while (!stack.isEmpty()) {
      MutableBasicBlock currentBlock = stack.pop();
      // If the method has already been visited, skip it
      if (!reachableNodes.add(currentBlock)) {
        continue;
      }
      // Get all the successors (i.e., called methods) of the current method
      List<MutableBasicBlock> currentBlockExceptionalSuccessors =
          new ArrayList<>(currentBlock.getExceptionalSuccessors().values());
      List<MutableBasicBlock> currentBlockSuccessors = currentBlock.getSuccessors();
      List<MutableBasicBlock> currentBlockAllSuccessors = new ArrayList<>(currentBlockSuccessors);
      currentBlockAllSuccessors.addAll(currentBlockExceptionalSuccessors);

      // Push the successors into the stack
      for (MutableBasicBlock successor : currentBlockAllSuccessors) {
        if (!reachableNodes.contains(successor)) {
          stack.push(successor);
        }
      }
    }

    Iterator<? extends BasicBlock<?>> iterator = allBlocks.iterator();
    while (iterator.hasNext()) {
      BasicBlock<?> basicBlock = iterator.next();
      if (!reachableNodes.contains(basicBlock)) {
        iterator.remove();
        graph.removeBlock(basicBlock);
      }
    }
  }
}
