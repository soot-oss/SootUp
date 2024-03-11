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

package qilin.microben.core.reflog;

import qilin.microben.utils.Assert;

public class FieldSet {
  public Object f;

  public static void main(String[] args) throws Exception {
    FieldSet fs1 = new FieldSet();
    FieldSet fs2 = new FieldSet();
    Object o1 = new Object(); // O1
    Object o2 = new Object(); // O2
    FieldSet.class.getField("f").set(fs1, o1);
    FieldSet.class.getField("f").set(fs2, o2);
    Object v1 = fs1.f;
    Assert.notAlias(o2, v1);
    Assert.mayAlias(v1, o1);
  }
}
