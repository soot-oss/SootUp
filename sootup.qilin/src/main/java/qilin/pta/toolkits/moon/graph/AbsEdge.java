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

import java.util.Objects;

public abstract class AbsEdge<Node> {
  private final Node source;
  private final Node target;

  public AbsEdge(Node source, Node target) {
    this.source = source;
    this.target = target;
  }

  public Node source() {
    return source;
  }

  public Node target() {
    return target;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AbsEdge<?> edge = (AbsEdge<?>) o;
    return source().equals(edge.source()) && target().equals(edge.target());
  }

  @Override
  public int hashCode() {
    return Objects.hash(source(), target());
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" + source() + " -> " + target() + '}';
  }
}
