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

package qilin.test.context;

import org.junit.jupiter.api.Test;
import qilin.core.config.ContextSensitivity;
import qilin.test.util.QilinFrameworkTests;

public class CFATests extends QilinFrameworkTests {

  @Test
  public void testCFA1k0() {
    checkAssertions(run("qilin.microben.context.cfa.CFA1k0", ContextSensitivity.callSite(1)));
  }

  @Test
  public void testCFA1k1() {
    checkAssertions(run("qilin.microben.context.cfa.CFA1k1", ContextSensitivity.callSite(1)));
  }

  @Test
  public void testCFA1k2() {
    checkAssertions(run("qilin.microben.context.cfa.CFA1k2", ContextSensitivity.callSite(1)));
  }

  @Test
  public void testCFA2k() {
    checkAssertions(run("qilin.microben.context.cfa.CFA2k", ContextSensitivity.callSite(2)));
  }
}
