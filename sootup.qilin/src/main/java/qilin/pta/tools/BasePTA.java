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
