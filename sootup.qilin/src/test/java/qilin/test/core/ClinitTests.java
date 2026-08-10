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
 * {@code testClinitNewExpr}'s microbenchmark does {@code "hello2" + a} (javac compiles this to an
 * {@code invokedynamic} call to {@code StringConcatFactory.makeConcatWithConstants}), which
 * confuses qilin's invokedynamic/lambda resolution when analyzed against a real {@code
 * java.lang.invoke} (present in the current JVM's runtime, essentially absent in jre1.6.0_45) - see
 * {@code qilin.core.invokedynamic.LambdaMetafactoryModel}. A real bug, not fixed here.
 */
public class ClinitTests extends QilinLegacyFrameworkTests {
  @Test
  public void testClinitNewExpr() {
    checkAssertions(run("qilin.microben.core.clinit.ClinitNewExpr"));
  }

  @Test
  public void testClinitStaticCall() {
    checkAssertions(run("qilin.microben.core.clinit.ClinitStaticCall"));
  }

  @Test
  public void testClinitStaticLoad() {
    checkAssertions(run("qilin.microben.core.clinit.ClinitStaticLoad"));
  }

  @Test
  public void testClinitStaticStore() {
    checkAssertions(run("qilin.microben.core.clinit.ClinitStaticStore"));
  }
}
