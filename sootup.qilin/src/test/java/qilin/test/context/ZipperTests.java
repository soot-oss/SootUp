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

public class ZipperTests extends QilinFrameworkTests {
  @Test
  public void testZipperOBJ2k0() {
    checkSoundAssertions(
        run("qilin.microben.context.obj.OBJ2k0", ContextSensitivity.zipperObjectSensitive(2, 1)));
  }

  @Test
  public void testZipperOBJ2k1() {
    checkSoundAssertions(
        run("qilin.microben.context.obj.OBJ2k1", ContextSensitivity.zipperObjectSensitive(2, 1)));
  }

  @Test
  public void testZipperOBJ2k2() {
    checkSoundAssertions(
        run("qilin.microben.context.obj.OBJ2k2", ContextSensitivity.zipperObjectSensitive(2, 1)));
  }

  @Test
  public void testZipperCFA1k0() {
    checkSoundAssertions(
        run("qilin.microben.context.cfa.CFA1k0", ContextSensitivity.zipperCallSite(1, 0)));
  }

  @Test
  public void testZipperExpressOBJ2k0() {
    checkSoundAssertions(
        run(
            "qilin.microben.context.obj.OBJ2k0",
            ContextSensitivity.zipperExpressObjectSensitive(2, 1)));
  }

  @Test
  public void testZipperExpressOBJ2k1() {
    checkSoundAssertions(
        run(
            "qilin.microben.context.obj.OBJ2k1",
            ContextSensitivity.zipperExpressObjectSensitive(2, 1)));
  }
}
