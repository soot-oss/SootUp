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

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.LocalGenerator;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.ref.JCaughtExceptionRef;
import de.upb.swt.soot.core.jimple.common.stmt.JIdentityStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import javax.annotation.Nonnull;

/**
 * Transformer to ensure that all exception handlers pull the exception object. In other words, if an exception handler must
 * always have a unit like
 *
 * $r10 = @caughtexception
 *
 * This is especially important if the dex code is later to be translated into Java bytecode. If no one ever accesses the
 * exception object, it will reside on the stack forever, potentially leading to mismatching stack heights.
 *
 * @author Steven Arzt
 *
 */
public class DexTrapStackFixer implements BodyInterceptor {

  /**
   * Checks whether the given statement stores an exception reference
   *
   * @param handlerUnit
   *          The statement to check
   * @return True if the given statement stores an exception reference, otherwise false
   */
  private boolean isCaughtExceptionRef(Stmt handlerUnit) {
    if (!(handlerUnit instanceof JIdentityStmt)) {
      return false;
    }
    JIdentityStmt stmt = (JIdentityStmt) handlerUnit;
    return stmt.getRightOp() instanceof JCaughtExceptionRef;
  }

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder) {
    // Find all Locals that must be split
    // If a local as a definition appears two or more times, then this local must be split

    for (Trap t : builder.getTraps()) {
      // If the first statement already catches the exception, we're fine
      if (isCaughtExceptionRef(t.getHandlerStmt())) {
        continue;
      }

      // Add the exception reference
      Local l = new LocalGenerator(builder.getLocals()).generateLocal(t.getExceptionType());
      Stmt caughtStmt = Jimple.newIdentityStmt(l, new JCaughtExceptionRef(t.getExceptionType()), StmtPositionInfo.createNoStmtPositionInfo());
      builder.getStmts().add(caughtStmt);
      builder.getStmts().add(Jimple.newGotoStmt(t.getHandlerStmt().getPositionInfo()));
      // FIXME - should I add a setter or not in de.upb.swt.soot.core.jimple.basic.Trap?
      t.setHandlerUnit(caughtStmt);
    }

  }
}
