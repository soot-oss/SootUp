/* Bean - Making k-Object-Sensitive Pointer Analysis More Precise with Still k-Limiting
 *
 * Copyright (C) 2016 Tian Tan, Yue Li, Jingling Xue
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package qilin.pta.toolkits.bean;

import qilin.core.context.ContextElements;
import qilin.core.pag.AllocNode;
import qilin.pta.toolkits.common.OAG;
import qilin.util.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Compute the contexts of each heap object via OAG.
 */
public abstract class ContextSelector {

    protected int depth; // the depth of context
    protected Map<AllocNode, Set<ContextElements>> contextMap;
    protected Map<Pair<ContextElements, AllocNode>, Set<Pair<ContextElements, AllocNode>>> allocation =
            new HashMap<>();

    protected ContextSelector(OAG oag) {
        this(oag, Integer.MAX_VALUE);
    }

    protected ContextSelector(OAG oag, int depth) {
        this.depth = depth;
        selectContext(oag);
    }

    public Set<ContextElements> contextsOf(AllocNode heap) {
        return contextMap.get(heap);
    }

    public Set<Pair<ContextElements, AllocNode>> allocatedBy(ContextElements ctx, AllocNode heap) {
        return allocation.get(new Pair<>(ctx, heap));
    }

    protected abstract void selectContext(OAG oag);

    protected void addAllocation(ContextElements ctx, AllocNode heap, ContextElements newCtx, AllocNode succ) {
        Pair<ContextElements, AllocNode> csheap = new Pair<>(ctx, heap);
        allocation.computeIfAbsent(csheap, k -> new HashSet<>());
        allocation.get(csheap).add(new Pair<>(newCtx, succ));
    }
}
