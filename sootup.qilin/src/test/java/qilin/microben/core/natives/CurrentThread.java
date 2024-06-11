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

import qilin.microben.utils.Assert;

public class CurrentThread {
  static Object t;

  static class A extends Thread {
    Object f;

    A(Object p) {
      this.f = p;
    }

    @Override
    public void run() {
      Assert.mayAlias(f, t);
      bar();
    }

    public void bar() {
      Thread thr = Thread.currentThread();
      Assert.mayAlias(thr, this);
      System.out.println(thr + ";;" + this);
    }
  }

  public static void main(String[] args) {
    Object o = new Object();
    A a = new A(o);
    t = o;
    a.start(); // run() will be invoked indirectly.
    Thread thr = Thread.currentThread();
    /*
     * Currently, the currentThread will point to all runnable thread whose run() is invoked.
     * It could not distinguish the exact current thread.
     * */
    Assert.notAlias(thr, a);
    System.out.println(thr + ";;" + a);
  }
}
