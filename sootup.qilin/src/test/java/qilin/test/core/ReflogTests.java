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

import org.junit.Test;
import qilin.core.PTA;
import qilin.test.util.JunitTests;

public class ReflogTests extends JunitTests {
    @Test
    public void testFieldGetStatic() {
        checkAssertions(run("qilin.microben.core.reflog.FieldGetStatic"));
    }

    @Test
    public void testFieldGet() {
        checkAssertions(run("qilin.microben.core.reflog.FieldGet"));
    }

    @Test
    public void testFieldSetStatic() {
        checkAssertions(run("qilin.microben.core.reflog.FieldSetStatic"));
    }

    @Test
    public void testFieldSet() {
        checkAssertions(run("qilin.microben.core.reflog.FieldSet"));
    }

    @Test
    public void testArrayNewInstance() {
        checkAssertions(run("qilin.microben.core.reflog.ArrayNewInstance"));
    }

    @Test
    public void testConstructorNewInstance() {
        checkAssertions(run("qilin.microben.core.reflog.ConstructorNewInstance"));
    }

    @Test
    public void testMethodInvokeStatic() {
        checkAssertions(run("qilin.microben.core.reflog.MethodInvokeStatic"));
    }

    @Test
    public void testMethodInvoke() {
        checkAssertions(run("qilin.microben.core.reflog.MethodInvoke"));
    }

    @Test
    public void testClassNewInstance() {
        checkAssertions(run("qilin.microben.core.reflog.ClassNewInstance"));
    }

    @Test
    public void testDoopRefBug() {
        PTA pta = run("qilin.microben.core.reflog.DoopRefBug");
        checkAssertions(pta);
    }

    @Test
    public void testClassForName() {
        PTA pta = run("qilin.microben.core.reflog.ClassForName");
        checkAssertions(pta);
    }

    @Test
    public void testClassForName1() {
        PTA pta = run("qilin.microben.core.reflog.ClassForName1");
        checkAssertions(pta);
    }
}