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
import qilin.core.pag.PagNode;

public class FlowEdge extends AbsEdge<PagNode> {

  private final FlowKind flowKind;

  public FlowEdge(PagNode src, PagNode tgt, FlowKind flowKind) {
    super(src, tgt);
    this.flowKind = flowKind;
  }

  public FlowKind flowKind() {
    return flowKind;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FlowEdge edge = (FlowEdge) o;
    return source().equals(edge.source())
        && target().equals(edge.target())
        && flowKind.equals(edge.flowKind);
  }

  @Override
  public int hashCode() {
    return Objects.hash(source(), target(), flowKind);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "["
        + flowKind
        + "]"
        + "{"
        + source()
        + " -> "
        + target()
        + '}';
  }
}
