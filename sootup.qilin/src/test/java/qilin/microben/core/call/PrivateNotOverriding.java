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

package qilin.microben.core.call;

import qilin.microben.utils.Assert;

/** Base.id is private, so it does not override Id.id: a call on Impl runs the default method. */
public class PrivateNotOverriding {
  interface Id {
    default Object id(Object p) {
      return p;
    }
  }

  static class Base {
    private Object id(Object p) {
      return new Object();
    }
  }

  static class Impl extends Base implements Id {}

  public static void main(String[] args) {
    Object o1 = new Object();
    Id i = new Impl();
    Object r = i.id(o1);
    Assert.mayAlias(r, o1);
  }
}
