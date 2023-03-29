package sootup.java.bytecode.interceptors.typeresolving;

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
import java.util.Map;
import javax.annotation.Nonnull;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.types.Type;

public class CastCounter extends TypeChecker {

  private int castCount = 0;
  private boolean countOnly;
  private final Map<Stmt, Map<Value, Value>> changedValues = new HashMap<>();
  private int newLocalsCount = 0;
  public Map<Stmt, Stmt> stmt2NewStmt = new HashMap<>();

  public CastCounter(
      @Nonnull Body.BodyBuilder builder,
      @Nonnull AugEvalFunction evalFunction,
      @Nonnull BytecodeHierarchy hierarchy) {
    super(builder, evalFunction, hierarchy);
  }

  public int getCastCount(@Nonnull Typing typing) {
    this.castCount = 0;
    this.countOnly = true;
    setTyping(typing);
    for (Stmt stmt : builder.getStmts()) {
      stmt.accept(this);
    }
    return this.castCount;
  }

  public int getCastCount() {
    return this.castCount;
  }

  public void insertCastStmts(@Nonnull Typing typing) {
    this.castCount = 0;
    this.countOnly = false;
    setTyping(typing);
    for (Stmt stmt : builder.getStmts()) {
      stmt.accept(this);
    }
  }

  /** This method is used to check whether a value in a stmt needs a cast. */
  public void visit(@Nonnull Value value, @Nonnull Type stdType, @Nonnull Stmt stmt) {
    AugEvalFunction evalFunction = getFuntion();
    BytecodeHierarchy hierarchy = getHierarchy();
    Typing typing = getTyping();
    if (countOnly) {
      Type evaType = evalFunction.evaluate(typing, value, stmt, graph);
      if (hierarchy.isAncestor(stdType, evaType)) {
        return;
      }
      this.castCount++;
    } else {
      Stmt oriStmt = stmt;
      Value oriValue = value;
      Stmt updatedStmt = stmt2NewStmt.get(stmt);
      if (updatedStmt != null) {
        stmt = stmt2NewStmt.get(stmt);
      }
      Map<Value, Value> m = changedValues.get(oriStmt);
      if (m != null) {
        Value updatedValue = m.get(value);
        if (updatedValue != null) {
          value = updatedValue;
        }
      }
      Type evaType = evalFunction.evaluate(typing, value, stmt, graph);
      if (hierarchy.isAncestor(stdType, evaType)) {
        return;
      }
      this.castCount++;
      // TODO: modifiers later must be added

      Local old_local;
      if (value instanceof Local) {
        old_local = (Local) value;
      } else {
        old_local = generateTempLocal(evaType);
        builder.addLocal(old_local);
        typing.set(old_local, evaType);
        JAssignStmt<?, ?> newAssign =
            Jimple.newAssignStmt(old_local, value, stmt.getPositionInfo());
        builder.insertBefore(stmt, newAssign);
      }
      Local new_local = generateTempLocal(stdType);
      builder.addLocal(new_local);
      typing.set(new_local, stdType);
      addUpdatedValue(oriValue, new_local, oriStmt);
      JAssignStmt<?, ?> newCast =
          Jimple.newAssignStmt(
              new_local, Jimple.newCastExpr(old_local, stdType), stmt.getPositionInfo());
      builder.insertBefore(stmt, newCast);

      Stmt newStmt;
      if (stmt.getUses().contains(value)) {
        newStmt = stmt.withNewUse(value, new_local);
      } else {
        newStmt = ((AbstractDefinitionStmt<?, ?>) stmt).withNewDef(new_local);
      }
      builder.replaceStmt(stmt, newStmt);
      this.stmt2NewStmt.put(oriStmt, newStmt);
    }
  }

  private void addUpdatedValue(Value oldValue, Value newValue, Stmt stmt) {
    Map<Value, Value> map;
    if (!this.changedValues.containsKey(stmt)) {
      map = new HashMap<>();
      this.changedValues.put(stmt, map);
    } else {
      map = this.changedValues.get(stmt);
    }
    map.put(oldValue, newValue);
    if (stmt instanceof JAssignStmt && stmt.containsArrayRef()) {
      Value leftOp = ((JAssignStmt<?, ?>) stmt).getLeftOp();
      Value rightOp = ((JAssignStmt<?, ?>) stmt).getRightOp();
      if (leftOp instanceof JArrayRef) {
        if (oldValue == leftOp) {
          Local base = ((JArrayRef) oldValue).getBase();
          Local nBase = ((JArrayRef) newValue).getBase();
          map.put(base, nBase);
        } else if (leftOp.getUses().contains(oldValue)) {
          JArrayRef nArrRef = ((JArrayRef) leftOp).withBase((Local) newValue);
          map.put(leftOp, nArrRef);
        }
      } else if (rightOp instanceof JArrayRef) {
        if (oldValue == rightOp) {
          Local base = ((JArrayRef) oldValue).getBase();
          Local nBase = ((JArrayRef) newValue).getBase();
          map.put(base, nBase);
        } else if (rightOp.getUses().contains(oldValue)) {
          JArrayRef nArrRef = ((JArrayRef) rightOp).withBase((Local) newValue);
          map.put(rightOp, nArrRef);
        }
      }
    }
  }

  private Local generateTempLocal(@Nonnull Type type) {
    String name = "#l" + newLocalsCount++;
    return Jimple.newLocal(name, type);
  }
}
