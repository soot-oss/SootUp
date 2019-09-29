package de.upb.soot.core.jimple.common.expr;

import de.upb.soot.core.jimple.Jimple;
import de.upb.soot.core.jimple.basic.Value;
import de.upb.soot.core.jimple.basic.ValueBox;
import java.util.List;

class ValueBoxUtils {

  private ValueBoxUtils() {}

  static ValueBox[] toValueBoxes(List<? extends Value> args) {
    return args.stream().map(Jimple::newImmediateBox).toArray(ValueBox[]::new);
  }
}
