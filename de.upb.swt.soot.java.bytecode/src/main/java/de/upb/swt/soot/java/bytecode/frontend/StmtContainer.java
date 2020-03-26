package de.upb.swt.soot.java.bytecode.frontend;

import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.StmtBox;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.ValueBox;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.common.ref.JArrayRef;
import de.upb.swt.soot.core.jimple.common.ref.JFieldRef;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * A psuedo unit containing different units.
 *
 * @author Aaloan Miftah
 */
class StmtContainer extends Stmt {

  @Nonnull final Stmt[] units;

  StmtContainer(@Nonnull Stmt... units) {
    this.units = units;
  }

  /**
   * Searches the depth of the StmtContainer until the actual first Unit represented is found.
   *
   * @return the first Stmt of the container
   */
  @Nonnull
  Stmt getFirstUnit() {
    Stmt ret = units[0];
    while (ret instanceof StmtContainer) {
      ret = ((StmtContainer) ret).units[0];
    }
    return ret;
  }

  @Override
  public List<ValueBox> getUseBoxes() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<ValueBox> getDefBoxes() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<StmtBox> getStmtBoxes() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<StmtBox> getBoxesPointingToThis() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<ValueBox> getUseAndDefBoxes() {
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
  public void toString(StmtPrinter up) {
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
  public ValueBox getInvokeExprBox() {
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
  public ValueBox getArrayRefBox() {
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
  public ValueBox getFieldRefBox() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int equivHashCode() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void accept(Visitor v) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
    throw new UnsupportedOperationException();
  }

  @Override
  public StmtPositionInfo getPositionInfo() {
    throw new UnsupportedOperationException();
  }
}
