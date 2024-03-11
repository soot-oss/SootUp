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

package qilin.microben.core.array;

import qilin.microben.utils.Assert;

public class ArrayCopy {
  public static void main(String[] ps) {
    Object[] xs = new Object[5];
    Object o1 = new Object(); // O1
    Object o2 = new Object(); // O2
    xs[0] = o1;
    xs[1] = o2;
    Object[] ys = new Object[5];
    System.arraycopy(xs, 0, ys, 0, 2);
    Assert.mayAlias(o1, ys[0]);
    Assert.mayAlias(o2, xs[0]);
  }
}
