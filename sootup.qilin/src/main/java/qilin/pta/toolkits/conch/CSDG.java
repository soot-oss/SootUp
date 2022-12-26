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

package qilin.pta.toolkits.conch;

import qilin.core.pag.AllocNode;
import qilin.util.graph.DirectedGraphImpl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/*
 * context-sensitivity dependent graph:
 * an edge l1 --> l2 means if l2 is context-insensitive, then l1 should be context-insensitive.
 * if l2 is context-sensitive, then l1 also should be context-sensitive.
 * if edges form a cycle, i.e, l1-->l2-->l3-->l1 and non nodes's context-sensitivity
 *  in this cycle could be decide yet, we assume all of them to be context-insensitive.
 * To efficiently check Obs 3(b) (lines 18-23 in Algorithm 1), we introduce this graph structure.
 * */
public class CSDG extends DirectedGraphImpl<AllocNode> {
    /*
     * Remove node on the graph that does not have successor.
     * */
    public void removeNode(final AllocNode to) {
        if (succs.containsKey(to) && !succs.get(to).isEmpty()) {
            return;
        }
        succs.remove(to);
        if (preds.containsKey(to)) {
            for (AllocNode from : preds.get(to)) {
                succs.get(from).remove(to);
            }
            preds.remove(to);
        }
        nodes.remove(to);
    }

    public Set<AllocNode> noOutDegreeNodes() {
        Set<AllocNode> ret = new HashSet<>();
        for (AllocNode node : allNodes()) {
            if (succs.getOrDefault(node, Collections.emptySet()).isEmpty()) {
                ret.add(node);
            }
        }
        return ret;
    }
}
