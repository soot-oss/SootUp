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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Reachability<N> {
    private final DirectedGraph<N> graph;
    private final Map<N, Set<N>> reachableNodes;
    private final Map<N, Set<N>> reachToNodes;

    public Reachability(final DirectedGraph<N> graph) {
        this.reachableNodes = new HashMap<>();
        this.reachToNodes = new HashMap<>();
        this.graph = graph;
    }

    public Set<N> reachableNodesFrom(final N source) {
        if (!this.reachableNodes.containsKey(source)) {
            final Set<N> visited = new HashSet<>();
            final Deque<N> stack = new ArrayDeque<>();
            stack.push(source);
            while (!stack.isEmpty()) {
                final N node = stack.pop();
                visited.add(node);
                this.graph.succsOf(node).stream().filter(n -> !visited.contains(n)).forEach(stack::push);
            }
            this.reachableNodes.put(source, visited);
        }
        return this.reachableNodes.get(source);
    }

    public Set<N> nodesReach(final N target) {
        if (!this.reachToNodes.containsKey(target)) {
            final Set<N> visited = new HashSet<>();
            final Deque<N> stack = new ArrayDeque<>();
            stack.push(target);
            while (!stack.isEmpty()) {
                final N node = stack.pop();
                visited.add(node);
                this.graph.predsOf(node).stream().filter(n -> !visited.contains(n)).forEach(stack::push);
            }
            this.reachToNodes.put(target, visited);
        }
        return this.reachToNodes.get(target);
    }

    public Set<N> passedNodes(final N source, final N target) {
        final Set<N> reachableFromSource = this.reachableNodesFrom(source);
        final Set<N> reachToTarget = this.nodesReach(target);
        final Set<N> ret = new HashSet<>(reachableFromSource);
        ret.retainAll(reachToTarget);
        return ret;
    }
}
