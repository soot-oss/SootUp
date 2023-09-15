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

public class TopologicalSorter<N> {
    private DirectedGraph<N> graph;
    private List<N> sortedList;
    private Set<N> visited;

    public List<N> sort(final DirectedGraph<N> graph) {
        return this.sort(graph, false);
    }

    public List<N> sort(final DirectedGraph<N> graph, final boolean reverse) {
        this.initialize(graph);
        graph.allNodes().stream().filter(n -> graph.succsOf(n).isEmpty()).forEach(this::visit);
        List<N> result = this.sortedList;
        if (reverse) {
            Collections.reverse(result);
        }
        this.clear();
        return result;
    }

    private void initialize(final DirectedGraph<N> graph) {
        this.graph = graph;
        this.sortedList = new LinkedList<>();
        this.visited = new HashSet<>();
    }

    private void visit(final N node) {
        if (!this.visited.contains(node)) {
            this.visited.add(node);
            this.graph.predsOf(node).forEach(this::visit);
            this.sortedList.add(node);
        }
    }

    private void clear() {
        this.graph = null;
        this.sortedList = null;
        this.visited = null;
    }
}
