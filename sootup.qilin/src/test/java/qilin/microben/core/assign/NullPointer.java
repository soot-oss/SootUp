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

package qilin.microben.core.assign;

import qilin.microben.utils.Assert;

/*
 * Currently, null is not a pointer value. As a result, the points-to set of both a and b are empty.
 * Thus, they are not alias.
 * */
public class NullPointer {
  public static void main(String[] args) {
    Object a = getNull();
    Object b = a;
    Assert.notAlias(b, a);
  }

  static Object getNull() {
    return null;
  }
}
