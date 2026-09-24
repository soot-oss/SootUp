package qilin.pta.toolkits.zipper.flowgraph;

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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ZMergedNode<N> {
  private Set<ZMergedNode<N>> preds;
  private Set<ZMergedNode<N>> succs;
  private final List<N> content;

  public ZMergedNode(final List<N> content) {
    this.content = List.copyOf(content);
  }

  public void addPred(final ZMergedNode<N> pred) {
    if (this.preds == null) {
      this.preds = new HashSet<>(4);
    }
    this.preds.add(pred);
  }

  public Set<ZMergedNode<N>> getPreds() {
    return (this.preds == null) ? Collections.emptySet() : this.preds;
  }

  public void addSucc(final ZMergedNode<N> succ) {
    if (this.succs == null) {
      this.succs = new HashSet<>(4);
    }
    this.succs.add(succ);
  }

  public Set<ZMergedNode<N>> getSuccs() {
    return (this.succs == null) ? Collections.emptySet() : this.succs;
  }

  public List<N> getContent() {
    return this.content;
  }

  @Override
  public String toString() {
    return this.content.toString();
  }
}
