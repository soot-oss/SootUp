package de.upb.soot.jimple.common.stmt;

import de.upb.soot.StmtPrinter;
import de.upb.soot.jimple.basic.StmtBox;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.common.expr.AbstractInvokeExpr;
import de.upb.soot.jimple.common.ref.FieldRef;
import de.upb.soot.jimple.common.ref.JArrayRef;
import de.upb.soot.jimple.visitor.IAcceptor;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;

import java.io.Serializable;
import java.util.List;

import scala.Unit;

public interface Stmt extends IAcceptor, Serializable {

  /** Returns a list of Boxes containing Values used in this Unit. */
  public List<ValueBox> getUseBoxes();

  /** Returns a list of Boxes containing Values defined in this Unit. */
  public List<ValueBox> getDefBoxes();

  /**
   * Returns a list of Boxes containing Units defined in this Unit; typically branch targets.
   */
  public List<StmtBox> getUnitBoxes();

  /** Returns a list of Boxes pointing to this Unit. */
  public List<StmtBox> getBoxesPointingToThis();

  /** Adds a box to the list returned by getBoxesPointingToThis. */
  public void addBoxPointingToThis(StmtBox b);

  /** Removes a box from the list returned by getBoxesPointingToThis. */
  public void removeBoxPointingToThis(StmtBox b);

  /** Clears any pointers to and from this Unit's UnitBoxes. */
  public void clearUnitBoxes();

  /**
   * Returns a list of Boxes containing any Value either used or defined in this Unit.
   */
  public List<ValueBox> getUseAndDefBoxes();

  public Stmt clone();

  /**
   * Returns true if execution after this statement may continue at the following statement. GotoStmt will return false but
   * IfStmt will return true.
   */
  public boolean fallsThrough();

  /**
   * Returns true if execution after this statement does not necessarily continue at the following statement. GotoStmt and
   * IfStmt will both return true.
   */
  public boolean branches();

  /**
   * Redirects jumps to this Unit to newLocation. In general, you shouldn't have to use this directly.
   * 
   * @see PatchingChain#getNonPatchingChain()
   * @see soot.shimple.Shimple#redirectToPreds(Chain, Unit)
   * @see soot.shimple.Shimple#redirectPointers(Unit, Unit)
   **/
  public void redirectJumpsToThisTo(Stmt newLocation);

  public void toString(StmtPrinter up);

  public boolean containsInvokeExpr();

  public AbstractInvokeExpr getInvokeExpr();

  public ValueBox getInvokeExprBox();

  public boolean containsArrayRef();

  public JArrayRef getArrayRef();

  public ValueBox getArrayRefBox();

  public boolean containsFieldRef();

  public FieldRef getFieldRef();

  public ValueBox getFieldRefBox();

  public void setPosition(Position position);
}
