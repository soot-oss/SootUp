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

package qilin.pta.toolkits.common;

import qilin.core.PTA;
import qilin.core.builder.MethodNodeFactory;
import qilin.core.pag.AllocNode;
import qilin.core.pag.LocalVarNode;
import qilin.core.pag.MethodPAG;
import qilin.core.sets.PointsToSet;
import qilin.util.PTAUtils;
import qilin.util.graph.DirectedGraph;
import soot.SootMethod;
import soot.util.queue.QueueReader;

import java.util.*;

/**
 * Implementation of Object Allocation Graph (OAG).
 */
public class OAG implements DirectedGraph<AllocNode> {
    protected final PTA pta;
    protected final Map<AllocNode, Set<AllocNode>> successors;
    protected final Map<AllocNode, Set<AllocNode>> predecessors;
    private final Set<AllocNode> nodes = new HashSet<>();
    private Collection<AllocNode> rootNodes;
    private Collection<AllocNode> tailNodes;

    public OAG(PTA prePta) {
        this.pta = prePta;
        this.predecessors = new HashMap<>();
        this.successors = new HashMap<>();
    }

    public void build() {
        buildOAG();
        rootNodes = computeRootNodes();
        tailNodes = computeTailNodes();
    }

    @Override
    public Collection<AllocNode> allNodes() {
        return nodes;
    }

    @Override
    public Collection<AllocNode> predsOf(AllocNode p) {
        return getPredsOf(p);
    }

    @Override
    public Collection<AllocNode> succsOf(AllocNode p) {
        return getSuccsOf(p);
    }

    public Collection<AllocNode> rootNodes() {
        return rootNodes;
    }

    public Collection<AllocNode> tailNodes() {
        return tailNodes;
    }

    public Set<AllocNode> getPredsOf(AllocNode n) {
        return predecessors.getOrDefault(n, Collections.emptySet());
    }

    public Set<AllocNode> getSuccsOf(AllocNode n) {
        return successors.getOrDefault(n, Collections.emptySet());
    }

    public int getInDegreeOf(AllocNode n) {
        return getPredsOf(n).size();
    }

    /**
     * @param source
     * @param dest
     * @return whether there is a path from source to target in the OAG
     */
    Map<AllocNode, Collection<AllocNode>> reachableMap = new HashMap<>();

    public boolean reaches(AllocNode source, AllocNode dest) {
        Collection<AllocNode> reachableNodes = reachableMap.get(source);
        if (reachableNodes == null) {
            reachableNodes = computeReachableNodes(source);
            reachableMap.put(source, reachableNodes);
        }
        return reachableNodes.contains(dest);
    }

    protected void buildOAG() {
        Map<LocalVarNode, Set<AllocNode>> pts = PTAUtils.calcStaticThisPTS(this.pta);
        for (SootMethod method : this.pta.getNakedReachableMethods()) {
            if (method.isPhantom()) {
                continue;
            }
            MethodPAG srcmpag = pta.getPag().getMethodPAG(method);
            MethodNodeFactory srcnf = srcmpag.nodeFactory();
            LocalVarNode thisRef = (LocalVarNode) srcnf.caseThis();
            QueueReader<qilin.core.pag.Node> reader = srcmpag.getInternalReader().clone();
            while (reader.hasNext()) {
                qilin.core.pag.Node from = reader.next(), to = reader.next();
                if (from instanceof AllocNode tgt) {
                    if (PTAUtils.isFakeMainMethod(method)) {
                        // special treatment for fake main
                        AllocNode src = pta.getRootNode();
                        addEdge(src, tgt);
                    } else if (method.isStatic()) {
                        pts.getOrDefault(thisRef, Collections.emptySet()).forEach(src -> {
                            addEdge(src, tgt);
                        });
                    } else {
                        PointsToSet thisPts = pta.reachingObjects(thisRef).toCIPointsToSet();
                        for (Iterator<AllocNode> it = thisPts.iterator(); it.hasNext(); ) {
                            AllocNode src = it.next();
                            addEdge(src, tgt);
                        }
                    }
                }
            }
        }
    }

    /**
     * Add a directed object allocation edge to the OAG.
     */
    protected void addEdge(AllocNode src, AllocNode tgt) {
        nodes.add(src);
        nodes.add(tgt);
        this.predecessors.computeIfAbsent(tgt, k -> new HashSet<>()).add(src);
        this.successors.computeIfAbsent(src, k -> new HashSet<>()).add(tgt);
    }

}
