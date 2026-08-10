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

package qilin.test.util;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import qilin.core.PTA;
import qilin.core.PointerAnalysisFactory;
import qilin.core.config.ContextSensitivity;
import qilin.core.config.PointerAnalysisConfig;
import qilin.util.PTAUtils;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * For the small minority of qilin's test suites that - unlike {@link QilinFrameworkTests} - can't
 * run against the current JVM's own runtime image: their assertions are calibrated to a real
 * jre1.6.0_45's exact library internals/object graph, or they otherwise hit an issue specific to
 * analyzing the full modern JDK runtime (see each subclass's own javadoc for why). Deliberately not
 * a subclass of {@link QilinFrameworkTests} - its {@code appPath} is unrelated to a JRE choice, but
 * sharing static state with a base that assumes the current-JVM-runtime path would still risk
 * confusion, so this pins its own copy instead. Reuses {@link QilinFrameworkTests}'s
 * config/assertion-checking helpers, which take no JRE-related state.
 */
public abstract class QilinLegacyFrameworkTests {
  protected static String appPath, jrePath, refLogPath;
  protected static boolean isSetUp = false;

  @BeforeAll
  public static void setUp() throws IOException {
    if (isSetUp) {
      return;
    }
    File rootDir = new File("../");
    File testDir =
        new File(
            rootDir, "sootup.qilin" + File.separator + "target" + File.separator + "test-classes");
    appPath = testDir.getCanonicalPath();
    File refLogDir =
        new File(
            rootDir,
            "sootup.qilin"
                + File.separator
                + "src"
                + File.separator
                + "test"
                + File.separator
                + "java"
                + File.separator
                + "qilin"
                + File.separator
                + "microben"
                + File.separator
                + "core"
                + File.separator
                + "reflog");
    refLogPath = refLogDir.getCanonicalPath();
    File jreFile =
        new File(
            rootDir,
            "artifact"
                + File.separator
                + "benchmarks"
                + File.separator
                + "JREs"
                + File.separator
                + "jre1.6.0_45");
    jrePath = jreFile.getCanonicalPath();
    isSetUp = true;
  }

  protected PTA run(String mainClass) {
    return run(mainClass, ContextSensitivity.insensitive());
  }

  protected PTA run(String mainClass, ContextSensitivity contextSensitivity) {
    PointerAnalysisConfig config =
        QilinFrameworkTests.configBuilder(contextSensitivity, refLogPath).build();
    View view = PTAUtils.createView(appPath, null, jrePath);
    ClassType mainClassType = view.getIdentifierFactory().getClassType(mainClass);
    PTA pta = PointerAnalysisFactory.create(view, mainClassType, config);
    // see QilinFrameworkTests#run(String, PointerAnalysisConfig) for why not pureRun()
    pta.run();
    return pta;
  }

  protected void checkAssertions(PTA pta) {
    QilinFrameworkTests.checkAssertions(pta);
  }
}
