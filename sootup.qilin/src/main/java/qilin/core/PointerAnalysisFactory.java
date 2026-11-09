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

package qilin.core;

import qilin.core.config.PointerAnalysisConfig;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * Type-safe entry point for constructing a {@link PTA}. The concrete variant - core
 * context-sensitivity (insensitive, call-site, object, type, hybrid-object, hybrid-type) or
 * research-toolkit (bean, zipper, eagle, turner, mahjong, selectx, data-driven, tunneling,
 * debloating) - is fully determined by {@link PointerAnalysisConfig#getContextSensitivity()}; see
 * {@code qilin.core.config.ContextSensitivity} for the available factory methods.
 *
 * <p>Every call constructs a fresh {@link PTA} (with its own {@link PTAScene}/{@code PAG}); no
 * state is cached on {@link View}, so multiple independent analyses - even over the same view - can
 * be constructed and run concurrently.
 */
public final class PointerAnalysisFactory {

  private PointerAnalysisFactory() {}

  public static PTA create(View view, ClassType mainClass, PointerAnalysisConfig config) {
    PTAScene scene = new PTAScene(view, mainClass, config);
    return config.getContextSensitivity().createPTA(scene, config);
  }
}
