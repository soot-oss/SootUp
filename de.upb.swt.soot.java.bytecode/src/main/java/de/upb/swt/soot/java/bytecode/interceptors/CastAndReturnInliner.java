package de.upb.swt.soot.java.bytecode.interceptors;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Christian Brüggemann, Marcus Nachtigall, Markus Schmidt and others
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
import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.expr.JCastExpr;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JGotoStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JReturnStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Transformers that inlines returns that cast and return an object. We take
 *
 * <pre>
 * a = ...;
 * goto label0;
 * label0:
 * b = (B) a;
 * return b;
 * </pre>
 *
 * and transform it into
 *
 * <pre>
 * a = ...;
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
 * @author Markus Schmidt
 */
public class CastAndReturnInliner implements BodyInterceptor {

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder) {

    StmtGraph<?> originalGraph = builder.getStmtGraph();

    builder.enableDeferredStmtGraphChanges();
    for (Stmt stmt : originalGraph.nodes()) {
      if (!(stmt instanceof JGotoStmt)) {
        continue;
      }
      JGotoStmt gotoStmt = (JGotoStmt) stmt;

      Stmt successorOfGoto = originalGraph.successors(gotoStmt).get(0);

      if (!(successorOfGoto instanceof JAssignStmt)) {
        continue;
      }
      JAssignStmt<?, ?> assign = (JAssignStmt) successorOfGoto;

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
      JReturnStmt newStmt = retStmt.withReturnValue(ce.getOp());

      // Redirect all flows coming into the GOTO to the new return
      List<Stmt> predecessors = originalGraph.predecessors(gotoStmt);
      for (Stmt pred : predecessors) {
        builder.addFlow(pred, newStmt);
        builder.removeFlow(pred, gotoStmt);
      }
      // cleanup now obsolete cast and return statements
      builder.removeFlow(gotoStmt, assign);
      builder.removeFlow(assign, retStmt);

      List<Trap> traps = builder.getTraps();
      // if used in a Trap replace occurences of goto by inlined return
      for (int i = 0; i < traps.size(); i++) {
        Trap trap = traps.get(i);
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
          traps.set(i, trap);
        }
      }
    }
    builder.commitDeferredStmtGraphChanges();
  }
}
