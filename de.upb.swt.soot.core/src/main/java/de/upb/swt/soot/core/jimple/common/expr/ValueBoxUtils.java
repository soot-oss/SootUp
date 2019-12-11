package de.upb.swt.soot.core.jimple.common.expr;

import de.upb.swt.soot.core.jimple.basic.ImmediateBox;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.basic.ValueBox;
import java.util.List;

class ValueBoxUtils {

  private ValueBoxUtils() {}

  static ValueBox[] toValueBoxes(List<? extends Value> args) {
    return args.stream().map(ValueBoxUtils::newImmediateBox).toArray(ValueBox[]::new);
  }

  public static ValueBox newImmediateBox(Value value) {
    return new ImmediateBox(value);
  }
}
