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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** A block iterator that iterates through the blocks of a StmtGraph in post-order. */
public class PostOrderBlockIterator implements BlockIterator {

  private final Stack<Frame> stack = new Stack<>();
  private final Set<BasicBlock<?>> visited = new HashSet<>();

  public PostOrderBlockIterator(@NonNull BasicBlock<?> startNode) {
    visitNode(startNode);
    stack.push(new Frame(startNode, ((List<BasicBlock<?>>) startNode.getSuccessors()).iterator()));
  }

  private boolean visitNode(@NonNull BasicBlock<?> node) {
    return visited.add(node);
  }

  @Override
  public boolean hasNext() {
    return !stack.isEmpty();
  }

  @Override
  @Nullable
  public BasicBlock<?> next() {
    while (!stack.isEmpty()) {
      Frame frame = stack.peek();
      if (frame.succIterator.hasNext()) {
        BasicBlock<?> succ = frame.succIterator.next();
        if (visitNode(succ)) {
          List<BasicBlock<?>> esuccs =
              succ.getExceptionalSuccessors().values().stream().collect(Collectors.toList());
          List<BasicBlock<?>> succs = (List<BasicBlock<?>>) succ.getSuccessors();
          succs.addAll(esuccs);
          stack.push(new Frame(succ, succs.iterator()));
        }
      } else {
        stack.pop();
        return frame.node;
      }
    }
    return null;
  }

  private static class Frame {
    final BasicBlock<?> node;
    final Iterator<BasicBlock<?>> succIterator;

    Frame(BasicBlock<?> node, Iterator<BasicBlock<?>> childIterator) {
      this.node = node;
      this.succIterator = childIterator;
    }
  }
}
