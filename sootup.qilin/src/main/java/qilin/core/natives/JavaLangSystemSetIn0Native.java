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

package qilin.core.natives;

import soot.SootMethod;
import soot.Value;

public class JavaLangSystemSetIn0Native extends NativeMethod {
    public JavaLangSystemSetIn0Native(SootMethod method) {
        super(method);
    }

    /**
     * NOTE: this native method is not documented in JDK API. It should have the
     * side effect: System.in = parameter
     * <p>
     * private static native void setIn0(java.io.InputStream);
     */
    public void simulate() {
        Value r1 = getPara(0);
        Value systemIn = getStaticFieldRef("java.lang.System", "in");
        addAssign(systemIn, r1);
    }
}
