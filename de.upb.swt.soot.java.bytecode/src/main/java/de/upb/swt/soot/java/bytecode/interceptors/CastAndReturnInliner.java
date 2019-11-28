package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.jimple.basic.JTrap;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.expr.JCastExpr;
import de.upb.swt.soot.core.jimple.common.stmt.AbstractSwitchStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JGotoStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JIfStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JReturnStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JLookupSwitchStmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JTableSwitchStmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Transformers that inlines returns that cast and return an object. We take
 *
 * <pre>
 * a = ..;
 * goto l0;
 * l0: b = (B) a;
 * return b;
 * </pre>
 *
 * and transform it into
 *
 * <pre>
 * a = ..;
 * return a;
 * </pre>
 *
 * This makes it easier for the local splitter to split distinct uses of the same variable. Imagine
 * that "a" can come from different parts of the code and have different types. To be able to find a
 * valid typing at all, we must break apart the uses of "a".
 *
 * @author Steven Arzt
 * @author Christian Brüggemann
 */
public class CastAndReturnInliner implements BodyInterceptor {

  @Nonnull
  @Override
  public Body interceptBody(@Nonnull Body originalBody) {
    // In case of performance issues, these copies could be avoided
    // in cases where the content is not changed by adding logic for this.
    List<Stmt> bodyStmts = new ArrayList<>(originalBody.getStmts());
    List<Trap> bodyTraps = new ArrayList<>(originalBody.getTraps());

    for (int i = 0; i < bodyStmts.size(); i++) {
      Stmt u = bodyStmts.get(i);
      if (!(u instanceof JGotoStmt)) {
        continue;
      }
      JGotoStmt gotoStmt = (JGotoStmt) u;

      if (!(gotoStmt.getTarget() instanceof JAssignStmt)) {
        continue;
      }
      JAssignStmt assign = (JAssignStmt) gotoStmt.getTarget();

      if (!(assign.getRightOp() instanceof JCastExpr)) {
        continue;
      }
      JCastExpr ce = (JCastExpr) assign.getRightOp();

      int nextStmtIndex = bodyStmts.indexOf(assign) + 1;
      Stmt nextStmt = bodyStmts.get(nextStmtIndex);
      if (nextStmt instanceof JReturnStmt) {
        JReturnStmt retStmt = (JReturnStmt) nextStmt;
        if (retStmt.getOp() == assign.getLeftOp()) {
          // We need to replace the GOTO with the return
          JReturnStmt newStmt = retStmt.withOp(ce.getOp());

          // Replaces the GOTO with the new return stmt
          bodyStmts.set(i, newStmt);

          for (int j = 0; j < bodyTraps.size(); j++) {
            Trap originalTrap = bodyTraps.get(j);
            JTrap fixedTrap = replaceStmtsOfTrap((JTrap) originalTrap, gotoStmt, newStmt);
            bodyTraps.set(j, fixedTrap);
          }

          // Fix targets of other statements: Switch, If, Goto
          for (int j = 0; j < bodyStmts.size(); j++) {
            if (i == j) continue;

            Stmt toFixStmt = bodyStmts.get(j);
            Stmt fixedStmt = replaceTargetsOfStmt(toFixStmt, gotoStmt, newStmt);
            bodyStmts.set(j, fixedStmt);
          }
        }
      }
    }

    return originalBody.withStmts(bodyStmts).withTraps(bodyTraps);
  }

  /**
   * Checks if <code>trap</code> contains <code>gotoStmt</code> as begin stmt, end stmt or handler
   * and returns a copy of <code>toFixStmt</code> where this has been replaced with <code>
   * newStmt</code>.
   */
  @Nonnull
  private JTrap replaceStmtsOfTrap(
      @Nonnull JTrap trap, @Nonnull JGotoStmt gotoStmt, @Nonnull JReturnStmt newStmt) {
    if (trap.getBeginStmt() == gotoStmt) {
      trap = trap.withBeginStmt(newStmt);
    }
    if (trap.getEndStmt() == gotoStmt) {
      trap = trap.withEndStmt(newStmt);
    }
    if (trap.getHandlerStmt() == gotoStmt) {
      trap = trap.withHandlerStmt(newStmt);
    }
    return trap;
  }

  /**
   * Checks if <code>toFixStmt</code> contains <code>gotoStmt</code> as a jump target and returns a
   * copy of <code>toFixStmt</code> where the target has been replaced with <code>newStmt</code>.
   */
  @Nonnull
  private Stmt replaceTargetsOfStmt(
      @Nonnull Stmt toFixStmt, @Nonnull JGotoStmt gotoStmt, @Nonnull JReturnStmt newStmt) {
    if (toFixStmt instanceof JIfStmt) {
      JIfStmt toFixIfStmt = (JIfStmt) toFixStmt;
      if (toFixIfStmt.getTarget() == gotoStmt) {
        return toFixIfStmt.withTarget(newStmt);
      }
    } else if (toFixStmt instanceof JGotoStmt) {
      JGotoStmt toFixGotoStmt = (JGotoStmt) toFixStmt;
      if (toFixGotoStmt.getTarget() == gotoStmt) {
        return toFixGotoStmt.withTarget(newStmt);
      }
    } else if (toFixStmt instanceof AbstractSwitchStmt) {
      AbstractSwitchStmt toFixSwitchStmt = (AbstractSwitchStmt) toFixStmt;
      List<Stmt> targets = toFixSwitchStmt.getTargets();
      List<Stmt> copiedTargets = null;
      for (int k = 0; k < targets.size(); k++) {
        Stmt switchTarget = targets.get(k);
        if (switchTarget == gotoStmt) {
          if (copiedTargets == null) {
            copiedTargets = new ArrayList<>(targets);
          }
          copiedTargets.set(k, newStmt);
        }
      }
      if (copiedTargets != null) {
        if (toFixSwitchStmt instanceof JTableSwitchStmt) {
          return ((JTableSwitchStmt) toFixSwitchStmt).withTargets(copiedTargets);
        } else if (toFixSwitchStmt instanceof JLookupSwitchStmt) {
          return ((JLookupSwitchStmt) toFixSwitchStmt).withTargets(copiedTargets);
        } else {
          throw new RuntimeException("Unknown switch type!");
        }
      }
    }

    return toFixStmt;
  }
}
