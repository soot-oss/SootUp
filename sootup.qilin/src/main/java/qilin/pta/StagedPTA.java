package qilin.pta;

import qilin.pta.tools.BasePTA;

/*
 * Many recent pointer analyses are two-staged analyses with a preanalysis and a main analysis.
 * This class gives a structure for such kinds of analyses.
 * */
public abstract class StagedPTA extends BasePTA {
    protected BasePTA prePTA;

    public BasePTA getPrePTA() {
        return this.prePTA;
    }

    protected abstract void preAnalysis();

    protected void mainAnalysis() {
        if (!PTAConfig.v().getPtaConfig().preAnalysisOnly) {
            System.out.println("selective pta starts!");
            super.run();
        }
    }

    @Override
    public void run() {
        preAnalysis();
        prePTA.getPag().resetPointsToSet();
        mainAnalysis();
    }
}
