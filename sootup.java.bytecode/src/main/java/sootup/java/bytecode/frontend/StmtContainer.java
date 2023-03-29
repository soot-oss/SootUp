package sootup.java.bytecode.frontend;
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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.visitor.StmtVisitor;
import sootup.core.util.printer.StmtPrinter;

/**
 * A psuedo stmt containing different stmts.
 *
 * <p>basically its used to map more than one Stmt to a single AbstractInsNode - used in a Map of
 * AsmMethodSource
 *
 * @author Aaloan Miftah
 * @author Markus Schmidt
 */
class StmtContainer extends Stmt {

  @Nonnull private final List<Stmt> stmts = new LinkedList<>();

  private StmtContainer(@Nonnull Stmt firstStmt) {
    super(firstStmt.getPositionInfo());
    stmts.add(firstStmt);
  }

  static Stmt getOrCreate(@Nonnull Stmt firstStmt, @Nonnull Stmt anotherStmt) {
    StmtContainer container;
    if (firstStmt instanceof StmtContainer) {
      container = (StmtContainer) firstStmt;
    } else {
      container = new StmtContainer(firstStmt);
    }
    container.stmts.add(anotherStmt);
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
  Collection<Stmt> getStmts() {
    return stmts;
  }

  @Nonnull
  @Override
  public List<Value> getUses() {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  @Override
  public List<Value> getDefs() {
    throw new UnsupportedOperationException();
  }

  @Nonnull
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
  public boolean containsArrayRef() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsFieldRef() {
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
    return "StmtContainer" + stmts;
  }

  @Override
  public StmtPositionInfo getPositionInfo() {
    throw new UnsupportedOperationException();
  }
}
