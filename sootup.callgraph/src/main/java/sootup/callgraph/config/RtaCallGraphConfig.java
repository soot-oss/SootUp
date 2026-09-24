package sootup.callgraph.config;

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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphConfig;
import sootup.callgraph.RapidTypeAnalysisAlgorithm;
import sootup.core.types.ClassType;

/**
 * RTA-specific stage of the unified {@link CallGraphConfig} builder chain. Obtained via {@link
 * CallGraphConfigBuilder#rta()}; adds RTA's one algorithm-specific knob, a set of classes to treat
 * as already-instantiated before traversal starts.
 */
public final class RtaCallGraphConfig implements CallGraphConfig {

  @NonNull private final CommonCallGraphSettings common;
  @NonNull private final Set<ClassType> preInstantiatedClasses;

  private RtaCallGraphConfig(
      @NonNull CommonCallGraphSettings common, @NonNull Set<ClassType> preInstantiatedClasses) {
    this.common = common;
    this.preInstantiatedClasses = preInstantiatedClasses;
  }

  @NonNull
  @Override
  public CallGraph computeCallGraph() {
    RapidTypeAnalysisAlgorithm rta =
        new RapidTypeAnalysisAlgorithm(
            common.getView(),
            preInstantiatedClasses,
            common.getCallResolver(),
            common.getVirtualCallResolver(),
            common.getSeedEntryPointClinits(true));
    return common.getEntryPoints().isEmpty()
        ? rta.initialize()
        : rta.initialize(common.getEntryPoints());
  }

  public static final class Builder {

    @NonNull private final CommonCallGraphSettings common;
    @NonNull private Set<ClassType> preInstantiatedClasses = Collections.emptySet();

    Builder(@NonNull CommonCallGraphSettings common) {
      this.common = common;
    }

    /** Classes to treat as already-instantiated before RTA's traversal starts. */
    @NonNull
    public Builder preInstantiatedClasses(@NonNull Set<ClassType> preInstantiatedClasses) {
      this.preInstantiatedClasses = new HashSet<>(preInstantiatedClasses);
      return this;
    }

    @NonNull
    public RtaCallGraphConfig build() {
      return new RtaCallGraphConfig(common, preInstantiatedClasses);
    }
  }
}
