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
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.stmt.AbstractDefinitionStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JIdentityStmt;
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

  public static List<Stmt> getDefsOfLocal(Local local, List<Stmt> defs) {
    List<Stmt> localDefs = new ArrayList<>();
    for (Stmt stmt : defs) {
      if (stmt instanceof AbstractDefinitionStmt
          && ((AbstractDefinitionStmt) stmt).getLeftOp().equals(local)) {
        localDefs.add(stmt);
      }
    }
    return localDefs;
  }

  /**
   * Get all definition-stmts which define the given local used by the given stmt.
   *
   * @param graph a stmt graph which contains the given stmts.
   * @param use a local that is used by the given stmt.
   * @param stmt a stmt which uses the given local.
   */
  public static List<Stmt> getDefsForLocalUse(StmtGraph graph, Local use, Stmt stmt) {
    if (!stmt.getUses().contains(use)) {
      throw new RuntimeException(stmt + " doesn't use the local " + use.toString());
    }
    List<Stmt> defStmts = new ArrayList<>();
    Set<Stmt> visited = new HashSet<>();

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
    return visitor.getResult();
  }

  /**
   * Use newDef to replace the definition in oldStmt.
   *
   * @param oldStmt a Stmt whose def is to be replaced.
   * @param newDef a Local to replace definition Local of oldStmt.
   * @return a new Stmt with newDef
   */
  @Nonnull
  public static Stmt withNewDef(@Nonnull Stmt oldStmt, @Nonnull Local newDef) {
    if (oldStmt instanceof JAssignStmt) {
      return ((JAssignStmt) oldStmt).withVariable(newDef);
    } else if (oldStmt instanceof JIdentityStmt) {
      return ((JIdentityStmt) oldStmt).withLocal(newDef);
    }
    throw new RuntimeException("The given stmt must be JAssignStmt or JIdentityStmt!");
  }

  /**
   * Replace corresponding oldStmt with newStmt in BodyBuilder
   */
  public static void replaceStmtInBuilder(Body.BodyBuilder builder, Stmt oldStmt, Stmt newStmt) {
    builder.replaceStmt(oldStmt, newStmt);
    adaptTraps(builder, oldStmt, newStmt);
  }
  /**
   * Fit the modified stmt in Traps
   *
   * @param builder a bodybuilder, use it to modify Trap
   * @param oldStmt a Stmt which maybe a beginStmt or endStmt in a Trap
   * @param newStmt a modified stmt to replace the oldStmt.
   */
  public static void adaptTraps(
      @Nonnull Body.BodyBuilder builder, @Nonnull Stmt oldStmt, @Nonnull Stmt newStmt) {
    List<Trap> traps = new ArrayList<>(builder.getStmtGraph().getTraps());
    for (ListIterator<Trap> iterator = traps.listIterator(); iterator.hasNext(); ) {
      Trap trap = iterator.next();
      if (oldStmt.equivTo(trap.getBeginStmt())) {
        Trap newTrap = trap.withBeginStmt(newStmt);
        iterator.set(newTrap);
      } else if (oldStmt.equivTo(trap.getEndStmt())) {
        Trap newTrap = trap.withEndStmt(newStmt);
        iterator.set(newTrap);
      }
    }
    builder.setTraps(traps);
  }
}
