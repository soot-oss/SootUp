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

public class OBJ1k1 {
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
    Object getF(A a) {
      return a.getF();
    }
  }

  public static void main(String[] args) {
    Object o1 = new Object(); // O1
    Object o2 = new Object(); // O2
    A a1 = new A(o1); // A1
    A a2 = new A(o2); // A2
    B b1 = new B(); // B1
    B b2 = new B(); // B2
    Object v1 = b1.getF(a1);
    Object v2 = b2.getF(a2);
    Assert.notAlias(v1, v2);
    Assert.mayAlias(v1, o1);
  }
}
