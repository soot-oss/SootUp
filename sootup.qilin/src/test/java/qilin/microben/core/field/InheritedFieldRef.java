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

/** javac emits getfield Sub.f and getstatic Sub.s although both are declared in Base. */
public class InheritedFieldRef {
  static class Base {
    Object f;
    static Object s;
  }

  static class Sub extends Base {}

  public static void main(String[] args) {
    Sub sub = new Sub();
    Base base = sub;
    Object o1 = new Object();
    Object o2 = new Object();
    base.f = o1;
    Object r = sub.f;
    Assert.mayAlias(r, o1);
    Assert.notAlias(r, o2);
    Base.s = o2;
    Object t = Sub.s;
    Assert.mayAlias(t, o2);
    Assert.notAlias(t, o1);
  }
}
