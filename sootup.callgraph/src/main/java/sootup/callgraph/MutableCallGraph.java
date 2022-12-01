package sootup.callgraph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Linghui Luo, Christian Brüggemann, Ben Hermann, Markus Schmidt
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

import javax.annotation.Nonnull;
import sootup.core.signatures.MethodSignature;

/**
 * This interface defines a mutable call graph. this means a call graph that can be modified after
 * the creation.
 */
public interface MutableCallGraph extends CallGraph {

  /**
   * This method enables to add method that are nodes in the call graph.
   *
   * @param calledMethod the method that will be added to the call graph.
   */
  void addMethod(@Nonnull MethodSignature calledMethod);

  /**
   * This method enables to add calls that are edges in the call graph.
   *
   * @param sourceMethod this parameter defines the source node of the edge in the call graph.
   * @param targetMethod this paramter defines the target node of the edge in the call graph.
   */
  void addCall(@Nonnull MethodSignature sourceMethod, @Nonnull MethodSignature targetMethod);
}
