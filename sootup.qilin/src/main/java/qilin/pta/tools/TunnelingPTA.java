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

import qilin.core.PTAScene;
import qilin.core.config.PointerAnalysisComponents;
import qilin.parm.contextconstruction.ContextConstructor;
import qilin.parm.heapabstraction.HeapAbstractor;
import qilin.parm.select.ContextSelector;
import qilin.parm.select.UniformSelector;
import qilin.pta.toolkits.dd.TunnelingConstructor;

/*
 * This class support context tunneling from the paper "Precise and Scalable Points-to Analysis via Data-Driven
 * Context Tunneling" (OOPSLA 2018). We reuse the trained formula from the paper. However, our evaluation does
 * not show the claimed effectiveness. Maybe we should train the benchmarks to get new formulas?
 * */
public class TunnelingPTA extends BasePTA {
  public TunnelingPTA(PTAScene scene, ContextConstructor contextConstructor, int k, int hk) {
    super(scene);
    ContextConstructor tunnelingCtxCons =
        new TunnelingConstructor(getView(), pag, contextConstructor);
    ContextSelector us = new UniformSelector(k, hk);
    ContextSelector contextSelector =
        PointerAnalysisComponents.wrapIgnoreTypesGuard(getConfig(), getView(), us);
    HeapAbstractor heapAbstractor =
        PointerAnalysisComponents.createHeapAbstractor(getConfig(), pag);
    initComponents(tunnelingCtxCons, contextSelector, heapAbstractor);
    System.out.println("context-tunneling ...");
  }
}
