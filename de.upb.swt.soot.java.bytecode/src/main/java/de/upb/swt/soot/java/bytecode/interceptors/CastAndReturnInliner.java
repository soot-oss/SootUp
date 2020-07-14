package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.graph.ImmutableStmtGraph;
import de.upb.swt.soot.core.jimple.basic.Immediate;
import de.upb.swt.soot.core.jimple.basic.JTrap;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.expr.JCastExpr;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JGotoStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JReturnStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
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
      Stmt nextStmt = originalGraph.successors(assign).get(0);

      if (!(nextStmt instanceof JReturnStmt)) {
        continue;
      }
      JReturnStmt retStmt = (JReturnStmt) nextStmt;

      if (retStmt.getOp() != assign.getLeftOp()) {
        continue;
      }

      // We need to replace the GOTO with the return
      JCastExpr ce = (JCastExpr) assign.getRightOp();
      JReturnStmt newStmt = retStmt.withReturnValue((Immediate) ce.getOp());

      // Redirect all flows coming into the GOTO to the new return
      List<Stmt> predecessors = originalGraph.predecessors(gotoStmt);
      for (Stmt pred : predecessors) {
        bodyBuilder.removeFlow(pred, gotoStmt);
        bodyBuilder.addFlow(pred, newStmt);
      }
      bodyBuilder.removeFlow(gotoStmt, assign);
      bodyBuilder.removeFlow(assign, nextStmt);

      // if used in a Trap replace occurences of goto by inlined return
      for (int j = 0; j < bodyTraps.size(); j++) {
        JTrap trap = (JTrap) bodyTraps.get(j);
        boolean modified = false;
        if (trap.getBeginStmt() == gotoStmt) {
          trap = trap.withBeginStmt(newStmt);
          modified = true;
        }
        if (trap.getEndStmt() == gotoStmt) {
          trap = trap.withEndStmt(newStmt);
          modified = true;
        }
        if (trap.getHandlerStmt() == gotoStmt) {
          trap = trap.withHandlerStmt(newStmt);
          modified = true;
        }
        if (modified) {
          bodyTraps.set(j, trap);
        }
      }
    }

    return bodyBuilder.build();
  }
}
