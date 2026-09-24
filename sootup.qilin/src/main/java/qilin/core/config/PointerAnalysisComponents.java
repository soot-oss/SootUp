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

package qilin.core.config;

import qilin.core.pag.PAG;
import qilin.parm.heapabstraction.AllocSiteAbstractor;
import qilin.parm.heapabstraction.HeapAbstractor;
import qilin.parm.heapabstraction.HeuristicAbstractor;
import qilin.parm.select.ContextSelector;
import qilin.parm.select.HeuristicSelector;
import qilin.parm.select.InsensitiveSelector;
import qilin.parm.select.PipelineSelector;
import qilin.parm.select.UniformSelector;
import sootup.core.views.View;

/**
 * Centralizes the two cross-cutting policy decisions (heap abstraction, ignore-type context
 * selector wrapping) that used to be copy-pasted, identically, across every {@code
 * qilin.pta.tools.*} constructor.
 */
public final class PointerAnalysisComponents {

  private PointerAnalysisComponents() {}

  public static HeapAbstractor createHeapAbstractor(PointerAnalysisConfig config, PAG pag) {
    return config.getHeapAbstractionPolicy()
            == PointerAnalysisConfig.HeapAbstractionPolicy.HEURISTIC_MERGE
        ? new HeuristicAbstractor(pag)
        : new AllocSiteAbstractor();
  }

  public static ContextSelector createContextSelector(
      PointerAnalysisConfig config, View view, ContextSensitivity contextSensitivity) {
    ContextSelector base =
        contextSensitivity.contextDepth() == 0
            ? new InsensitiveSelector()
            : new UniformSelector(
                contextSensitivity.selectorContextDepth(), contextSensitivity.heapContextDepth());
    return wrapIgnoreTypesGuard(config, view, base);
  }

  /**
   * Wraps {@code base} with a {@link HeuristicSelector} guard that forces an empty context for
   * ignore-types, when the config asks for it; otherwise returns {@code base} unchanged.
   */
  public static ContextSelector wrapIgnoreTypesGuard(
      PointerAnalysisConfig config, View view, ContextSelector base) {
    return config.isEnforceEmptyContextForIgnoreTypes()
        ? new PipelineSelector(new HeuristicSelector(view), base)
        : base;
  }
}
