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

import qilin.core.PTAScene;
import qilin.core.config.ContextSensitivity;
import qilin.core.config.PointerAnalysisComponents;

/**
 * A single, factory-configured pointer analysis covering all core context-sensitivity variants
 * (context-insensitive Spark, call-site k-CFA, k-object-sensitive, k-type-sensitive, and their
 * hybrid-object/hybrid-type counterparts). Replaces the former {@code Spark}, {@code
 * CallSiteSensPTA}, {@code ObjectSensPTA}, {@code TypeSensPTA}, {@code HybridObjectSensPTA} and
 * {@code HybridTypeSensPTA} classes, which differed only in which {@link ContextSensitivity} they
 * were parameterized with - the cross-cutting heap-abstraction/context-selector wiring that used to
 * be copy-pasted across all six now lives once in {@link PointerAnalysisComponents}.
 */
public final class CoreVariantPTA extends BasePTA {

  public CoreVariantPTA(PTAScene scene, ContextSensitivity contextSensitivity) {
    super(scene);
    initComponents(
        contextSensitivity.createCtxConstructor(),
        PointerAnalysisComponents.createCtxSelector(getConfig(), getView(), contextSensitivity),
        PointerAnalysisComponents.createHeapAbstractor(getConfig(), pag));
  }
}
