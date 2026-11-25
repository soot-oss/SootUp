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
import qilin.core.config.ContextSensitivity;
import qilin.core.config.PointerAnalysisConfig;
import qilin.test.util.QilinFrameworkTests;

/**
 * Exercises {@link PointerAnalysisConfig#isPreciseExceptions()}, which the rest of the suite leaves
 * off: with it, a thrown object flows to the matching {@code catch} parameter (and out to the
 * caller's handler when nothing in the method catches it) instead of being merged into one global
 * throw field.
 */
public class ExceptionTests extends QilinFrameworkTests {

  @Override
  protected PointerAnalysisConfig.Builder configBuilder(ContextSensitivity contextSensitivity) {
    return super.configBuilder(contextSensitivity).preciseExceptions(true);
  }

  @Test
  public void testSimpleException() {
    checkAssertions(run("qilin.microben.core.exception.SimpleException"));
  }

  @Test
  public void testExceptionChain() {
    checkAssertions(run("qilin.microben.core.exception.ExceptionChain"));
  }

  @Test
  public void testMethodThrow() {
    checkAssertions(run("qilin.microben.core.exception.MethodThrow"));
  }
}
