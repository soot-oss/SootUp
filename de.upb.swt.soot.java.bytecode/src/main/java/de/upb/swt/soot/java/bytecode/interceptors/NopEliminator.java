package de.upb.swt.soot.java.bytecode.interceptors;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2020 Raja Vallée-Rai and others
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
import de.upb.swt.soot.core.jimple.common.stmt.JNopStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * This class removes {@link JNopStmt}s from a given {@link Body}.
 *
 * @author Marcus Nachtigall
 * @author Markus Schmidt
 */
public class NopEliminator implements BodyInterceptor {

  /**
   * Removes {@link JNopStmt}s from the given {@link Body}. Complexity is linear with respect to the
   * statements.
   *
   * @param originalBody The current body before interception.
   * @return The transformed body.
   */
  @Nonnull
  @Override
  public Body interceptBody(@Nonnull Body originalBody) {
    Body.BodyBuilder builder = Body.builder(originalBody);
    StmtGraph originalGraph = originalBody.getStmtGraph();
    Set<Stmt> stmtSet = originalGraph.nodes();

    for (Stmt stmt : stmtSet) {
      if (stmt instanceof JNopStmt) {
        final List<Stmt> successors = originalGraph.successors(stmt);
        // relink predecessors to successor of nop
        // [ms] in a valid Body there is always a successor of nop -> last stmt (no
        // successor) is a
        // return|throw stmt
        final Stmt successorOfNop = successors.iterator().next();
        builder.removeFlow(stmt, successorOfNop);
        for (Stmt pred : originalGraph.predecessors(stmt)) {
          builder.removeFlow(pred, stmt);
          builder.addFlow(pred, successorOfNop);
        }
      }
    }

    return builder.build();
  }
}
