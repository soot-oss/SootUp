package de.upb.soot.jimple;

import de.upb.soot.StmtPrinter;

/**
 * Reference implementation for ValueBox; just add a canContainValue method.
 */
@SuppressWarnings("serial")
public abstract class AbstractValueBox implements ValueBox {
  Value value;

  public void setValue(Value value) {
    if (value == null) {
      throw new IllegalArgumentException("value may not be null");
    }
    if (canContainValue(value)) {
      this.value = value;
    } else {
      throw new RuntimeException(
          "Box " + this + " cannot contain value: " + value + "(" + value.getClass() + ")");
    }
  }

  public Value getValue() {
    return value;
  }

  public void toString(StmtPrinter up) {
    up.startValueBox(this);
    value.toString(up);
    up.endValueBox(this);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(" + value + ")";
  }
}
