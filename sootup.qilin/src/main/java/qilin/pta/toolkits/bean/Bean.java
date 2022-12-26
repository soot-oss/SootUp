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

package qilin.pta.toolkits.bean;

import qilin.core.PTA;
import qilin.core.context.ContextElements;
import qilin.core.pag.AllocNode;
import qilin.pta.toolkits.common.OAG;
import qilin.util.ANSIColor;
import qilin.util.Pair;
import qilin.util.Stopwatch;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Bean {
    public static void run(PTA pta, Map<Object, Map<Object, Map<Object, Object>>> beanNexCtxMap) {
        System.out.println("Constructing object allocation graph (OAG) ...");
        Stopwatch timer = Stopwatch.newAndStart("OAG construction");
        OAG oag = new OAG(pta);
        oag.build();
        timer.stop();
        System.out.print(ANSIColor.BOLD + "OAG construction: " + ANSIColor.GREEN +
                String.format("%.2fs", timer.elapsed()) + ANSIColor.RESET + "\n");

        System.out.println("Computing contexts...");
        timer.restart();
        // The depth indicates the depth of heap context.
        // The method context has 1 more level than heap context.
        // Here depth is 1 which corresponds to 2-object-sensitive analysis
        // with 1 heap context.
        ContextSelector cs = new RepresentativeContextSelector(oag, 1);
        timer.stop();
        System.out.print(ANSIColor.BOLD + "Context computation: " + ANSIColor.GREEN +
                String.format("%.2fs", timer.elapsed()) + ANSIColor.RESET + "\n");

        writeContext(cs, oag, beanNexCtxMap);
    }

    /*
     * Should be generalized for k >= 3.
     * */
    private static void writeContext(ContextSelector cs, OAG oag, Map<Object, Map<Object, Map<Object, Object>>> beanNexCtxMap) {
        oag.allNodes().forEach(allocator -> {
            Set<ContextElements> ctxs = cs.contextsOf(allocator);
            for (ContextElements ctx : ctxs) {
                AllocNode allocHctx = (AllocNode) ctx.get(0);
                Set<Pair<ContextElements, AllocNode>> csheaps = cs.allocatedBy(ctx, allocator);
                if (csheaps != null) {
                    csheaps.forEach(csheap -> {
                        AllocNode newHctx = (AllocNode) csheap.getFirst().get(0);
                        AllocNode heap = csheap.getSecond();
                        beanNexCtxMap.computeIfAbsent(heap.getNewExpr(), k -> new HashMap<>()).computeIfAbsent(allocator.getNewExpr(), k -> new HashMap<>()).put(allocHctx.getNewExpr(), newHctx.getNewExpr());
                    });
                }
            }
        });
    }

}
