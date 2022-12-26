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
import qilin.parm.select.CtxSelector;
import qilin.parm.select.HeuristicSelector;
import qilin.parm.select.PipelineSelector;
import qilin.pta.PTAConfig;
import qilin.pta.toolkits.dd.DataDrivenSelector;
/*
 * Support Data-driven context-sensitivity for Points-to Analysis (OOPSLA 2017):
 * https://doi.org/10.1145/3133924
 * The limitation of this paper is that it only support a flavour of context sensitivity with k = 2.
 * k > 2 will costs more time (which is often unbearable) for training.
 * The length of heap context is k - 1 by default, where k is a fixed number (i.e., 2)
 * We use the same formula presented in the paper. However, our evaluation does not show the same effectiveness
 * as claimed in the paper. Maybe we should retrain the formulas in our framework.
 * */

public class DataDrivenPTA extends BasePTA {

    public DataDrivenPTA(CtxConstructor ctxCons) {
        this.ctxCons = ctxCons;
        CtxSelector us = new DataDrivenSelector(ctxCons.getClass());
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
        System.out.println("data-driven ...");
    }
}
