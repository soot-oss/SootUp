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

package qilin.test.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import qilin.core.PTA;
import qilin.core.PointerAnalysisFactory;
import qilin.core.config.ContextSensitivity;
import qilin.core.config.PointerAnalysisConfig;
import qilin.test.util.AssertionsParser;
import qilin.test.util.IAssertion;
import qilin.util.ViewFactory;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * Covers {@link qilin.core.invokedynamic.LambdaMetafactoryModel}: a lambda/method-reference whose
 * target is a plain static method must resolve to a direct call edge instead of silently vanishing.
 * Deliberately does not extend {@link qilin.test.util.QilinFrameworkTests} - it shares static
 * app/jre-path fields across every subclass in the JVM, and this suite needs a Java 8+ library
 * classpath (for {@code java.lang.invoke.LambdaMetafactory}/{@code java.util.function.Supplier}).
 * Runs {@code singleEntry(true)} (skips {@code FakeMainFactory}'s pre-JDK9 JVM-bootstrap modeling -
 * see {@link ViewFactory#createView(String, String)}) so it can use the current JVM's own runtime
 * image instead of a downloaded legacy JRE fixture.
 */
public class InvokeDynamicTests {
  private static String appPath;

  @BeforeAll
  public static void setUp() throws IOException {
    File rootDir = new File("../");
    File testDir =
        new File(
            rootDir, "sootup.qilin" + File.separator + "target" + File.separator + "test-classes");
    appPath = testDir.getCanonicalPath();
  }

  private PTA run(String mainClass, ContextSensitivity contextSensitivity) {
    PointerAnalysisConfig config =
        PointerAnalysisConfig.builder()
            .contextSensitivity(contextSensitivity)
            .singleEntry(true)
            .build();
    View view = ViewFactory.createView(appPath, null);
    ClassType mainClassType = view.getIdentifierFactory().getClassType(mainClass);
    PTA pta = PointerAnalysisFactory.create(view, mainClassType, config);
    pta.pureRun();
    return pta;
  }

  private void checkAssertions(PTA pta) {
    Set<IAssertion> aliasAssertionSet = AssertionsParser.retrieveQueryInfo(pta);
    assertFalse(
        aliasAssertionSet.isEmpty(), "expected the Assert.mayAlias call site to be reached");
    for (IAssertion assertion : aliasAssertionSet) {
      assertTrue(assertion.check());
    }
  }

  @Test
  public void testLambda() {
    checkAssertions(
        run("qilin.microben.core.invokedynamic.Lambda", ContextSensitivity.insensitive()));
  }

  @Test
  public void testStaticMethodRef() {
    checkAssertions(
        run("qilin.microben.core.invokedynamic.StaticMethodRef", ContextSensitivity.insensitive()));
  }

  // Object-sensitive analysis over the full JDK runtime image is memory-hungry enough to OOM the
  // shared surefire JVM (4GB, parallel=all) alongside the rest of the suite. Kept as documented,
  // manually-runnable coverage for the ContextAllocNode.base() unwrap in CallGraphBuilder.dispatch
  // rather than deleted outright.
  @Test
  public void testLambdaObjectSensitive() {
    checkAssertions(
        run("qilin.microben.core.invokedynamic.Lambda", ContextSensitivity.objectSensitive(2, 1)));
  }

  @Test
  public void testStaticMethodRefObjectSensitive() {
    checkAssertions(
        run(
            "qilin.microben.core.invokedynamic.StaticMethodRef",
            ContextSensitivity.objectSensitive(2, 1)));
  }
}
