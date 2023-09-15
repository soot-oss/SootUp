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

package qilin.pta.tools;

import qilin.parm.ctxcons.CtxConstructor;
import qilin.parm.heapabst.MahjongAbstractor;
import qilin.parm.select.CtxSelector;
import qilin.parm.select.DebloatingSelector;
import qilin.parm.select.PipelineSelector;
import qilin.parm.select.UniformSelector;
import qilin.pta.PTAConfig;
import qilin.pta.toolkits.mahjong.Mahjong;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * refer to "Efficient and Precise Points-to Analysis: Modeling the Heap by Merging Equivalent Automata" (PLDI'17)
 * */
public class MahjongPTA extends StagedPTA {
    protected final Map<Object, Object> heapModelMap = new HashMap<>();
    public Set<Object> mergedHeap = new HashSet<>();
    public Set<Object> csHeap = new HashSet<>();

    public MahjongPTA(int k, int hk, CtxConstructor ctxCons) {
        this.ctxCons = ctxCons;
        CtxSelector us = new UniformSelector(k, hk);
        CtxSelector ds = new DebloatingSelector(csHeap);
        this.ctxSel = new PipelineSelector(us, ds);
        this.heapAbst = new MahjongAbstractor(pag, mergedHeap, heapModelMap);
        System.out.println("Mahjong ...");
    }

    @Override
    protected void preAnalysis() {
        PTAConfig.v().getPtaConfig().mergeHeap = false;
        prePTA.pureRun();

        Mahjong.run(prePTA, heapModelMap);
        heapModelMap.forEach((origin, merged) -> {
            if (pag.findAllocNode(origin) == null || pag.findAllocNode(merged) == null) {
                return;
            }
            if (pag.findAllocNode(origin).getType() != pag.findAllocNode(merged).getType()) {
                return;
            }
            if (!csHeap.add(merged)) {
                mergedHeap.add(merged);
            }
        });
        csHeap.removeAll(mergedHeap);
        System.out.println("#MERGE HEAP (not-single):" + mergedHeap.size());
        System.out.println("#NON-MERGED HEAP (single):" + csHeap.size());
        for (Object mh : mergedHeap) {
            System.out.println(mh);
        }
    }
}
