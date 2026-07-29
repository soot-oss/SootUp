package qilin.pta.toolkits.bean;

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
import qilin.pta.tools.BeanPTA;

/** BEAN-guided 2-object-sensitivity. Only k=2/hk=1 is supported by {@link BeanPTA}. */
public final class BeanObjectSensitivity extends ContextSensitivity {

  public BeanObjectSensitivity() {}

  @Override
  public CtxConstructor createCtxConstructor() {
    return new ObjCtxConstructor();
  }

  @Override
  public int contextDepth() {
    return 2;
  }

  @Override
  public int heapContextDepth() {
    return 1;
  }

  @Override
  public BasePTA createPTA(PTAScene scene, PointerAnalysisConfig config) {
    return new BeanPTA(scene, createCtxConstructor());
  }

  @Override
  public String toString() {
    return label("bean", 2, "o", 1);
  }
}
