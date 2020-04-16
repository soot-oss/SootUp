package de.upb.swt.soot.core.jimple.common.expr;

import de.upb.swt.soot.core.jimple.basic.Immediate;
import de.upb.swt.soot.core.jimple.basic.Value;
import sun.awt.im.InputMethodManager;

import javax.annotation.Nonnull;
import java.util.List;

class ValueUtils {

  private ValueUtils() {}

  static Value[] toValueArray(List<? extends Immediate> args) {
    Value[] argsArray = new Value[args.size()];
    for (int i = 0; i < args.size(); i++) {
      @Nonnull Immediate value =  args.get(i);
      if (value == null) {
        throw new IllegalArgumentException("value may not be null");
      }
      argsArray[i] = (Value) value;
    }
    return argsArray;
  }
}
