package qilin.core.config;

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
import qilin.parm.ctxcons.CtxConstructor;
import qilin.parm.ctxcons.HybTypeCtxConstructor;
import qilin.pta.tools.BasePTA;
import qilin.pta.tools.CoreVariantPTA;

final class HybridTypeSens extends ContextSensitivity {
  private final int k;
  private final int hk;

  HybridTypeSens(int k, int hk) {
    requireNonNegative(k, "k");
    requireNonNegative(hk, "hk");
    this.k = k;
    this.hk = hk;
  }

  @Override
  public CtxConstructor createCtxConstructor() {
    return new HybTypeCtxConstructor();
  }

  @Override
  public int contextDepth() {
    return k;
  }

  @Override
  public int heapContextDepth() {
    return hk;
  }

  @Override
  public BasePTA createPTA(PTAScene scene, PointerAnalysisConfig config) {
    return new CoreVariantPTA(scene, this);
  }

  @Override
  public String toString() {
    return k + "hybtype+" + hk + "heap";
  }
}
