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

import qilin.CoreConfig;
import qilin.core.CorePTA;
import qilin.core.builder.CallGraphBuilder;
import qilin.core.pag.PAG;
import qilin.core.solver.Propagator;
import qilin.core.solver.Solver;
import qilin.stat.IEvaluator;
import qilin.stat.PTAEvaluator;
import qilin.stat.SimplifiedEvaluator;
import qilin.util.PTAUtils;

public abstract class BasePTA extends CorePTA {
    protected IEvaluator evaluator;

    public BasePTA() {
//        this.evaluator = new PTAEvaluator(this);
        this.evaluator = new SimplifiedEvaluator(this);
    }

    public IEvaluator evaluator() {
        return this.evaluator;
    }

    @Override
    protected PAG createPAG() {
        return new PAG(this);
    }

    @Override
    protected CallGraphBuilder createCallGraphBuilder() {
        return new CallGraphBuilder(this);
    }

    @Override
    public Propagator getPropagator() {
        return new Solver(this);
    }

    @Override
    public void run() {
        evaluator.begin();
        pureRun();
        evaluator.end();
        dumpStats();
        System.out.println(evaluator());
    }

    protected void dumpStats() {
        if (CoreConfig.v().getOutConfig().dumppts) {
            PTAUtils.dumpPts(this, !CoreConfig.v().getOutConfig().dumplibpts);
        }
        if (CoreConfig.v().getOutConfig().dumpCallGraph) {
            PTAUtils.dumpCallGraph(getCallGraph(), false);
//            PTAUtils.dumpSlicedCallGraph(getCallGraph(),
//                    parameterize(PTAScene.v().getMethod("<java.lang.String: java.lang.String valueOf(java.lang.Object)>"), emptyContext()));
        }
        if (CoreConfig.v().getOutConfig().dumppag) {
            PTAUtils.dumpPAG(pag, "pag");
//            PTAUtils.dumpMPAGs(this, "mpags");
            PTAUtils.dumpNodeNames("nodeNames");
        }
    }
}
