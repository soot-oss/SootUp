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

public class DirectedGraphImpl<N> implements DirectedGraph<N> {
    protected Set<N> nodes;
    protected Map<N, Set<N>> preds;
    protected Map<N, Set<N>> succs;

    public DirectedGraphImpl() {
        this.nodes = new HashSet<>();
        this.preds = new HashMap<>();
        this.succs = new HashMap<>();
    }

    public void addNode(final N node) {
        this.nodes.add(node);
    }

    public void addEdge(final N from, final N to) {
        this.addNode(from);
        this.addNode(to);
        this.preds.computeIfAbsent(to, k -> new HashSet<>()).add(from);
        this.succs.computeIfAbsent(from, k -> new HashSet<>()).add(to);
    }

    @Override
    public Collection<N> allNodes() {
        return this.nodes;
    }

    @Override
    public Collection<N> predsOf(final N n) {
        return this.preds.getOrDefault(n, Collections.emptySet());
    }

    @Override
    public Collection<N> succsOf(final N n) {
        return this.succs.getOrDefault(n, Collections.emptySet());
    }
}
