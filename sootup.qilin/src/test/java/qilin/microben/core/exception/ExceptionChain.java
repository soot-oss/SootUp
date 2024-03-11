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

package qilin.microben.core.exception;

import qilin.microben.utils.Assert;

public class ExceptionChain {
  Exception t1;
  Exception t2;

  void foo(int x) {
    try {
      if (x <= 10) {
        try {
          t1 = new ArithmeticException();
          throw t1;
        } catch (ArithmeticException ex) {
          Assert.mayAlias(t1, ex);
          Assert.notAlias(ex, t2);
        }
      } else {
        t2 = new ClassCastException();
        throw t2;
      }
    } catch (ClassCastException e) {
      Assert.notAlias(e, t1);
      Assert.mayAlias(e, t2);
    } catch (Exception ey) {
      Assert.notAlias(ey, t1);
      Assert.notAlias(ey, t2);
    }
  }

  public static void main(String[] args) {
    ExceptionChain ec = new ExceptionChain();
    ec.foo(args.length);
  }
}
