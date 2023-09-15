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

import soot.ArrayType;
import soot.RefType;
import soot.SootMethod;
import soot.Value;

/*
 * <java.lang.reflect.Array: java.lang.Object get(java.lang.Object,int)>
 * */

public class JavaLangReflectArrayGet extends NativeMethod {
    JavaLangReflectArrayGet(SootMethod method) {
        super(method);
    }

    @Override
    void simulate() {
        Value arrayBase = getPara(0, ArrayType.v(RefType.v("java.lang.Object"), 1));
        Value arrayRef = getArrayRef(arrayBase);
        Value ret = getNextLocal(RefType.v("java.lang.Object"));
        addAssign(ret, arrayRef);
        addReturn(ret);
    }
}
