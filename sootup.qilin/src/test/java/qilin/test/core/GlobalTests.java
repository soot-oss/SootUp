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
import qilin.test.util.JunitTests;

public class GlobalTests extends JunitTests {
    @Test
    public void testStaticLoad() {
        checkAssertions(run("qilin.microben.core.global.StaticLoad"));
    }

    @Test
    public void testStaticStore() {
        checkAssertions(run("qilin.microben.core.global.StaticStore"));
    }

    @Test
    public void testStringConstant() {
        checkAssertions(run("qilin.microben.core.global.StringConstant"));
    }
}
