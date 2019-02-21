package de.upb.soot.jimple.common.stmt;

import java.io.Serializable;
import java.util.List;

import de.upb.soot.jimple.basic.EquivTo;
import de.upb.soot.jimple.basic.IStmtBox;
import de.upb.soot.jimple.basic.PositionInfo;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.common.expr.AbstractInvokeExpr;
import de.upb.soot.jimple.common.ref.JArrayRef;
import de.upb.soot.jimple.common.ref.JFieldRef;
import de.upb.soot.jimple.visitor.IAcceptor;
import de.upb.soot.util.printer.IStmtPrinter;

public interface IStmt extends EquivTo, IAcceptor, Serializable {
  /** Returns a list of Boxes containing Values used in this Stmt. */
  List<ValueBox> getUseBoxes();

  /** Returns a list of Boxes containing Values defined in this Stmt. */
  List<ValueBox> getDefBoxes();

  /**
   * Returns a list of Boxes containing Stmts defined in this Stmt; typically branch targets.
   */
  List<IStmtBox> getStmtBoxes();

  /** Returns a list of Boxes pointing to this Stmt. */
  List<IStmtBox> getBoxesPointingToThis();

  /** Adds a box to the list returned by getBoxesPointingToThis. */
  void addBoxPointingToThis(IStmtBox b);

  /** Removes a box from the list returned by getBoxesPointingToThis. */
  void removeBoxPointingToThis(IStmtBox b);

  /** Clears any pointers to and from this Stmt's StmtBoxes. */
  void clearStmtBoxes();

  /**
   * Returns a list of Boxes containing any Value either used or defined in this Stmt.
   */
  List<ValueBox> getUseAndDefBoxes();

  IStmt clone();

  /**
   * Returns true if execution after this statement may continue at the following statement. GotoStmt will return false but
   * IfStmt will return true.
   */
  boolean fallsThrough();

  /**
   * Returns true if execution after this statement does not necessarily continue at the following statement. GotoStmt and
   * IfStmt will both return true.
   */
  boolean branches();

  /**
   * Redirects jumps to this Stmt to newLocation. In general, you shouldn't have to use this directly.
   *
   **/
  void redirectJumpsToThisTo(IStmt newLocation);

  void toString(IStmtPrinter up);

  boolean containsInvokeExpr();

  AbstractInvokeExpr getInvokeExpr();

  ValueBox getInvokeExprBox();

  boolean containsArrayRef();

  JArrayRef getArrayRef();

  ValueBox getArrayRefBox();

  boolean containsFieldRef();

  JFieldRef getFieldRef();

  ValueBox getFieldRefBox();

  /**
   * Return the position information of this statement.
   * @return he position information of this statement
   */
  public PositionInfo getPositionInfo();
  
}
