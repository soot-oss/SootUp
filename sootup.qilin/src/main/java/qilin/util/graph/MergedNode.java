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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MergedNode<N> {
    private Set<MergedNode<N>> preds;
    private Set<MergedNode<N>> succs;
    private final Set<N> content;

    public MergedNode(final Collection<N> content) {
        this.content = new HashSet<>(content);
    }

    public void addPred(final MergedNode<N> pred) {
        if (this.preds == null) {
            this.preds = new HashSet<>(4);
        }
        this.preds.add(pred);
    }

    public Set<MergedNode<N>> getPreds() {
        return (this.preds == null) ? Collections.emptySet() : this.preds;
    }

    public void addSucc(final MergedNode<N> succ) {
        if (this.succs == null) {
            this.succs = new HashSet<>(4);
        }
        this.succs.add(succ);
    }

    public Set<MergedNode<N>> getSuccs() {
        return (this.succs == null) ? Collections.emptySet() : this.succs;
    }

    public Set<N> getContent() {
        return this.content;
    }

    @Override
    public String toString() {
        return this.content.toString();
    }
}
