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

package qilin.microben.core.natives;

import java.security.AccessController;
import java.security.PrivilegedAction;
import qilin.microben.utils.Assert;

public class PrivilegedActions2 {

  public static void main(String[] args) {
    PrivilegedActions2 p1 = new PrivilegedActions2();
    PrivilegedActions2 p2 = new PrivilegedActions2();
    Object o1 = new Object(); // O1
    Object o2 = new Object(); // O2
    Object v1 = p1.getSystemProperty(o1);
    Object v2 = p2.getSystemProperty(o2);
    Assert.notAlias(v1, v2);
    Assert.mayAlias(o1, v1);
  }

  Object getSystemProperty(final Object propName) {
    return AccessController.doPrivileged(new MyPrivilegedAction(propName));
  }

  static class MyPrivilegedAction implements PrivilegedAction<Object> {
    Object propName;

    public MyPrivilegedAction(Object propName) {
      this.propName = propName;
    }

    public Object run() {
      return propName;
    }
  }
}
