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

/** A strategy to traverse a StmtGraph in post-order. */
public class PostOrderBlockTraversal implements BlockTraversalStrategy {

  private final StmtGraph<?> cfg;

  public PostOrderBlockTraversal(StmtGraph<?> cfg) {
    this.cfg = cfg;
  }

  public Iterable<BasicBlock<?>> getOrder() {
    return this::iterator;
  }

  @Override
  public BlockIterator iterator() {
    return new PostOrderBlockIterator(this.cfg.getStartingStmtBlock());
  }

  @Override
  @NonNull
  public List<BasicBlock<?>> getBlocksSorted() {
    return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(this.iterator(), Spliterator.ORDERED), false)
        .collect(Collectors.toList());
  }
}
