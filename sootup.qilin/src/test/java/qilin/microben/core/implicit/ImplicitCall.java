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

package qilin.microben.core.implicit;

/** Calls that are made implicitly: by a modeled {@code Scheduler.submit} and by threads. */
public class ImplicitCall {

  interface Job {
    void work();
  }

  static class Passed implements Job {
    public void work() {}
  }

  static class NotPassed implements Job {
    public void work() {}
  }

  static class Scheduler {
    void submit(Job job) {} // modeled to call job.work()
  }

  static class Worker extends Thread {
    public void run() {}
  }

  static class Task implements Runnable {
    public void run() {}
  }

  public static void main(String[] args) {
    Job other = new NotPassed();
    new Scheduler().submit(new Passed());
    new Worker().start();
    new Thread(new Task()).start();
  }
}
