package sootup.interceptors;

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
import com.google.common.collect.Lists;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JGotoStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.transform.BodyInterceptor;
import sootup.core.views.View;

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
  public void interceptBody(Body.@NonNull BodyBuilder builder, @NonNull View view) {

    MutableStmtGraph graph = builder.getStmtGraph();
    Set<Local> locals = builder.getLocals();

    for (Stmt stmt : Lists.newArrayList(graph.getNodes())) {
      if (!(stmt instanceof JGotoStmt)) {
        continue;
      }
      JGotoStmt gotoStmt = (JGotoStmt) stmt;

      Stmt successorOfGoto = graph.successors(gotoStmt).get(0);

      if (!(successorOfGoto instanceof JAssignStmt)) {
        continue;
      }
      JAssignStmt assign = (JAssignStmt) successorOfGoto;

      if (!(assign.getRightOp() instanceof JCastExpr)) {
        continue;
      }
      Stmt nextStmt = graph.successors(assign).get(0);

      if (!(nextStmt instanceof JReturnStmt)) {
        continue;
      }
      JReturnStmt retStmt = (JReturnStmt) nextStmt;

      if (retStmt.getOp() != assign.getLeftOp()) {
        continue;
      }

      // We need to replace the JGoto with the assignment/cast + return
      JCastExpr ce = (JCastExpr) assign.getRightOp();
      Local localCandidate;
      int i = 0;
      do {
        localCandidate = Jimple.newLocal(ce.getOp() + "_ret" + i++, ce.getType());
        // handle possible Local name collisions - so the LocalSplitter does not handle this then.
      } while (locals.contains(localCandidate));

      JAssignStmt newAssignStmt =
          Jimple.newAssignStmt(localCandidate, ce, assign.getPositionInfo());
      JReturnStmt newReturnStmt = retStmt.withReturnValue(localCandidate);

      // Redirect all flows coming into the JGoto to the new cast + return
      graph.replaceNode(gotoStmt, newReturnStmt);
      graph.insertBefore(newReturnStmt, newAssignStmt);
      builder.addLocal(localCandidate);

      boolean removeExistingCastReturn = graph.predecessors(assign).isEmpty();
      if (removeExistingCastReturn) {
        graph.removeNode(assign, false);
        if (graph.predecessors(retStmt).isEmpty()) {
          graph.removeNode(retStmt, false);
          builder.removeDefLocalsOf(assign);
        }
      }
    }
  }
}
