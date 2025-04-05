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

import java.util.Optional;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.*;
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
  public boolean isJAssignStmt() {
    return false;
  }

  @Override
  public boolean isJBreakpointStmt() {
    return false;
  }

  @Override
  public boolean isJEnterMonitorStmt() {
    return false;
  }

  @Override
  public boolean isJExitMonitorStmt() {
    return true;
  }

  @Override
  public boolean isJGotoStmt() {
    return false;
  }

  @Override
  public boolean isJIdentityStmt() {
    return false;
  }

  @Override
  public boolean isJIfStmt() {
    return false;
  }

  @Override
  public boolean isJInvokeStmt() {
    return false;
  }

  @Override
  public boolean isJNopStmt() {
    return false;
  }

  @Override
  public boolean isJRetStmt() {
    return false;
  }

  @Override
  public boolean isJReturnStmt() {
    return false;
  }

  @Override
  public boolean isJReturnVoidStmt() {
    return false;
  }

  @Override
  public boolean isJSwitchStmt() {
    return false;
  }

  @Override
  public boolean isJThrowStmt() {
    return false;
  }

  @Override
  public JAssignStmt asJAssignStmt() {
    return null;
  }

  @Override
  public JBreakpointStmt asJBreakpointStmt() {
    return null;
  }

  @Override
  public JEnterMonitorStmt asJEnterMonitorStmt() {
    return null;
  }

  @Override
  public JExitMonitorStmt asJExitMonitorStmt() {
    return this;
  }

  @Override
  public JGotoStmt asJGotoStmt() {
    return null;
  }

  @Override
  public JIdentityStmt asJIdentityStmt() {
    return null;
  }

  @Override
  public JIfStmt asJIfStmt() {
    return null;
  }

  @Override
  public JInvokeStmt asJInvokeStmt() {
    return null;
  }

  @Override
  public JNopStmt asJNopStmt() {
    return null;
  }

  @Override
  public JRetStmt asJRetStmt() {
    return null;
  }

  @Override
  public JReturnStmt asJReturnStmt() {
    return null;
  }

  @Override
  public JReturnVoidStmt asJReturnVoidStmt() {
    return null;
  }

  @Override
  public JSwitchStmt asJSwitchStmt() {
    return null;
  }

  @Override
  public JThrowStmt asJThrowStmt() {
    return null;
  }

  @Override
  public Optional<JAssignStmt> toJAssignStmt() {
    return Optional.empty();
  }

  @Override
  public Optional<JBreakpointStmt> toJBreakpointStmt() {
    return Optional.empty();
  }

  @Override
  public Optional<JEnterMonitorStmt> toJEnterMonitorStmt() {
    return Optional.empty();
  }

  @Override
  public Optional<JExitMonitorStmt> toJExitMonitorStmt() {
    return Optional.of(this);
  }

  @Override
  public Optional<JGotoStmt> toJGotoStmt() {
    return Optional.empty();
  }

  @Override
  public Optional<JIdentityStmt> toJIdentityStmt() {
    return Optional.empty();
  }

  @Override
  public Optional<JIfStmt> toJIfStmt() {
    return Optional.empty();
  }

  @Override
  public Optional<JInvokeStmt> toJInvokeStmt() {
    return Optional.empty();
  }

  @Override
  public Optional<JNopStmt> toJNopStmt() {
    return Optional.empty();
  }

  @Override
  public Optional<JRetStmt> toJRetStmt() {
    return Optional.empty();
  }

  @Override
  public Optional<JReturnStmt> toJReturnStmt() {
    return Optional.empty();
  }

  @Override
  public Optional<JReturnVoidStmt> toJReturnVoidStmt() {
    return Optional.empty();
  }

  @Override
  public Optional<JSwitchStmt> toJSwitchStmt() {
    return Optional.empty();
  }

  @Override
  public Optional<JThrowStmt> toJThrowStmt() {
    return Optional.empty();
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
