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

/*
 * 1Type uses declaring type, not the reciever type.
 * */
public class Type1k0 {
  static class A {
    Object id(Object p) {
      return p;
    }
  }

  static class B {
    A create() {
      return new A(); // A1
    }
  }

  static class C {
    A create() {
      return new A(); // A2
    }
  }

  public static void main(String[] args) {
    Object o1 = new Object(); // O1
    Object o2 = new Object(); // O2
    B b = new B(); // B
    C c = new C(); // C
    A a1 = b.create();
    A a2 = c.create();
    Object v1 = a1.id(o1);
    Object v2 = a2.id(o2);
    Assert.mayAlias(v1, o1);
    Assert.notAlias(v1, v2);
  }
}
