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

import java.util.Set;
import java.util.stream.Stream;

public abstract class BNode {
    public Node sparkNode;
    /**
     * visited is not overrided by para: this represents value flows in *any* contexts,
     * this can avoid unmatched exit without missing exit from inner alloc or global
     */
    private boolean visited = false;

    BNode(Node origin) {
        this.sparkNode = origin;
    }

    public void reset() {
        visited = false;
    }

    public boolean isVisited() {
        return this.visited;
    }

    public boolean setVisited() {
        if (visited)
            return false;
        return visited = true;
    }

    public abstract boolean addOutEdge(BNode to);

    public abstract Stream<? extends BNode> forwardTargets();

    @Override
    public String toString() {
        return this.getClass().toString() + " : " + sparkNode.toString();
    }
}
