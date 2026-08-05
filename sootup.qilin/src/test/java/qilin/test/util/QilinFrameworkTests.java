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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import qilin.core.PTA;
import qilin.core.PointerAnalysisFactory;
import qilin.core.config.ContextSensitivity;
import qilin.core.config.PointerAnalysisConfig;
import qilin.pta.tools.DebloatedPTA;
import qilin.util.PTAUtils;
import sootup.core.types.ClassType;
import sootup.core.views.View;

public abstract class QilinFrameworkTests {
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
    System.out.println("APP_PATH:" + appPath);
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
            ".."
                + File.separator
                + "artifact"
                + File.separator
                + "benchmarks"
                + File.separator
                + "JREs"
                + File.separator
                + "jre1.6.0_45"
            //    + "jre1.8.0_121_debug"
            );
    jrePath = jreFile.getCanonicalPath();
    isSetUp = true;
  }

  public PTA run(String mainClass) {
    return run(mainClass, ContextSensitivity.insensitive());
  }

  public PTA run(String mainClass, ContextSensitivity contextSensitivity) {
    return run(mainClass, configBuilder(contextSensitivity).build());
  }

  public PTA run(
      String mainClass,
      ContextSensitivity contextSensitivity,
      PointerAnalysisConfig.DebloatApproach debloatApproach) {
    return run(
        mainClass,
        configBuilder(contextSensitivity)
            .ctxDebloating(true)
            .debloatApproach(debloatApproach)
            .build());
  }

  private PointerAnalysisConfig.Builder configBuilder(ContextSensitivity contextSensitivity) {
    return PointerAnalysisConfig.builder()
        .contextSensitivity(contextSensitivity)
        .singleEntry(true)
        // ON_THE_FLY clinit handling (default: seedEntryPointClinits=false + admit-all resolver)
        .enforceEmptyCtxForIgnoreTypes(true)
        .heapAbstractionPolicy(PointerAnalysisConfig.HeapAbstractionPolicy.HEURISTIC_MERGE)
        .preciseArrayElement(true)
        .preciseExceptions(true)
        .reflectionLogPath(refLogPath + File.separator + "Reflection.log")
        .analysisName(contextSensitivity.toString());
  }

  private PTA run(String mainClass, PointerAnalysisConfig config) {
    View view = PTAUtils.createView(appPath, null, jrePath);
    ClassType mainClassType = view.getIdentifierFactory().getClassType(mainClass);
    PTA pta = PointerAnalysisFactory.create(view, mainClassType, config);
    // NOT pta.pureRun(): for staged toolkit variants (Zipper, DebloatedPTA/Moon, Bean, ...)
    // pureRun() only calls getPropagator().propagate() and skips StagedPTA's
    // preAnalysis()/mainAnalysis() entirely. run() is the correct top-level entry point for
    // every PTA - it delegates to pureRun() for plain CoreVariantPTA too, so this is safe there.
    pta.run();
    return pta;
  }

  protected void checkAssertions(PTA pta) {
    checkAssertions(pta, true);
  }

  /**
   * For selective/heuristic context-sensitivity approaches (Zipper, Moon, ...): those only promise
   * soundness ("may-alias" claims must still hold), not full k-obj precision - they may
   * legitimately decide a given object/method isn't "precision-critical" and merge contexts there,
   * which can make a "not-alias" claim from the plain-k-obj benchmark suite fail without that being
   * a bug. Precision-only failures are printed, not asserted, so a real regression (or an unsound
   * may-alias miss) still fails the build.
   */
  protected void checkSoundAssertions(PTA pta) {
    checkAssertions(pta, false);
  }

  private void checkAssertions(PTA pta, boolean requirePrecision) {
    // DebloatedPTA's own getPag()/getReachableMethods() are its throwaway pre-basePTA-assignment
    // state (see DebloatedPTA#getBasePTA) - the finished analysis and its PAG live on basePTA.
    if (pta instanceof DebloatedPTA debloatedPTA) {
      pta = debloatedPTA.getBasePTA();
    }
    Set<IAssertion> aliasAssertionSet = AssertionsParser.retrieveQueryInfo(pta);
    for (IAssertion mAssert : aliasAssertionSet) {
      boolean answer = mAssert.check();
      System.out.println("Assertion is " + answer);
      if (requirePrecision || mAssert.isSoundnessCritical()) {
        assertTrue(answer);
      } else if (!answer) {
        System.out.println(
            "Precision-only assertion failed (not a soundness issue, not asserted): " + mAssert);
      }
    }
  }
}
