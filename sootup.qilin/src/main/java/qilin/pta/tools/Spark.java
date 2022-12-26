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

import qilin.parm.ctxcons.InsensCtxConstructor;
import qilin.parm.heapabst.AllocSiteAbstractor;
import qilin.parm.heapabst.HeuristicAbstractor;
import qilin.parm.select.InsenSelector;
import qilin.pta.PTAConfig;

/*
 * refer to "Scaling Java Points-To Analysis using SPARK" (CC'03)
 * */
public class Spark extends BasePTA {
    public Spark() {
        this.ctxCons = new InsensCtxConstructor();
        this.ctxSel = new InsenSelector();
        if (PTAConfig.v().getPtaConfig().mergeHeap) {
            this.heapAbst = new HeuristicAbstractor(pag);
        } else {
            this.heapAbst = new AllocSiteAbstractor();
        }
        System.out.println("Context-Insensitive ...");
    }
}
