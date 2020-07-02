package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.graph.ImmutableStmtGraph;
import de.upb.swt.soot.core.jimple.basic.Immediate;
import de.upb.swt.soot.core.jimple.basic.JTrap;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.expr.JCastExpr;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JGotoStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JIfStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JReturnStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JSwitchStmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
 * @author Marcus Nachtigall
 */
public class CastAndReturnInliner implements BodyInterceptor {

  @Nonnull
  @Override
  public Body interceptBody(@Nonnull Body originalBody) {
    // In case of performance issues, these copies could be avoided
    // in cases where the content is not changed by adding logic for this.

    Body.BodyBuilder bodyBuilder = Body.builder(originalBody);
    Set<Stmt> bodyStmts = originalBody.getStmtGraph().nodes();
    List<Trap> bodyTraps = new ArrayList<>(originalBody.getTraps());
    ImmutableStmtGraph originalGraph = originalBody.getStmtGraph();

    for (Stmt stmt : bodyStmts) {
      if (!(stmt instanceof JGotoStmt)) {
        continue;
      }
      JGotoStmt gotoStmt = (JGotoStmt) stmt;

      if (!(gotoStmt.getTarget(originalBody) instanceof JAssignStmt)) {
        continue;
      }
      JAssignStmt assign = (JAssignStmt) gotoStmt.getTarget(originalBody);

      if (!(assign.getRightOp() instanceof JCastExpr)) {
        continue;
      }
      JCastExpr ce = (JCastExpr) assign.getRightOp();

      Stmt nextStmt = originalGraph.successors(assign).get(0);

      if (nextStmt instanceof JReturnStmt) {
        JReturnStmt retStmt = (JReturnStmt) nextStmt;
        if (retStmt.getOp() == assign.getLeftOp()) {
          // We need to replace the GOTO with the return
          JReturnStmt newStmt = retStmt.withReturnValue((Immediate) ce.getOp());

          // Redirect all flows coming into the GOTO to the return
          List<Stmt> predecessors = originalGraph.predecessors(gotoStmt);
          for(Stmt preds : predecessors){
            bodyBuilder.removeFlow(preds, gotoStmt);
            bodyBuilder.addFlow(preds, newStmt);
            bodyBuilder.removeFlow(gotoStmt, gotoStmt.getTargetStmts(originalBody).get(0));
          }

          for (int j = 0; j < bodyTraps.size(); j++) {
            Trap originalTrap = bodyTraps.get(j);
            JTrap fixedTrap = replaceStmtsOfTrap((JTrap) originalTrap, gotoStmt, newStmt);
            bodyTraps.set(j, fixedTrap);
          }

          // Fix targets of other statements: Switch, If, Goto
          for (Stmt toFixStmt : bodyStmts) {
            if (stmt == toFixStmt) continue;

            replaceTargetsOfStmt(originalBody, bodyBuilder, toFixStmt, gotoStmt, newStmt);
          }
        }
      }
    }

    return bodyBuilder.build();
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
  private void replaceTargetsOfStmt(
      Body originalBody,
      Body.BodyBuilder builder,
      @Nonnull Stmt toFixStmt,
      @Nonnull JGotoStmt gotoStmt,
      @Nonnull JReturnStmt newStmt) {
    if (toFixStmt instanceof JIfStmt) {
      JIfStmt toFixIfStmt = (JIfStmt) toFixStmt;
      if (toFixIfStmt.getTarget(originalBody) == gotoStmt) {
        builder.removeFlow(toFixIfStmt, gotoStmt);
        builder.addFlow(toFixIfStmt, newStmt);
      }
    } else if (toFixStmt instanceof JGotoStmt) {
      JGotoStmt toFixGotoStmt = (JGotoStmt) toFixStmt;
      if (toFixGotoStmt.getTarget(originalBody) == gotoStmt) {
        builder.removeFlow(toFixGotoStmt, gotoStmt);
        builder.addFlow(toFixGotoStmt, newStmt);
      }
    } else if (toFixStmt instanceof JSwitchStmt) {
      JSwitchStmt toFixSwitchStmt = (JSwitchStmt) toFixStmt;
      List<Stmt> targets = originalBody.getStmtGraph().successors(toFixSwitchStmt);
      for(Stmt switchTarget : targets){
        if(switchTarget == gotoStmt){
          builder.removeFlow(switchTarget, gotoStmt);
          builder.addFlow(switchTarget, newStmt);
        }
      }
    }
  }
}
