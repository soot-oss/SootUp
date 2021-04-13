package de.upb.swt.soot.java.bytecode.frontend;

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
