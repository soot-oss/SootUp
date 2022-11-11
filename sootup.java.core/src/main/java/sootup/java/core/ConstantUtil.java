package sootup.java.core;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2020 Bastian Haverkamp, Markus Schmidt
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
import sootup.core.jimple.common.constant.BooleanConstant;
import sootup.core.jimple.common.constant.ClassConstant;
import sootup.core.jimple.common.constant.Constant;
import sootup.core.jimple.common.constant.DoubleConstant;
import sootup.core.jimple.common.constant.FloatConstant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.LongConstant;
import sootup.core.jimple.common.constant.NullConstant;
import sootup.java.core.language.JavaJimple;

public class ConstantUtil {
  public static Constant fromObject(Object obj) {
    if (obj == null) {
      return NullConstant.getInstance();
    }
    if (obj instanceof Boolean) {
      return BooleanConstant.getInstance((Boolean) obj);
    }
    if (obj instanceof Float) {
      return FloatConstant.getInstance((Float) obj);
    }
    if (obj instanceof Double) {
      return DoubleConstant.getInstance((Double) obj);
    }
    if (obj instanceof Integer) {
      return IntConstant.getInstance((Integer) obj);
    }
    if (obj instanceof Long) {
      return LongConstant.getInstance((Long) obj);
    }
    if (obj instanceof String) {
      return JavaJimple.getInstance().newStringConstant((String) obj);
    }

    if (obj instanceof String[]) {
      // is an enum
      // [0] is the fully qualified name of the enum
      // [1] is the value of the enum
      String[] enumData = (String[]) obj;
      return JavaJimple.getInstance().newEnumConstant(enumData[1], enumData[0]);
    }

    if (obj instanceof ClassConstant) {
      return JavaJimple.getInstance().newClassConstant(((ClassConstant) obj).getValue());
    }
    // TODO: [bh] implement MethodHandle, MethodType?

    throw new IllegalArgumentException("cannot convert Object to (Soot-)Constant.");
  }
}
