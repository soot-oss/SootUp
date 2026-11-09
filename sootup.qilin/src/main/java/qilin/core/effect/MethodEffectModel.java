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

import sootup.core.model.SootMethod;

/**
 * Rewrites a method's body to make an effect explicit that the pointer analysis cannot derive from
 * the method's own Jimple statements - e.g. a reflective call resolved from an external log, a
 * native method's simulated side effect, or an invokedynamic call site resolved from bootstrap
 * constants. {@link qilin.core.pag.PAG#getMethodPAG(SootMethod)} applies every configured model to
 * a method exactly once, before building its {@link qilin.core.pag.MethodPAG}.
 */
public interface MethodEffectModel {
  boolean appliesTo(SootMethod m);

  void apply(SootMethod m);
}
