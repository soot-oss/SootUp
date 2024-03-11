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

/*
 * @testcase Branching1
 * @description Condition. a and b alias on one path, not on the other
 */
public class Branching1 {
  public static void main(String[] args) {
    Object a = new Object();
    Object b = new Object();
    if (args.length > 2) {
      a = b;
    }
    Assert.mayAlias(a, b);
  }
}
