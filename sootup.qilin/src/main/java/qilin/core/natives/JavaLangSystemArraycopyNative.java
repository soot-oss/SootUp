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
 * This file will be temporarily discarded. Yes, it is correct but need one more level of context.
 * Thus it will make qilin.spark less precise than its counterpart in Doop.
 * */
public class JavaLangSystemArraycopyNative extends NativeMethod {
    public JavaLangSystemArraycopyNative(SootMethod method) {
        super(method);
    }

    /**
     * never make a[] = b[], it violates the principle of jimple statement. make a
     * temporary variable.
     */
    public void simulate() {
//        Value srcArr = getPara(0);
//        Value dstArr = getPara(2);
        Value srcArr = getPara(0, ArrayType.v(RefType.v("java.lang.Object"), 1));
        Value dstArr = getPara(2, ArrayType.v(RefType.v("java.lang.Object"), 1));
        Value src = getArrayRef(srcArr);
        Value dst = getArrayRef(dstArr);
        Value temp = getNextLocal(RefType.v("java.lang.Object"));
        addAssign(temp, src);
        addAssign(dst, temp);
    }
}
