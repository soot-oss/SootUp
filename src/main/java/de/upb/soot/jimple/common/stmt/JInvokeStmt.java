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

package de.upb.soot.jimple.common.stmt;

import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.common.expr.AbstractInvokeExpr;
import de.upb.soot.jimple.visitor.IStmtVisitor;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.util.printer.IStmtPrinter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class JInvokeStmt extends AbstractStmt {
  /**
   * 
   */
  private static final long serialVersionUID = 3929309661335452051L;
  final ValueBox invokeExprBox;

  public JInvokeStmt(Value c) {
    this(Jimple.newInvokeExprBox(c));
  }

  protected JInvokeStmt(ValueBox invokeExprBox) {
    this.invokeExprBox = invokeExprBox;
  }

  @Override
  public JInvokeStmt clone() {
    return new JInvokeStmt(Jimple.cloneIfNecessary(getInvokeExpr()));
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
  public void toString(IStmtPrinter up) {
    invokeExprBox.toString(up);
  }

  public void setInvokeExpr(Value invokeExpr) {
    invokeExprBox.setValue(invokeExpr);
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
  public List<ValueBox> getUseBoxes() {
    List<ValueBox> list = new ArrayList<ValueBox>();

    list.addAll(invokeExprBox.getValue().getUseBoxes());
    list.add(invokeExprBox);

    return list;
  }

  @Override
  public void accept(IVisitor sw) {
    ((IStmtVisitor) sw).caseInvokeStmt(this);
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
  public boolean equivTo(Object o) {

    if(!(o instanceof JInvokeStmt)){
      return false;
    }
    if( invokeExprBox.getValue().equivTo(((JInvokeStmt) o).getInvokeExpr() ) ){
      return false;
    }

    return true;
  }

  @Override
  public int equivHashCode() {
    return invokeExprBox.getValue().equivHashCode();
  }

  @Override
  public boolean equivTo(Object o, Comparator comparator) {
    return comparator.compare( this, o) == 0;
  }


}
