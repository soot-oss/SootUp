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

package qilin.stat;

import qilin.CoreConfig;
import qilin.core.PTA;

/**
 * Gather stats on the performance and precision of a PTA run.
 * <p>
 * Each new run of the PTA will over-write stats
 * <p>
 * - Wall time (sec) - Memory (max, current) before and after - Reachable
 * methods (context and no-context) - Total reachable casts - Reachable casts
 * that may fail - Call graph edges - Context sensitive call graph edges - Total
 * reachable virtual call sites - Polymorphic virtual call sites (sites with >1
 * target methods) - Number of pointers (local and global) - Total points to
 * sets size (local and global) context insensitive (convert to alloc site)
 */
public class PTAEvaluator implements IEvaluator {
    protected final RuntimeStat runtimeStat;
    protected final Exporter exporter;
    protected final PTA pta;

    public PTAEvaluator(PTA pta) {
        this.pta = pta;
        runtimeStat = new RuntimeStat();
        exporter = new Exporter();
    }

    /**
     * Note the start of a qilin.pta run.
     */
    @Override
    public void begin() {
        Runtime runtime = Runtime.getRuntime();// Getting the runtime reference
        // from system
        exporter.addLine(" ====== Memory Usage ======");
        exporter.collectMetric("Used Memory Before:", (runtime.totalMemory() - runtime.freeMemory()) / GB + " GB");// Print
        // used memory
        exporter.collectMetric("Free Memory Before:", runtime.freeMemory() / GB + " GB");// Print free memory
        exporter.collectMetric("Total Memory Before:", runtime.totalMemory() / GB + " GB");// Print total available memory
        exporter.collectMetric("Max Memory Before:", runtime.maxMemory() / GB + " GB");// Print Maximum available memory
        exporter.collectMetric("Analysis: ", CoreConfig.v().getPtaConfig().ptaName);
        runtimeStat.begin();
    }

    protected PointsToStat createPointsToStat() {
        return new PointsToStat(pta);
    }

    /**
     * Note the end of a qilin.pta run.
     */
    @Override
    public void end() {
        // done with processing
        runtimeStat.end();
        /**
         * all method reachable from the harness main
         */
        PAGStat pagStat = new PAGStat(pta);
        BenchmarkStat benchmarkStat = new BenchmarkStat(pta);
        CallGraphStat callGraphStat = new CallGraphStat(pta);
        TypeClientStat typeClientStat = new TypeClientStat(pta);
        PointsToStat ptsStat = createPointsToStat();
        YummyStat yummyStat = new YummyStat(pta);
        runtimeStat.export(exporter);
        // memory stats
        Runtime runtime = Runtime.getRuntime();// Getting the runtime reference
        // from system
        exporter.collectMetric("Used Memory After:", (runtime.totalMemory() - runtime.freeMemory()) / GB + " GB");// Print
        // used memory
        exporter.collectMetric("Free Memory After:", runtime.freeMemory() / GB + " GB");// Print free memory
        exporter.collectMetric("Total Memory After:", runtime.totalMemory() / GB + " GB");// Print total available memory
        exporter.collectMetric("Max Memory After:", runtime.maxMemory() / GB + " GB");// Print Maximum available memory
        exporter.addLine(" ====== Yummy ======");
        yummyStat.export(exporter);
        exporter.addLine(" ====== Call Graph ======");
        callGraphStat.export(exporter);
        exporter.addLine(" ====== Statements ======");
        typeClientStat.export(exporter);
        exporter.addLine(" ====== Nodes ======");
        ptsStat.export(exporter);
        exporter.addLine(" ====== Assignments ======");
        pagStat.export(exporter);
        exporter.addLine(" ====== Classes ======");
        benchmarkStat.export(exporter);

    }

    @Override
    public String toString() {
        return exporter.report();
    }

}
