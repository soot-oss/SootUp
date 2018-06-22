package de.upb.soot.jimple;

import de.upb.soot.StmtPrinter;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.jimple.visitor.IAcceptor;

import java.io.Serializable;
import java.util.List;

/**
 * Data used as, for instance, arguments to instructions; typical implementations are constants or expressions.
 *
 * Values are typed, clonable and must declare which other Values they use (contain).
 */
public interface Value extends IAcceptor, EquivTo, Serializable {
  /**
   * Returns a List of boxes corresponding to Values which are used by (ie contained within) this Value.
   */
  public List<ValueBox> getUseBoxes();

  /** Returns the Soot type of this Value. */
  public Type getType();

  /** Returns a clone of this Value. */
  public Object clone();

  public void toString(StmtPrinter up);
}
