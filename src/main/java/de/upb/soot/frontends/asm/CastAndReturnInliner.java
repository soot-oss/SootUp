package de.upb.soot.frontends.asm;

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

import de.upb.soot.jimple.basic.IStmtBox;
import de.upb.soot.jimple.basic.Trap;
import de.upb.soot.jimple.common.expr.JCastExpr;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.jimple.common.stmt.JAssignStmt;
import de.upb.soot.jimple.common.stmt.JGotoStmt;
import de.upb.soot.jimple.common.stmt.JReturnStmt;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Transformers that inlines returns that cast and return an object. We take a = .. goto l0;
 *
 * <p>l0: b = (B) a; return b;
 *
 * <p>and transform it into a = .. return a;
 *
 * <p>This makes it easier for the local splitter to split distinct uses of the same variable.
 * Imagine that "a" can come from different parts of the code and have different types. To be able
 * to find a valid typing at all, we must break apart the uses of "a".
 *
 * @author Steven Arzt
 */
class CastAndReturnInliner {

  void transform(@Nonnull List<IStmt> bodyUnits, @Nonnull List<Trap> bodyTraps) {
    // original snapshot iterator
    // Iterator<IStmt> it = body.getUnits().snapshotIterator();

    // FIXME: that is why lists do not work

    for (IStmt u : bodyUnits) {
      if (u instanceof JGotoStmt) {
        JGotoStmt gtStmt = (JGotoStmt) u;
        if (gtStmt.getTarget() instanceof JAssignStmt) {
          JAssignStmt assign = (JAssignStmt) gtStmt.getTarget();
          if (assign.getRightOp() instanceof JCastExpr) {
            JCastExpr ce = (JCastExpr) assign.getRightOp();
            // We have goto that ends up at a cast statement
            // IStmt nextStmt = bodyUnits.getSuccOf(assign);
            // FIXME: this migration is ugly ... urg ... :(
            IStmt nextStmt = bodyUnits.get(bodyUnits.indexOf(assign) + 1);
            if (nextStmt instanceof JReturnStmt) {
              JReturnStmt retStmt = (JReturnStmt) nextStmt;
              if (retStmt.getOp() == assign.getLeftOp()) {
                // We need to replace the GOTO with the return
                JReturnStmt newStmt = retStmt.withOp(ce.getOp());

                for (Trap t : bodyTraps) {
                  for (IStmtBox ubox : t.getStmtBoxes()) {
                    if (ubox.getStmt() == gtStmt) {
                      ubox.setStmt(newStmt);
                    }
                  }
                }

                while (!gtStmt.getBoxesPointingToThis().isEmpty()) {
                  gtStmt.getBoxesPointingToThis().get(0).setStmt(newStmt);
                }
                // original code
                // body.getUnits().swapWith(gtStmt, newStmt);
              }
            }
          }
        }
      }
    }
  }
}
