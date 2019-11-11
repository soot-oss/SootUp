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

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.basic.ValueBox;
import de.upb.swt.soot.core.jimple.visitor.StmtVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import javax.annotation.Nonnull;

/** A statement that ends the method, returning a value. */
public final class JReturnStmt extends AbstractOpStmt implements Copyable {

  public JReturnStmt(Value returnValue, StmtPositionInfo positionInfo) {
    this(Jimple.newImmediateBox(returnValue), positionInfo);
  }

  protected JReturnStmt(ValueBox returnValueBox, StmtPositionInfo positionInfo) {
    super(returnValueBox, positionInfo);
  }

  @Override
  public String toString() {
    return Jimple.RETURN + " " + opBox.getValue().toString();
  }

  @Override
  public void toString(StmtPrinter up) {
    up.literal(Jimple.RETURN);
    up.literal(" ");
    opBox.toString(up);
  }

  @Override
  public void accept(Visitor sw) {
    ((StmtVisitor) sw).caseReturnStmt(this);
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
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseReturnStmt(this, o);
  }

  public JReturnStmt withOp(Value op) {
    return new JReturnStmt(op, getPositionInfo());
  }

  @Nonnull
  public JReturnStmt withReturnValue(Value returnValue) {
    return new JReturnStmt(returnValue, getPositionInfo());
  }

  @Nonnull
  public JReturnStmt withPositionInfo(StmtPositionInfo positionInfo) {
    return new JReturnStmt(getOp(), positionInfo);
  }
}
