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

package qilin.microben.context.type;

import qilin.microben.utils.Assert;

public class Type2k0 {
  static class A {
    Object id(Object q) {
      return q;
    }
  }

  static class B {
    A create1() {
      return new A();
    }

    A create2() {
      return new A();
    }
  }

  static class C {
    Object foo(Object p) {
      B b1 = new B(); // B1
      A a1 = b1.create1();
      return a1.id(p);
    }
  }

  static class D {
    Object bar(Object o) {
      B b2 = new B(); // B2
      A a2 = b2.create2();
      return a2.id(o);
    }
  }

  public static void main(String[] args) {
    C c = new C();
    D d = new D();
    Object o1 = new Object(); // O1
    Object o2 = new Object(); // O2
    Object v1 = c.foo(o1);
    Object v2 = d.bar(o2);
    Assert.mayAlias(v1, o1);
    Assert.notAlias(v1, v2);
  }
}
