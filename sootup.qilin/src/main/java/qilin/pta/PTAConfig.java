/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021 Dongjie He
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

package qilin.pta;

import driver.PTAPattern;
import qilin.CoreConfig;
import qilin.pta.tools.DebloatedPTA;

public class PTAConfig extends CoreConfig {
    private static PTAConfig config = null;

    public static PTAConfig v() {
        if (config == null) {
            config = new PTAConfig();
            coreConfig = config;
        }
        return config;
    }

    public static void reset() {
        CoreConfig.reset();
        config = null;
    }

    public static class PointerAnalysisConfiguration extends CorePTAConfiguration {
        public PTAPattern ptaPattern;

        /**
         * If this option is turned on, then main analysis will not run.
         */
        public boolean preAnalysisOnly = false;

        /**
         * If this option is turned on, we will apply context debloating techniques.
         */
        public boolean ctxDebloating = false;
        public DebloatedPTA.DebloatApproach debloatApproach = DebloatedPTA.DebloatApproach.CONCH;

    }

    /*
     * Notice that the DEFAULT option is equivalent to Full Turner.
     * PHASE_TWO: all objects are assumed to be CS-likely.
     * PHASE_ONE: only non-CS-likely objects are analyzed context-insensitively. All other variables and objects
     * are analyzed context-sensitively.
     * */
    public enum TurnerConfig {
        DEFAULT, PHASE_TWO, PHASE_ONE
    }

    public TurnerConfig turnerConfig = TurnerConfig.DEFAULT;

    private PTAConfig() {
        this.ptaConfig = new PointerAnalysisConfiguration();
    }

    public PointerAnalysisConfiguration getPtaConfig() {
        return (PointerAnalysisConfiguration) this.ptaConfig;
    }

    /*
     * Configures the callgraph options
     * */
    public enum CallgraphAlgorithm {
        /*CHA, VTA, RTA, GEOM, and SPARK are all provided by Soot */
        CHA, VTA, RTA, GEOM, SPARK, QILIN
    }

    public CallgraphAlgorithm callgraphAlg = CallgraphAlgorithm.QILIN;
}
