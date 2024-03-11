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

public class ClassForName1 {
  public static void main(String[] args) throws Exception {
    String realClassName = "qilin.microben.core.reflog.ClassForName1";
    Class<?> c = Class.forName(realClassName, true, ClassLoader.getSystemClassLoader());
    String realClassName2 = "qilin.microben.core.reflog.ClassForName1";
    Class<?> c2 = Class.forName(realClassName2, true, ClassLoader.getSystemClassLoader());
    String realClassName3 = "java.lang.Object";
    Class<?> c3 = Class.forName(realClassName3, true, ClassLoader.getSystemClassLoader());
    Assert.mayAlias(c, c2);
    Assert.notAlias(c, c3);
  }
}
