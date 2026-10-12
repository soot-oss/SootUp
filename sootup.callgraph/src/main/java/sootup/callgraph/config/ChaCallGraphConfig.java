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

import org.jspecify.annotations.NonNull;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphConfig;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.callgraph.FastClassHierarchyAnalysisAlgorithm;

/**
 * CHA-specific stage of the unified {@link CallGraphConfig} builder chain. Obtained via {@link
 * CallGraphConfigBuilder#cha()}; has no algorithm-specific knobs of its own beyond the properties
 * {@link CallGraphConfigBuilder} already collected.
 */
public final class ChaCallGraphConfig implements CallGraphConfig {

  @NonNull private final CommonCallGraphSettings common;
  private final boolean fast;

  private ChaCallGraphConfig(@NonNull CommonCallGraphSettings common, boolean fast) {
    this.common = common;
    this.fast = fast;
  }

  @NonNull
  @Override
  public CallGraph computeCallGraph() {
    ClassHierarchyAnalysisAlgorithm cha =
        fast
            ? new FastClassHierarchyAnalysisAlgorithm(
                common.getView(),
                common.getCallResolver(),
                common.getVirtualCallResolver(),
                common.getSeedEntryPointClinits(true),
                common.getReflectionModel(),
                common.getDynamicInvokeResolver())
            : new ClassHierarchyAnalysisAlgorithm(
                common.getView(),
                common.getCallResolver(),
                common.getVirtualCallResolver(),
                common.getSeedEntryPointClinits(true),
                common.getReflectionModel(),
                common.getDynamicInvokeResolver());
    return common.getEntryPoints().isEmpty()
        ? cha.initialize()
        : cha.initialize(common.getEntryPoints());
  }

  public static final class Builder {

    @NonNull private final CommonCallGraphSettings common;
    private boolean fast;

    Builder(@NonNull CommonCallGraphSettings common) {
      this.common = common;
    }

    /**
     * Resolves virtual calls on the type of the receiver local, as Soot's CHA does, with a {@link
     * FastClassHierarchyAnalysisAlgorithm}. Read its documentation for the caveats: the bodies of
     * the view have to be typed after their locals were split.
     */
    @NonNull
    public Builder fast() {
      this.fast = true;
      return this;
    }

    @NonNull
    public ChaCallGraphConfig build() {
      return new ChaCallGraphConfig(common, fast);
    }
  }
}
