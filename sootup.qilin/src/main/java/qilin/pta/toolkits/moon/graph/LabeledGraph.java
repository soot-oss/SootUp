package qilin.pta.toolkits.moon.graph;

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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class LabeledGraph<N, L> {

  private final SetMultimap<N, LabelEdge> outEdges;
  private final SetMultimap<N, LabelEdge> inEdges;

  private final Set<N> nodes;

  public LabeledGraph() {
    this.outEdges = HashMultimap.create();
    this.inEdges = HashMultimap.create();
    this.nodes = new HashSet<>();
  }

  private LabeledGraph(LabeledGraph<N, L> other) {
    this.outEdges = Multimaps.unmodifiableSetMultimap(other.outEdges);
    this.inEdges = Multimaps.unmodifiableSetMultimap(other.inEdges);
    this.nodes = Collections.unmodifiableSet(other.nodes);
  }

  public void addEdge(N from, N to, L label) {
    LabelEdge labelEdge = new LabelEdge(from, to, label);
    outEdges.put(from, labelEdge);
    inEdges.put(to, labelEdge);
    nodes.add(from);
    nodes.add(to);
  }

  public Set<LabelEdge> getOutEdgesOf(N node) {
    return outEdges.get(node);
  }

  public Set<LabelEdge> getInEdgesOf(N node) {
    return inEdges.get(node);
  }

  public static <N, L> LabeledGraph<N, L> unmodifiableGraph(LabeledGraph<N, L> g) {
    return new LabeledGraph<>(g);
  }

  public Set<N> getNodes() {
    return nodes;
  }

  public class LabelEdge {
    N from;
    N to;
    L label;

    public LabelEdge(N from, N to, L label) {
      this.from = from;
      this.to = to;
      this.label = label;
    }

    @Override
    public int hashCode() {
      return Objects.hash(from, to, label);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null || getClass() != obj.getClass()) return false;
      LabelEdge other = (LabelEdge) obj;
      return from.equals(other.from) && to.equals(other.to) && label.equals(other.label);
    }

    public N source() {
      return from;
    }

    public N target() {
      return to;
    }

    public L label() {
      return label;
    }
  }
}
