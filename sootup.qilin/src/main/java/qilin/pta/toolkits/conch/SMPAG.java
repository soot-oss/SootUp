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

import qilin.core.pag.*;
import qilin.util.Pair;
import qilin.util.graph.DirectedGraphImpl;
import qilin.util.queue.UniqueQueue;
import soot.util.queue.QueueReader;

import java.util.HashSet;
import java.util.Set;

/*
 * a simplified method PAG with the effects of static load and store being eliminated.
 * */
public class SMPAG extends DirectedGraphImpl<Node> {
    MethodPAG srcmpag;
    Set<Pair<Node, Node>> loads;
    Set<Pair<Node, Node>> stores;

    public SMPAG(MethodPAG srcmpag) {
        this.srcmpag = srcmpag;
        init();
    }

    private void init() {
        loads = new HashSet<>();
        stores = new HashSet<>();
        QueueReader<Node> reader = srcmpag.getInternalReader().clone();
        UniqueQueue<Node> workList = new UniqueQueue<>();
        Set<Node> visit = new HashSet<>();
        while (reader.hasNext()) {
            qilin.core.pag.Node from = reader.next(), to = reader.next();
            if (from instanceof LocalVarNode) {
                if (to instanceof LocalVarNode) {
                    this.addEdge(from, to); // ASSIGN
                } else if (to instanceof FieldRefNode) {
                    this.addEdge(from, to); // STORE
                }  // local-global : A.f = a;


            } else if (from instanceof AllocNode) {
                this.addEdge(from, to); // NEW
            } else if (from instanceof FieldRefNode) {
                this.addEdge(from, to); // LOAD
            } else { // global-local
                workList.add(to);
            }
        }
        while (!workList.isEmpty()) {
            Node curr = workList.poll();
            visit.add(curr);
            if (this.predsOf(curr).isEmpty()) {
                for (Node next : this.succsOf(curr)) {
                    if (!visit.contains(next)) {
                        workList.add(next);
                    }
                    this.preds.get(next).remove(curr);
                }
                if (this.succs.containsKey(curr)) {
                    this.succs.get(curr).clear();
                }
            }
        }

        // initialize stores and loads
        for (Node node : this.allNodes()) {
            if (node instanceof FieldRefNode) {
                for (Node from : this.predsOf(node)) {
                    stores.add(new Pair<>(node, from));
                }
                for (Node to : this.succsOf(node)) {
                    loads.add(new Pair<>(to, node));
                }
            }
        }
    }

    // <FieldRefNode, from>
    public Set<Pair<Node, Node>> getStores() {
        return stores;
    }

    // <to, FieldRefNode>
    public Set<Pair<Node, Node>> getLoads() {
        return loads;
    }
}
