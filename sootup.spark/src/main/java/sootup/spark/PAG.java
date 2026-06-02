package sootup.spark;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2002-2026 Ondrej Lhotak, Kadiray Karakaya and others
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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.graph4j.Digraph;
import org.graph4j.GraphBuilder;
import sootup.spark.node.AllocationNode;
import sootup.spark.node.InstanceFieldRefNode;
import sootup.spark.node.Node;
import sootup.spark.node.VariableNode;

/** Pointer assigment graph */
@Slf4j
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PAG {

  SparkOptions options;
  @NonNull Digraph<Node, PAGEdge> delegate = GraphBuilder.empty().buildDigraph();

  public PAG(@NonNull SparkOptions options) {
    this.options = options;
  }

  public void addEdge(Node source, Node target) {
    if (source instanceof VariableNode) {
      if (target instanceof VariableNode) {
        addEdge(source, target, PAGEdge.assignment());
      } else if (target instanceof InstanceFieldRefNode) {
        addEdge(source, target, PAGEdge.store());
      }
    } else if (source instanceof InstanceFieldRefNode) {
      addEdge(source, target, PAGEdge.load());
    } else if (source instanceof AllocationNode) {
      addEdge(source, target, PAGEdge.allocation());
    } else {
      log.error("Invalid edge type for source: {} -> target: {}", source, target);
    }
  }

  private void addEdge(Node source, Node target, PAGEdge edge) {
    int sIdx = delegate.findVertex(source);
    if (sIdx == -1) sIdx = delegate.addLabeledVertex(source);
    int tIdx = delegate.findVertex(target);
    if (tIdx == -1) tIdx = delegate.addLabeledVertex(target);
    delegate.addLabeledEdge(sIdx, tIdx, edge);
  }
}
