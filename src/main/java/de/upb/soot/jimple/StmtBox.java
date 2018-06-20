package de.upb.soot.jimple;

import java.io.Serializable;

import de.upb.soot.StmtPrinter;
import de.upb.soot.jimple.common.stmt.Stmt;


public interface StmtBox extends Serializable
{
  /** Sets this box to contain the given unit.  Subject to canContainValue() checks. */
  public void setStmt(Stmt u);

  /** Returns the unit contained within this box. */
  public Stmt getStmt();

  /** Returns true if this box can contain the given Stmt. */
  public boolean canContainStmt(Stmt u);   
  
  /**
   * Returns true if the StmtBox is holding a Stmt that is the
   * target of a branch (ie a Stmt at the beginning of a CFG block).
   * This is the default case.
   *
   * <p> Returns false if the StmtBox is holding a Stmt that
   * indicates the end of a CFG block and may require specialised
   * processing for SSA.
   **/
  public boolean isBranchTarget();

  public void toString(StmtPrinter up); 
}
