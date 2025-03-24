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
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.AbstractStmt;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.visitor.StmtVisitor;
import sootup.core.util.printer.StmtPrinter;

/**
 * Represents the deprecated JVM <code>ret</code> statement (&lt; java 1.6) - which is used in JSR
 * Context - which is deprecated as well.
 */
public final class JRetStmt extends AbstractStmt implements FallsThroughStmt {

  @NonNull private final Value stmtAddress;

  public JRetStmt(@NonNull Value stmtAddress, @NonNull StmtPositionInfo positionInfo) {
    super(positionInfo);
    this.stmtAddress = stmtAddress;
  }

  @Override
  public String toString() {
    return Jimple.RET + " " + stmtAddress;
  }

  @Override
  public void toString(@NonNull StmtPrinter up) {
    up.literal(Jimple.RET);
    up.literal(" ");
    stmtAddress.toString(up);
  }

  @NonNull
  public Value getStmtAddress() {
    return stmtAddress;
  }

  @Override
  @NonNull
  public Stream<Value> getUses() {
    return Stream.concat(stmtAddress.getUses(), Stream.of(stmtAddress));
  }

  @Override
  public <V extends StmtVisitor> V accept(@NonNull V v) {
    v.caseRetStmt(this);
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
    return comparator.caseRetStmt(this, o);
  }

  @Override
  public int equivHashCode() {
    return stmtAddress.equivHashCode();
  }

  @NonNull
  public JRetStmt withStmtAddress(@NonNull Value stmtAddress) {
    return new JRetStmt(stmtAddress, getPositionInfo());
  }

  @NonNull
  public JRetStmt withPositionInfo(@NonNull StmtPositionInfo positionInfo) {
    return new JRetStmt(getStmtAddress(), positionInfo);
  }
}
