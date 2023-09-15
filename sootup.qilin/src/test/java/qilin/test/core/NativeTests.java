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

import org.junit.Ignore;
import org.junit.Test;
import qilin.test.util.JunitTests;

public class NativeTests extends JunitTests {
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
        checkAssertions(run("qilin.microben.core.natives.PrivilegedActions2", "2o"));
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
    public void testTreadRun() {
        checkAssertions(run("qilin.microben.core.natives.TreadRun"));
    }

    @Test
    @Ignore
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
