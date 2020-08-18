package de.upb.swt.soot.core.jimple.javabytecode.stmt;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999-2020 Patrick Lam, Markus Schmidt, Linghui luo and others
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
import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.basic.ValueBox;
import de.upb.swt.soot.core.jimple.common.stmt.AbstractOpStmt;
import de.upb.swt.soot.core.jimple.visitor.StmtVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import javax.annotation.Nonnull;

/** A statement that exits a JVM monitor, thereby ending synchronization. */
public final class JExitMonitorStmt extends AbstractOpStmt implements Copyable {

  public JExitMonitorStmt(Value op, StmtPositionInfo positionInfo) {
    this(Jimple.newImmediateBox(op), positionInfo);
  }

  private JExitMonitorStmt(ValueBox opBox, StmtPositionInfo positionInfo) {
    super(opBox, positionInfo);
  }

  @Override
  public String toString() {
    return Jimple.EXITMONITOR + " " + opBox.getValue().toString();
  }

  @Override
  public void toString(@Nonnull StmtPrinter up) {
    up.literal(Jimple.EXITMONITOR);
    up.literal(" ");
    opBox.toString(up);
  }

  @Override
  public void accept(@Nonnull Visitor sw) {
    ((StmtVisitor) sw).caseExitMonitorStmt(this);
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
    return comparator.caseExitMonitorStmt(this, o);
  }

  @Nonnull
  public JExitMonitorStmt withOp(Value op) {
    return new JExitMonitorStmt(op, getPositionInfo());
  }

  @Nonnull
  public JExitMonitorStmt withPositionInfo(StmtPositionInfo positionInfo) {
    return new JExitMonitorStmt(getOp(), positionInfo);
  }
}
