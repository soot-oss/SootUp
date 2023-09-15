/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.util.graph;

import java.util.*;

public class SCCMergedGraph<N> implements DirectedGraph<MergedNode<N>> {
    private Set<MergedNode<N>> nodes;
    private final Map<N, MergedNode<N>> nodeMap = new HashMap<>();

    public SCCMergedGraph(final DirectedGraph<N> graph) {
        this.init(graph);
    }

    public MergedNode<N> getMergedNode(N contentNode) {
        return nodeMap.get(contentNode);
    }

    @Override
    public Collection<MergedNode<N>> allNodes() {
        return this.nodes;
    }

    @Override
    public Collection<MergedNode<N>> predsOf(final MergedNode<N> node) {
        return node.getPreds();
    }

    @Override
    public Collection<MergedNode<N>> succsOf(final MergedNode<N> node) {
        return node.getSuccs();
    }

    private void init(final DirectedGraph<N> graph) {
        this.nodes = new HashSet<>();
        final StronglyConnectedComponents<N> scc = new StronglyConnectedComponents<>(graph);
        scc.getComponents().forEach(component -> {
            final MergedNode<N> node2 = new MergedNode<>(component);
            component.forEach(n -> nodeMap.put(n, node2));
            this.nodes.add(node2);
        });
        this.nodes.forEach(node -> node.getContent().stream().map(graph::succsOf).flatMap(Collection::stream)
                .map(nodeMap::get).filter(succ -> succ != node).forEach(succ -> {
                    node.addSucc(succ);
                    succ.addPred(node);
                }));
    }
}
