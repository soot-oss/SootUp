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

import qilin.core.reflection.ReflectionModel;
import sootup.core.model.SootMethod;

/** Adapts {@link ReflectionModel} to {@link MethodEffectModel}; delegate is unmodified. */
public class ReflectionEffectModel implements MethodEffectModel {
  private final ReflectionModel delegate;

  public ReflectionEffectModel(ReflectionModel delegate) {
    this.delegate = delegate;
  }

  @Override
  public boolean appliesTo(SootMethod m) {
    return m.isConcrete();
  }

  @Override
  public void apply(SootMethod m) {
    delegate.buildReflection(m);
  }
}
