package sootup.core.jimple.common.stmt;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999-2020 Patrick Lam, Linghui Luo, Markus Schmidt and others
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

import java.util.List;
import javax.annotation.Nonnull;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.visitor.StmtVisitor;
import sootup.core.model.Body;
import sootup.core.util.Copyable;
import sootup.core.util.printer.StmtPrinter;

/** Unconditionally jumps to a target Stmt */
public class JGotoStmt extends BranchingStmt implements Copyable {

  public JGotoStmt(StmtPositionInfo positionInfo) {
    super(positionInfo);
  }

  @Override
  public String toString() {
    return Jimple.GOTO;
  }

  @Override
  public void toString(@Nonnull StmtPrinter stmtPrinter) {
    stmtPrinter.literal(Jimple.GOTO);
    stmtPrinter.literal(" ");
    // [ms] bounds are validated in Body if its a valid StmtGraph
    stmtPrinter.stmtRef(stmtPrinter.getGraph().getBranchTargetsOf(this).get(0), true);
  }

  @Override
  @Nonnull
  public List<Stmt> getTargetStmts(@Nonnull Body body) {
    return body.getBranchTargetsOf(this);
  }

  @Override
  public int getExpectedSuccessorCount() {
    return 1;
  }

  @Override
  public void accept(@Nonnull StmtVisitor sw) {
    sw.caseGotoStmt(this);
  }

  @Override
  public boolean fallsThrough() {
    return false;
  }

  @Override
  public boolean equivTo(@Nonnull Object o, @Nonnull JimpleComparator comparator) {
    return comparator.caseGotoStmt(this, o);
  }

  @Override
  public int equivHashCode() {
    return 44;
  }

  @Nonnull
  public JGotoStmt withPositionInfo(@Nonnull StmtPositionInfo positionInfo) {
    return new JGotoStmt(positionInfo);
  }
}
