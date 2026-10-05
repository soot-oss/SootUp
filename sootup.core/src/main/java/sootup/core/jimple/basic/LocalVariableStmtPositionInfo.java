package sootup.core.jimple.basic;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2023 Linghui Luo, Markus Schmidt
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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.LocalVariableScope;
import sootup.core.model.Position;

/** Position information with a captured, shared immutable debug variable scope. */
public interface LocalVariableStmtPositionInfo extends StmtPositionInfo {

  @NonNull LocalVariableScope getLocalVariables();

  /** Returns the same coordinate variant with a different captured scope. */
  @NonNull LocalVariableStmtPositionInfo withLocalVariables(@NonNull LocalVariableScope scope);

  @Nullable
  static LocalVariableScope getLocalVariables(@Nullable StmtPositionInfo info) {
    if (info instanceof LocalVariableStmtPositionInfo lv) {
      return lv.getLocalVariables();
    }
    return null;
  }

  @Nullable
  static LocalVariableScope getLocalVariables(@Nullable Stmt stmt) {
    if (stmt != null && stmt.getPositionInfo() instanceof LocalVariableStmtPositionInfo lv) {
      return lv.getLocalVariables();
    }
    return null;
  }

  @NonNull
  static StmtPositionInfo withStmtPositionInfo(
      @NonNull StmtPositionInfo stmt, @Nullable LocalVariableScope scope) {
    if (scope == null) {
      if (stmt instanceof Full full) {
        return new FullStmtPositionInfo(full.stmtPosition, full.operandPositions);
      }
      if (stmt instanceof Simple simple) {
        return new SimpleStmtPositionInfo(simple.stmtPosition);
      }
      return stmt;
    }
    if (stmt instanceof LocalVariableStmtPositionInfo lv) {
      return lv.withLocalVariables(scope);
    }
    if (stmt instanceof FullStmtPositionInfo full) {
      return new Full(full, scope);
    }
    return new Simple(stmt.getStmtPosition(), scope);
  }

  @NonNull
  static Stmt withLocalVariables(@NonNull Stmt stmt, @Nullable LocalVariableScope scope) {
    StmtPositionInfo current = stmt.getPositionInfo();
    StmtPositionInfo updated = withStmtPositionInfo(current, scope);
    return current == updated ? stmt : stmt.withPositionInfo(updated);
  }

  /** LocalVariable + FullStmtPositionInfo */
  class Full extends FullStmtPositionInfo implements LocalVariableStmtPositionInfo {
    @NonNull protected final LocalVariableScope scope;

    public Full(@NonNull FullStmtPositionInfo stmt, @NonNull LocalVariableScope scope) {
      this(stmt.stmtPosition, stmt.operandPositions, scope);
    }

    public Full(
        @NonNull Position stmtPosition,
        @NonNull Position[] operandPositions,
        @NonNull LocalVariableScope scope) {
      super(stmtPosition, operandPositions);
      this.scope = scope;
    }

    @Override
    public @NonNull LocalVariableScope getLocalVariables() {
      return scope;
    }

    @Override
    public @NonNull LocalVariableStmtPositionInfo withLocalVariables(
        @NonNull LocalVariableScope scope) {
      return new Full(stmtPosition, operandPositions, scope);
    }

    @NonNull
    @Override
    public Full withStmtPosition(@NonNull Position stmtPosition) {
      return new Full(stmtPosition, operandPositions, scope);
    }

    @NonNull
    @Override
    public Full withOperandPositions(@NonNull Position[] operandPositions) {
      return new Full(stmtPosition, operandPositions, scope);
    }
  }

  /** LocalVariable + SimpleStmtPositionInfo */
  class Simple extends SimpleStmtPositionInfo implements LocalVariableStmtPositionInfo {
    @NonNull protected final LocalVariableScope scope;

    public Simple(@NonNull Position stmtPosition, @NonNull LocalVariableScope scope) {
      super(stmtPosition);
      this.scope = scope;
    }

    @Override
    public @NonNull LocalVariableScope getLocalVariables() {
      return scope;
    }

    @Override
    public @NonNull LocalVariableStmtPositionInfo withLocalVariables(
        @NonNull LocalVariableScope scope) {
      return new Simple(stmtPosition, scope);
    }

    @NonNull
    @Override
    public Simple withStmtPosition(@NonNull Position stmtPosition) {
      return new Simple(stmtPosition, scope);
    }
  }
}
