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

package qilin.microben.core.field;

import qilin.microben.utils.Assert;

public class FieldSensitivity1 {
  static class A {
    Object f;
    Object g;

    A(Object o1, Object o2) {
      this.f = o1;
      this.g = o2;
    }
  }

  public static void main(String[] args) {
    Object o1 = new Object(); // O1
    Object o2 = new Object(); // O2
    A a = new A(o1, o2);
    Assert.notAlias(a.f, o2);
    Assert.mayAlias(a.g, o2);
  }
}
