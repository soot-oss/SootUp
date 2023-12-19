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

import qilin.util.PTAUtils;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.model.SootMethod;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;

/*
 * This file will be temporarily discarded. Yes, it is correct but need one more level of context.
 * Thus it will make qilin.spark less precise than its counterpart in Doop.
 * */
public class JavaLangSystemArraycopyNative extends NativeMethod {
  public JavaLangSystemArraycopyNative(SootMethod method) {
    super(method);
  }

  /**
   * never make a[] = b[], it violates the principle of jimple statement. make a temporary variable.
   */
  protected void simulateImpl() {
    //        Value srcArr = getPara(0);
    //        Value dstArr = getPara(2);
    ClassType objType = PTAUtils.getClassType("java.lang.Object");
    Value srcArr = getPara(0, new ArrayType(objType, 1));
    Value dstArr = getPara(2, new ArrayType(objType, 1));
    Value src = getArrayRef(srcArr);
    JArrayRef dst = getArrayRef(dstArr);
    Local temp = getNextLocal(objType);
    addAssign(temp, src);
    addAssign(dst, temp);
  }
}
