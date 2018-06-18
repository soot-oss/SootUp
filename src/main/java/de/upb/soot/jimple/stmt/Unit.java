package de.upb.soot.jimple.stmt;

import java.io.Serializable;
import java.util.List;

import de.upb.soot.UnitPrinter;
import de.upb.soot.jimple.ValueBox;
import de.upb.soot.jimple.visitor.IAcceptor;

public interface Unit extends IAcceptor, Serializable {

  /** Returns a list of Boxes containing Values used in this Unit. */
  public List<ValueBox> getUseBoxes();

  /** Returns a list of Boxes containing Values defined in this Unit. */
  public List<ValueBox> getDefBoxes();

  /**
   * Returns a list of Boxes containing Units defined in this Unit; typically branch targets.
   */
  public List<UnitBox> getUnitBoxes();

  /** Returns a list of Boxes pointing to this Unit. */
  public List<UnitBox> getBoxesPointingToThis();

  /** Adds a box to the list returned by getBoxesPointingToThis. */
  public void addBoxPointingToThis(UnitBox b);

  /** Removes a box from the list returned by getBoxesPointingToThis. */
  public void removeBoxPointingToThis(UnitBox b);

  /** Clears any pointers to and from this Unit's UnitBoxes. */
  public void clearUnitBoxes();

  /**
   * Returns a list of Boxes containing any Value either used or defined in this Unit.
   */
  public List<ValueBox> getUseAndDefBoxes();

  public Object clone();

  /**
   * Returns true if execution after this statement may continue at the following statement.
   * GotoStmt will return false but IfStmt will return true.
   */
  public boolean fallsThrough();

  /**
   * Returns true if execution after this statement does not necessarily continue at the following
   * statement. GotoStmt and IfStmt will both return true.
   */
  public boolean branches();

  public void toString(UnitPrinter up);

  /**
   * Redirects jumps to this Unit to newLocation. In general, you shouldn't have to use this
   * directly.
   * 
   * @see PatchingChain#getNonPatchingChain()
   * @see soot.shimple.Shimple#redirectToPreds(Chain, Unit)
   * @see soot.shimple.Shimple#redirectPointers(Unit, Unit)
   **/
  public void redirectJumpsToThisTo(Unit newLocation);

}
