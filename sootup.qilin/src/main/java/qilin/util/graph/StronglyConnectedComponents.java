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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class StronglyConnectedComponents<N> {
    private final List<List<N>> componentList;
    private final List<List<N>> trueComponentList;
    private int index;
    private Map<N, Integer> indexForNode;
    private Map<N, Integer> lowlinkForNode;
    private Stack<N> stack;
    private DirectedGraph<N> graph;

    public StronglyConnectedComponents(final DirectedGraph<N> graph) {
        this.componentList = new ArrayList<>();
        this.trueComponentList = new ArrayList<>();
        this.index = 0;
        this.graph = graph;
        this.stack = new Stack<>();
        this.indexForNode = new HashMap<>();
        this.lowlinkForNode = new HashMap<>();
        for (final N node : graph.allNodes()) {
            if (!this.indexForNode.containsKey(node)) {
                this.recurse(node);
            }
        }
        this.validate(graph, this.componentList);
        this.indexForNode = null;
        this.lowlinkForNode = null;
        this.stack = null;
        this.graph = null;
    }

    public List<List<N>> getComponents() {
        return this.componentList;
    }

    public List<List<N>> getTrueComponents() {
        return this.trueComponentList;
    }

    private void recurse(final N node) {
        this.indexForNode.put(node, this.index);
        this.lowlinkForNode.put(node, this.index);
        ++this.index;
        this.stack.push(node);
        for (final N succ : this.graph.succsOf(node)) {
            if (!this.indexForNode.containsKey(succ)) {
                this.recurse(succ);
                this.lowlinkForNode.put(node, Math.min(this.lowlinkForNode.get(node), this.lowlinkForNode.get(succ)));
            } else {
                if (!this.stack.contains(succ)) {
                    continue;
                }
                this.lowlinkForNode.put(node, Math.min(this.lowlinkForNode.get(node), this.indexForNode.get(succ)));
            }
        }
        if (this.lowlinkForNode.get(node) == (int) this.indexForNode.get(node)) {
            final List<N> scc = new ArrayList<>();
            N v2;
            do {
                v2 = this.stack.pop();
                scc.add(v2);
            } while (node != v2);
            this.componentList.add(scc);
            if (scc.size() > 1) {
                this.trueComponentList.add(scc);
            } else {
                final N n = scc.get(0);
                if (this.graph.succsOf(n).contains(n)) {
                    this.trueComponentList.add(scc);
                }
            }
        }
    }

    private void validate(final DirectedGraph<N> graph, final List<List<N>> SCCs) {
        assert graph.allNodes().size() == SCCs.stream().mapToInt(List::size).sum();
    }
}
