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
import qilin.core.config.PointerAnalysisConfig;
import qilin.test.util.QilinFrameworkTests;

public class MoonTests extends QilinFrameworkTests {
  @Test
  public void testMoonOBJ2k0() {
    checkSoundAssertions(
        run(
            "qilin.microben.context.obj.OBJ2k0",
            ContextSensitivity.objectSensitive(2, 1),
            PointerAnalysisConfig.DebloatApproach.MOON));
  }

  @Test
  public void testMoonOBJ2k1() {
    checkSoundAssertions(
        run(
            "qilin.microben.context.obj.OBJ2k1",
            ContextSensitivity.objectSensitive(2, 1),
            PointerAnalysisConfig.DebloatApproach.MOON));
  }

  @Test
  public void testMoonOBJ2k2() {
    checkSoundAssertions(
        run(
            "qilin.microben.context.obj.OBJ2k2",
            ContextSensitivity.objectSensitive(2, 1),
            PointerAnalysisConfig.DebloatApproach.MOON));
  }
}
