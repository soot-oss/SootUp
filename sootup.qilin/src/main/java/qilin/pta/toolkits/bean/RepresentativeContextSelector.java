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
import qilin.parm.ctxcons.CtxConstructor;
import qilin.pta.toolkits.common.OAG;
import qilin.util.Triple;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Context selector of BEAN. This selector aims to avoid redundant
 * context elements while selecting contexts.
 */
public class RepresentativeContextSelector extends ContextSelector {

    public RepresentativeContextSelector(OAG oag, int depth) {
        super(oag, depth);
    }

    @Override
    protected void selectContext(OAG oag) {
        contextMap = new HashMap<>();
        oag.tailNodes().forEach(node -> {
            mergeContextMap(selectContext(oag, node));
        });
        oag.allNodes().forEach(node -> {
            if (!contextMap.containsKey(node)) {
                mergeContextMap(selectContext(oag, node));
            }
        });
    }

    private Map<AllocNode, Set<ContextElements>> selectContext(OAG oag, AllocNode dest) {
        Map<AllocNode, Set<ContextElements>> tempContextMap = new HashMap<>();
        Queue<Triple<AllocNode, ContextElements, Boolean>> worklist = new LinkedList<>();
        initialWorkList(worklist, oag, dest);
        while (!worklist.isEmpty()) {
            Triple<AllocNode, ContextElements, Boolean> triple = worklist.poll();
            AllocNode heap = triple.getFirst();
            ContextElements ctx = triple.getSecond();
            boolean split = triple.getThird();
            if (!tempContextMap.containsKey(heap)) {
                tempContextMap.put(heap, new HashSet<>());
            }
            if (tempContextMap.get(heap).add(ctx)) {
                Set<AllocNode> reachSuccs = selectReachNodes(oag.getSuccsOf(heap), dest, oag);
                final boolean isFork = reachSuccs.size() > 1;
                reachSuccs.forEach(succ -> {
                    final boolean isJoinSucc = oag.getInDegreeOf(succ) > 1;
                    ContextElements newCtx;
                    boolean succSplit = split;
                    if (split && isJoinSucc && !ctx.contains(heap)) {
                        newCtx = ContextElements.newContext(ctx, heap, depth);
                        succSplit = false;
                    } else {
                        newCtx = ctx;
                    }
                    if (isFork) {
                        succSplit = true;
                    }
                    Set<ContextElements> ctxs = tempContextMap.get(succ);
                    if (ctxs == null || !ctxs.contains(newCtx)) {
                        addAllocation(ctx, heap, newCtx, succ);
                        worklist.add(new Triple<>(succ, newCtx, succSplit));
                    }
                });
            }
        }
        return tempContextMap;
    }

    private void mergeContextMap(Map<AllocNode, Set<ContextElements>> anoContextMap) {
        anoContextMap.forEach((heap, ctxs) -> {
            if (contextMap.containsKey(heap)) {
                contextMap.get(heap).addAll(ctxs);
            } else {
                contextMap.put(heap, ctxs);
            }
        });
    }

    /**
     * Select the nodes from a node set which can reach the destination node
     *
     * @param nodes
     * @param dest
     * @param oag
     * @return
     */
    private Set<AllocNode> selectReachNodes(Collection<AllocNode> nodes, AllocNode dest, OAG oag) {
        return nodes.stream().filter(node -> oag.reaches(node, dest)).collect(Collectors.toSet());
    }

    private void initialWorkList(Queue<Triple<AllocNode, ContextElements, Boolean>> worklist, OAG oag, AllocNode node) {
        Set<AllocNode> reachRoots = selectReachNodes(oag.rootNodes(), node, oag);
        boolean split = reachRoots.size() > 1;
        ContextElements emptyCtx = (ContextElements) CtxConstructor.emptyContext;
        reachRoots.forEach(root ->
                worklist.add(new Triple<>(root, ContextElements.newContext(emptyCtx, root, depth), split)));
    }

}
