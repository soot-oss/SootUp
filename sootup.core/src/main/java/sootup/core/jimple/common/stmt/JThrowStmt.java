package sootup.core.jimple.common.stmt;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999-2020 Patrick Lam, Linghui Luo, Christian Brüggemann and others
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

import javax.annotation.Nonnull;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.visitor.StmtVisitor;
import sootup.core.util.Copyable;
import sootup.core.util.printer.StmtPrinter;

/** A statement that throws an Exception */
public final class JThrowStmt extends AbstractOpStmt implements Copyable {

  public JThrowStmt(@Nonnull Immediate op, @Nonnull StmtPositionInfo positionInfo) {
    super(op, positionInfo);
  }

  @Override
  public String toString() {
    return "throw " + op.toString();
  }

  @Override
  public void toString(@Nonnull StmtPrinter up) {
    up.literal(Jimple.THROW);
    up.literal(" ");
    op.toString(up);
  }

  @Override
  public void accept(@Nonnull StmtVisitor sw) {
    sw.caseThrowStmt(this);
  }

  @Override
  public boolean fallsThrough() {
    return false;
  }

  @Override
  public boolean branches() {
    return false;
  }

  @Override
  public int getExpectedSuccessorCount() {
    return 0;
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseThrowStmt(this, o);
  }

  @Nonnull
  public JThrowStmt withOp(@Nonnull Immediate op) {
    return new JThrowStmt(op, getPositionInfo());
  }

  @Nonnull
  public JThrowStmt withPositionInfo(@Nonnull StmtPositionInfo positionInfo) {
    return new JThrowStmt(getOp(), positionInfo);
  }
}
