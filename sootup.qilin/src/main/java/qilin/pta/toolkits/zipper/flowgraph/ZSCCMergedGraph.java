package qilin.pta.toolkits.zipper.flowgraph;

/*-
 * #%L
 * SootUp - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Markus Schmidt and others
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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import qilin.util.graph.DirectedGraph;
import qilin.util.graph.StronglyConnectedComponents;

public class ZSCCMergedGraph<N> implements DirectedGraph<ZMergedNode<N>> {
  private Set<ZMergedNode<N>> nodes;
  private final Map<N, ZMergedNode<N>> nodeMap = new HashMap<>();

  public ZSCCMergedGraph(final DirectedGraph<N> graph) {
    this.init(graph);
  }

  public ZMergedNode<N> getLstMergedNode(N contentNode) {
    return nodeMap.get(contentNode);
  }

  @Override
  public Collection<ZMergedNode<N>> allNodes() {
    return this.nodes;
  }

  @Override
  public Collection<ZMergedNode<N>> predsOf(final ZMergedNode<N> node) {
    return node.getPreds();
  }

  @Override
  public Collection<ZMergedNode<N>> succsOf(final ZMergedNode<N> node) {
    return node.getSuccs();
  }

  private void init(final DirectedGraph<N> graph) {
    this.nodes = new HashSet<>();
    StronglyConnectedComponents<N> scc = new StronglyConnectedComponents<>(graph);
    scc.getComponents()
        .forEach(
            component -> {
              final ZMergedNode<N> node2 = new ZMergedNode<>(component);
              component.forEach(n -> nodeMap.put(n, node2));
              this.nodes.add(node2);
            });
    this.nodes.forEach(
        node ->
            node.getContent().stream()
                .map(graph::succsOf)
                .flatMap(Collection::stream)
                .map(nodeMap::get)
                .filter(succ -> succ != node)
                .forEach(
                    succ -> {
                      node.addSucc(succ);
                      succ.addPred(node);
                    }));
  }
}
