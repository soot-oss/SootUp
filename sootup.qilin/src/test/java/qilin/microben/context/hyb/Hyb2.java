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

package qilin.microben.context.hyb;

import qilin.microben.utils.Assert;

public class Hyb2 {
  static class A {
    Object f;
  }

  static class B {
    A foo(Object p) {
      A a = create();
      a.f = p;
      return a;
    }
  }

  static A create() {
    return new A();
  }

  public static void main(String[] args) {
    B b1 = new B(); // B1;
    B b2 = new B(); // B2;
    Object o1 = new Object(); // O1
    Object o2 = new Object(); // O2
    A a1 = b1.foo(o1);
    A a2 = b2.foo(o2);
    Object v1 = a1.f;
    Object v2 = a2.f;
    Assert.mayAlias(v1, o1);
    Assert.notAlias(v1, v2);
  }
}
