package sootup.core.jimple.common.stmt;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999-2020 Patrick Lam, , Linghui Luo, Christian Brüggemann
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
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.ref.IdentityRef;
import sootup.core.jimple.visitor.StmtVisitor;
import sootup.core.util.printer.StmtPrinter;

public final class JIdentityStmt extends AbstractDefinitionStmt implements FallsThroughStmt {

  @Nonnull final Local leftOp;
  @Nonnull final IdentityRef rightOp;

  public JIdentityStmt(
      @Nonnull Local local,
      @Nonnull IdentityRef identityValue,
      @Nonnull StmtPositionInfo positionInfo) {
    super(positionInfo);
    leftOp = local;
    rightOp = identityValue;
  }

  @Override
  public String toString() {
    return getLeftOp() + " := " + getRightOp();
  }

  @Nonnull
  public Local getLeftOp() {
    return leftOp;
  }

  @Nonnull
  @Override
  public IdentityRef getRightOp() {
    return rightOp;
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
  public void toString(@Nonnull StmtPrinter up) {
    getLeftOp().toString(up);
    up.literal(" := ");
    getRightOp().toString(up);
  }

  @Override
  public void accept(@Nonnull StmtVisitor sw) {
    sw.caseIdentityStmt(this);
  }

  @Override
  public boolean equivTo(Object o, @Nonnull JimpleComparator comparator) {
    return comparator.caseIdentityStmt(this, o);
  }

  @Override
  public int equivHashCode() {
    return getLeftOp().equivHashCode() + 31 * getRightOp().equivHashCode();
  }

  @Nonnull
  public JIdentityStmt withLocal(@Nonnull Local local) {
    return new JIdentityStmt(local, getRightOp(), getPositionInfo());
  }

  @Nonnull
  public JIdentityStmt withIdentityValue(@Nonnull IdentityRef identityValue) {
    return new JIdentityStmt(getLeftOp(), identityValue, getPositionInfo());
  }

  @Nonnull
  public JIdentityStmt withPositionInfo(@Nonnull StmtPositionInfo positionInfo) {
    return new JIdentityStmt(getLeftOp(), getRightOp(), positionInfo);
  }

  @Nonnull
  @Override
  public FallsThroughStmt withNewDef(@Nonnull Local newLocal) {
    return withLocal(newLocal);
  }
}
