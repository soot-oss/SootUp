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

package qilin.core.effect;

import qilin.core.PTAScene;
import qilin.core.pag.PAG;
import sootup.callgraph.reflection.ReflectionModel;
import sootup.core.model.SootMethod;

/** Applies a shared {@link ReflectionModel} to qilin's (possibly already rewritten) PAG bodies. */
public class ReflectionEffectModel implements MethodEffectModel {
  private final ReflectionModel delegate;
  private final PTAScene ptaScene;
  private final PAG pag;

  public ReflectionEffectModel(ReflectionModel delegate, PTAScene ptaScene, PAG pag) {
    this.delegate = delegate;
    this.ptaScene = ptaScene;
    this.pag = pag;
  }

  @Override
  public boolean appliesTo(SootMethod m) {
    return m.isConcrete() && !delegate.isNone();
  }

  @Override
  public void apply(SootMethod m) {
    if (!ptaScene.reflectionBuilt.add(m)) {
      return;
    }
    pag.updateMethodBody(m, delegate.resolve(m, pag.getMethodBody(m)));
  }
}
