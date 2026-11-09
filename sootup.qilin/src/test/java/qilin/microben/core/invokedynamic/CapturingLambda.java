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

package qilin.microben.core.invokedynamic;

import qilin.microben.utils.Assert;

/**
 * A capturing lambda - the captured values become the leading parameters of its static synthetic
 * body and are bound at the invokedynamic statement.
 */
public class CapturingLambda {
  static Object VALUE = new Object();

  public static void main(String[] args) {
    Object a = VALUE;
    Object b = new Object();
    Runnable r = () -> Assert.mayAlias(a, VALUE);
    Runnable r2 = () -> Assert.notAlias(b, VALUE);
    r.run();
    r2.run();
  }
}
