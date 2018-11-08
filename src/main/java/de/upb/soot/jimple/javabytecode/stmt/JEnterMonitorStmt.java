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

package de.upb.soot.jimple.javabytecode.stmt;

import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.common.stmt.AbstractOpStmt;
import de.upb.soot.jimple.common.stmt.JReturnStmt;
import de.upb.soot.jimple.visitor.IStmtVisitor;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.util.printer.IStmtPrinter;

import java.util.Comparator;

public class JEnterMonitorStmt extends AbstractOpStmt {
  /**
   * 
   */
  private static final long serialVersionUID = -8580317170617446624L;

  public JEnterMonitorStmt(Value op) {
    this(Jimple.newImmediateBox(op));
  }

  protected JEnterMonitorStmt(ValueBox opBox) {
    super(opBox);
  }

  @Override
  public JEnterMonitorStmt clone() {
    return new JEnterMonitorStmt(Jimple.cloneIfNecessary(getOp()));
  }

  @Override
  public String toString() {
    return Jimple.ENTERMONITOR + " " + opBox.getValue().toString();
  }

  @Override
  public void toString(IStmtPrinter up) {
    up.literal(Jimple.ENTERMONITOR);
    up.literal(" ");
    opBox.toString(up);
  }

  @Override
  public void accept(IVisitor sw) {
    ((IStmtVisitor) sw).caseEnterMonitorStmt(this);

  }

  @Override
  public boolean fallsThrough() {
    return true;
  }

  @Override
  public boolean branches() {
    return false;
  }

  public boolean equivTo(Object o) {
    if(!(o instanceof JEnterMonitorStmt)){
      return false;
    }
    return super.equivTo( (AbstractOpStmt) o);
  }

  @Override
  public int equivHashCode() {
    return super.equivHashCode();
  }


  @Override
  public boolean equivTo(Object o, Comparator<?> comparator) {
    // TODO: implement!
    return false;
  }

}
