package sootup.java.bytecode.interceptors;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Marcus Nachtigall, Markus Schmidt and others
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
import java.util.*;
import javax.annotation.Nonnull;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.stmt.JNopStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.transform.BodyInterceptor;
import sootup.core.views.View;

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
   * @param builder The current body before interception.
   */
  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder, @Nonnull View<?> view) {
    StmtGraph<?> graph = builder.getStmtGraph();

    Queue<Stmt> q = new ArrayDeque<>();
    for (Stmt stmt : graph.getNodes()) {
      if (stmt instanceof JNopStmt) {
        q.add(stmt);
      }
    }

    for (Stmt stmt : q) {
      builder.removeStmt(stmt);
    }
  }
}
