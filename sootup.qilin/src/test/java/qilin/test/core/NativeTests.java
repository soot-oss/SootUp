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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import qilin.core.PTA;
import qilin.core.PointerAnalysisFactory;
import qilin.core.config.ContextSensitivity;
import qilin.core.config.PointerAnalysisConfig;
import qilin.test.util.QilinFrameworkTests;
import qilin.util.ViewFactory;
import sootup.callgraph.scope.AppOnlyClinitCallResolver;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * Overrides {@code run} to use {@link AppOnlyClinitCallResolver} - drops {@code <clinit>}
 * candidates for library classes (everything from the current JVM runtime, {@link
 * qilin.test.util.QilinFrameworkTests}'s default) while still triggering app classes' own clinits.
 * Without it, analyzing this suite's native-modeling-heavy microbenchmarks against the full modern
 * JDK runtime image reaches into java.base's own unrelated internals (Character's Unicode tables,
 * sun.security.* policy/X.509/OID machinery, java.lang.invoke, BigInteger, java.time/locale - none
 * of it related to what these tests actually exercise, all of it reached purely because qilin
 * recursively triggers every reached class's superclass/interface chain's clinits) - ~13000
 * reachable methods for a single test, OOMs the shared 4GB surefire JVM. With app-only clinit
 * resolution, the same test measures well under jre1.6.0_45's own baseline.
 */
public class NativeTests extends QilinFrameworkTests {

  @Override
  public PTA run(String mainClass, ContextSensitivity contextSensitivity) {
    View view = ViewFactory.createView(appPath, null);
    ClassType mainClassType = view.getIdentifierFactory().getClassType(mainClass);
    PointerAnalysisConfig config =
        QilinFrameworkTests.configBuilder(contextSensitivity, refLogPath)
            .clinitVirtualCallResolver(new AppOnlyClinitCallResolver(view))
            .build();
    PTA pta = PointerAnalysisFactory.create(view, mainClassType, config);
    pta.run();
    return pta;
  }

  @Override
  public PTA run(String mainClass) {
    return run(mainClass, ContextSensitivity.insensitive());
  }

  @Test
  public void testArrayCopy() {
    checkAssertions(run("qilin.microben.core.natives.ArrayCopy"));
  }

  @Test
  public void testObjectClone() {
    checkAssertions(run("qilin.microben.core.natives.ObjectClone"));
  }

  @Test
  public void testPrivilegedActions0() {
    checkAssertions(run("qilin.microben.core.natives.PrivilegedActions0"));
  }

  @Test
  public void testPrivilegedActions1() {
    checkAssertions(run("qilin.microben.core.natives.PrivilegedActions1"));
  }

  @Test
  public void testPrivilegedActions2() {
    checkAssertions(
        run(
            "qilin.microben.core.natives.PrivilegedActions2",
            ContextSensitivity.objectSensitive(2, 1)));
  }

  @Test
  public void testSystemIn() {
    checkAssertions(run("qilin.microben.core.natives.SystemIn"));
  }

  @Test
  public void testSystemOut() {
    checkAssertions(run("qilin.microben.core.natives.SystemOut"));
  }

  @Test
  public void testSystemErr() {
    checkAssertions(run("qilin.microben.core.natives.SystemErr"));
  }

  @Test
  public void testFinalize() {
    checkAssertions(run("qilin.microben.core.natives.Finalize"));
  }

  @Test
  public void testThreadRun() {
    checkAssertions(run("qilin.microben.core.natives.ThreadRun"));
  }

  @Disabled
  @Test
  public void testCurrentThread() {
    checkAssertions(run("qilin.microben.core.natives.CurrentThread"));
  }

  @Test
  public void testRefArrayGet() {
    checkAssertions(run("qilin.microben.core.natives.RefArrayGet"));
  }

  @Test
  public void testRefArraySet() {
    checkAssertions(run("qilin.microben.core.natives.RefArraySet"));
  }
}
