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

  public List<N> reverse_sort(final DirectedGraph<N> graph) {
    LinkedList<N> sortedList = new LinkedList<>();
    Set<N> visited = new HashSet<>();

    graph.allNodes().stream()
        .filter(n -> graph.succsOf(n).isEmpty())
        .forEach(n -> collect_reversed(graph, visited, sortedList, n));
    return sortedList;
  }

  private void collect_reversed(
      final DirectedGraph<N> graph,
      final Set<N> visited,
      final LinkedList<N> sortedList,
      final N node) {
    if (visited.add(node)) {
      graph.predsOf(node).forEach(n -> collect_reversed(graph, visited, sortedList, n));
      sortedList.addFirst(node);
    }
  }

  public List<N> sort(final DirectedGraph<N> graph) {
    LinkedList<N> sortedList = new LinkedList<>();
    Set<N> visited = new HashSet<>();

    graph.allNodes().stream()
        .filter(n -> graph.succsOf(n).isEmpty())
        .forEach(n -> collect(graph, visited, sortedList, n));
    return sortedList;
  }

  private void collect(
      final DirectedGraph<N> graph,
      final Set<N> visited,
      final LinkedList<N> sortedList,
      final N node) {
    if (visited.add(node)) {
      graph.predsOf(node).forEach(n -> collect(graph, visited, sortedList, n));
      sortedList.addLast(node);
    }
  }
}
