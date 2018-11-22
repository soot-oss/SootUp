package de.upb.soot.jimple.common.stmt;

import de.upb.soot.jimple.basic.EquivTo;
import de.upb.soot.jimple.basic.IStmtBox;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.common.expr.AbstractInvokeExpr;
import de.upb.soot.jimple.common.ref.FieldRef;
import de.upb.soot.jimple.common.ref.JArrayRef;
import de.upb.soot.jimple.visitor.IAcceptor;
import de.upb.soot.util.printer.IStmtPrinter;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;

import java.io.Serializable;
import java.util.List;

public interface IStmt extends EquivTo, IAcceptor, Serializable {
  /** Returns a list of Boxes containing Values used in this Stmt. */
  public List<ValueBox> getUseBoxes();

  /** Returns a list of Boxes containing Values defined in this Stmt. */
  public List<ValueBox> getDefBoxes();

  /**
   * Returns a list of Boxes containing Stmts defined in this Stmt; typically branch targets.
   */
  public List<IStmtBox> getStmtBoxes();

  /** Returns a list of Boxes pointing to this Stmt. */
  public List<IStmtBox> getBoxesPointingToThis();

  /** Adds a box to the list returned by getBoxesPointingToThis. */
  public void addBoxPointingToThis(IStmtBox b);

  /** Removes a box from the list returned by getBoxesPointingToThis. */
  public void removeBoxPointingToThis(IStmtBox b);

  /** Clears any pointers to and from this Stmt's StmtBoxes. */
  public void clearStmtBoxes();

  /**
   * Returns a list of Boxes containing any Value either used or defined in this Stmt.
   */
  public List<ValueBox> getUseAndDefBoxes();

  public IStmt clone();

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
   * Redirects jumps to this Stmt to newLocation. In general, you shouldn't have to use this directly.
   *
   **/
  public void redirectJumpsToThisTo(IStmt newLocation);

  public void toString(IStmtPrinter up);

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

  public Position getPosition();
}
