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

public class OBJ2k2 {
  static class A {
    Object f;

    A(Object p) {
      this.f = p;
    }

    Object getF() {
      return this.f;
    }
  }

  static class B {
    A create(Object q) {
      return new A(q);
    }
  }

  public static void main(String[] args) {
    B b1 = new B();
    B b2 = new B();
    Object o1 = new Object();
    Object o2 = new Object();
    A a1 = b1.create(o1);
    A a2 = b2.create(o2);
    Object v1 = a1.getF();
    Object v2 = a2.getF();
    Assert.mayAlias(v1, o1);
    Assert.notAlias(v1, v2);
  }
}
