package de.upb.swt.soot.java.bytecode.interceptors;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Zun Wang
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
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.stmt.AbstractDefinitionStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.visitor.ReplaceUseStmtVisitor;
import de.upb.swt.soot.core.model.Body;
import java.util.*;
import javax.annotation.Nonnull;

/** @author Zun Wang */
public class InterceptorUtils {

  /**
   * Get all definition-stmts which define the given local used by the given stmt.
   *
   * @param builder an instance of BodyBuilder whose body contains the given stmt.
   * @param use a local that is used by the given stmt.
   * @param stmt a stmt which uses the given local.
   * @return
   */
  public static List<Stmt> getDefsForLocalUse(Body.BodyBuilder builder, Local use, Stmt stmt) {
    if (!stmt.getUses().contains(use)) {
      throw new RuntimeException(stmt + " doesn't use the local " + use.toString());
    }
    List<Stmt> defStmts = new ArrayList<>();
    Set<Stmt> visited = new HashSet<>();
    StmtGraph graph = builder.getStmtGraph();
    Deque<Stmt> queue = new ArrayDeque<>();
    queue.add(stmt);
    while (!queue.isEmpty()) {
      Stmt s = queue.removeFirst();
      if (!visited.contains(s)) {
        visited.add(s);
        if (s instanceof AbstractDefinitionStmt && s.getDefs().get(0).equivTo(use)) {
          defStmts.add(s);
        } else {
          for (Stmt pred : graph.predecessors(s)) {
            queue.add(pred);
          }
        }
      }
    }
    return defStmts;
  }

  /**
   * Use newUse to replace the oldUse in oldStmt.
   *
   * @param oldStmt a Stmt that has oldUse.
   * @param oldUse a Value in the useList of oldStmt.
   * @param newUse a Value is to replace oldUse
   * @return a new Stmt with newUse
   */
  @Nonnull
  public static Stmt withNewUse(
      @Nonnull Stmt oldStmt, @Nonnull Value oldUse, @Nonnull Value newUse) {
    ReplaceUseStmtVisitor visitor = new ReplaceUseStmtVisitor(oldUse, newUse);
    oldStmt.accept(visitor);
    return visitor.getNewStmt();
  }
}
