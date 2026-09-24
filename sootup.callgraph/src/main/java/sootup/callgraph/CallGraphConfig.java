package sootup.callgraph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Markus Schmidt
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
import sootup.callgraph.config.CallGraphConfigBuilder;

/**
 * A configuration object that already carries everything needed to build a {@link CallGraph} - the
 * view, entry points, algorithm choice, and algorithm-specific options - so that constructing a
 * call graph with any algorithm (CHA, RTA, Spark, a Qilin context-sensitivity variant) follows one
 * consistent shape:
 *
 * <pre>
 *   CallGraph cg = CallGraphConfig.builder()
 *       .view(view)
 *       .entryPoints(List.of(mainSig))
 *       .cha()
 *       .build()
 *       .computeCallGraph();
 * </pre>
 *
 * The builder ({@link CallGraphConfigBuilder}) collects properties shared across every family
 * (view, entry points, {@code <clinit>}/dispatch scoping via {@code
 * sootup.callgraph.scope.CallResolver}/{@code VirtualCallResolver}), then transitions to a
 * family-specific stage: {@code .cha()}/{@code .rta()} directly (both live in this module), or
 * {@code .into(SparkCallGraphConfig::from)}/{@code .into(QilinCallGraphConfig::from)} for the
 * {@code sootup.spark}/{@code sootup.qilin} modules, which this module cannot depend on. Each
 * family-specific builder ends in {@code .build()}, producing a {@link CallGraphConfig}.
 */
public interface CallGraphConfig {

  /**
   * Builds the call graph for the analysis described by this configuration object. All state needed
   * (view, entry points, algorithm-specific options) is already captured by the concrete
   * implementation, typically via its own builder.
   */
  @NonNull CallGraph computeCallGraph();

  /** Starts a new, family-agnostic {@link CallGraphConfig} builder. */
  @NonNull
  static CallGraphConfigBuilder builder() {
    return new CallGraphConfigBuilder();
  }
}
