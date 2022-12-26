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

package qilin.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UnionFindSet<E> {
    private final Map<E, Entry> entries;
    private int nrsets;

    public UnionFindSet(final Collection<E> elems) {
        this.entries = new HashMap<>();
        elems.forEach(elem -> this.entries.put(elem, new Entry(elem)));
        this.nrsets = this.entries.size();
    }

    public boolean union(final E e1, final E e2) {
        final Entry root1 = this.findRoot(this.entries.get(e1));
        final Entry root2 = this.findRoot(this.entries.get(e2));
        if (root1 == root2) {
            return false;
        }
        if (root1.rank < root2.rank) {
            root1.parent = root2;
        } else if (root1.rank > root2.rank) {
            root2.parent = root1;
        } else {
            root2.parent = root1;
            ++root2.rank;
        }
        --this.nrsets;
        return true;
    }

    public boolean isConnected(final E e1, final E e2) {
        final Entry root1 = this.findRoot(this.entries.get(e1));
        final Entry root2 = this.findRoot(this.entries.get(e2));
        return root1 == root2;
    }

    public E find(final E e) {
        final Entry ent = this.findRoot(this.entries.get(e));
        return ent.elem;
    }

    public int numberOfSets() {
        return this.nrsets;
    }

    public Collection<Set<E>> getDisjointSets() {
        return this.entries.keySet().stream()
                .collect(Collectors.groupingBy(this::find, Collectors.toSet())).values();
    }

    private Entry findRoot(final Entry ent) {
        if (ent.parent != ent) {
            ent.parent = this.findRoot(ent.parent);
        }
        return ent.parent;
    }

    private class Entry {
        private final E elem;
        private Entry parent;
        private int rank;

        private Entry(final E elem) {
            this.elem = elem;
            this.parent = this;
            this.rank = 0;
        }
    }
}
