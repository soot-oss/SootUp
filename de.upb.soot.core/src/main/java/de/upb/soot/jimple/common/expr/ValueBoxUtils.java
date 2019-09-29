package de.upb.soot.jimple.common.expr;

import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.basic.ValueBox;
import java.util.List;

class ValueBoxUtils {

  private ValueBoxUtils() {}

  static ValueBox[] toValueBoxes(List<? extends Value> args) {
    return args.stream().map(Jimple::newImmediateBox).toArray(ValueBox[]::new);
  }
}
