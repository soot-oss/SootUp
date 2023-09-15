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
import qilin.parm.heapabst.AllocSiteAbstractor;
import qilin.parm.heapabst.HeuristicAbstractor;
import qilin.parm.select.BeanSelector;
import qilin.parm.select.CtxSelector;
import qilin.parm.select.HeuristicSelector;
import qilin.parm.select.PipelineSelector;
import qilin.pta.PTAConfig;
import qilin.pta.toolkits.bean.Bean;
import qilin.util.Stopwatch;

import java.util.HashMap;
import java.util.Map;

/*
 * refer to "Making k-Object-Sensitive Pointer Analysis More Precise with Still k-Limiting" (SAS'16)
 * */
public class BeanPTA extends StagedPTA {
    // currently, we only support k = 2 and hk = 1;
    // [current heap, [allocator heap, [heap ctx, new ctx]]] only for B-2obj;
    Map<Object, Map<Object, Map<Object, Object>>> beanNexCtxMap = new HashMap<>();

    public BeanPTA(CtxConstructor ctxCons) {
        this.ctxCons = ctxCons;
        CtxSelector us = new BeanSelector(pag, beanNexCtxMap);
        if (PTAConfig.v().getPtaConfig().enforceEmptyCtxForIgnoreTypes) {
            this.ctxSel = new PipelineSelector(new HeuristicSelector(), us);
        } else {
            this.ctxSel = us;
        }
        if (PTAConfig.v().getPtaConfig().mergeHeap) {
            this.heapAbst = new HeuristicAbstractor(pag);
        } else {
            this.heapAbst = new AllocSiteAbstractor();
        }
        prePTA = new Spark();
        System.out.println("bean ...");
    }

    @Override
    protected void preAnalysis() {
        Stopwatch sparkTimer = Stopwatch.newAndStart("Spark");
        prePTA.pureRun();
        sparkTimer.stop();
        System.out.println(sparkTimer);
        Stopwatch beanTimer = Stopwatch.newAndStart("Bean");
        Bean.run(prePTA, beanNexCtxMap);
        beanTimer.stop();
        System.out.println(beanTimer);
    }
}
