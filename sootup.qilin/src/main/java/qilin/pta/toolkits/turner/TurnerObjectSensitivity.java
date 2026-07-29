package qilin.pta.toolkits.turner;

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
import qilin.parm.ctxcons.CtxConstructor;
import qilin.parm.ctxcons.ObjCtxConstructor;
import qilin.pta.tools.BasePTA;
import qilin.pta.tools.TurnerPTA;

/** TURNER-guided k-object-sensitivity. */
public final class TurnerObjectSensitivity extends ContextSensitivity {
  private final int k;

  public TurnerObjectSensitivity(int k) {
    requirePositive(k);
    this.k = k;
  }

  @Override
  public CtxConstructor createCtxConstructor() {
    return new ObjCtxConstructor();
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
    return new TurnerPTA(scene, k);
  }

  @Override
  public String toString() {
    return label("turner", k, "o", k - 1);
  }
}
