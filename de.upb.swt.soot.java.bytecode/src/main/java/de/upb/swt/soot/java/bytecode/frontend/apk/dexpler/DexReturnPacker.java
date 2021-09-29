package de.upb.swt.soot.java.bytecode.frontend.apk.dexpler;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2018 Raja Vallée-Rai and others
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

import de.upb.swt.soot.core.jimple.common.stmt.JReturnStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JReturnVoidStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;


import javax.annotation.Nonnull;

/**
 * This transformer is the inverse of the DexReturnInliner. It looks for unnecessary duplicates of return statements and
 * removes them.
 *
 * @author Steven Arzt
 *
 */
public class DexReturnPacker implements BodyInterceptor {

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder) {
    // Look for consecutive return statements
    Stmt lastStmt = null;
    for (Stmt stmt :builder.getStmts()) {

      if (stmt instanceof JReturnStmt || stmt instanceof JReturnVoidStmt) {
        // Check for duplicates
        if (lastStmt != null && isEqual(lastStmt, stmt)) {
          // FIXME
          stmt.redirectJumpsToThisTo(lastStmt);
          stmt.remove();
        } else {
          lastStmt = stmt;
        }
      } else {
        // Start over
        lastStmt = null;
      }
    }
  }

  /**
   * Checks whether the two given units are semantically equal
   *
   * @param stmt1
   *          The first unit
   * @param stmt2
   *          The second unit
   * @return True if the two given units are semantically equal, otherwise false
   */
  private boolean isEqual(Stmt stmt1, Stmt stmt2) {
    // Trivial case
    if (stmt1 == stmt2 || stmt1.equals(stmt2)) {
      return true;
    }

    // Semantic check
    if (stmt1.getClass() == stmt2.getClass()) {
      if (stmt1 instanceof JReturnVoidStmt) {
        return true;
      } else if (stmt1 instanceof JReturnStmt) {
        return ((JReturnStmt) stmt1).getOp() == ((JReturnStmt) stmt2).getOp();
      }
    }

    return false;
  }

}
