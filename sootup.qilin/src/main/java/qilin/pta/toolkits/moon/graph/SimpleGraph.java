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
import com.google.common.collect.SetMultimap;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SimpleGraph<N> {

  private final Set<N> nodes = new HashSet<>();

  private final SetMultimap<N, N> preds = HashMultimap.create();

  private final SetMultimap<N, N> succs = HashMultimap.create();

  public SimpleGraph() {}

  public void addNode(N node) {
    nodes.add(node);
  }

  public void addEdge(N source, N target) {
    nodes.add(source);
    nodes.add(target);
    preds.put(target, source);
    succs.put(source, target);
  }

  public boolean hasEdge(N source, N target) {
    return succs.containsKey(source) && succs.get(source).contains(target);
  }

  public Set<N> getPredsOf(N node) {
    return preds.get(node);
  }

  public Set<N> getSuccsOf(N node) {
    return succs.get(node);
  }

  public Set<N> getNodes() {
    return Collections.unmodifiableSet(nodes);
  }
}
