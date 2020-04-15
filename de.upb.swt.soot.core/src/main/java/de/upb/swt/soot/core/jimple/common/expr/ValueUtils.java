package de.upb.swt.soot.core.jimple.common.expr;

import de.upb.swt.soot.core.jimple.basic.Immediate;
import de.upb.swt.soot.core.jimple.basic.Value;
import java.util.List;

class ValueUtils {

  private ValueUtils() {}

  static Value[] toValuesArray(List<? extends Value> args) {
    Value[] argsArray = new Value[args.size()];
    for (int i = 0; i < args.size(); i++) {
      Value value = args.get(i);
      if (value == null) {
        throw new IllegalArgumentException("value may not be null");
      }
      if (value instanceof Immediate) {
        argsArray[i] = value;
      } else {
        throw new RuntimeException(
            "List cannot contain value: " + value + " (" + value.getClass() + ")");
      }
    }
    return argsArray;
  }
}
