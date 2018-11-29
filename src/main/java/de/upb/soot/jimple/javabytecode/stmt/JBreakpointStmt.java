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
import de.upb.soot.jimple.common.stmt.AbstractStmt;
import de.upb.soot.jimple.visitor.IStmtVisitor;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.util.printer.IStmtPrinter;

import java.util.Comparator;

public class JBreakpointStmt extends AbstractStmt {
  /**
   * 
   */
  private static final long serialVersionUID = 7082976523552855249L;

  public JBreakpointStmt() {
  }

  @Override
  public String toString() {
    return Jimple.BREAKPOINT;
  }

  @Override
  public void toString(IStmtPrinter up) {
    up.literal(Jimple.BREAKPOINT);
  }

  @Override
  public void accept(IVisitor sw) {
    ((IStmtVisitor) sw).caseBreakpointStmt(this);
  }

  @Override
  public JBreakpointStmt clone() {
    return new JBreakpointStmt();
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
    return (o instanceof JBreakpointStmt);
  }

  @Override
  public int equivHashCode() {
    return 42+1;
  }

  @Override
  public boolean equivTo(Object o, Comparator comparator) {
    return comparator.compare(this, o) == 0;
  }

}
