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
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.visitor.StmtVisitor;
import sootup.core.util.printer.StmtPrinter;

/** A statement that ends the method, returning a value. */
public final class JReturnStmt extends AbstractStmt implements ReturnStmt {

  private final Immediate op;

  public JReturnStmt(@NonNull Immediate returnValue, @NonNull StmtPositionInfo positionInfo) {
    super(positionInfo);
    this.op = returnValue;
  }

  @Override
  public String toString() {
    return Jimple.RETURN + " " + op.toString();
  }

  @Override
  public void toString(@NonNull StmtPrinter up) {
    up.literal(Jimple.RETURN);
    up.literal(" ");
    op.toString(up);
  }

  @Override
  public boolean isJReturnStmt() {
    return true;
  }

  @Override
  public JReturnStmt asJReturnStmt() {
    return this;
  }

  @Override
  public Optional<JReturnStmt> toJReturnStmt() {
    return Optional.of(this);
  }

  @Override
  public <V extends StmtVisitor> V accept(@NonNull V v) {
    v.caseReturnStmt(this);
    return v;
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

  @NonNull
  public Immediate getOp() {
    return op;
  }

  @Override
  public void collectUses(List<Value> collector) {
    op.collectUses(collector);
    collector.add(op);
  }

  @Override
  public int equivHashCode() {
    return op.equivHashCode();
  }

  @Override
  public boolean equivTo(@NonNull Object o, @NonNull JimpleComparator comparator) {
    return comparator.caseReturnStmt(this, o);
  }

  @NonNull
  public JReturnStmt withReturnValue(@NonNull Immediate returnValue) {
    return new JReturnStmt(returnValue, getPositionInfo());
  }

  @NonNull
  public JReturnStmt withPositionInfo(@NonNull StmtPositionInfo positionInfo) {
    return new JReturnStmt(getOp(), positionInfo);
  }
}
