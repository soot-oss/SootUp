package de.upb.swt.soot.java.bytecode.interceptors;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Christian Brüggemann
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
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.common.stmt.JGotoStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JSwitchStmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import javax.annotation.Nonnull;

/**
 * Removes empty switch statements which always take the default action from a method body, i.e.
 * blocks of the form switch(x) { default: ... }. Such blocks are replaced by the code of the
 * default block.
 *
 * @author Steven Arzt, Zun Wang
 */
public class EmptySwitchEliminator implements BodyInterceptor {

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder) {
    // Iterate all stmts in the body

    for (Stmt stmt : builder.getStmtGraph()) {
      // If the observed stmt an instance of JSwitchStmt
      if (stmt instanceof JSwitchStmt) {
        Body body = builder.build();
        JSwitchStmt sw = (JSwitchStmt) stmt;
        // if there's only defaultStmt
        if (sw.getValueCount() == 1) {
          StmtPositionInfo positionInfo = sw.getPositionInfo();
          JGotoStmt gotoStmt = Jimple.newGotoStmt(positionInfo);
          builder.replaceStmt(sw, gotoStmt);
        }
      }
    }
  }
}
