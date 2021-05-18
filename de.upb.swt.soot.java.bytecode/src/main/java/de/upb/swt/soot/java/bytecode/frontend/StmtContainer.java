package de.upb.swt.soot.java.bytecode.frontend;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Andreas Dann, Markus Schmidt and others
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
import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.common.ref.JArrayRef;
import de.upb.swt.soot.core.jimple.common.ref.JFieldRef;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.visitor.StmtVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * A psuedo stmt containing different stmts.
 *
 * @author Aaloan Miftah
 * @author Markus Schmidt
 */
class StmtContainer extends Stmt {

  @Nonnull private final List<Stmt> stmts = new LinkedList<>();

  private StmtContainer() {
    super(StmtPositionInfo.createNoStmtPositionInfo());
  }

  static Stmt create(Stmt prevStmt, Stmt nextStmt) {
    StmtContainer container;
    if (prevStmt instanceof StmtContainer) {
      container = (StmtContainer) prevStmt;
    } else {
      container = new StmtContainer();
      container.stmts.add(prevStmt);
    }
    container.stmts.add(nextStmt);
    return container;
  }

  /**
   * Searches the depth of the StmtContainer until the actual first Stmt represented is found.
   *
   * @return the first Stmt of the container
   */
  @Nonnull
  Stmt getFirstStmt() {
    return stmts.get(0);
  }

  @Nonnull
  Iterable<Stmt> getStmts() {
    return stmts;
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
  public void accept(@Nonnull StmtVisitor v) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean equivTo(@Nonnull Object o, @Nonnull JimpleComparator comparator) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return "StmtContainer" + (stmts);
  }

  @Override
  public StmtPositionInfo getPositionInfo() {
    throw new UnsupportedOperationException();
  }
}
