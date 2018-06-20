package de.upb.soot.jimple;

import java.io.Serializable;

import de.upb.soot.StmtPrinter;

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

  public void toString(StmtPrinter up);

}