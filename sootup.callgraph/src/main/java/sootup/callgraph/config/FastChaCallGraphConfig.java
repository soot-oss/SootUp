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
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.callgraph.FastClassHierarchyAnalysisAlgorithm;

/**
 * Stage of the unified {@link sootup.callgraph.CallGraphConfig} builder chain for {@link
 * FastClassHierarchyAnalysisAlgorithm}, which resolves virtual calls on the type of the receiver
 * local, as Soot's CHA does. Obtained via {@link CallGraphConfigBuilder#fastCha()}. Read the
 * algorithm's documentation for the caveats: the bodies of the view have to be typed after their
 * locals were split.
 */
public final class FastChaCallGraphConfig extends ChaCallGraphConfig {

  private FastChaCallGraphConfig(@NonNull CommonCallGraphSettings common) {
    super(common);
  }

  @NonNull
  @Override
  protected ClassHierarchyAnalysisAlgorithm createAlgorithm() {
    return new FastClassHierarchyAnalysisAlgorithm(
        common.getView(),
        common.getCallResolver(),
        common.getVirtualCallResolver(),
        common.getSeedEntryPointClinits(true),
        common.getReflectionModel(),
        common.getDynamicInvokeResolver());
  }

  public static final class Builder {

    @NonNull private final CommonCallGraphSettings common;

    Builder(@NonNull CommonCallGraphSettings common) {
      this.common = common;
    }

    @NonNull
    public FastChaCallGraphConfig build() {
      return new FastChaCallGraphConfig(common);
    }
  }
}
