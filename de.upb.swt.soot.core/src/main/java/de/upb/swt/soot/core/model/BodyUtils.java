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
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Util class for the Body
 *
 * @author Marcus Nachtigall
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
}
