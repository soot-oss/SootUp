package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Util class for BodyInterceptors
 *
 * @author Marcus Nachtigall
 */
public class UtilInterceptors {

  /**
   * Collects all defining statements of a Local from a list of statements and updates the Map of
   * all definitions
   *
   * @param stmts The searched list of statements
   * @param allDefs The Map of definitions that has to be updated
   * @return The updated Map of definitions
   */
  static Map<Local, List<Stmt>> collectDefs(List<Stmt> stmts, Map<Local, List<Stmt>> allDefs) {
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
   * Collects all statements using Locals and update the Map of all used Locals with their
   * corresponding statements
   *
   * @param stmts The searched list of statements
   * @param allUses The Map of uses that has to be updated
   * @return The updated Map of uses
   */
  static Map<Local, List<Stmt>> collectUses(List<Stmt> stmts, Map<Local, List<Stmt>> allUses) {
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
