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

import qilin.pta.PTAConfig;

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
