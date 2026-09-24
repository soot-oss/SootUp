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

import org.jspecify.annotations.NonNull;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphConfig;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.callgraph.config.CommonCallGraphSettings;

/**
 * Spark-specific stage of the unified {@link CallGraphConfig} builder chain. Obtained via {@code
 * CallGraphConfigBuilder.into(SparkCallGraphConfig::from)}, since {@code sootup.callgraph} cannot
 * depend on {@code sootup.spark} directly:
 *
 * <pre>
 *   CallGraph cg = CallGraphConfig.builder()
 *       .view(view).entryPoints(entryPoints)
 *       .into(SparkCallGraphConfig::from)
 *       .sparkOptions(SparkOptions.builder().onFlyCallGraph(true).build())
 *       .build()
 *       .computeCallGraph();
 * </pre>
 *
 * <p>In on-the-fly mode ({@code SparkOptions#isOnFlyCallGraph()}), the common {@code
 * CallResolver}/{@code VirtualCallResolver}/{@code seedEntryPointClinits} settings are not
 * consulted - OTF mode grows its own call graph incrementally and, as of today, has no {@code
 * <clinit>} modeling at all (see {@code Spark}'s javadoc). Otherwise, they drive a CHA graph built
 * up front and handed to {@link Spark} via {@code callGraph(...)}, exactly the pattern {@code
 * sootup.spark.test.options.ClinitHandlingViaCustomCallGraphTest} demonstrates by hand.
 */
public final class SparkCallGraphConfig implements CallGraphConfig {

  @NonNull private final CommonCallGraphSettings common;
  @NonNull private final SparkOptions sparkOptions;

  private SparkCallGraphConfig(
      @NonNull CommonCallGraphSettings common, @NonNull SparkOptions sparkOptions) {
    this.common = common;
    this.sparkOptions = sparkOptions;
  }

  /** Entry point for {@code CallGraphConfigBuilder.into(SparkCallGraphConfig::from)}. */
  @NonNull
  public static Builder from(@NonNull CommonCallGraphSettings common) {
    return new Builder(common);
  }

  @NonNull
  @Override
  public CallGraph computeCallGraph() {
    Spark spark;
    if (sparkOptions.isOnFlyCallGraph()) {
      spark =
          Spark.builder()
              .view(common.getView())
              .entryPoints(common.getEntryPoints())
              .sparkOptions(sparkOptions)
              .build();
    } else {
      CallGraph cha =
          new ClassHierarchyAnalysisAlgorithm(
                  common.getView(),
                  common.getCallResolver(),
                  common.getVirtualCallResolver(),
                  common.getSeedEntryPointClinits(true))
              .initialize(common.getEntryPoints());
      spark =
          Spark.builder().view(common.getView()).callGraph(cha).sparkOptions(sparkOptions).build();
    }
    return spark.getCallGraph();
  }

  public static final class Builder {

    @NonNull private final CommonCallGraphSettings common;
    @NonNull private SparkOptions sparkOptions = SparkOptions.defaultOptions();

    Builder(@NonNull CommonCallGraphSettings common) {
      this.common = common;
    }

    @NonNull
    public Builder sparkOptions(@NonNull SparkOptions sparkOptions) {
      this.sparkOptions = sparkOptions;
      return this;
    }

    @NonNull
    public SparkCallGraphConfig build() {
      return new SparkCallGraphConfig(common, sparkOptions);
    }
  }
}
