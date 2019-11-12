/* Soot - a J*va Optimization Framework
 * Copyright (C) 1997-1999 Raja Vallee-Rai
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
/*
 *  The original class name was UnitBox in soot, renamed by Linghui Luo, 22.06.2018
 */

package de.upb.swt.soot.core.jimple.basic;

import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Contains a {@link Stmt}.
 *
 * <p>Prefer to use the factory methods in {@link de.upb.swt.soot.core.jimple.Jimple}.
 */
public abstract class StmtBox {

  @Nullable private Stmt stmt;

  public StmtBox(@Nullable Stmt stmt) {
    this.stmt = stmt;
  }

  @Deprecated
  private void setStmt(@Nullable Stmt stmt) {
    // Remove this from set of back pointers.
    if (this.stmt != null) {
      Stmt.$Accessor.removeBoxPointingToThis(this.stmt, this);
    }

    // Perform link
    this.stmt = stmt;

    // Add this to back pointers
    if (this.stmt != null) {
      Stmt.$Accessor.addBoxPointingToThis(this.stmt, this);
    }
  }

  public @Nullable Stmt getStmt() {
    return stmt;
  }

  /**
   * Returns true if the StmtBox is holding a Stmt that is the target of a branch (ie a Stmt at the
   * beginning of a CFG block). This is the default case.
   *
   * <p>Returns false if the StmtBox is holding a Stmt that indicates the end of a CFG block and may
   * require specialised processing for SSA.
   */
  public abstract boolean isBranchTarget();

  public void toString(@Nonnull StmtPrinter up) {
    up.startStmtBox(this);
    up.stmtRef(stmt, isBranchTarget());
    up.endStmtBox(this);
  }

  /** This class is for internal use only. It will be removed in the future. */
  @Deprecated
  public static class $Accessor {
    // This class deliberately starts with a $-sign to discourage usage
    // of this Soot implementation detail.

    /** Violates immutability. Only use this for legacy code. */
    @Deprecated
    public static void setStmt(StmtBox box, Stmt stmt) {
      box.setStmt(stmt);
    }

    private $Accessor() {}
  }
}
