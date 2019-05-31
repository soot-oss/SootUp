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

package de.upb.soot.jimple.basic;

import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.common.stmt.Stmt;
import de.upb.soot.types.JavaClassType;

public class JTrap extends AbstractTrap {

  /** */
  private static final long serialVersionUID = -1514595956359087470L;

  public JTrap(JavaClassType exception, Stmt beginStmt, Stmt endStmt, Stmt handlerStmt) {
    super(
        exception,
        Jimple.newStmtBox(beginStmt),
        Jimple.newStmtBox(endStmt),
        Jimple.newStmtBox(handlerStmt));
  }

  public JTrap(JavaClassType exception, StmtBox beginStmt, StmtBox endStmt, StmtBox handlerStmt) {
    super(exception, beginStmt, endStmt, handlerStmt);
  }

  @Override
  public Object clone() {
    return new JTrap(exception, getBeginStmt(), getEndStmt(), getHandlerStmt());
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder("Trap :");
    buf.append("\nbegin  : ");
    buf.append(getBeginStmt());
    buf.append("\nend    : ");
    buf.append(getEndStmt());
    buf.append("\nhandler: ");
    buf.append(getHandlerStmt());
    return new String(buf);
  }
}
