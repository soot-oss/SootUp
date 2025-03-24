package sootup.core.jimple.javabytecode.stmt;

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

import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.AbstractStmt;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.visitor.StmtVisitor;
import sootup.core.util.printer.StmtPrinter;

/** A statement that exits a JVM monitor, thereby ending synchronization. */
public final class JExitMonitorStmt extends AbstractStmt implements FallsThroughStmt {

  private final Immediate op;

  public JExitMonitorStmt(@NonNull Immediate op, @NonNull StmtPositionInfo positionInfo) {
    super(positionInfo);
    this.op = op;
  }

  @Override
  public String toString() {
    return Jimple.EXITMONITOR + " " + op.toString();
  }

  @Override
  public void toString(@NonNull StmtPrinter up) {
    up.literal(Jimple.EXITMONITOR);
    up.literal(" ");
    op.toString(up);
  }

  @Override
  public <V extends StmtVisitor> V accept(@NonNull V v) {
    v.caseExitMonitorStmt(this);
    return v;
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
  public boolean equivTo(@NonNull Object o, @NonNull JimpleComparator comparator) {
    return comparator.caseExitMonitorStmt(this, o);
  }

  @NonNull
  public JExitMonitorStmt withOp(@NonNull Immediate op) {
    return new JExitMonitorStmt(op, getPositionInfo());
  }

  @NonNull
  public JExitMonitorStmt withPositionInfo(@NonNull StmtPositionInfo positionInfo) {
    return new JExitMonitorStmt(getOp(), positionInfo);
  }

  @NonNull
  public Immediate getOp() {
    return op;
  }

  @Override
  @NonNull
  public Stream<Value> getUses() {
    return Stream.concat(op.getUses(), Stream.of(op));
  }

  @Override
  public int equivHashCode() {
    return op.equivHashCode();
  }
}
