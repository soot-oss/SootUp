package de.upb.swt.soot.core.model;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2020 Marcus Nachtigall
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
import java.util.*;
import javax.annotation.Nonnull;

/**
 * Util class for the Body
 *
 * @author Marcus Nachtigall, Zun Wang
 */
public class BodyUtils {

  /**
   * Collects all defining statements of a Local from a list of statements
   *
   * @param stmts The searched list of statements
   * @return A map of Locals and their using statements
   */
  public static Map<Local, List<Stmt>> collectDefs(List<Stmt> stmts) {
    Map<Local, List<Stmt>> allDefs = new HashMap<>();
    for (Stmt stmt : stmts) {
      List<Value> defs = stmt.getDefs();
      for (Value value : defs) {
        if (value instanceof Local) {
          List<Stmt> localDefs = allDefs.get(value);
          if (localDefs == null) {
            localDefs = new ArrayList<>();
          }
          localDefs.add(stmt);
          allDefs.put((Local) value, localDefs);
        }
      }
    }
    return allDefs;
  }

  /**
   * Collects all using statements of a Local from a list of statements
   *
   * @param stmts The searched list of statements
   * @return A map of Locals and their using statements
   */
  public static Map<Local, List<Stmt>> collectUses(List<Stmt> stmts) {
    Map<Local, List<Stmt>> allUses = new HashMap<>();
    for (Stmt stmt : stmts) {
      List<Value> uses = stmt.getUses();
      for (Value value : uses) {
        if (value instanceof Local) {
          List<Stmt> localUses = allUses.get(value);
          if (localUses == null) {
            localUses = new ArrayList<>();
          }
          localUses.add(stmt);
          allUses.put((Local) value, localUses);
        }
      }
    }
    return allUses;
  }

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
