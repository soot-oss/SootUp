package sootup.spark;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2002-2026 Ondrej Lhotak, Kadiray Karakaya and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import sootup.callgraph.CallGraph;
import sootup.core.signatures.MethodSignature;
import sootup.core.views.View;

/**
 * Main access point to SPARK's functionality.
 *
 * <p>Exposes the call graph (CHA up-front, or built on-the-fly when
 * {@code SparkOptions#isOnFlyCallGraph()} is set) and a {@link PointsToAnalysis} over the resulting
 * PAG.
 * Lower-level components such as the solver are package-private; clients should not bypass this
 * facade.
 *
 * <pre>
 *   Spark spark = Spark.builder()
 *       .view(view)
 *       .entryPoints(List.of(mainSig))
 *       .sparkOptions(SparkOptions.builder().onFlyCallGraph(true).build())
 *       .build();
 *   PointsToAnalysis pta = spark.getPointsToAnalysis();
 *   CallGraph cg = spark.getCallGraph();
 * </pre>
 */
public class Spark {

  private final Solver solver;
  private boolean solved;
  private PointsToAnalysis pta;

  @Builder
  Spark(@NonNull View view, @NonNull List<MethodSignature> entryPoints, SparkOptions sparkOptions) {
    this.solver =
        Solver.builder().view(view).entryPoints(entryPoints).sparkOptions(sparkOptions).build();
  }

  /**
   * Builds the pointer assignment graph and (in OTF mode) the call graph. Invoked automatically by
   * the accessors; calling it explicitly is optional and idempotent.
   */
  public void solve() {
    if (!solved) {
      solver.solve();
      solved = true;
    }
  }

  /**
   * Returns the call graph. Built via class-hierarchy analysis at construction time, or grown
   * on-the-fly during {@link #solve()} when {@code SparkOptions#isOnFlyCallGraph()} is set.
   */
  public CallGraph getCallGraph() {
    solve();
    return solver.getCallGraph();
  }

  /** Returns a points-to analysis over the solved PAG. The result is cached. */
  public PointsToAnalysis getPointsToAnalysis() {
    solve();
    if (pta == null) {
      pta = PointsToAnalysis.fromSolver(solver);
    }
    return pta;
  }

  /** Returns the underlying pointer assignment graph — intended for inspection and debugging. */
  public PAG getPag() {
    solve();
    return solver.getPag();
  }

  public SparkOptions getSparkOptions() {
    return solver.getSparkOptions();
  }
}
