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

import java.util.HashMap;
import qilin.microben.utils.Assert;

public class HashMap0 {
  public static void main(String[] args) {
    HashMap<String, Object> map1 = new HashMap<>();
    HashMap<String, Object> map2 = new HashMap<>();
    Object o1 = new Object(); // O1
    Object o2 = new Object(); // O2
    Object o3 = new Object(); // O3
    map1.put("first", o1);
    map1.put("second", o2);
    map2.put("first", o3);
    Object v2 = map1.get("second");
    Object v3 = map2.get("first");
    Assert.mayAlias(v2, o2);
    Assert.notAlias(v2, v3);
  }
}
