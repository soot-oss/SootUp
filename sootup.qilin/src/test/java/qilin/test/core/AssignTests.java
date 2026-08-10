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

import org.junit.jupiter.api.Test;
import qilin.test.util.QilinLegacyFrameworkTests;

/**
 * Extends {@link QilinLegacyFrameworkTests}, not {@link qilin.test.util.QilinFrameworkTests} -
 * {@code testCastFail} hits {@code CallGraphBuilder.processCallAssign}'s static-call arg/param
 * arity mismatch (IndexOutOfBoundsException at MethodSignature.getParameterType) when analyzed
 * against the current JVM's full runtime image instead of jre1.6.0_45 - same crash signature as
 * {@link ReflogTests}, root cause not yet isolated (not the invokedynamic/StringConcatFactory issue
 * from {@link ClinitTests} - this microbenchmark has no indy call sites).
 */
public class AssignTests extends QilinLegacyFrameworkTests {
  @Test
  public void testCastFail() {
    checkAssertions(run("qilin.microben.core.assign.CastFail"));
  }

  @Test
  public void testCastSucc() {
    checkAssertions(run("qilin.microben.core.assign.CastSucc"));
  }

  @Test
  public void testReceiver2This() {
    checkAssertions(run("qilin.microben.core.assign.Receiver2This"));
  }

  @Test
  public void testSimpleAssign() {
    checkAssertions(run("qilin.microben.core.assign.SimpleAssign"));
  }

  @Test
  public void testReturnValue0() {
    checkAssertions(run("qilin.microben.core.assign.ReturnValue0"));
  }

  @Test
  public void testReturnValue1() {
    checkAssertions(run("qilin.microben.core.assign.ReturnValue1"));
  }

  @Test
  public void testReturnValue2() {
    checkAssertions(run("qilin.microben.core.assign.ReturnValue2"));
  }

  @Test
  public void testReturnValue3() {
    checkAssertions(run("qilin.microben.core.assign.ReturnValue3"));
  }

  @Test
  public void testInterAssign() {
    checkAssertions(run("qilin.microben.core.assign.InterAssign"));
  }

  @Test
  public void testStaticParameter() {
    checkAssertions(run("qilin.microben.core.assign.StaticParameter"));
  }

  @Test
  public void testNullPointer() {
    checkAssertions(run("qilin.microben.core.assign.NullPointer"));
  }

  @Test
  public void testRecursion() {
    checkAssertions(run("qilin.microben.core.assign.Recursion"));
  }
}
