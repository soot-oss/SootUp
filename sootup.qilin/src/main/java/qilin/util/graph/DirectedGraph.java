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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

public interface DirectedGraph<N> {
    Collection<N> allNodes();

    Collection<N> predsOf(final N p);

    Collection<N> succsOf(final N p);

    /* no cache, very slow.*/
    default Collection<N> computeReachableNodes(N source) {
        Set<N> reachableNodes = new HashSet<>();
        Stack<N> stack = new Stack<>();
        stack.push(source);
        while (!stack.isEmpty()) {
            N node = stack.pop();
            if (reachableNodes.add(node)) {
                stack.addAll(succsOf(node));
            }
        }
        return reachableNodes;
    }

    default Collection<N> computeRootNodes() {
        return allNodes().stream().filter(node -> predsOf(node).size() == 0).collect(Collectors.toSet());
    }

    default Collection<N> computeTailNodes() {
        return allNodes().stream().filter(node -> succsOf(node).size() == 0).collect(Collectors.toSet());
    }
}
