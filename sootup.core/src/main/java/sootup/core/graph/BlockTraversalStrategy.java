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

import java.util.List;

/** An interface for defining a strategy to traverse a StmtGraph. */
public interface BlockTraversalStrategy {

  /**
   * This method provides an iterator to traverse a StmtGraph according to the defined strategy.
   *
   * @return an iterator for traversing StmtGraph
   */
  public BlockIterator iterator();

  /**
   * This method returns a list of Blocks ordered by the traversal sequence.
   *
   * @return a list of Blocks in traversal order
   */
  public List<BasicBlock<?>> getBlocksSorted();
}
