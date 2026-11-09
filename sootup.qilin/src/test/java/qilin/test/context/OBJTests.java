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

public class OBJTests extends QilinFrameworkTests {
  @Test
  public void testOBJ1k0() {
    checkAssertions(
        run("qilin.microben.context.obj.OBJ1k0", ContextSensitivity.objectSensitive(1, 0)));
  }

  @Test
  public void testOBJ1k1() {
    checkAssertions(
        run("qilin.microben.context.obj.OBJ1k1", ContextSensitivity.objectSensitive(1, 0)));
  }

  @Test
  public void testOBJ1k2() {
    checkAssertions(
        run("qilin.microben.context.obj.OBJ1k2", ContextSensitivity.objectSensitive(1, 0)));
  }

  @Test
  public void testOBJ1k3() {
    checkAssertions(
        run("qilin.microben.context.obj.OBJ1k3", ContextSensitivity.objectSensitive(1, 0)));
  }

  @Test
  public void testOBJ1k4() {
    checkAssertions(
        run("qilin.microben.context.obj.OBJ1k4", ContextSensitivity.objectSensitive(1, 0)));
  }

  @Test
  public void testOBJ1k5() {
    checkAssertions(
        run("qilin.microben.context.obj.OBJ1k5", ContextSensitivity.objectSensitive(1, 0)));
  }

  @Test
  public void testOBJ2k0() {
    checkAssertions(
        run("qilin.microben.context.obj.OBJ2k0", ContextSensitivity.objectSensitive(2, 1)));
  }

  @Test
  public void testOBJ2k1() {
    checkAssertions(
        run("qilin.microben.context.obj.OBJ2k1", ContextSensitivity.objectSensitive(2, 1)));
  }

  @Test
  public void testOBJ2k2() {
    checkAssertions(
        run("qilin.microben.context.obj.OBJ2k2", ContextSensitivity.objectSensitive(2, 1)));
  }
}
