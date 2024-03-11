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

package qilin.microben.flowsens;

import qilin.microben.utils.Assert;

// need flow-sensitivity.
public class StrongUpdate2 {
  static StrongUpdate2 v;
  Object f, g;

  public static void main(String[] args) {
    StrongUpdate2 v1 = new StrongUpdate2();
    StrongUpdate2 v2 = new StrongUpdate2();
    Object o1 = new Object(); // O1
    Object o2 = new Object(); // O2
    Object o3 = new Object(); // O3
    v1.f = o1;
    v2.f = o2;
    v1.g = o3;
    v = v1;
    v = v2; // strong update pointer.
    v.g = v.f;
    Assert.notAlias(v1.g, v2.g);
    Assert.mayAlias(v2.g, o2);
  }
}
