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

package qilin.microben.context.obj;

import qilin.microben.utils.Assert;

public class OBJ2k0 {
  static class A {
    Object foo(Object o) {
      B b = new B();
      return b.id(o);
    }
  }

  static class B {
    Object id(Object o) {
      return o;
    }
  }

  public static void main(String[] args) {
    A a1 = new A(); // A1
    A a2 = new A(); // A2
    Object o1 = new Object(); // O1
    Object o2 = new Object(); // O2
    Object v1 = a1.foo(o1);
    Object v2 = a2.foo(o2);
    Assert.notAlias(v1, v2);
    Assert.mayAlias(v1, o1);
  }
}
