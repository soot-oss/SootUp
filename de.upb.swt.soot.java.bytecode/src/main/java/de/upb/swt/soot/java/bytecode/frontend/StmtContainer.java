package de.upb.swt.soot.java.bytecode.frontend;

import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.common.ref.JArrayRef;
import de.upb.swt.soot.core.jimple.common.ref.JFieldRef;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * A psuedo stmt containing different stmts.
 *
 * @author Aaloan Miftah
 */
class StmtContainer extends Stmt {

  @Nonnull final Stmt[] stmts;

  StmtContainer(@Nonnull Stmt... stmts) {
    this.stmts = stmts;
  }

  /**
   * Searches the depth of the StmtContainer until the actual first Stmt represented is found.
   *
   * @return the first Stmt of the container
   */
  @Nonnull
  Stmt getFirstStmt() {
    Stmt ret = stmts[0];
    while (ret instanceof StmtContainer) {
      ret = ((StmtContainer) ret).stmts[0];
    }
    return ret;
  }

  @Override
  public List<Value> getUses() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Value> getDefs() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Stmt> getStmts() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Stmt> getStmtsPointingToThis() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Value> getUsesAndDefs() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean fallsThrough() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean branches() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void toString(@Nonnull StmtPrinter up) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsInvokeExpr() {
    throw new UnsupportedOperationException();
  }

  @Override
  public AbstractInvokeExpr getInvokeExpr() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsArrayRef() {
    throw new UnsupportedOperationException();
  }

  @Override
  public JArrayRef getArrayRef() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsFieldRef() {
    throw new UnsupportedOperationException();
  }

  @Override
  public JFieldRef getFieldRef() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int equivHashCode() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void accept(@Nonnull Visitor v) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean equivTo(@Nonnull Object o, @Nonnull JimpleComparator comparator) {
    throw new UnsupportedOperationException();
  }

  @Override
  public StmtPositionInfo getPositionInfo() {
    throw new UnsupportedOperationException();
  }
}
