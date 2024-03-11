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

package qilin.microben.context.collections;

import java.util.Stack;
import qilin.microben.utils.Assert;

public class Stack0 {
  public static void main(String[] args) {
    Stack<Object> s1 = new Stack<>();
    Stack<Object> s2 = new Stack<>();
    Object o1 = new Object(); // O1
    Object o2 = new Object(); // O2
    Object o3 = new Object(); // O3
    s1.push(o1);
    s2.push(o2);
    s2.push(o3);
    Object v1 = s1.pop();
    Object v2 = s2.pop();
    Assert.mayAlias(v1, o1);
    Assert.notAlias(v1, v2);
  }
}
