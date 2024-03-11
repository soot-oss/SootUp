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

package qilin.microben.context.cfa;

import qilin.microben.utils.Assert;

class CFA2k {
  static Object id(Object a) {
    return a;
  }

  static Object id2(Object a) {
    return id(a);
  }

  public static void main(String[] argv) {
    Object o1 = new Object();
    Object o2 = new Object();
    Object v1 = id2(o1);
    Object v2 = id2(o2);
    Assert.notAlias(v1, v2);
    Assert.mayAlias(v1, o1);
  }
}
