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
import qilin.util.ViewFactory;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * Runs against the current JVM's own runtime image ({@link ViewFactory#createView(String, String)})
 * - safe because {@code singleEntry(true)} (below) skips {@code FakeMainFactory}'s pre-JDK9
 * JVM-bootstrap modeling, and none of this base's subclasses' microbenchmarks touch java.util
 * internals whose object graph differs across JRE versions.
 *
 * <p>{@link qilin.test.context.CollectionsTests} is the one exception - its assertions are
 * calibrated to a real jre1.6.0_45's exact {@code java.util.HashMap}/{@code HashSet} internals - so
 * it deliberately does not extend this class (see its own javadoc).
 */
public abstract class QilinFrameworkTests {
  protected static String appPath, refLogPath;
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

  protected PointerAnalysisConfig.Builder configBuilder(ContextSensitivity contextSensitivity) {
    return configBuilder(contextSensitivity, refLogPath);
  }

  /**
   * Static/parameterized so classes that don't extend {@link QilinFrameworkTests} (e.g. {@link
   * qilin.test.context.CollectionsTests}, pinned to a different JRE) can still share these config
   * defaults instead of duplicating them.
   */
  public static PointerAnalysisConfig.Builder configBuilder(
      ContextSensitivity contextSensitivity, String refLogPath) {
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
    View view = ViewFactory.createView(appPath, null);
    ClassType mainClassType = view.getIdentifierFactory().getClassType(mainClass);
    PTA pta = PointerAnalysisFactory.create(view, mainClassType, config);
    // NOT pta.pureRun(): for staged toolkit variants (Zipper, DebloatedPTA/Moon, Bean, ...)
    // pureRun() only calls getPropagator().propagate() and skips StagedPTA's
    // preAnalysis()/mainAnalysis() entirely. run() is the correct top-level entry point for
    // every PTA - it delegates to pureRun() for plain CoreVariantPTA too, so this is safe there.
    pta.run();
    return pta;
  }

  public static void checkAssertions(PTA pta) {
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

  public static void checkAssertions(PTA pta, boolean requirePrecision) {
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
