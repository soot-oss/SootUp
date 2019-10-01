package de.upb.swt.soot.java.bytecode.frontend;

import de.upb.swt.soot.core.jimple.basic.StmtBox;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.expr.JCastExpr;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JGotoStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JReturnStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
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

  void transform(@Nonnull List<Stmt> bodyUnits, @Nonnull List<Trap> bodyTraps) {
    // original snapshot iterator
    // Iterator<Stmt> it = body.getUnits().snapshotIterator();

    // FIXME: that is why lists do not work

    for (Stmt u : bodyUnits) {
      if (u instanceof JGotoStmt) {
        JGotoStmt gtStmt = (JGotoStmt) u;
        if (gtStmt.getTarget() instanceof JAssignStmt) {
          JAssignStmt assign = (JAssignStmt) gtStmt.getTarget();
          if (assign.getRightOp() instanceof JCastExpr) {
            JCastExpr ce = (JCastExpr) assign.getRightOp();
            // We have goto that ends up at a cast statement
            // Stmt nextStmt = bodyUnits.getSuccOf(assign);
            // FIXME: this migration is ugly ... urg ... :(
            Stmt nextStmt = bodyUnits.get(bodyUnits.indexOf(assign) + 1);
            if (nextStmt instanceof JReturnStmt) {
              JReturnStmt retStmt = (JReturnStmt) nextStmt;
              if (retStmt.getOp() == assign.getLeftOp()) {
                // We need to replace the GOTO with the return
                JReturnStmt newStmt = retStmt.withOp(ce.getOp());

                for (Trap t : bodyTraps) {
                  for (StmtBox ubox : t.getStmtBoxes()) {
                    if (ubox.getStmt() == gtStmt) {
                      StmtBox.$Accessor.setStmt(ubox, newStmt);
                    }
                  }
                }

                while (!gtStmt.getBoxesPointingToThis().isEmpty()) {
                  StmtBox.$Accessor.setStmt(gtStmt.getBoxesPointingToThis().get(0), newStmt);
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
