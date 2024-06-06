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

package qilin.driver;

import java.lang.management.ManagementFactory;
import qilin.core.PTA;
import qilin.pta.PTAConfig;
import qilin.util.MemoryWatcher;
import qilin.util.PTAUtils;
import qilin.util.Stopwatch;
import sootup.core.views.View;

public class Main {

  public static PTA run(String[] args) {
    new PTAOption().parseCommandLine(args);
    PTAConfig.ApplicationConfiguration appConfig = PTAConfig.v().getAppConfig();
    if (appConfig.MAIN_CLASS == null) {
      appConfig.MAIN_CLASS = PTAUtils.findMainFromMetaInfo(appConfig.APP_PATH);
    }
    View view = PTAUtils.createView();
    PTAPattern ptaPattern = PTAConfig.v().getPtaConfig().ptaPattern;
    PTA pta = PTAFactory.createPTA(ptaPattern, view, appConfig.MAIN_CLASS);
    if (PTAConfig.v().getOutConfig().dumpJimple) {
      String jimplePath = PTAConfig.v().getAppConfig().APP_PATH.replace(".jar", "");
      PTAUtils.dumpJimple(pta.getScene(), jimplePath);
      System.out.println("Jimple files have been dumped to: " + jimplePath);
    }
    pta.run();
    return pta;
  }

  public static void mainRun(String[] args) {
    Stopwatch ptaTimer = Stopwatch.newAndStart("Main PTA (including pre-analysis)");
    String jvmName = ManagementFactory.getRuntimeMXBean().getName();
    String s = jvmName.split("@")[0];
    long pid = Long.parseLong(s);
    MemoryWatcher memoryWatcher = new MemoryWatcher(pid, "Main PTA");
    memoryWatcher.start();
    run(args);
    ptaTimer.stop();
    System.out.println(ptaTimer);
    memoryWatcher.stop();
    System.out.println(memoryWatcher);
  }

  public static void main(String[] args) {
    mainRun(args);
  }
}
