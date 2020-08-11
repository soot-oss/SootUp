package de.upb.swt.soot.core.jimple.javabytecode.stmt;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999 Patrick Lam
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

/*
 * Modified by the Sable Research Group and others 1997-1999.
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.visitor.StmtVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import javax.annotation.Nonnull;

public final class JBreakpointStmt extends Stmt implements Copyable {

  public JBreakpointStmt(@Nonnull StmtPositionInfo positionInfo) {
    super(positionInfo);
  }

  @Override
  public String toString() {
    return Jimple.BREAKPOINT;
  }

  @Override
  public void toString(@Nonnull StmtPrinter up) {
    up.literal(Jimple.BREAKPOINT);
  }

  @Override
  public void accept(@Nonnull Visitor sw) {
    ((StmtVisitor) sw).caseBreakpointStmt(this);
  }

  @Override
  public boolean fallsThrough() {
    return true;
  }

  @Override
  public boolean branches() {
    return false;
  }

  @Override
  public boolean equivTo(@Nonnull Object o, @Nonnull JimpleComparator comparator) {
    return comparator.caseBreakpointStmt(this, o);
  }

  @Override
  public int equivHashCode() {
    return 42 + 1;
  }

  @Nonnull
  public JBreakpointStmt withPositionInfo(@Nonnull StmtPositionInfo positionInfo) {
    return new JBreakpointStmt(positionInfo);
  }
}
