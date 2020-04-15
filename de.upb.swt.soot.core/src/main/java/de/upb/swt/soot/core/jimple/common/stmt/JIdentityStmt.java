/* Soot - a J*va Optimization Framework
 * Copyright (C) 1999 Patrick Lam
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

package de.upb.swt.soot.core.jimple.common.stmt;

import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.common.ref.IdentityRef;
import de.upb.swt.soot.core.jimple.visitor.StmtVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import javax.annotation.Nonnull;

public final class JIdentityStmt extends AbstractDefinitionStmt implements Copyable {

  public JIdentityStmt(Value local, Value identityValue, StmtPositionInfo positionInfo) {
    super(local, identityValue, positionInfo);
    if (local == null || identityValue == null) {
      throw new IllegalArgumentException("value may not be null");
    }
    if (!(local instanceof Local)) {
      throw new RuntimeException(
          "JIdentityStmt "
              + this
              + " cannot contain value: "
              + local
              + " ("
              + local.getClass()
              + ")");
    }

    if (!(identityValue instanceof IdentityRef)) {
      throw new RuntimeException(
          "JIdentityStmt "
              + this
              + " cannot contain value: "
              + identityValue
              + " ("
              + identityValue.getClass()
              + ")");
    }
  }

  @Override
  public String toString() {
    return getLeftOp().toString() + " := " + getRightOp().toString();
  }

  @Override
  public void toString(StmtPrinter up) {
    getLeftOp().toString(up);
    up.literal(" := ");
    getRightOp().toString(up);
  }

  @Override
  public void accept(Visitor sw) {
    ((StmtVisitor) sw).caseIdentityStmt(this);
  }

  public Type getType() {
    return getLeftOp().getType();
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseIdentityStmt(this, o);
  }

  @Override
  public int equivHashCode() {
    return getLeftOp().equivHashCode() + 31 * getRightOp().equivHashCode();
  }

  @Nonnull
  public JIdentityStmt withLocal(Value local) {
    return new JIdentityStmt(local, getRightOp(), getPositionInfo());
  }

  @Nonnull
  public JIdentityStmt withIdentityValue(Value identityValue) {
    return new JIdentityStmt(getLeftOp(), identityValue, getPositionInfo());
  }

  @Nonnull
  public JIdentityStmt withPositionInfo(StmtPositionInfo positionInfo) {
    return new JIdentityStmt(getLeftOp(), getRightOp(), positionInfo);
  }
}
