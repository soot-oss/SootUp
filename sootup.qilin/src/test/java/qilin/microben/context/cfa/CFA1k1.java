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

public class CFA1k1 {
  public static void main(String[] args) {
    Object o1 = new Object(); // O1
    Object o2 = new Object(); // O2
    bar(o1);
    bar(o2);
  }

  static void bar(Object o) {
    Object o5 = new Object();
    Object v5 = id(o5);
    Assert.notAlias(v5, o);
    id2(o);
  }

  static Object id(Object o) {
    return o;
  }

  static Object id2(Object o) {
    return id(o);
  }
}
