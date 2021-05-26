package de.upb.swt.soot.core.jimple.common.stmt;

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

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.basic.ValueBox;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.visitor.StmtVisitor;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/** A method call */
public final class JInvokeStmt extends Stmt implements Copyable {

  private final ValueBox invokeExprBox;
  // new attributes: later if ValueBox is deleted, then add "final" to it.
  private Value invokeExpr;

  public JInvokeStmt(Value invokeExpr, StmtPositionInfo positionInfo) {
    this(Jimple.newInvokeExprBox(invokeExpr), positionInfo);
  }

  private JInvokeStmt(ValueBox invokeExprBox, StmtPositionInfo positionInfo) {
    super(positionInfo);
    this.invokeExprBox = invokeExprBox;
    // new attribute: later if ValueBox is deleted, then fit the constructor.
    this.invokeExpr = invokeExprBox.getValue();
  }

  @Override
  public boolean containsInvokeExpr() {
    return true;
  }

  @Override
  public String toString() {
    return invokeExprBox.getValue().toString();
  }

  @Override
  public void toString(@Nonnull StmtPrinter up) {
    invokeExprBox.toString(up);
  }

  @Override
  public AbstractInvokeExpr getInvokeExpr() {
    return (AbstractInvokeExpr) invokeExprBox.getValue();
  }

  @Override
  public ValueBox getInvokeExprBox() {
    return invokeExprBox;
  }

  @Override
  public List<Value> getUses() {
    List<Value> list = new ArrayList<>(invokeExpr.getUses());
    list.add(invokeExpr);
    return list;
  }

  @Override
  public void accept(@Nonnull StmtVisitor sw) {
    sw.caseInvokeStmt(this);
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
  public boolean equivTo(Object o, @Nonnull JimpleComparator comparator) {
    return comparator.caseInvokeStmt(this, o);
  }

  @Override
  public int equivHashCode() {
    return invokeExprBox.getValue().equivHashCode();
  }

  @Nonnull
  public JInvokeStmt withInvokeExpr(Value invokeExpr) {
    return new JInvokeStmt(invokeExpr, getPositionInfo());
  }

  @Nonnull
  public JInvokeStmt withPositionInfo(StmtPositionInfo positionInfo) {
    return new JInvokeStmt(invokeExprBox, positionInfo);
  }
}
