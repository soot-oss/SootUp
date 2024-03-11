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

import java.util.PriorityQueue;
import qilin.microben.utils.Assert;

public class PriorityQueue0 {
  public static void main(String[] args) {
    PriorityQueue<Object> queue1 = new PriorityQueue<>();
    PriorityQueue<Object> queue2 = new PriorityQueue<>();
    Object o1 = new Object(); // O1
    Object o2 = new Object(); // O2
    Object o3 = new Object(); // O3
    queue1.add(o1);
    queue1.add(o2);
    queue2.add(o3);
    Object v1 = queue1.poll();
    Object v3 = queue2.poll();
    Assert.mayAlias(v1, o2);
    Assert.notAlias(v1, v3);
  }
}
