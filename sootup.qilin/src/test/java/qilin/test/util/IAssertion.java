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

package qilin.test.util;

public interface IAssertion {
  boolean check();

  /**
   * Whether this assertion must hold under any sound (over-approximating) points-to analysis,
   * however imprecise - e.g. a "may-alias" claim, since collapsing context can only ever merge
   * points-to sets, never split them. A "not-alias" claim is a precision claim instead: it can
   * legitimately fail under a coarser or heuristically-selective analysis (Zipper, Moon, ...)
   * without indicating unsoundness.
   */
  default boolean isSoundnessCritical() {
    return true;
  }
}
