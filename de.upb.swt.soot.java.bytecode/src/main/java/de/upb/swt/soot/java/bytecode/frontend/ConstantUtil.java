package de.upb.swt.soot.java.bytecode.frontend;
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
import de.upb.swt.soot.core.jimple.common.constant.BooleanConstant;
import de.upb.swt.soot.core.jimple.common.constant.Constant;
import de.upb.swt.soot.core.jimple.common.constant.DoubleConstant;
import de.upb.swt.soot.core.jimple.common.constant.FloatConstant;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.constant.LongConstant;
import de.upb.swt.soot.core.jimple.common.constant.NullConstant;
import de.upb.swt.soot.java.core.language.JavaJimple;

public class ConstantUtil {
  static Constant fromObject(Object obj) {
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

    // TODO: [bh] implement MethodHandle, MethodType, ClassConstant?

    throw new IllegalArgumentException("cannot convert Object to (Soot-)Constant.");
  }
}
