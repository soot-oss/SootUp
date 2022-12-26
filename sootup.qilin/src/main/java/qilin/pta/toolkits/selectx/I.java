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

package qilin.pta.toolkits.selectx;

import qilin.core.pag.Node;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;


/**
 * local nodes
 */
public abstract class I extends BNode {
    private final Set<I> outIs = new HashSet<>();
    protected Set<L> paras = new HashSet<>();

    I(Node origin) {
        super(origin);
    }

    public void clearParas() {
        this.paras.clear();
    }

    boolean update(I another) {
        return this.paras.addAll(another.paras);
    }

    public boolean addOutEdge(I to) {
        return outIs.add(to);
    }

    @Override
    public boolean addOutEdge(BNode toE) {
        return addOutEdge((I) toE);
    }

    @Override
    public Stream<? extends BNode> forwardTargets() {
        return outIs.stream();
    }

    public Set<I> getOutTargets() {
        return this.outIs;
    }
}
