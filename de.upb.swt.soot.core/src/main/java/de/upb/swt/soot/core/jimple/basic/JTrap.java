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

package de.upb.swt.soot.core.jimple.basic;

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.util.Copyable;
import javax.annotation.Nonnull;

public final class JTrap extends AbstractTrap implements Copyable {

  public JTrap(ClassType exception, Stmt beginStmt, Stmt endStmt, Stmt handlerStmt) {
    super(
        exception,
        Jimple.newStmtBox(beginStmt),
        Jimple.newStmtBox(endStmt),
        Jimple.newStmtBox(handlerStmt));
  }

  public JTrap(ClassType exception, StmtBox beginStmt, StmtBox endStmt, StmtBox handlerStmt) {
    super(exception, beginStmt, endStmt, handlerStmt);
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

  @Nonnull
  public JTrap withException(ClassType exception) {
    return new JTrap(exception, getBeginStmt(), getEndStmt(), getHandlerStmt());
  }

  @Nonnull
  public JTrap withBeginStmt(Stmt beginStmt) {
    return new JTrap(getException(), beginStmt, getEndStmt(), getHandlerStmt());
  }

  @Nonnull
  public JTrap withHandlerStmt(Stmt handlerStmt) {
    return new JTrap(getException(), getBeginStmt(), getEndStmt(), handlerStmt);
  }

  @Nonnull
  public JTrap withEndStmt(Stmt endStmt) {
    return new JTrap(getException(), getBeginStmt(), endStmt, getHandlerStmt());
  }
}
