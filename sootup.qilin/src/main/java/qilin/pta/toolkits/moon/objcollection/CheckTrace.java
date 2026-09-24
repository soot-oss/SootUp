package qilin.pta.toolkits.moon.objcollection;

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

public class CheckTrace {

  private final PagNode node;
  private final CheckStatus state;

  public CheckTrace(PagNode node, CheckStatus state) {
    this.node = node;
    this.state = state;
  }

  public PagNode getNode() {
    return node;
  }

  public CheckStatus getState() {
    return state;
  }

  @Override
  public int hashCode() {
    return Objects.hash(node, state);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof CheckTrace other) {
      if (o == this) return true;
      return this.node.equals(other.node) && this.state.equals(other.state);
    }
    return false;
  }
}
