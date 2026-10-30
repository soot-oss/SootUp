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

import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.LocalVariableScope;
import sootup.core.model.Position;

/**
 * Creates built-in position information and dispatches copies to each source object's {@link
 * StmtPositionInfo#getFactory() factory}. The shared default implementation accepts only SootUp's
 * built-in variants; custom implementations must provide their own factory.
 */
public final class DefaultStmtPositionInfoFactory {
  private static final DefaultFactory INSTANCE = new DefaultFactory();
  private static final StmtPositionInfo NO_POSITION =
      new SimpleStmtPositionInfo(NoPositionInformation.getInstance()) {
        @Override
        public String toString() {
          return "No StmtPositionnfo";
        }
      };

  private DefaultStmtPositionInfoFactory() {}

  /** Returns the shared, stateless factory for SootUp's built-in position-info variants. */
  @NonNull
  public static StmtPositionInfoFactory getInstance() {
    return INSTANCE;
  }

  @NonNull
  public static StmtPositionInfo getNoStmtPositionInfo() {
    return NO_POSITION;
  }

  /** Creates statement coordinates without operand positions or a captured debug scope. */
  @NonNull
  public static StmtPositionInfo create(@NonNull Position stmtPosition) {
    return stmtPosition instanceof NoPositionInformation
        ? NO_POSITION
        : new SimpleStmtPositionInfo(stmtPosition);
  }

  /** Creates statement coordinates and captured locals, without operand coordinates. */
  @NonNull
  public static LocalVariableStmtPositionInfo create(
      @NonNull Position stmtPosition, @NonNull LocalVariableScope scope) {
    return new LocVarInfo(stmtPosition, scope);
  }

  /** Creates statement and operand coordinates, defensively copying the operand positions. */
  @NonNull
  public static FullStmtPositionInfo create(
      @NonNull Position stmtPosition, @NonNull Position[] operandPositions) {
    return new FullInfo(stmtPosition, List.of(operandPositions));
  }

  /** Copies statement coordinates using the source object's factory. */
  @NonNull
  public static StmtPositionInfo withStmtPosition(
      @NonNull StmtPositionInfo info, @NonNull Position stmtPosition) {
    return info.getFactory().withStmtPosition(info, stmtPosition);
  }

  /** Attaches, replaces or removes operand coordinates using the source object's factory. */
  @NonNull
  public static StmtPositionInfo withOperandPositions(
      @NonNull StmtPositionInfo info, @Nullable Position[] operandPositions) {
    return info.getFactory().withOperandPositions(info, operandPositions);
  }

  /** Attaches, replaces or removes a captured scope using the source object's factory. */
  @NonNull
  public static StmtPositionInfo withLocalVariables(
      @NonNull StmtPositionInfo info, @Nullable LocalVariableScope scope) {
    return info.getFactory().withLocalVariables(info, scope);
  }

  /** Copies a statement with the supplied scope, retaining its other metadata. */
  @NonNull
  public static Stmt withLocalVariables(@NonNull Stmt stmt, @Nullable LocalVariableScope scope) {
    StmtPositionInfo current = stmt.getPositionInfo();
    StmtPositionInfo updated = withLocalVariables(current, scope);
    return current == updated ? stmt : stmt.withPositionInfo(updated);
  }

  /** Copies built-in variants directly; rejects unknown types to avoid losing custom metadata. */
  private static final class DefaultFactory implements StmtPositionInfoFactory {
    /** Recreates the source metadata variant with different statement coordinates. */
    @NonNull
    @Override
    public StmtPositionInfo withStmtPosition(
        @NonNull StmtPositionInfo info, @NonNull Position stmtPosition) {
      if (info instanceof LocVarFullInfo localVariables) {
        return new LocVarFullInfo(
            stmtPosition, localVariables.operandPositions, localVariables.scope);
      }
      if (info instanceof FullInfo full) {
        return new FullInfo(stmtPosition, full.operandPositions);
      }
      if (info instanceof LocVarInfo localVariables) {
        return new LocVarInfo(stmtPosition, localVariables.scope);
      }
      if (info == NO_POSITION || info.getClass() == SimpleStmtPositionInfo.class) {
        return stmtPosition instanceof NoPositionInformation
            ? NO_POSITION
            : new SimpleStmtPositionInfo(stmtPosition);
      }
      throw new IllegalArgumentException(
          "The default statement-position factory cannot copy "
              + info.getClass().getName()
              + "; override getFactory() to provide a factory for this implementation.");
    }

    /** Adds or removes operand positions, preserving statement coordinates and captured locals. */
    @NonNull
    @Override
    public StmtPositionInfo withOperandPositions(
        @NonNull StmtPositionInfo info, @Nullable Position[] operandPositions) {
      if (operandPositions == null) {
        if (info instanceof LocVarFullInfo localVariables) {
          return new LocVarInfo(info.getStmtPosition(), localVariables.scope);
        }
        if (info instanceof FullInfo) {
          return info.getStmtPosition() instanceof NoPositionInformation
              ? NO_POSITION
              : new SimpleStmtPositionInfo(info.getStmtPosition());
        }
        if (info instanceof LocVarInfo
            || info == NO_POSITION
            || info.getClass() == SimpleStmtPositionInfo.class) {
          return info;
        }
      } else {
        List<Position> positions = List.of(operandPositions);
        if (info instanceof LocVarFullInfo localVariables) {
          return new LocVarFullInfo(info.getStmtPosition(), positions, localVariables.scope);
        }
        if (info instanceof LocVarInfo localVariables) {
          return new LocVarFullInfo(info.getStmtPosition(), positions, localVariables.scope);
        }
        if (info instanceof FullInfo
            || info == NO_POSITION
            || info.getClass() == SimpleStmtPositionInfo.class) {
          return new FullInfo(info.getStmtPosition(), positions);
        }
      }
      throw new IllegalArgumentException(
          "The default statement-position factory cannot copy "
              + info.getClass().getName()
              + "; override getFactory() to provide a factory for this implementation.");
    }

    /** Adds or removes captured locals, preserving statement and operand coordinates. */
    @NonNull
    @Override
    public StmtPositionInfo withLocalVariables(
        @NonNull StmtPositionInfo info, @Nullable LocalVariableScope scope) {
      if (scope == null) {
        if (info instanceof LocVarFullInfo localVariables) {
          return new FullInfo(info.getStmtPosition(), localVariables.operandPositions);
        }
        if (info instanceof FullInfo) {
          return info;
        }
        if (info instanceof LocVarInfo) {
          return info.getStmtPosition() instanceof NoPositionInformation
              ? NO_POSITION
              : new SimpleStmtPositionInfo(info.getStmtPosition());
        }
        if (info == NO_POSITION || info.getClass() == SimpleStmtPositionInfo.class) {
          return info;
        }
      } else {
        if (info instanceof FullInfo full) {
          return new LocVarFullInfo(info.getStmtPosition(), full.operandPositions, scope);
        }
        if (info instanceof LocVarInfo
            || info == NO_POSITION
            || info.getClass() == SimpleStmtPositionInfo.class) {
          return new LocVarInfo(info.getStmtPosition(), scope);
        }
      }
      throw new IllegalArgumentException(
          "The default statement-position factory cannot copy "
              + info.getClass().getName()
              + "; override getFactory() to provide a factory for this implementation.");
    }
  }

  /** Built-in statement and operand coordinates. */
  private static class FullInfo extends SimpleStmtPositionInfo implements FullStmtPositionInfo {
    /** Immutable list, copied on input and shared when only other metadata changes. */
    final List<Position> operandPositions;

    private FullInfo(Position position, List<Position> operandPositions) {
      super(position);
      this.operandPositions = operandPositions;
    }

    @NonNull
    @Override
    public Position getOperandPosition(int index) {
      return index >= 0 && index < operandPositions.size()
          ? operandPositions.get(index)
          : NoPositionInformation.getInstance();
    }

    @Override
    public String toString() {
      StringBuilder s = new StringBuilder(super.toString()).append("operands at: ");
      for (int i = 0; i < operandPositions.size(); i++) {
        s.append(i).append(": ").append(operandPositions.get(i)).append(" ");
      }
      return s.toString();
    }
  }

  /** Statement coordinates and captured locals, without operand coordinates. */
  private static final class LocVarInfo extends SimpleStmtPositionInfo
      implements LocalVariableStmtPositionInfo {
    private final LocalVariableScope scope;

    private LocVarInfo(Position position, LocalVariableScope scope) {
      super(position);
      this.scope = scope;
    }

    @NonNull
    @Override
    public LocalVariableScope getLocalVariables() {
      return scope;
    }
  }

  /** Statement and operand coordinates with captured locals. */
  private static final class LocVarFullInfo extends FullInfo
      implements LocalVariableStmtPositionInfo {
    private final LocalVariableScope scope;

    private LocVarFullInfo(Position position, List<Position> operands, LocalVariableScope scope) {
      super(position, operands);
      this.scope = scope;
    }

    @NonNull
    @Override
    public LocalVariableScope getLocalVariables() {
      return scope;
    }
  }
}
