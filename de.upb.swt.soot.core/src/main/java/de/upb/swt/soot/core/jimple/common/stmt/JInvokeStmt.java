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

import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.visitor.StmtVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/** A method call */
public final class JInvokeStmt extends AbstractStmt implements Copyable {

  private final Value invokeExpr;

  public JInvokeStmt(Value invokeExpr, StmtPositionInfo positionInfo) {
    super(positionInfo);
    if (invokeExpr == null) {
      throw new IllegalArgumentException("value may not be null");
    }
    if (invokeExpr instanceof AbstractInvokeExpr) {
      this.invokeExpr = invokeExpr;
    } else {
      throw new RuntimeException(
          "JInvokeStmt "
              + this
              + " cannot contain value: "
              + invokeExpr
              + " ("
              + invokeExpr.getClass()
              + ")");
    }
  }

  @Override
  public boolean containsInvokeExpr() {
    return true;
  }

  @Override
  public String toString() {
    return invokeExpr.toString();
  }

  @Override
  public void toString(StmtPrinter up) {
    invokeExpr.toString(up);
  }

  @Override
  public AbstractInvokeExpr getInvokeExpr() {
    return (AbstractInvokeExpr) invokeExpr;
  }

  @Override
  public List<Value> getUses() {
    List<Value> list = new ArrayList<>(invokeExpr.getUses());
    list.add(invokeExpr);
    return list;
  }

  @Override
  public void accept(Visitor sw) {
    ((StmtVisitor) sw).caseInvokeStmt(this);
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
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseInvokeStmt(this, o);
  }

  @Override
  public int equivHashCode() {
    return invokeExpr.equivHashCode();
  }

  @Nonnull
  public JInvokeStmt withInvokeExpr(Value invokeExpr) {
    return new JInvokeStmt(invokeExpr, getPositionInfo());
  }

  @Nonnull
  public JInvokeStmt withPositionInfo(StmtPositionInfo positionInfo) {
    return new JInvokeStmt(getInvokeExpr(), positionInfo);
  }
}
