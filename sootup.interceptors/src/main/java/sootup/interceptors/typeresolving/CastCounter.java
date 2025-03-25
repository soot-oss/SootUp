package sootup.interceptors.typeresolving;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 1997 - 2023 Raja Vallée-Rai and others
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.types.Type;

/**
 * For a given body, type hierarchy and typing, this class calculates all statements that are
 * necessary for the body to become use-valid. <br>
 * That means it inserts casts when a variable is used in a place where a sub-type of the type of
 * the variable is required. <br>
 * It may also introduce additional local variables when a cast is necessary, but the use of the
 * original variable doesn't allow inserting a cast into the statement directly. <br>
 * Calling the constructor {@link #CastCounter} will calculate all necessary modifications to the
 * body, but not apply them yet. Then you can use {@link #getCastCount} to get the amount of casts
 * that are necessary for this typing. If you have multiple possible {@link Typing}s, this can be
 * used to select the one requiring the least number of casts. <br>
 * {@link #insertCastStmts()} will actually apply the modifications to the body and insert the new
 * locals into the typing as well.
 */
public class CastCounter extends TypeChecker {
  private final Map<Stmt, Set<JAssignStmt>> tempAssignments = new HashMap<>();
  private final Map<Stmt, Stmt> stmt2NewStmt = new HashMap<>();
  private final Map<Local, Type> tempLocalTypes = new HashMap<>();

  private int castCount = 0;
  private int newLocalsCount = 0;

  public CastCounter(
      Body.@NonNull BodyBuilder builder,
      @NonNull AugEvalFunction evalFunction,
      @NonNull BytecodeHierarchy hierarchy,
      @NonNull Typing typing) {
    super(builder, evalFunction, hierarchy);
    setTyping(typing);

    for (Stmt stmt : graph.getNodes()) {
      stmt.accept(this);
    }
  }

  public int getCastCount() {
    return castCount;
  }

  public void insertCastStmts() {
    for (Map.Entry<Local, Type> tempLocal : tempLocalTypes.entrySet()) {
      builder.addLocal(tempLocal.getKey());
      getTyping().set(tempLocal.getKey(), tempLocal.getValue());
    }

    for (Map.Entry<Stmt, Set<JAssignStmt>> casts : tempAssignments.entrySet()) {
      for (JAssignStmt cast : casts.getValue()) {
        // FIXME this also places before labels, which is not correct
        graph.insertBefore(casts.getKey(), cast);
      }
    }

    for (Map.Entry<Stmt, Stmt> stmt : stmt2NewStmt.entrySet()) {
      graph.replaceNode(stmt.getKey(), stmt.getValue());
    }
  }

  /** This method is used to check whether a value in a stmt needs a cast. */
  public void visit(@NonNull Value value, @NonNull Type stdType, @NonNull Stmt stmt) {
    if (!(value instanceof Immediate)) {
      return;
    }

    if (stmt.getUses().noneMatch(v -> v == value)) {
      return;
    }

    Stmt currentStmt = stmt2NewStmt.getOrDefault(stmt, stmt);

    Type evaType = getFuntion().evaluate(getTyping(), value, currentStmt, graph);
    if (evaType == null || getHierarchy().isAncestor(stdType, evaType)) {
      return;
    }

    JCastExpr cast = Jimple.newCastExpr((Immediate) value, stdType);

    Stmt newStmt = currentStmt.withNewUse(value, cast);

    // This happens when the use of `value` in currentStatement can't be replaced with a cast.
    if (newStmt == null || newStmt == currentStmt) {
      Local tempLocal = generateTempLocal(stdType);
      tempLocalTypes.put(tempLocal, stdType);

      JAssignStmt assignStmt = Jimple.newAssignStmt(tempLocal, cast, stmt.getPositionInfo());
      tempAssignments.computeIfAbsent(stmt, _x -> new HashSet<>()).add(assignStmt);

      newStmt = currentStmt.withNewUse(value, tempLocal);
    }

    if (currentStmt == newStmt) {
      // This can happen when the same local is used multiple times in the same statement, and the
      // invocation of this method for the first occurrence has already replaced all uses.
      return;
    }
    castCount++;

    stmt2NewStmt.put(stmt, newStmt);
  }

  private Local generateTempLocal(@NonNull Type type) {
    String name = "#l" + newLocalsCount++;
    return Jimple.newLocal(name, type);
  }
}
