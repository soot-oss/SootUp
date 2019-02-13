package de.upb.soot.frontends.asm;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2014 Raja Vallee-Rai and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import com.ibm.wala.cast.tree.CAstSourcePositionMap;
import de.upb.soot.jimple.basic.IStmtBox;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.common.expr.AbstractInvokeExpr;
import de.upb.soot.jimple.common.ref.JArrayRef;
import de.upb.soot.jimple.common.ref.JFieldRef;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.util.printer.IStmtPrinter;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;

/**
 * A psuedo unit containing different units.
 * 
 * @author Aaloan Miftah
 */
@SuppressWarnings("serial")
class StmtContainer implements IStmt {

  final @Nonnull IStmt[] units;

  StmtContainer(@Nonnull IStmt... units) {
    this.units = units;
  }

  /**
   * Searches the depth of the StmtContainer until the actual first Unit represented is found.
   *
   * @return the first IStmt of the container
   */
  @Nonnull IStmt getFirstUnit() {
    IStmt ret = units[0];
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
  public List<IStmtBox> getStmtBoxes() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<IStmtBox> getBoxesPointingToThis() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addBoxPointingToThis(IStmtBox b) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeBoxPointingToThis(IStmtBox b) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clearStmtBoxes() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<ValueBox> getUseAndDefBoxes() {
    throw new UnsupportedOperationException();
  }

  @Override
  public IStmt clone() {
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
  public void redirectJumpsToThisTo(IStmt newLocation) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void toString(IStmtPrinter up) {
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
  public void setPosition(CAstSourcePositionMap.Position position) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean equivTo(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int equivHashCode() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean equivTo(Object o, Comparator<?> comparator) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void accept(IVisitor v) {
    throw new UnsupportedOperationException();
  }
}
