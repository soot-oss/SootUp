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

class CFA1k2 {
  static class A {
    Object id(Object a) {
      return a;
    }

    Object id2(Object a) {
      return id(a);
    }
  }

  static class A2 extends A {
    Object id(Object a) {
      return a;
    }
  }

  public static void main(String[] argv) {
    A a1 = new A(); // A1
    A a2 = new A2(); // A2
    Object o1 = new Object(); // O1
    Object o2 = new Object(); // O2
    Object v1 = a1.id2(o1);
    Object v2 = a2.id2(o2);
    Assert.notAlias(v1, v2);
    Assert.mayAlias(v1, o1);
  }
}
