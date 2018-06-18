package de.upb.soot.jimple;

import de.upb.soot.jimple.type.Type;

public interface Local extends Value, Numberable, Immediate {
  /** Returns the name of the current Local variable. */
  public String getName();

  /** Sets the name of the current variable. */
  public void setName(String name);

  /** Sets the type of the current variable. */
  public void setType(Type t);
}
