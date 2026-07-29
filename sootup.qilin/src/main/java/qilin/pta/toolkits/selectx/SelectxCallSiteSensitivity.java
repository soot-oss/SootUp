package qilin.pta.toolkits.selectx;

/*-
 * #%L
 * SootUp - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Markus Schmidt and others
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

import qilin.core.PTAScene;
import qilin.core.config.ContextSensitivity;
import qilin.core.config.PointerAnalysisConfig;
import qilin.parm.ctxcons.CallsiteCtxConstructor;
import qilin.parm.ctxcons.CtxConstructor;
import qilin.pta.tools.BasePTA;
import qilin.pta.tools.SelectxPTA;

/** SELECTX-guided k-callsite-sensitivity. */
public final class SelectxCallSiteSensitivity extends ContextSensitivity {
  private final int k;

  public SelectxCallSiteSensitivity(int k) {
    requirePositive(k);
    this.k = k;
  }

  @Override
  public CtxConstructor createCtxConstructor() {
    return new CallsiteCtxConstructor();
  }

  @Override
  public int contextDepth() {
    return k;
  }

  @Override
  public int heapContextDepth() {
    return k - 1;
  }

  @Override
  public BasePTA createPTA(PTAScene scene, PointerAnalysisConfig config) {
    return new SelectxPTA(scene, k);
  }

  @Override
  public String toString() {
    return label("selectx", k, "c", k - 1);
  }
}
