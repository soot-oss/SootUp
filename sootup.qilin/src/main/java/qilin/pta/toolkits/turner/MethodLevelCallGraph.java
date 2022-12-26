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

package qilin.pta.toolkits.turner;

import qilin.util.graph.DirectedGraph;
import qilin.util.graph.DirectedGraphImpl;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;

import java.util.Collection;

public class MethodLevelCallGraph implements DirectedGraph<SootMethod> {
    private final CallGraph callGraph;
    private final DirectedGraphImpl<SootMethod> mcg;

    public MethodLevelCallGraph(CallGraph callGraph) {
        this.callGraph = callGraph;
        this.mcg = new DirectedGraphImpl<>();
        init();
    }

    private void init() {
        callGraph.iterator().forEachRemaining(edge -> {
            SootMethod src = edge.getSrc().method();
            SootMethod tgt = edge.getTgt().method();
            if (src != null && tgt != null) {
                mcg.addEdge(src, tgt);
            } else {
                if (src != null) {
                    mcg.addNode(src);
                }
                if (tgt != null) {
                    mcg.addNode(tgt);
                }
            }
        });
    }

    @Override
    public Collection<SootMethod> allNodes() {
        return mcg.allNodes();
    }

    @Override
    public Collection<SootMethod> predsOf(SootMethod n) {
        return mcg.predsOf(n);
    }

    @Override
    public Collection<SootMethod> succsOf(final SootMethod n) {
        return mcg.succsOf(n);
    }
}
