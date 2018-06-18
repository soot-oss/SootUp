package de.upb.soot.core;

import java.io.Serializable;

import de.upb.soot.UnitPrinter;

/**
 * A box which can contain values.
 *
 * @see Value
 */
public interface ValueBox extends Serializable {
  /** Sets the value contained in this box as given. Subject to canContainValue() checks. */
  public void setValue(Value value);

  /** Returns the value contained in this box. */
  public Value getValue();

  /** Returns true if the given Value fits in this box. */
  public boolean canContainValue(Value value);

  public void toString(UnitPrinter up);

}