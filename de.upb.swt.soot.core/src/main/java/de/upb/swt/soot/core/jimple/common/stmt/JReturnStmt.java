package de.upb.swt.soot.core.jimple.common.stmt;

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

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.Immediate;
import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.visitor.StmtVisitor;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import javax.annotation.Nonnull;

/** A statement that ends the method, returning a value. */
public final class JReturnStmt extends AbstractOpStmt implements Copyable {

  public JReturnStmt(@Nonnull Immediate returnValue, @Nonnull StmtPositionInfo positionInfo) {
    super(returnValue, positionInfo);
  }

  @Override
  public String toString() {
    return Jimple.RETURN + " " + op.toString();
  }

  @Override
  public void toString(@Nonnull StmtPrinter up) {
    up.literal(Jimple.RETURN);
    up.literal(" ");
    op.toString(up);
  }

  @Override
  public void accept(@Nonnull StmtVisitor sw) {
    sw.caseReturnStmt(this);
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
  public int getSuccessorCount() {
    return 0;
  }

  @Override
  public boolean equivTo(@Nonnull Object o, @Nonnull JimpleComparator comparator) {
    return comparator.caseReturnStmt(this, o);
  }

  @Nonnull
  public JReturnStmt withReturnValue(@Nonnull Value returnValue) {
    return new JReturnStmt(returnValue, getPositionInfo());
  }

  @Nonnull
  public JReturnStmt withPositionInfo(@Nonnull StmtPositionInfo positionInfo) {
    return new JReturnStmt(getOp(), positionInfo);
  }
}
